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
            loadChatRooms()
        } else {
            alert("❌ Tạo nhóm thất bại: " + json.message);
        }
    } catch (e) {
        console.error(e);
        alert("❌ Lỗi khi gọi API tạo nhóm.");
    }
}

/////////////////////////////
async function loadChatRooms(page = 0, size = 10) {
    try {
        // gọi API lấy danh sách phòng
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

        // gọi API lấy danh sách bạn bè 1 lần
        const friendsRes = await fetch(`${API_BASE}/friend/with-admin?page=0&size=100`, {
            headers: { Authorization: `Bearer ${accessToken}` }
        });
        const friendsJson = await friendsRes.json();
        const allFriends = friendsJson.success ? friendsJson.data.content : [];

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

            // 🚀 render danh sách thành viên
            const membersHtml = Object.entries(room.members).map(([userId, username]) => `
                <li>
                    ${username} 
                    <button onclick="actMemberToChatRoom(${room.id}, ${userId}, false)">❌ Xóa</button>
                </li>
            `).join("");

            // lọc ra bạn bè chưa có trong group
            const memberIds = Object.keys(room.members).map(Number);
            const optionsHtml = allFriends
                .filter(f => !memberIds.includes(f.friendId))
                .map(f => `<option value="${f.friendId}">${f.friendUsername}</option>`)
                .join("");

            div.innerHTML = `
                <strong>${room.chatRoomName}</strong><br>
                <small>Thành viên:</small>
                <ul>${membersHtml}</ul>
                <div>
                    <button style="background-color: #4CAF50" onclick="openChatRoom(${room.id}, '${room.chatRoomName}')">💬 Vào nhóm</button>
                    <button style="background-color: gray" onclick="exitChatRoom(${room.id})">🚪 Thoát nhóm</button>
                    <button style="background-color: red" onclick="deleteChatRoom(${room.id})">🗑 Xóa nhóm</button>
                    <button style="background-color: orange" onclick="renameChatRoom(${room.id}, '${room.chatRoomName}')">✏️ Đổi tên</button>
                </div>
                <br>
                <label>➕ Thêm thành viên:</label>
                <select id="addMemberSelect_${room.id}">
                    ${optionsHtml || "<option disabled>(Không còn bạn để thêm)</option>"}
                </select>
                <button onclick="
                    actMemberToChatRoom(${room.id}, 
                        document.getElementById('addMemberSelect_${room.id}').value, 
                        true)">Thêm</button>
            `;

            container.appendChild(div);
        });

    } catch (e) {
        console.error(e);
        alert("❌ Lỗi khi gọi API lấy danh sách phòng chat.");
    }
}


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
            const div = createMessageDiv(msg);
            msgContainer.appendChild(div);
        });

        // scroll xuống cuối cùng
        msgContainer.scrollTop = msgContainer.scrollHeight;

    } catch (e) {
        console.error(e);
        alert("❌ Lỗi khi tải tin nhắn nhóm.");
    }
}

// Hàm tách riêng để tạo div message + replies
function createMessageDiv(msg) {
    const div = document.createElement("div");
    div.classList.add("message-item");
    div.dataset.messageId = msg.messageId;

    if (msg.senderFullName === currentUser) {
        div.classList.add("message-me");
    } else {
        div.classList.add("message-other");
    }

    let repliesHtml = "";
    if (msg.messageReplies && msg.messageReplies.length > 0) {
        repliesHtml = msg.messageReplies.map(reply => `
            <div class="message-reply">
                <small><b>${reply.replyUser}</b>: ${reply.messageReply}</small>
            </div>
        `).join("");
    }

    div.innerHTML = `
        <strong style="color: blue">${msg.senderFullName}</strong><br>
        "${msg.message}"<br>
        <hr>
        ${repliesHtml}
        <small>${new Date(msg.timestamp).toLocaleString()}</small>
        <br><button onclick="startReply(${msg.messageId}, '${msg.senderFullName}', '${msg.message}')">↩️ Reply</button>
    `;
    return div;
}

function displayGroupMessage(msg) {
    const msgContainer = document.getElementById("groupMessages");
    if (!msgContainer) return;

    const div = createMessageDiv(msg);
    msgContainer.appendChild(div);
    msgContainer.scrollTop = msgContainer.scrollHeight;
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

window.currentChatRoomId = null;
window.currentChatRoomName = null;
async function openChatRoom(roomId, roomName) {
    currentChatRoomId = roomId;
    currentChatRoomName = roomName;

    const container = document.getElementById("groupChatWindow");
    container.innerHTML = `<h3>💬 Nhóm: ${roomName}</h3><div id="groupMessages"></div>`;

    await loadGroupMessages(roomId);

    //fix real time
    // 🔔 mỗi lần mở phòng → subscribe topic của phòng đó
    stompClient.subscribe(`/topic/message${roomId}`, (message) => {
        const received = JSON.parse(message.body);

        if (received.messageId && received.replyUser) {
            // Đây là reply
            displayReply(received);
        } else {
            // Đây là tin nhắn mới
            displayGroupMessage(received);
        }
    });
}
window.openChatRoom=openChatRoom

function displayReply(reply) {
    // tìm message gốc theo data-message-id
    const msgDiv = document.querySelector(`[data-message-id='${reply.messageId}']`);
    if (!msgDiv) return;

    const replyDiv = document.createElement("div");
    replyDiv.classList.add("message-reply");
    replyDiv.innerHTML = `<small><b>${reply.replyUser}</b>: ${reply.messageReply}</small>`;

    msgDiv.appendChild(replyDiv);
}

let replyTarget = null;

function startReply(messageId, sender, messageText) {
    replyTarget = { messageId, sender, messageText };

    // Hiện box "đang reply..."
    const replyBox = document.getElementById("replyBox");
    replyBox.innerHTML = `
        <div style="border-left:2px solid #666; padding-left:6px; margin-bottom:4px;">
            <small>↩️ Replying to <b>${sender}</b>: ${messageText}</small>
            <button onclick="cancelReply()">❌</button>
        </div>
    `;
}
window.startReply=startReply;

function cancelReply() {
    replyTarget = null;
    document.getElementById("replyBox").innerHTML = "";
}
window.cancelReply=cancelReply

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

        if (replyTarget) {
            stompClient.send("/app/message-reply", {}, JSON.stringify({
                chatRoomId: currentChatRoomId,
                messageId: replyTarget.messageId,
                replyText: message
            }));
            cancelReply();
        } else {
            stompClient.send("/app/chat-group", {}, JSON.stringify({
                chatRoomId: currentChatRoomId,
                message: message
            }));
        }
        input.value = "";
    });
}

// 📌 Gọi API thêm / xóa thành viên
async function actMemberToChatRoom(chatRoomId, memberId, act) {
    try {
        const body = {
            chatRoomId: chatRoomId,
            memberId: memberId,
            act: act   // true = thêm, false = xóa
        };

        const res = await fetch(`${API_BASE}/chat-room/act-member`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${accessToken}`
            },
            body: JSON.stringify(body)
        });

        const json = await res.json();
        if (json.success) {
            alert(`✅ ${act ? "Thêm" : "Xóa"} thành viên thành công!`);
            // Sau khi thêm / xoá thì reload lại danh sách phòng
            loadChatRooms();
        } else {
            alert(`${act ? "Thêm" : "Xóa"}: ${json.message}`);
        }
    } catch (e) {
        console.error(e);
        alert("❌ Lỗi khi gọi API act-member.");
    }
}
window.actMemberToChatRoom=actMemberToChatRoom

async function exitChatRoom(chatRoomId) {
    if (!confirm("⚠️ Bạn có chắc chắn muốn rời nhóm này không?")) return;

    try {
        const res = await fetch(`${API_BASE}/chat-room/${chatRoomId}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${accessToken}`
            }
        });

        const json = await res.json();
        if (json.success) {
            alert("✅ Rời nhóm thành công!");
            loadChatRooms();
        } else {
            alert("❌ Không rời được nhóm: " + json.message);
        }
    } catch (e) {
        console.error(e);
        alert("❌ Lỗi khi gọi API rời nhóm.");
    }
}
window.exitChatRoom = exitChatRoom;

async function renameChatRoom(chatRoomId, oldName) {
    const newName = prompt("✏️ Nhập tên mới cho nhóm:", oldName);
    if (!newName || newName.trim() === "" || newName === oldName) return;

    try {
        const body = {
            chatRoomId: chatRoomId,
            newName: newName.trim()
        };

        const res = await fetch(`${API_BASE}/chat-room/rename`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${accessToken}`
            },
            body: JSON.stringify(body)
        });

        const json = await res.json();
        if (json.success) {
            alert("✅ Đổi tên nhóm thành công:" + json.message);
            loadChatRooms();
        } else {
            alert("❌ Không đổi được tên nhóm: " + json.message);
        }
    } catch (e) {
        console.error(e);
        alert("❌ Lỗi khi gọi API đổi tên nhóm.");
    }
}
window.renameChatRoom = renameChatRoom;

async function deleteChatRoom(chatRoomId) {
    if (!confirm("⚠️ Bạn có chắc muốn XÓA NHÓM này không? Hành động này không thể hoàn tác!")) return;

    try {
        const res = await fetch(`${API_BASE}/chat-room/${chatRoomId}`, {
            method: "DELETE",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${accessToken}`
            }
        });

        const json = await res.json();
        if (json.success) {
            alert("✅ Đã xóa nhóm thành công: " + json.message);
            loadChatRooms();
        } else {
            alert("❌ Không xóa được nhóm: " + json.message);
        }
    } catch (e) {
        console.error(e);
        alert("❌ Lỗi khi gọi API xóa nhóm.");
    }
}
window.deleteChatRoom = deleteChatRoom;

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
