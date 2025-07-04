const API_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");

window.onload = () => {
    if (!accessToken) {
        alert("Vui lòng đăng nhập lại.");
        window.location.href = "auth.html";
        return;
    }
    fetchOrderHistory();
};

async function fetchOrderHistory() {
    try {
        const res = await fetch(`${API_BASE}/order/my-order`, {
            headers: {
                Authorization: `Bearer ${accessToken}`
            }
        });

        const result = await res.json();
        if (!res.ok || !result.success) {
            alert(result.message || "Không thể tải lịch sử đơn hàng");
            return;
        }

        const orders = result.data;
        renderOrders(orders);
    } catch (err) {
        alert("Lỗi server: " + err.message);
    }
}

function renderOrders(orders) {
    const container = document.getElementById("orderList");

    if (!orders || orders.length === 0) {
        container.innerHTML = "<p>Bạn chưa đặt đơn hàng nào.</p>";
        return;
    }

    container.innerHTML = orders.map(order => `
        <div class="order-card">
            <div class="order-header">
                🧾 Đơn hàng #${order.orderId}<br/>
                ⏰ Ngày: ${formatDate(order.orderDate)}<br/>
                💸 Tổng đơn (trước giảm): <s>${(order.totalPrice + (order.totalDiscount || 0)).toLocaleString()}₩</s><br/>
                🎁 Giảm giá: <span style="color: red;">- ${(order.totalDiscount || 0).toLocaleString()}₩</span><br/>
                💳 Thanh toán: <b>${order.totalPrice.toLocaleString()}₩</b><br/>
                📦 Trạng thái: ${order.orderStatus}
            </div>
            ${order.items.map(item => `
                <div class="order-item">
                    📚 <b>${item.title}</b><br/>
                    <img src="${item.imgUrl}" style="max-width:60px;" /> x ${item.quantity} cuốn - Giá: ${item.price.toLocaleString()}₩
                </div>
            `).join('')}
        </div>
    `).join('');
}


function formatDate(timestamp) {
    const date = new Date(timestamp);
    return date.toLocaleDateString("ko-KR");
}
