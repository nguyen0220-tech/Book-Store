const API_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");

let currentSort = "default"; // hoặc "descPrice"
let currentStatus = "";
let currentPage = 0;
const pageSize = 5;

function handleStatusFilter() {
    const status = document.getElementById("statusFilter").value;
    currentStatus = status;
    loadOrders(0);
}
window.handleStatusFilter=handleStatusFilter

async function loadOrders(page = 0) {
    let url = `${API_BASE}/order/admin/all?page=${page}&size=${pageSize}`;

    if (currentSort === "descPrice") {
        url = `${API_BASE}/order/admin/all-desc?page=${page}&size=${pageSize}`;
    } else if (currentSort === "ascPrice") {
        url = `${API_BASE}/order/admin/all-asc?page=${page}&size=${pageSize}`;
    } else if (currentSort === "createdAt") {
        url = `${API_BASE}/order/admin/all-createdAt?page=${page}&size=${pageSize}`;
    }
    if (currentStatus !== "") {
        url = `${API_BASE}/order/admin/all-status?page=${page}&size=${pageSize}&status=${currentStatus}`;
    }

    try {
        const res = await fetch(url, {
            headers: {
                "Authorization": `Bearer ${accessToken}`
            }
        });

        const result = await res.json();
        if (res.ok && result.success) {
            const data = result.data.content;
            const totalPages = result.data.totalPages;

            const tbody = document.getElementById("orderTableBody");
            tbody.innerHTML = "";

            data.forEach(order => {
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${order.orderId}</td>
                    <td>${order.userId}</td>
                    <td>${order.orderUsername}</td>
                    <td>${order.totalPrice}₩</td>
                    <td>${order.totalDiscount || 0}₩</td>
                    <td>${order.couponCode || '-'}</td>
                    <td>${new Date(order.orderDate).toLocaleString()}</td>
                    <td>${order.orderStatus}</td>
                    <td>${order.recipientName || ''}</td>
                    <td>${order.recipientPhone || ''}</td>
                    <td>${order.shippingAddress || ''}</td>
                    <td>${order.confirmed ? "✅ Đã xác nhận" : "❌ Chưa xác nhận"}<td>
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

            renderPagination(totalPages, page);
            currentPage = page;
        } else {
            alert(result.message || "Không thể tải danh sách đơn hàng");
        }

    } catch (err) {
        alert("Lỗi: " + err.message);
    }
}
window.loadOrders=loadOrders

function handleSortChange() {
    const sortValue = document.getElementById("sortSelect").value;
    currentSort = sortValue;
    loadOrders(0); // Reset về trang đầu khi đổi sort
}
window.handleSortChange=handleSortChange

function renderPagination(totalPages, current) {
    const paginationDiv = document.getElementById("pagination");
    paginationDiv.innerHTML = "";

    // Previous button
    if (current > 0) {
        const prevBtn = document.createElement("button");
        prevBtn.innerText = "« Prev";
        prevBtn.onclick = () => loadOrders(current - 1);
        prevBtn.style.margin = "0 5px";
        paginationDiv.appendChild(prevBtn);
    }

    // Determine start and end page index
    let startPage = Math.max(0, current - 1);
    let endPage = Math.min(totalPages - 1, startPage + 2);
    if (endPage - startPage < 2 && startPage > 0) {
        startPage = Math.max(0, endPage - 2);
    }

    for (let i = startPage; i <= endPage; i++) {
        const btn = document.createElement("button");
        btn.innerText = i + 1;
        btn.disabled = (i === current);
        btn.onclick = () => loadOrders(i);
        btn.style.margin = "0 5px";
        paginationDiv.appendChild(btn);
    }

    // Next button
    if (current < totalPages - 1) {
        const nextBtn = document.createElement("button");
        nextBtn.innerText = "Next »";
        nextBtn.onclick = () => loadOrders(current + 1);
        nextBtn.style.margin = "0 5px";
        paginationDiv.appendChild(nextBtn);
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
            loadOrders(currentPage); // refresh current page
        } else {
            alert(result.message || "Cập nhật thất bại");
        }
    } catch (err) {
        alert("Lỗi: " + err.message);
    }
}
window.updateStatus=updateStatus

window.onload = () => loadOrders(0);
