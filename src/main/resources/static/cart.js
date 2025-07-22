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

async function placeOrder() {
    if (!accessToken) return alert("Vui lòng đăng nhập");

    const recipientName = document.getElementById("recipientName").value.trim();
    const recipientPhone = document.getElementById("recipientPhone").value.trim();
    const shippingAddress = document.getElementById("shippingAddress").value.trim();
    const couponCode = document.getElementById("couponCode").value.trim();

    if (!recipientName || !recipientPhone || !shippingAddress) {
        alert("❗ Vui lòng nhập đầy đủ thông tin giao hàng");
        return;
    }

    const orderRequest = {
        recipientName,
        recipientPhone,
        shippingAddress,
        couponCode
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

window.onload = loadCart;