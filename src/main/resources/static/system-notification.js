const API_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken"); // nếu cần xác thực

async function sendSystemNotification() {
    const message = document.getElementById("message").value.trim();

    if (!message) {
        alert("Vui lòng nhập nội dung thông báo.");
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/notify/creat`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${accessToken}` // nếu có xác thực
            },
            body: JSON.stringify({ message: message })
        });

        const data = await response.json();

        if (response.ok && data.success) {
            const sentMessage = data.data.message;
            const count = data.data.sendCount;

            document.getElementById("result").innerText =
                `✅ Đã gửi thông báo "${sentMessage}" đến ${count} người dùng.`;
        } else {
            document.getElementById("result").innerText = `❌ Gửi thất bại: ${data.message}`;
        }
    } catch (error) {
        console.error("Lỗi:", error);
        document.getElementById("result").innerText = `❌ Lỗi kết nối đến server.`;
    }
}
