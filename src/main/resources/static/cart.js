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

    if (!recipientName || !recipientPhone || !shippingAddress) {
        alert("❗ Vui lòng nhập đầy đủ thông tin giao hàng");
        return;
    }

    const orderRequest = {
        recipientName,
        recipientPhone,
        shippingAddress,
        note,
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

window.onload = async () => {
    await loadCart();
    await loadFriends();
};
