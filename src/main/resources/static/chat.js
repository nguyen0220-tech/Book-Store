const API_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");

if (!accessToken) {
    alert("⚠️ Vui lòng đăng nhập trước!");
    throw new Error("No access token");
}

function parseJwt(token) {
    return JSON.parse(atob(token.split('.')[1]));
}

const senderUsername = parseJwt(accessToken).sub;
let recipientUsername = null; // sẽ set sau khi user chọn

const chatWindow = document.getElementById("chatWindow");
const statusDiv = document.getElementById("status");
const inputForm = document.getElementById("inputForm");
const messageInput = document.getElementById("messageInput");
const recipientSelect = document.getElementById("recipientSelect");

function displayMessage(message) {
    const div = document.createElement("div");
    const time = new Date(message.timestamp).toLocaleTimeString();
    div.textContent = `[${time}] ${message.sender}: ${message.message}`;
    div.classList.add(message.sender === senderUsername ? "sent" : "received");
    chatWindow.appendChild(div);
    chatWindow.scrollTop = chatWindow.scrollHeight;
}

async function loadFriends() {
    try {
        const res = await fetch(`${API_BASE}/friend/with-admin?page=0&size=50`, {
            headers: { Authorization: `Bearer ${accessToken}` }
        });
        const json = await res.json();

        if (json.success) {
            recipientSelect.innerHTML = "";
            json.data.content.forEach(friend => {
                const option = document.createElement("option");
                option.value = friend.friendUsername;
                option.textContent = friend.friendUsername;
                recipientSelect.appendChild(option);
            });

            // mặc định chọn bạn đầu tiên
            if (json.data.content.length > 0) {
                recipientUsername = recipientSelect.value;
                loadChatHistory();
            }
        } else {
            alert("❌ Không tải được danh sách bạn bè.");
        }
    } catch (e) {
        console.error(e);
        alert("❌ Lỗi khi tải danh sách bạn bè.");
    }
}

async function loadChatHistory() {
    if (!recipientUsername) return;

    try {
        const res = await fetch(`${API_BASE}/message?recipient=${recipientUsername}&page=0&size=20&direction=TWO_WAY`, {
            headers: { Authorization: `Bearer ${accessToken}` }
        });
        const json = await res.json();
        if (json.success) {
            chatWindow.innerHTML = "";
            json.data.content.forEach(displayMessage);
        } else {
            alert("❌ Không tải được lịch sử tin nhắn.");
        }
    } catch (e) {
        console.error(e);
        alert("❌ Lỗi khi tải lịch sử tin nhắn.");
    }
}

let stompClient = null;
function connectWebSocket() {
    const socket = new SockJS(`${API_BASE}/ws`);
    stompClient = Stomp.over(socket);

    const headers = { "Authorization": `Bearer ${accessToken}` };

    stompClient.connect(headers, frame => {
        console.log("✅ WebSocket connected:", frame);

        stompClient.subscribe("/user/queue/message", (message) => {
            const receivedMessage = JSON.parse(message.body);
            displayMessage(receivedMessage);
        });

    }, error => {
        console.error("🔴 WebSocket connection error:", error);
        statusDiv.textContent = "Lỗi kết nối WebSocket (token không hợp lệ hoặc hết hạn).";
        statusDiv.style.color = "red";
    });
}

function sendMessage() {
    const content = messageInput.value.trim();
    if (!content || !recipientUsername || !stompClient || !stompClient.connected) {
        alert("🔴 Không thể gửi tin nhắn.");
        return;
    }

    const message = {
        recipient: recipientUsername,
        message: content,
        timestamp: new Date().toISOString(),
        fromAdmin: false
    };

    stompClient.send("/app/chat", {}, JSON.stringify(message));
    messageInput.value = "";
}

inputForm.addEventListener("submit", function (e) {
    e.preventDefault();
    sendMessage();
});

// Khi đổi người nhận thì load lại lịch sử chat
recipientSelect.addEventListener("change", () => {
    recipientUsername = recipientSelect.value;
    loadChatHistory();
});

window.onload = () => {
    loadFriends();
    connectWebSocket();
};
