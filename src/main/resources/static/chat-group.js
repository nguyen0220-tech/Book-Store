const API_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");

if (!accessToken) {
    alert("⚠️ Vui lòng đăng nhập trước!");
    throw new Error("No access token");
}

function parseJwt(token) {
    return JSON.parse(atob(token.split('.')[1]));
}
const currentUser = parseJwt(accessToken).sub; // username hiện tại

async function loadFriendsForGroup() {
    try {
        const res = await fetch(`${API_BASE}/friend/with-admin?page=0&size=50`, {
            headers: { Authorization: `Bearer ${accessToken}` }
        });
        const json = await res.json();

        if (json.success) {
            const friendsDiv = document.getElementById("friendsList");
            friendsDiv.innerHTML = "";
            json.data.content.forEach(friend => {
                const label = document.createElement("label");
                const checkbox = document.createElement("input");
                checkbox.type = "checkbox";
                checkbox.value = friend.friendId; // dùng friendId cho backend
                label.appendChild(checkbox);
                label.appendChild(document.createTextNode(" " + friend.friendUsername));
                friendsDiv.appendChild(label);
                friendsDiv.appendChild(document.createElement("br"));
            });
        } else {
            alert("❌ Không tải được danh sách bạn bè.");
        }
    } catch (e) {
        console.error(e);
        alert("❌ Lỗi khi tải danh sách bạn bè.");
    }
}

async function createChatRoom() {
    const chatRoomName = document.getElementById("chatRoomName").value.trim();
    if (!chatRoomName) {
        alert("⚠️ Vui lòng nhập tên nhóm!");
        return;
    }

    // Lấy danh sách bạn được chọn
    const selected = Array.from(document.querySelectorAll("#friendsList input[type=checkbox]:checked"))
        .map(cb => parseInt(cb.value)); // chỉ gửi id

    if (selected.length === 0) {
        alert("⚠️ Vui lòng chọn ít nhất 1 thành viên!");
        return;
    }

    const body = {
        chatRoomName: chatRoomName,
        memberIds: selected   // ✅ đúng key theo BE
    };

    try {
        const res = await fetch(`${API_BASE}/chat-room`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${accessToken}`
            },
            body: JSON.stringify(body)
        });

        const json = await res.json();
        if (json.success) {
            alert("✅ Tạo nhóm thành công!");
            document.getElementById("chatRoomName").value = "";
            document.querySelectorAll("#friendsList input[type=checkbox]").forEach(cb => cb.checked = false);
        } else {
            alert("❌ Tạo nhóm thất bại: " + json.message);
        }
    } catch (e) {
        console.error(e);
        alert("❌ Lỗi khi gọi API tạo nhóm.");
    }
}

async function loadChatRooms(page = 0, size = 10) {
    try {
        const res = await fetch(`${API_BASE}/chat-room?page=${page}&size=${size}`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${accessToken}`
            }
        });

        const json = await res.json();
        if (!json.success) {
            alert("❌ Không lấy được danh sách phòng chat: " + json.message);
            return;
        }

        const chatRooms = json.data.content; // Page<ChatRoomDTO>
        const container = document.getElementById("chatRoomList");
        container.innerHTML = "";

        if (chatRooms.length === 0) {
            container.innerHTML = "<p>Không có phòng chat nào.</p>";
            return;
        }

        chatRooms.forEach(room => {
            const div = document.createElement("div");
            div.classList.add("chat-room-item");
            div.dataset.roomId = room.id;

            div.innerHTML = `
                <strong>${room.chatRoomName}</strong><br>
                <small>Thành viên: ${Array.from(room.usernames).join(", ")}</small>
            `;

            div.addEventListener("click", () => {
                openChatRoom(room.id, room.chatRoomName);
            });

            container.appendChild(div);
        });

    } catch (e) {
        console.error(e);
        alert("❌ Lỗi khi gọi API lấy danh sách phòng chat.");
    }
}

let currentChatRoomId = null;
let currentChatRoomName = null;

// async function openChatRoom(roomId, roomName) {
//     currentChatRoomId = roomId;
//     currentChatRoomName = roomName;
//
//     const container = document.getElementById("groupChatWindow");
//     container.innerHTML = `<h3>💬 Nhóm: ${roomName}</h3><div id="groupMessages"></div>`;
//
//     await loadGroupMessages(roomId);
// }

async function loadGroupMessages(roomId, page = 0, size = 20) {
    try {
        const res = await fetch(`${API_BASE}/chat-room/${roomId}?page=${page}&size=${size}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${accessToken}`
            }
        });

        const json = await res.json();
        if (!json.success) {
            alert("❌ Không tải được tin nhắn: " + json.message);
            return;
        }

        const messages = json.data.content; // Page<MessageForGroupChatDTO>
        const msgContainer = document.getElementById("groupMessages");
        msgContainer.innerHTML = "";

        if (messages.length === 0) {
            msgContainer.innerHTML = "<p>Chưa có tin nhắn nào.</p>";
            return;
        }

        messages.forEach(msg => {
            const div = document.createElement("div");
            div.classList.add("message-item");

            if (msg.senderFullName === currentUser) {
                div.classList.add("message-me");
            } else {
                div.classList.add("message-other");
            }

            div.innerHTML = `
        <strong>${msg.senderFullName}</strong><br>
        ${msg.message}
        <br><small>${new Date(msg.timestamp).toLocaleString()}</small>
    `;
            msgContainer.appendChild(div);
        });

        // scroll xuống cuối cùng
        msgContainer.scrollTop = msgContainer.scrollHeight;

    } catch (e) {
        console.error(e);
        alert("❌ Lỗi khi tải tin nhắn nhóm.");
    }
}
//xong load tin nhan

let stompClient = null;
function connectWebSocket() {
    const socket = new SockJS(`${API_BASE}/ws`);
    stompClient = Stomp.over(socket);

    const headers = { "Authorization": `Bearer ${accessToken}` };

    stompClient.connect(headers, frame => {
        console.log("✅ WebSocket connected:", frame);

        // chưa subscribe ngay, mà subscribe khi mở phòng chat
    }, error => {
        console.error("🔴 WebSocket connection error:", error);
    });
}

async function openChatRoom(roomId, roomName) {
    currentChatRoomId = roomId;
    currentChatRoomName = roomName;

    const container = document.getElementById("groupChatWindow");
    container.innerHTML = `<h3>💬 Nhóm: ${roomName}</h3><div id="groupMessages"></div>`;

    await loadGroupMessages(roomId);

    // 🔔 mỗi lần mở phòng → subscribe topic của phòng đó
    stompClient.subscribe(`/topic/message${roomId}`, (message) => {
        const received = JSON.parse(message.body);
        displayGroupMessage(received);
    });
}

function displayGroupMessage(msg) {
    const msgContainer = document.getElementById("groupMessages");
    if (!msgContainer) return;

    const div = document.createElement("div");
    div.classList.add("message-item");

    if (msg.senderFullName === currentUser) {
        div.classList.add("message-me");
    } else {
        div.classList.add("message-other");
    }

    div.innerHTML = `
        <strong>${msg.senderFullName}</strong><br>
        ${msg.message}
        <br><small>${new Date(msg.timestamp).toLocaleString()}</small>
    `;
    msgContainer.appendChild(div);
    msgContainer.scrollTop = msgContainer.scrollHeight;
}

// gửi tin nhắn nhóm qua socket
const groupInputForm = document.getElementById("groupInputForm");
if (groupInputForm) {
    groupInputForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        if (!currentChatRoomId) {
            alert("⚠️ Vui lòng chọn nhóm chat trước!");
            return;
        }

        const input = document.getElementById("groupMessageInput");
        const message = input.value.trim();
        if (!message) return;

        const body = { chatRoomId: currentChatRoomId, message: message };

        // 🚀 gửi qua WebSocket
        stompClient.send("/app/chat-group", {}, JSON.stringify(body));

        input.value = "";
    });
}

window.onload = () => {
    loadFriendsForGroup();
    loadChatRooms();
    connectWebSocket();

    // ✅ gắn sự kiện cho nút Tạo nhóm
    const btn = document.getElementById("createGroupBtn");
    if (btn) {
        btn.addEventListener("click", (e) => {
            e.preventDefault();
            createChatRoom();
        });
    }
};
