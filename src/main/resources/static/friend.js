const API_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");

// Tìm kiếm người dùng
async function searchUsers() {
    const keyword = document.getElementById("searchInput").value;
    const response = await fetch(`${API_BASE}/user/search-name?keyword=${encodeURIComponent(keyword)}&page=0&size=10`, {
        headers: {
            "Authorization": `Bearer ${accessToken}`
        }
    });

    const data = await response.json();
    const users = data.data.content;
    const userListDiv = document.getElementById("userList");
    userListDiv.innerHTML = "";

    if (users.length === 0) {
        userListDiv.innerHTML = "<p>Không tìm thấy người dùng.</p>";
        return;
    }

    users.forEach(user => {
        const div = document.createElement("div");
        div.innerHTML = `
            <p><strong>${user.username}</strong></p>
            <button onclick="sendFriendRequest(${user.id})">Thêm bạn</button>
        `;
        userListDiv.appendChild(div);
    });
}

// Gửi lời mời kết bạn
async function sendFriendRequest(friendId) {
    const response = await fetch(`${API_BASE}/friend`, {
        method: "POST",
        headers: {
            "Authorization": `Bearer ${accessToken}`,
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ friendId: friendId })
    });

    const result = await response.json();
    if (result.success) {
        alert("✅ Đã gửi lời mời kết bạn!");
    } else {
        alert("Thông báo: " + result.message);
    }
}

// Tải danh sách bạn bè
async function loadFriends() {
    const response = await fetch(`${API_BASE}/friend?page=0&size=10`, {
        headers: {
            "Authorization": `Bearer ${accessToken}`
        }
    });

    const data = await response.json();
    const friends = data.data.content;
    const friendListDiv = document.getElementById("friendList");
    friendListDiv.innerHTML = "";

    if (friends.length === 0) {
        friendListDiv.innerHTML = "<p>Bạn chưa có bạn bè nào.</p>";
        return;
    }

    friends.forEach(friend => {
        const div = document.createElement("div");
        div.innerHTML = `
  <p><strong>${friend.friendName}</strong> - Trạng thái: <span style="color: #16a085">${friend.status}</span></p>
  <div class="friend-actions">
    <button class="remove-btn" onclick="deleteFriend(${friend.friendId})">❌ Xóa bạn</button>
    <button class="block-btn" onclick="blockFriend(${friend.friendId})">🚫 Chặn</button>
  </div>
`;

        friendListDiv.appendChild(div);
    });
}

async function fetchPendingRequests(page = 0, size = 5) {
    try {
        const response = await fetch(`${API_BASE}/friend/pending?page=${page}&size=${size}`, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${accessToken}`
            }
        });

        const result = await response.json();
        if (result.success) {
            showPendingRequests(result.data.content);
        } else {
            alert("Không thể lấy danh sách lời mời.");
        }
    } catch (error) {
        console.error("Lỗi khi lấy lời mời:", error);
    }
}

function showPendingRequests(requests) {
    const list = document.getElementById("pendingList");
    list.innerHTML = "";

    requests.forEach(req => {
        const li = document.createElement("li");
        li.innerHTML = `
            <strong>${req.username}</strong> gửi lời mời kết bạn 
            <button onclick="acceptFriend(${req.userId})">Chấp nhận</button>
            <button onclick="rejectFriend(${req.userId})">Từ chối</button>
        `;
        list.appendChild(li);
    });
}


async function acceptFriend(userId) {
    try {
        const response = await fetch(`${API_BASE}/friend/accept`, {
            method: "PUT",
            headers: {
                "Authorization": `Bearer ${accessToken}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ friendId: userId }) // Gửi userId làm friendId
        });

        const result = await response.json();
        alert(result.message);
        fetchPendingRequests(); // refresh list
    } catch (error) {
        console.error("Lỗi khi chấp nhận lời mời:", error);
    }
}

async function rejectFriend(userId) {
    try {
        const response = await fetch(`${API_BASE}/friend/cancel`, {
            method: "PUT",
            headers: {
                "Authorization": `Bearer ${accessToken}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ friendId: userId }) // Gửi userId làm friendId
        });

        const result = await response.json();
        alert(result.message);
        fetchPendingRequests(); // refresh list
    } catch (error) {
        console.error("Lỗi khi từ chối lời mời:", error);
    }
}

async function deleteFriend(friendId) {
    if (!confirm("Bạn có chắc chắn muốn xóa bạn này không?")) return;

    try {
        const response = await fetch(`${API_BASE}/friend/${friendId}`, {
            method: "DELETE",
            headers: {
                "Authorization": `Bearer ${accessToken}`
            }
        });

        const result = await response.json();
        alert(result.message || "Đã xóa bạn.");
        loadFriends(); // refresh danh sách bạn bè
    } catch (error) {
        console.error("Lỗi khi xóa bạn:", error);
    }
}

async function blockFriend(friendId) {
    if (!confirm("Bạn có chắc chắn muốn chặn người này không?")) return;

    try {
        const response = await fetch(`${API_BASE}/friend/block`, {
            method: "PUT",
            headers: {
                "Authorization": `Bearer ${accessToken}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ friendId: friendId })
        });

        const result = await response.json();
        alert(result.message || "Đã chặn bạn.");
        loadFriends(); // refresh danh sách bạn bè
    } catch (error) {
        console.error("Lỗi khi chặn bạn:", error);
    }
}

async function loadBlockedFriends(page = 0, size = 10) {
    try {
        const response = await fetch(`${API_BASE}/friend/blocking?page=${page}&size=${size}`, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${accessToken}`
            }
        });

        const result = await response.json();
        const blockedFriends = result.data.content;
        const blockedListDiv = document.getElementById("blockedFriendList");
        blockedListDiv.innerHTML = "";

        if (blockedFriends.length === 0) {
            blockedListDiv.innerHTML = "<p>Không có bạn bè nào bị chặn.</p>";
            return;
        }

        blockedFriends.forEach(friend => {
            const div = document.createElement("div");
            div.innerHTML = `
        <p>
            <strong>${friend.friendName}</strong> - Đã bị chặn
            <button onclick="unblockFriend(${friend.friendId})">Bỏ chặn</button>
        </p>
    `;
            blockedListDiv.appendChild(div);
        });


    } catch (error) {
        console.error("Lỗi khi tải danh sách bị chặn:", error);
    }
}

async function unblockFriend(friendId) {
    try {
        const response = await fetch(`${API_BASE}/friend/un-block`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${accessToken}`
            },
            body: JSON.stringify({ friendId: friendId })
        });

        const result = await response.json();

        if (response.ok) {
            alert("Đã bỏ chặn thành công!");
            loadBlockedFriends(); // Reload danh sách
        } else {
            alert(result.message || "Lỗi khi bỏ chặn.");
        }
    } catch (error) {
        console.error("Lỗi khi bỏ chặn bạn bè:", error);
    }
}

async function fetchFriendCount() {
    const accessToken = localStorage.getItem("accessToken");

    try {
        const response = await fetch("/friend/count", {
            headers: {
                "Authorization": `Bearer ${accessToken}`
            }
        });

        if (!response.ok) {
            throw new Error("Lỗi khi gọi API");
        }

        const result = await response.json();

        if (result.success) {
            const count = result.data;
            document.getElementById("friendCountNumber").innerText = count;
        } else {
            console.warn("Không lấy được số lượng bạn bè:", result.message);
        }
    } catch (error) {
        console.error("Lỗi khi lấy số lượng bạn bè:", error);
    }
}
window.addEventListener("DOMContentLoaded", () => {
    loadFriends()
    fetchFriendCount();
    fetchPendingRequests(); // initial load
    loadBlockedFriends()
});

