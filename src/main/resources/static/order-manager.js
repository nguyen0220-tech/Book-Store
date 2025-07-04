const API_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");

async function loadOrders() {
    try {
        const res = await fetch(`${API_BASE}/order/admin/all`, {
            headers: {
                "Authorization": `Bearer ${accessToken}`
            }
        });

        const result = await res.json();
        if (res.ok && result.success) {
            const tbody = document.getElementById("orderTableBody");
            tbody.innerHTML = "";

            result.data.forEach(order => {
                const row = document.createElement("tr");

                row.innerHTML = `
                    <td>${order.orderId}</td>
                    <td>${order.userId}</td>
                    <td>${order.totalPrice}₩</td>
                    <td>${order.totalDiscount || 0}₩</td>
                    <td>${new Date(order.orderDate).toLocaleString()}</td>
                    <td>${order.orderStatus}</td>
                    <td>${order.recipientName || ''}</td>
                    <td>${order.recipientPhone || ''}</td>
                    <td>${order.shippingAddress || ''}</td>
                    <td>
                        <select onchange="updateStatus(${order.orderId}, this.value)">
                            <option value="PENDING" ${order.orderStatus === 'PENDING' ? 'selected' : ''}>Đang xử lý</option>
                            <option value="PAID" ${order.orderStatus === 'PAID' ? 'selected' : ''}>Đã thanh toán</option>
                            <option value="SHIPPED" ${order.orderStatus === 'SHIPPED' ? 'selected' : ''}>Đã giao</option>
                            <option value="CANCELLED" ${order.orderStatus === 'CANCELLED' ? 'selected' : ''}>Đã hủy</option>
                        </select>
                    </td>
                `;

                tbody.appendChild(row);
            });

        } else {
            alert(result.message || "Không thể tải danh sách đơn hàng");
        }

    } catch (err) {
        alert("Lỗi: " + err.message);
    }
}

async function updateStatus(orderId, newStatus) {
    try {
        const res = await fetch(`${API_BASE}/order/admin/${orderId}/status?status=${newStatus}`, {
            method: "PUT",
            headers: {
                "Authorization": `Bearer ${accessToken}`
            }
        });

        const result = await res.json();
        if (res.ok && result.success) {
            alert("✅ Cập nhật thành công");
            loadOrders();
        } else {
            alert(result.message || "Cập nhật thất bại");
        }
    } catch (err) {
        alert("Lỗi: " + err.message);
    }
}

window.onload = loadOrders;
