const API_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");
const userId = localStorage.getItem("userId");

async function loadCart() {
    try {
        const res = await fetch(`${API_BASE}/cart?userId=${userId}`, {
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
                            ${items.map(item => `
                                <tr>
                                    <td><img src="${item.imgUrl}" /></td>
                                    <td>${item.title}</td>
                                    <td>${item.price}₩</td>
                                    <td>
                                        <input type="number" value="${item.quantity}" min="1"
                                            onchange="updateQuantity(${item.bookId}, this.value)" />
                                    </td>
                                    <td>${item.price * item.quantity}₩</td>
                                    <td class="actions">
                                        <button onclick="removeItem(${item.bookId})">❌ Xóa</button>
                                    </td>
                                </tr>
                            `).join("")}
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
                userId: parseInt(userId),
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
        const res = await fetch(`${API_BASE}/cart/items?userId=${userId}&bookId=${bookId}`, {
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

    const res = await fetch(`${API_BASE}/cart?userId=${userId}`, {
        method: "DELETE",
        headers: { "Authorization": `Bearer ${accessToken}` }
    });
    const result = await res.json();
    if (res.ok && result.success) {
        await loadCart();
    } else {
        alert(result.message || "Xóa thất bại");
    }
}

window.onload = loadCart;