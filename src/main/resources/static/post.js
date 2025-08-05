const API_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");

let currentPage = 0;
let isLoading = false;
const pageSize = 5;

async function createPost() {
    const content = document.getElementById("postContent").value.trim();
    const postShare = document.getElementById("postShare").value;
    const imageUrl = document.getElementById("imageUrl").value.trim();

    if (!content) {
        alert("Nội dung bài viết không được để trống.");
        return;
    }

    const postData = { content, postShare };
    if (imageUrl !== "") postData.imageUrl = imageUrl;

    try {
        const response = await fetch(`${API_BASE}/post`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${accessToken}`
            },
            body: JSON.stringify(postData)
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error("Lỗi khi đăng bài:", errorText);
            alert("Đăng bài thất bại: " + errorText);
            return;
        }

        alert("Đăng bài thành công!");
        // Clear form
        document.getElementById("postContent").value = "";
        document.getElementById("imageUrl").value = "";

        // Reset và tải lại bài viết từ trang đầu
        currentPage = 0;
        const postListDiv = document.getElementById("postList");
        if (postListDiv) postListDiv.innerHTML = "";
        await fetchPosts(currentPage);

    } catch (error) {
        console.error("Lỗi kết nối server:", error);
        alert("Lỗi kết nối server.");
    }
}

async function fetchPosts(page = 0, size = pageSize) {
    if (isLoading) return;
    isLoading = true;

    try {
        const response = await fetch(`${API_BASE}/post?page=${page}&size=${size}`, {
            method: "GET",
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + accessToken
            }
        });

        if (!response.ok) throw new Error("Không thể tải bài viết");

        const json = await response.json();
        const posts = json.data?.content || [];
        const postListDiv = document.getElementById('postList');
        if (!postListDiv) return;

        posts.forEach(post => {
            const postHtml = `
            <div class="post-item" data-postid="${post.id}" style="border:1px solid #ccc; padding:1rem; margin:1rem 0; position:relative;">
                <div class="post-actions" style="position:absolute; top:1rem; right:1rem; display:flex; gap:5px;">
                    <button class="edit-btn" style="background:#ffc107; border:none; border-radius:4px; padding:4px 8px; cursor:pointer;">Sửa</button>
                    <button class="delete-btn" style="background:#dc3545; color:#fff; border:none; border-radius:4px; padding:4px 8px; cursor:pointer;">Xóa</button>
                </div>
                <p>
                    <strong>${post.username || "Ẩn danh"}</strong> 
                    | ${new Date(post.postDate).toLocaleString()} 
                    | <span style="font-style:italic; color:gray;">
                        ${post.postShare === 'PUBLIC' ? '🌍 Công khai' :
                post.postShare === 'FRIEND' ? '👥 Bạn bè' :
                    '🔒 Riêng tư'}
                      </span>
                </p>
                <p class="post-content">${post.content}</p>
                ${post.imageUrl ? `<img src="${post.imageUrl}" style="max-width:400px; height:auto; display:block; margin-top:0.5rem; border-radius:6px;">` : ''}
                <div class="edit-form" style="display:none; margin-top:1rem;">
                  <textarea rows="3" class="edit-content" style="width:100%; padding:0.5rem;">${post.content}</textarea>
                  <input type="text" class="edit-imageUrl" placeholder="Link ảnh" style="width:100%; margin-top:0.5rem; padding:0.5rem;" value="${post.imageUrl || ''}">
                  <select class="edit-postShare" style="margin-top:0.5rem; padding:0.5rem; width:100%;">
                    <option value="PUBLIC" ${post.postShare === 'PUBLIC' ? 'selected' : ''}>Công khai</option>
                    <option value="FRIEND" ${post.postShare === 'FRIEND' ? 'selected' : ''}>Bạn bè</option>
                    <option value="PRIVATE" ${post.postShare === 'PRIVATE' ? 'selected' : ''}>Riêng tư</option>
                  </select>
                  <button class="save-edit-btn" style="margin-top:0.5rem; padding:0.5rem 1rem; background:#007bff; color:#fff; border:none; border-radius:6px; cursor:pointer;">Lưu</button>
                  <button class="cancel-edit-btn" style="margin-top:0.5rem; padding:0.5rem 1rem; background:#6c757d; color:#fff; border:none; border-radius:6px; cursor:pointer; margin-left:5px;">Hủy</button>
                </div>
            </div>`;
            postListDiv.insertAdjacentHTML('beforeend', postHtml);
        });

        attachEventListeners();

        if (!json.data.last) {
            currentPage++;
        } else {
            window.removeEventListener('scroll', handleScroll);
        }
    } catch (error) {
        console.error("Lỗi khi tải bài viết:", error);
        alert("⚠️ Lỗi khi tải bài viết: " + error.message);
    } finally {
        isLoading = false;
    }
}

function attachEventListeners() {
    // Nút Sửa
    document.querySelectorAll('.edit-btn').forEach(btn => {
        btn.onclick = e => {
            const postItem = e.target.closest('.post-item');
            toggleEditForm(postItem, true);
        };
    });

    // Nút Hủy sửa
    document.querySelectorAll('.cancel-edit-btn').forEach(btn => {
        btn.onclick = e => {
            const postItem = e.target.closest('.post-item');
            toggleEditForm(postItem, false);
        };
    });

    // Nút Lưu sửa
    document.querySelectorAll('.save-edit-btn').forEach(btn => {
        btn.onclick = async e => {
            const postItem = e.target.closest('.post-item');
            const postId = postItem.getAttribute('data-postid');
            const content = postItem.querySelector('.edit-content').value.trim();
            const imageUrl = postItem.querySelector('.edit-imageUrl').value.trim();
            const postShare = postItem.querySelector('.edit-postShare').value;

            if (!content) {
                alert("Nội dung bài viết không được để trống.");
                return;
            }

            try {
                const response = await fetch(`${API_BASE}/post/${postId}`, {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json",
                        "Authorization": `Bearer ${accessToken}`
                    },
                    body: JSON.stringify({ content, postShare, imageUrl: imageUrl || null })
                });

                if (!response.ok) {
                    const errText = await response.text();
                    alert("Cập nhật thất bại: " + errText);
                    return;
                }

                const updatedPost = await response.json();
                postItem.querySelector('.post-content').textContent = content;

                if (imageUrl) {
                    let imgTag = postItem.querySelector('img');
                    if (imgTag) imgTag.src = imageUrl;
                    else {
                        const imgHtml = `<img src="${imageUrl}" style="max-width:400px; height:auto; display:block; margin-top:0.5rem; border-radius:6px;">`;
                        postItem.insertAdjacentHTML('beforeend', imgHtml);
                    }
                } else {
                    const imgTag = postItem.querySelector('img');
                    if (imgTag) imgTag.remove();
                }

                const spanShare = postItem.querySelector('p > span');
                spanShare.textContent = postShare === 'PUBLIC' ? '🌍 Công khai' :
                    postShare === 'FRIEND' ? '👥 Bạn bè' : '🔒 Riêng tư';

                toggleEditForm(postItem, false);

                alert("Cập nhật bài viết thành công!");
            } catch (error) {
                console.error(error);
                alert("Lỗi khi cập nhật bài viết.");
            }
        };
    });

    // Nút Xóa
    document.querySelectorAll('.delete-btn').forEach(btn => {
        btn.onclick = async e => {
            if (!confirm("Bạn có chắc muốn xóa bài viết này?")) return;
            const postItem = e.target.closest('.post-item');
            const postId = postItem.getAttribute('data-postid');

            try {
                const response = await fetch(`${API_BASE}/post/${postId}`, {
                    method: "DELETE",
                    headers: {
                        "Authorization": `Bearer ${accessToken}`
                    }
                });

                if (!response.ok) {
                    const errText = await response.text();
                    alert("Xóa thất bại: " + errText);
                    return;
                }

                postItem.remove();
                alert("Xóa bài viết thành công!");
            } catch (error) {
                console.error(error);
                alert("Lỗi khi xóa bài viết.");
            }
        };
    });
}

function toggleEditForm(postItem, show) {
    const editForm = postItem.querySelector('.edit-form');
    const postContent = postItem.querySelector('.post-content');
    const postActions = postItem.querySelector('.post-actions');
    const imgTag = postItem.querySelector('img');

    if (show) {
        editForm.style.display = 'block';
        postContent.style.display = 'none';
        postActions.style.display = 'none';
        if(imgTag) imgTag.style.display = 'none';
    } else {
        editForm.style.display = 'none';
        postContent.style.display = 'block';
        postActions.style.display = 'flex';
        if(imgTag) imgTag.style.display = 'block';
    }
}

function handleScroll() {
    const scrollTop = window.scrollY;
    const windowHeight = window.innerHeight;
    const documentHeight = document.documentElement.scrollHeight;

    if (scrollTop + windowHeight >= documentHeight - 200) {
        fetchPosts(currentPage);
    }
}

async function fetchUnreadNotificationCount() {
    try {
        const res = await fetch(`${API_BASE}/notify/un-read`, {
            headers: {
                'Authorization': `Bearer ${accessToken}`
            }
        });
        if (res.ok) {
            const data = await res.json();
            const count = data.data;
            const unreadSpan = document.getElementById("unreadCount");
            if (count > 0) {
                unreadSpan.textContent = `(${count})`;
            } else {
                unreadSpan.textContent = "";
            }
        }
    } catch (err) {
        console.error("Failed to fetch unread notifications:", err);
    }
}

fetchUnreadNotificationCount();

document.addEventListener('DOMContentLoaded', () => {
    fetchPosts(currentPage);
    window.addEventListener('scroll', handleScroll);
});

