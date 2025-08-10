const API_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");

let currentPage = 0;
const pageSize = 5;
let totalPages = 0;

const unreadCountSpan = document.getElementById("unreadCount");
const notificationList = document.getElementById("notificationList");
const detailSection = document.getElementById("notificationDetail");
const detailContent = document.getElementById("detailContent");
const typeFilter = document.getElementById("typeFilter");

let currentNotificationId = null;

// Lấy số lượng thông báo chưa đọc
function fetchUnreadCount() {
    fetch(`${API_BASE}/notify/un-read`, {
        headers: {
            Authorization: `Bearer ${accessToken}`
        }
    })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                unreadCountSpan.textContent = data.data;
            }
        });
}

// Lấy danh sách thông báo
function fetchNotifications(page = 0) {
    const selectedType = typeFilter.value;
    const apiUrl = selectedType
        ? `${API_BASE}/notify/filter?type=${selectedType}&page=${page}&size=${pageSize}`
        : `${API_BASE}/notify?page=${page}&size=${pageSize}`;

    fetch(apiUrl, {
        headers: {
            Authorization: `Bearer ${accessToken}`
        }
    })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                const pageData = data.data;
                totalPages = pageData.totalPages;
                currentPage = pageData.number;

                notificationList.innerHTML = "";

                pageData.content.forEach(notification => {
                    const li = document.createElement("li");
                    const icon = notification.read ? "✅" : "📩";
                    const date = new Date(notification.createdAt).toLocaleDateString("vi-VN");

                    let typeText = "";
                    switch (notification.type) {
                        case "ORDER":
                            typeText = "📦 Đơn hàng";
                            break;
                        case "BOOK_DISCOUNT":
                            typeText = "📚 Khuyến mãi sách";
                            break;
                        case "COUPON":
                            typeText = "🎟️ Thông báo coupon";
                            break;
                        case "FRIEND":
                            typeText = "👥 Lời mời kểt bạn";
                            break;
                        case "SYSTEM":
                            typeText = "⚙️ Hệ thống";
                            break;
                        default:
                            typeText = notification.type;
                    }

                    li.innerHTML = `
                        <input type="checkbox" class="notification-checkbox" data-id="${notification.id}">
                        ${icon} <strong>${typeText}</strong> - 🕒 ${date}
                    `;
                    li.style.cursor = "pointer";
                    li.style.marginBottom = "10px";
                    li.style.padding = "10px";
                    li.style.borderRadius = "8px";
                    li.style.backgroundColor = notification.read ? "#f2f2f2" : "#fff9c4";
                    li.dataset.id = notification.id;

                    li.addEventListener("click", () => {
                        showNotificationDetail(notification.id);
                    });

                    notificationList.appendChild(li);
                });

                renderPaginationButtons();
            }
        });
}

// Phân trang
function renderPaginationButtons() {
    const container = document.getElementById("paginationControls");
    container.innerHTML = "";

    for (let i = 0; i < totalPages; i++) {
        const btn = document.createElement("button");
        btn.textContent = i + 1;
        btn.disabled = i === currentPage;
        btn.style.margin = "0 5px";
        btn.addEventListener("click", () => {
            fetchNotifications(i);
        });
        container.appendChild(btn);
    }
}

// Hiển thị chi tiết thông báo
function showNotificationDetail(notificationId) {
    fetch(`${API_BASE}/notify/${notificationId}`, {
        headers: {
            Authorization: `Bearer ${accessToken}`
        }
    })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                currentNotificationId = notificationId;
                const notification = data.data;
                const createdDate = new Date(notification.createdAt).toLocaleString();

                detailContent.innerHTML = `
                    <p><strong>Nội dung:</strong> ${notification.message}</p>
                    ${notification.type === "BOOK_DISCOUNT" && notification.title ? `<p><strong>Sách:</strong> ${notification.title}</p>` : ""}
                    ${notification.type === "ORDER" && notification.orderId ? `<p><strong>Đơn hàng:</strong> #${notification.orderId}</p>` : ""}
                    <p><strong>Thời gian:</strong> ${createdDate}</p>
                    <p><strong>Trạng thái:</strong> ${notification.read ? "Đã đọc" : "Chưa đọc"}</p>
                `;

                detailSection.style.display = "block";
            }
        });
}

// Đánh dấu đã đọc
document.getElementById("markSelectedReadBtn").addEventListener("click", () => {
    const checkboxes = document.querySelectorAll(".notification-checkbox:checked");
    const ids = Array.from(checkboxes).map(cb => cb.dataset.id);

    if (ids.length === 0) {
        alert("Vui lòng chọn ít nhất một thông báo.");
        return;
    }

    Promise.all(
        ids.map(id =>
            fetch(`${API_BASE}/notify/${id}/read`, {
                method: "PUT",
                headers: {Authorization: `Bearer ${accessToken}`}
            })
        )
    )
        .then(() => {
            alert("Đã đánh dấu các thông báo đã đọc.");
            fetchNotifications();
            fetchUnreadCount();
        })
        .catch(err => {
            console.error(err);
            alert("Đã có lỗi xảy ra.");
        });
});

document.getElementById("deleteSelectedBtn").addEventListener("click", () => {
    const checkboxes = document.querySelectorAll(".notification-checkbox:checked");
    const ids = Array.from(checkboxes).map(cb => cb.dataset.id);

    if (ids.length === 0) {
        alert("Vui lòng chọn ít nhất một thông báo để xóa.");
        return;
    }

    if (!confirm("Bạn có chắc muốn xóa các thông báo đã chọn?")) return;

    Promise.all(
        ids.map(id =>
            fetch(`${API_BASE}/notify/${id}`, {
                method: "DELETE",
                headers: {
                    Authorization: `Bearer ${accessToken}`
                }
            })
        )
    )
        .then(() => {
            alert("Đã xóa các thông báo đã chọn.");
            fetchNotifications();
            fetchUnreadCount();
        })
        .catch(err => {
            console.error(err);
            alert("Đã có lỗi xảy ra khi xóa.");
        });
});


// Sự kiện khi thay đổi bộ lọc loại thông báo
typeFilter.addEventListener("change", () => {
    currentPage = 0;
    fetchNotifications();
});

// Khởi tạo
fetchUnreadCount();
fetchNotifications();
