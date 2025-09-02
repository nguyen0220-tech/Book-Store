const API_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");

let currentPage = 0;
let isLoading = false;
const pageSize = 5;

function showMessage(text, type = "success") {
    const msg = document.getElementById("message");
    msg.textContent = text;
    msg.className = `message ${type}`;
    msg.style.display = "block";
    setTimeout(() => msg.style.display = "none", 3000);
}

async function fetchDeletedPosts(page = 0, size = pageSize) {
    if (isLoading) return;
    isLoading = true;

    try {
        const res = await fetch(`${API_BASE}/post/deleted?page=${page}&size=${size}`, {
            headers: { "Authorization": `Bearer ${accessToken}` }
        });

        if (!res.ok) throw new Error("Không thể tải bài viết đã xoá");

        const json = await res.json();
        const posts = json.data?.content || [];
        const container = document.getElementById("deletedPostList");
        if (!container) return;

        posts.forEach(post => {
            const expiryTime = post.expiryRestore ? new Date(post.expiryRestore) : null;
            const now = new Date();
            const remainingSec = expiryTime ? Math.max(0, Math.floor((expiryTime - now) / 1000)) : 0;

            const html = `
            <div class="deleted-post" data-id="${post.id}">
                <p><strong>${post.username || "Ẩn danh"}</strong> | ${new Date(post.postDate).toLocaleString()} 
                   | <em style="color:red;">[ĐÃ XOÁ]</em></p>
                <p>${post.content}</p>
                ${post.imageUrl ? `<img src="${post.imageUrl}" style="max-width:400px; border-radius:6px;">` : ""}
                <p class="restore-timer" data-expiry="${expiryTime}">Thời gian khôi phục còn lại: <span>${formatRemainingTime(remainingSec)}</span></p>
                <button class="restore-btn">Khôi phục</button>
            </div>
            `;
            container.insertAdjacentHTML("beforeend", html);
        });

        attachRestoreEvents();
        startRestoreCountdowns();

        if (!json.data.last) {
            currentPage++;
        } else {
            window.removeEventListener("scroll", handleScroll);
        }
    } catch (err) {
        console.error(err);
        showMessage("Lỗi khi tải bài viết đã xoá.", "error");
    } finally {
        isLoading = false;
    }
}

// format thời gian từ giây sang DD:HH:MM:SS
function formatRemainingTime(seconds) {
    const days = Math.floor(seconds / (24 * 3600));
    seconds %= 24 * 3600;
    const hours = Math.floor(seconds / 3600);
    seconds %= 3600;
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${days}d:${hours}h:${mins}m:${secs}s`;
}

function startRestoreCountdowns() {
    document.querySelectorAll(".restore-timer").forEach(timer => {
        const span = timer.querySelector("span");
        const expiry = new Date(timer.getAttribute("data-expiry"));

        const interval = setInterval(() => {
            const now = new Date();
            let remainingSec = Math.floor((expiry - now) / 1000);

            if (remainingSec <= 0) {
                span.textContent = "Hết hạn khôi phục";
                clearInterval(interval);
            } else {
                span.textContent = formatRemainingTime(remainingSec);
            }
        }, 1000);
    });
}

function attachRestoreEvents() {
    document.querySelectorAll(".restore-btn").forEach(btn => {
        btn.onclick = async () => {
            const postDiv = btn.closest(".deleted-post");
            const postId = postDiv.getAttribute("data-id");

            try {
                const res = await fetch(`${API_BASE}/post/${postId}/restore`, {
                    method: "PUT",
                    headers: { "Authorization": `Bearer ${accessToken}` }
                });
                const json = await res.json();

                if (res.ok && json.success) {
                    postDiv.remove();
                    showMessage(json.message || "Khôi phục thành công!");
                } else {
                    showMessage(json.message || "Không thể khôi phục bài viết.", "error");
                }

            } catch (err) {
                console.error(err);
                showMessage("Lỗi khi khôi phục bài viết.", "error");
            }
        };
    });
}

function handleScroll() {
    if (window.scrollY + window.innerHeight >= document.documentElement.scrollHeight - 200) {
        fetchDeletedPosts(currentPage);
    }
}

document.addEventListener("DOMContentLoaded", () => {
    fetchDeletedPosts(currentPage);
    window.addEventListener("scroll", handleScroll);
});
