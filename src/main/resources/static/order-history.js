const API_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");

let currentPage = 0;
const pageSize = 5;

window.onload = () => {
    if (!accessToken) {
        alert("Vui lòng đăng nhập lại.");
        window.location.href = "auth.html";
        return;
    }
    fetchOrderHistory();
};

async function fetchOrderHistory(page = 0) {
    currentPage = page;

    try {
        const res = await fetch(`${API_BASE}/order/my-order?page=${page}&size=${pageSize}`, {
            headers: {
                Authorization: `Bearer ${accessToken}`
            }
        });

        const result = await res.json();
        if (!res.ok || !result.success) {
            alert(result.message || "Không thể tải lịch sử đơn hàng");
            return;
        }

        const ordersPage = result.data;
        renderOrders(ordersPage.content); // dữ liệu đơn hàng
        renderPagination(ordersPage.totalPages, ordersPage.number); // phân trang
    } catch (err) {
        alert("Lỗi server: " + err.message);
    }
}

window.fetchOrderHistory = fetchOrderHistory

function renderPagination(totalPages, currentPage) {
    const paginationDiv = document.getElementById("pagination");
    if (!paginationDiv) return;

    let html = "";

    for (let i = 0; i < totalPages; i++) {
        html += `
            <button 
                onclick="fetchOrderHistory(${i})" 
                style="margin: 0 5px; padding: 5px 10px; ${i === currentPage ? 'font-weight: bold; background-color: #ccc;' : ''}">
                ${i + 1}
            </button>
        `;
    }

    paginationDiv.innerHTML = html;
}

async function deleteOrder(orderId) {
    if (!confirm(`Bạn có chắc muốn xoá đơn hàng #${orderId}?`)) return;

    try {
        const res = await fetch(`${API_BASE}/order/${orderId}`, {
            method: "DELETE",
            headers: {
                Authorization: `Bearer ${accessToken}`
            }
        });

        const result = await res.json();
        if (result.success) {
            alert("🗑️ Đã xoá đơn hàng thành công.");
            fetchOrderHistory(); // reload lại danh sách
        } else {
            alert(result.message || "Không thể xoá đơn hàng.");
        }
    } catch (err) {
        alert("Lỗi khi xoá đơn hàng: " + err.message);
    }
}

window.deleteOrder = deleteOrder

function renderOrders(orders) {
    const container = document.getElementById("orderList");

    if (!orders || orders.length === 0) {
        container.innerHTML = "<p>Bạn chưa đặt đơn hàng nào.</p>";
        return;
    }

    let html = "";

    for (const order of orders) {
        const hasDiscount = (order.totalDiscount || 0) > 0;

        html += `
        <div class="order-card">
            <div class="order-header">
                🧾 Đơn hàng #${order.orderId}<br/>
                ⏰ Ngày: ${formatDate(order.orderDate)}<br/>
                ${order.totalDefaultPrice > order.totalPrice
            ? `💸 Tổng đơn (trước giảm): <s>${(order.totalDefaultPrice).toLocaleString()}₩</s><br/>`
            : ""}
                ${hasDiscount
            ? `🎁 Giảm giá (Coupon+Point): <span style="color: red;">- ${order.totalDiscount.toLocaleString()}₩</span><br/>`
            : ""}
                🎟️ Mã coupon: <b>${order.couponCode || "Không dùng"}</b><br/>
                💳 Thanh toán: <b>${order.totalPrice.toLocaleString()}₩</b><br/>
                🔻 Điểm đã sử dụng: <b style="color:red;">${(order.pointUsage || 0).toLocaleString()} P</b><br/>
                ⭐ Điểm tích luỹ: <b>${(order.pointHoard || 0).toLocaleString()} P</b><br/>
                📦 Trạng thái: ${order.orderStatus}<br/>
                <button onclick="downloadInvoice(${order.orderId})"
                    style="margin-top: 10px; color: white; background-color: green; border: none; padding: 5px 10px; border-radius: 5px;">
                    📄 Xem hoá đơn PDF
                </button>
                <button onclick="deleteOrder(${order.orderId})" style="margin-top: 10px; color: white; background-color: red; border: none; padding: 5px 10px; border-radius: 5px;">❌ Xoá đơn</button>
            </div>
        `;

        for (const item of order.items) {
            const hasSale = item.salePrice > 0 && item.salePrice < item.price;

            html += `
            <div class="order-item">
                📚 <b>${item.title}</b><br/>
                <img src="${item.imgUrl}" style="max-width:60px;" /> x ${item.quantity} cuốn - 
                Giá: 
                ${hasSale
                ? `<s>${item.price.toLocaleString()}₩</s> <span style="color:red;">${item.salePrice.toLocaleString()}₩</span>`
                : `${item.price.toLocaleString()}₩`}
            `;

            if (!item.reviewed) {
                html += `
                <div style="margin-top: 10px;">
                    <textarea id="review-input-${order.orderId}-${item.bookId}" placeholder="Viết đánh giá..." style="width: 100%; height: 60px;"></textarea>
                    <button onclick="submitReview(${item.bookId}, ${order.orderId})">✍️ Gửi đánh giá</button>
                </div>
                `;
            } else {
                html += `<p style="color: green; margin-top: 10px;">✅ Bạn đã đánh giá sách này</p>`;
            }

            html += `</div>`; // close .order-item
        }

        html += `</div>`; // close order-card
    }

    container.innerHTML = html;
}

async function submitReview(bookId, orderId) {
    const textarea = document.getElementById(`review-input-${orderId}-${bookId}`);
    const content = textarea.value.trim();

    if (!content) {
        alert("⚠️ Vui lòng nhập nội dung đánh giá.");
        return;
    }

    const review = {
        bookId: bookId,
        orderId: orderId,
        content: content
    };

    try {
        const res = await fetch(`${API_BASE}/review/upload`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${accessToken}`
            },
            body: JSON.stringify(review)
        });

        const result = await res.json();
        if (res.ok && result.success) {
            alert("✅ Đã gửi đánh giá thành công!");
            textarea.disabled = true;
        } else {
            alert(result.message || "❌ Gửi đánh giá thất bại.");
        }
    } catch (err) {
        alert("Lỗi server: " + err.message);
    }
}

window.submitReview = submitReview

function formatDate(timestamp) {
    const date = new Date(timestamp);
    return date.toLocaleDateString("ko-KR");
}

async function downloadInvoice(orderId) {
    try {
        const res = await fetch(`${API_BASE}/order/${orderId}/invoice`, {
            method: "GET",
            headers: {
                Authorization: `Bearer ${accessToken}`
            }
        });

        if (!res.ok) {
            alert("❌ Không thể tải hoá đơn.");
            return;
        }

        const blob = await res.blob();
        const url = window.URL.createObjectURL(blob);

        window.open(url, '_blank');

    } catch (err) {
        alert("Lỗi khi tải hoá đơn: " + err.message);
    }
}

window.downloadInvoice = downloadInvoice
