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
const recipientUsername = prompt("👤 Nhập tên người nhận:");

const chatWindow = document.getElementById("chatWindow");
const statusDiv = document.getElementById("status");
const inputForm = document.getElementById("inputForm");
const messageInput = document.getElementById("messageInput");


function displayMessage(message) {
    const div = document.createElement("div");
    const time = new Date(message.timestamp).toLocaleTimeString();
    div.textContent = `[${time}] ${message.sender}: ${message.message}`;
    div.classList.add(message.sender === senderUsername ? "sent" : "received");
    chatWindow.appendChild(div);
    chatWindow.scrollTop = chatWindow.scrollHeight;
}

async function loadChatHistory() {
    try {
        const res = await fetch(`${API_BASE}/message?recipient=${recipientUsername}&page=0&size=20&direction=TWO_WAY`, {
            headers: {
                Authorization: `Bearer ${accessToken}`
            }
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

    const headers = {
        "Authorization": `Bearer ${accessToken}`
    };

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
    if (!content || !stompClient || !stompClient.connected) {
        alert("🔴 Không thể gửi tin nhắn khi WebSocket chưa kết nối.");
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

window.onload = () => {
    loadChatHistory();
    connectWebSocket();
};
