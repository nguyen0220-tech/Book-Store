const API_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");

async function loadCart() {
    try {
        const res = await fetch(`${API_BASE}/cart`, {
            headers: {
                "Authorization": `Bearer ${accessToken}`
            }
        });

        const result = await res.json();
        if (res.ok && result.success) {
            const items = result.data.items || [];
            if (items.length === 0) {
                document.getElementById("cartContainer").innerHTML = "<p>🛒 Giỏ hàng trống</p>";
                document.getElementById("totalPrice").textContent = "Tổng tiền: 0₩";
                return;
            }

            const html = `
    <table>
        <thead>
            <tr>
                <th>Ảnh</th>
                <th>Tiêu đề</th>
                <th>Giá</th>
                <th>Số lượng</th>
                <th>Thành tiền</th>
                <th>Thao tác</th>
            </tr>
        </thead>
        <tbody>
            ${items.map(item => {
                const hasSale = item.salePrice !== null && item.salePrice < item.price;
                const displayPrice = hasSale
                    ? `<span style="text-decoration: line-through; color: gray;">${item.price.toLocaleString()}₩</span>
                       <br/><span style="color: red; font-weight: bold;">${item.salePrice.toLocaleString()}₩</span>`
                    : `${item.price.toLocaleString()}₩`;

                const unitPrice = hasSale ? item.salePrice : item.price;
                const subtotal = unitPrice * item.quantity;

                return `
                    <tr>
                        <td><img src="${item.imgUrl}" /></td>
                        <td>${item.title}</td>
                        <td>${displayPrice}</td>
                        <td>
                            <input type="number" value="${item.quantity}" min="1"
                                onchange="updateQuantity(${item.bookId}, this.value)" />
                        </td>
                        <td>${subtotal.toLocaleString()}₩</td>
                        <td class="actions">
                            <button onclick="removeItem(${item.bookId})">❌ Xóa</button>
                        </td>
                    </tr>
                `;
            }).join("")}
        </tbody>
    </table>
`;

            document.getElementById("cartContainer").innerHTML = html;
            document.getElementById("totalPrice").textContent =
                "Tổng tiền: " + result.data.totalPrice + "₩";

        } else {
            alert(result.message || "Không thể tải giỏ hàng");
        }

    } catch (err) {
        alert("Lỗi server: " + err.message);
    }
}

async function updateQuantity(bookId, quantity) {
    try {
        const res = await fetch(`${API_BASE}/cart/items`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${accessToken}`
            },
            body: JSON.stringify({
                bookId,
                quantity: parseInt(quantity)
            })
        });

        const result = await res.json();
        if (res.ok && result.success) {
            await loadCart();
        } else {
            alert(result.message || "Không thể cập nhật");
        }

    } catch (err) {
        alert("Lỗi: " + err.message);
    }
}
window.updateQuantity=updateQuantity

async function removeItem(bookId) {
    try {
        const res = await fetch(`${API_BASE}/cart/items?bookId=${bookId}`, {
            method: "DELETE",
            headers: {"Authorization": `Bearer ${accessToken}`}
        });

        const result = await res.json();
        if (res.ok && result.success) {
            await loadCart();
        } else {
            alert(result.message || "Xóa thất bại");
        }

    } catch (err) {
        alert("Lỗi: " + err.message);
    }
}
window.removeItem=removeItem

async function clearCart() {
    if (!confirm("Bạn có chắc muốn xóa toàn bộ giỏ hàng?")) return;

    const res = await fetch(`${API_BASE}/cart`, {
        method: "DELETE",
        headers: {"Authorization": `Bearer ${accessToken}`}
    });
    const result = await res.json();
    if (res.ok && result.success) {
        await loadCart();
    } else {
        alert(result.message || "Xóa thất bại");
    }
}
window.clearCart=clearCart

async function placeOrder() {
    if (!accessToken) return alert("Vui lòng đăng nhập");

    const recipientName = document.getElementById("recipientName").value.trim();
    const recipientPhone = document.getElementById("recipientPhone").value.trim();
    const shippingAddress = document.getElementById("shippingAddress").value.trim();
    const note = document.getElementById("note").value.trim();
    const couponCode = document.getElementById("couponCode").value.trim();
    const usePoint = document.getElementById("usePoint").value.trim();

    if (!recipientName || !recipientPhone || !shippingAddress) {
        alert("❗ Vui lòng nhập đầy đủ thông tin giao hàng");
        return;
    }

    const orderRequest = {
        recipientName,
        recipientPhone,
        shippingAddress,
        note,
        couponCode,
        usePoint
    };

    try {
        const res = await fetch(`${API_BASE}/order/checkout`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${accessToken}`
            },
            body: JSON.stringify(orderRequest)
        });

        const result = await res.json();
        if (res.ok && result.success) {
            alert("✅ Đặt hàng thành công!");
            window.location.href = "order-history.html";
        } else {
            alert("❌ " + (result.message || "Lỗi khi đặt hàng"));
        }

    } catch (err) {
        alert("⚠️ Lỗi server: " + err.message);
    }
}
window.placeOrder=placeOrder

async function loadFriends() {
    try {
        const res = await fetch(`${API_BASE}/friend/to-give`, {
            headers: { "Authorization": `Bearer ${accessToken}` }
        });

        const result = await res.json();
        if (res.ok && result.success) {
            const friends = result.data || [];
            const select = document.getElementById("friendSelect");

            friends.forEach((f, idx) => {
                const opt = document.createElement("option");
                opt.value = idx; // lưu index để dễ lấy object
                opt.textContent = `${f.recipientName} (${f.friendPhone})`;
                select.appendChild(opt);
            });

            window.friendsData = friends;

        } else {
            console.warn("Không có bạn bè để tặng quà");
        }

    } catch (err) {
        console.error("Lỗi khi load bạn bè:", err);
    }
}

function fillFriendInfo(index) {
    if (index === "") return; // không chọn gì
    const friend = window.friendsData[index];
    if (friend) {
        document.getElementById("recipientName").value = friend.recipientName || "";
        document.getElementById("recipientPhone").value = friend.friendPhone || "";
        document.getElementById("shippingAddress").value = friend.friendAddress || "";
    }
}
window.fillFriendInfo=fillFriendInfo

async function loadSuggestFromCart() {
    try {
        const res = await fetch(`${API_BASE}/book/suggest-from-cart`, {
            headers: { "Authorization": `Bearer ${accessToken}` }
        });

        const result = await res.json();
        let books = [];
        let useRandomId = false; // đánh dấu nếu dùng API random (id khác)

        if (res.ok && result.success) {
            books = result.data || [];
        }

        // Nếu không có sách gợi ý từ cart, gọi API random
        if (!books || books.length === 0) {
            const randomRes = await fetch(`${API_BASE}/book/random?page=0&size=6`, {
                headers: { "Authorization": `Bearer ${accessToken}` }
            });
            const randomResult = await randomRes.json();
            if (randomRes.ok && randomResult.success) {
                books = randomResult.data?.content || [];
                useRandomId = true;
            }
        }

        const suggestContainer = document.getElementById("suggestContainer");
        if (!books || books.length === 0) {
            suggestContainer.innerHTML = "<p>❌ Không có sách gợi ý</p>";
            return;
        }

        const html = `
            <div class="suggest-grid">
                ${books.map(b => {
            const idToUse = useRandomId ? b.id : b.bookId;
            return `
                    <div class="suggest-card">
                        <img src="${b.imgUrl}" alt="${b.title}" />
                        <h3>${b.title}</h3>
                        <p>
                            ${b.salePrice && b.salePrice < b.price
                ? `<span style="text-decoration: line-through; color: gray;">
                                    ${b.price.toLocaleString()}₩
                                  </span>
                                  <br/>
                                  <span style="color: red; font-weight: bold;">
                                    ${b.salePrice.toLocaleString()}₩
                                  </span>`
                : `${b.price.toLocaleString()}₩`
            }
                        </p>
                        <button onclick="addToCart(${idToUse})">🛒 Thêm vào giỏ</button>
                    </div>
                    `;
        }).join("")}
            </div>
        `;
        suggestContainer.innerHTML = html;

    } catch (err) {
        alert("Lỗi server khi tải gợi ý: " + err.message);
    }
}

async function addToCart(bookId, quantity = 1) {
    try {
        const res = await fetch(`${API_BASE}/cart/items`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${accessToken}`
            },
            body: JSON.stringify({
                bookId,
                quantity
            })
        });

        const result = await res.json();
        if (res.ok && result.success) {
            alert("✅ " + result.message);
            await loadCart();
            await loadSuggestFromCart();
        } else {
            alert("❌ " + (result.message || "Thêm sách thất bại"));
        }

    } catch (err) {
        alert("⚠️ Lỗi server: " + err.message);
    }
}
window.addToCart = addToCart;

async function loadPoint() {
    try {
        const res = await fetch(`${API_BASE}/points`, {
            headers: { "Authorization": `Bearer ${accessToken}` }
        });

        const result = await res.json();
        if (res.ok && result.success) {
            const point = result.data.point || 0;
            document.getElementById("pointBalance").textContent =
                `⭐ Điểm hiện có: ${point.toLocaleString()} P`;
        } else {
            document.getElementById("pointBalance").textContent = "⭐ Điểm hiện có: 0 P";
        }

    } catch (err) {
        console.error("Lỗi khi load point:", err);
        document.getElementById("pointBalance").textContent = "⭐ Điểm hiện có: 0 P";
    }
}

let currentPointPage = 0;
const pointPageSize = 5;

async function loadPointHistory(page = 0, size = pointPageSize) {
    try {
        const res = await fetch(`${API_BASE}/points/history?page=${page}&size=${size}`, {
            headers: { "Authorization": `Bearer ${accessToken}` }
        });

        const result = await res.json();
        if (res.ok && result.success) {
            const history = result.data.content || [];
            const totalPages = result.data.totalPages;
            currentPointPage = result.data.number;

            if (history.length === 0) {
                document.getElementById("pointHistoryContainer").innerHTML =
                    "<p>❌ Chưa có lịch sử point</p>";
                document.getElementById("pointHistoryPagination").innerHTML = "";
                return;
            }

            // Bảng hiển thị lịch sử
            const html = `
                <table border="1" cellspacing="0" cellpadding="5">
                    <thead>
                        <tr>
                            <th>Thời gian</th>
                            <th>Điểm tích lũy</th>
                            <th>Điểm sử dụng</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${history.map(h => `
                            <tr>
                                <td>${new Date(h.updatedAt).toLocaleString()}</td>
                                <td style="color:green;">${h.pointHoard ? h.pointHoard.toLocaleString() : "-"}</td>
                                <td style="color:red;">${h.pointUsage ? h.pointUsage.toLocaleString() : "-"}</td>
                            </tr>
                        `).join("")}
                    </tbody>
                </table>
            `;
            document.getElementById("pointHistoryContainer").innerHTML = html;

            // Nút phân trang
            let paginationHtml = `
                <button ${currentPointPage === 0 ? "disabled" : ""}
                    onclick="loadPointHistory(${currentPointPage - 1}, ${size})">⬅ Trang trước</button>
                <span>Trang ${currentPointPage + 1} / ${totalPages}</span>
                <button ${currentPointPage >= totalPages - 1 ? "disabled" : ""}
                    onclick="loadPointHistory(${currentPointPage + 1}, ${size})">Trang sau ➡</button>
            `;
            document.getElementById("pointHistoryPagination").innerHTML = paginationHtml;

        } else {
            alert(result.message || "Không thể tải lịch sử point");
        }

    } catch (err) {
        alert("⚠️ Lỗi server khi tải lịch sử point: " + err.message);
    }
}
window.loadPointHistory = loadPointHistory;

async function loadCoupons() {
    try {
        const res = await fetch(`${API_BASE}/coupon/user`, {
            headers: {
                "Authorization": `Bearer ${accessToken}`
            }
        });

        const result = await res.json();
        if (res.ok && result.success) {
            const select = document.getElementById("couponSelect");
            select.innerHTML = `<option value="">-- Chọn coupon từ danh sách --</option>`;

            result.data.forEach(coupon => {
                // Hiển thị rõ ràng loại giảm
                let displayText = `${coupon.couponCode} - ${coupon.description} (`;
                if (coupon.percentDiscount) {
                    if (coupon.discountPercent && coupon.discountPercent > 0) {
                        displayText += `Giảm ${coupon.discountPercent}%`;
                    } else {
                        displayText += `Giảm %`;
                    }
                } else {
                    if (coupon.discountAmount && coupon.discountAmount > 0) {
                        displayText += `Giảm ${coupon.discountAmount.toLocaleString()}₩`;
                    } else {
                        displayText += `Giảm cố định`;
                    }
                }
                displayText += `)`;

                const opt = document.createElement("option");
                opt.value = coupon.couponCode;
                opt.textContent = displayText;
                select.appendChild(opt);
            });
        } else {
            console.warn(result.message || "Không thể tải coupon");
        }
    } catch (err) {
        console.error("Lỗi khi load coupon:", err.message);
    }
}

function applyCoupon(code) {
    document.getElementById("couponCode").value = code;
}
window.applyCoupon = applyCoupon;

window.onload = async () => {
    await loadCart();
    await loadFriends();
    await loadSuggestFromCart();
    await loadPoint();
    await loadCoupons()
};
