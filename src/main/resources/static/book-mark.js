const API_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");

window.onload = () => {
    fetchBookMarks();
};

async function fetchBookMarks() {
    try {
        const res = await fetch(`${API_BASE}/book-mark`, {
            headers: {
                Authorization: `Bearer ${accessToken}`
            }
        });

        const result = await res.json();

        if (!res.ok || !result.success) {
            document.getElementById("bookmarkList").innerHTML = `<p style="color:red;">Lỗi: ${result.message}</p>`;
            return;
        }

        const bookmarks = result.data;
        if (!bookmarks || bookmarks.length === 0) {
            document.getElementById("bookmarkList").innerHTML = "<p>📭 Chưa có sách nào trong danh sách yêu thích.</p>";
            return;
        }

        renderBookmarks(bookmarks);
    } catch (err) {
        document.getElementById("bookmarkList").innerHTML = `<p style="color:red;">Lỗi server: ${err.message}</p>`;
    }
}

function renderBookmarks(bookmarks) {
    const html = bookmarks.map(book => {
        const hasSale = book.salePrice && book.salePrice > 0;
        const priceHtml = hasSale
            ? `<b>💰 Price:</b> <span style="text-decoration: line-through; color:gray;">${book.price.toLocaleString()}₫</span>
               <span style="color:red; font-weight:bold;"> → ${book.salePrice.toLocaleString()}₫</span>`
            : `<b>💰 Price:</b> ${book.price.toLocaleString()}₫`;

        // Hiển thị saleExpiry nếu có
        const saleExpiryHtml = book.saleExpiry
            ? `<br/><b>⏳ Sale Expiry:</b> ${book.saleExpiry}`
            : "";

        return `
            <div class="book-card">
                <div>
                    <b>📖 Title:</b> ${book.title} <br/>
                    <b>✍️ Author:</b> ${book.author} <br/>
                    ${priceHtml} 
                    ${saleExpiryHtml} <br/>
                    <b>📝 Description:</b> ${book.description} <br/>
                    <img src="${book.imgUrl}" alt="${book.title}" style="max-width:100px; max-height:100px;" />
                </div>
                <div style="display:flex; flex-direction:column; justify-content:center; gap: 6px;">
                    <button onclick="removeBookmark(${book.bookId})">💔 Bỏ yêu thích</button>

                    <label for="qty-${book.bookId}">Số lượng:</label>
                    <input type="number" id="qty-${book.bookId}" value="1" min="1" style="width: 60px;"/>
                    <button onclick="addToCart(${book.bookId})">🛒 Thêm vào giỏ</button>
                </div>
            </div>
        `;
    }).join("");

    document.getElementById("bookmarkList").innerHTML = html;
}

async function addToCart(bookId) {
    if (!accessToken) return alert("Vui lòng đăng nhập");

    const qtyInput = document.getElementById(`qty-${bookId}`);
    const quantity = parseInt(qtyInput.value);

    if (isNaN(quantity) || quantity <= 0) {
        alert("Số lượng không hợp lệ");
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/cart/items`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${accessToken}`
            },
            body: JSON.stringify({bookId, quantity})
        });

        const result = await res.json();
        if (res.ok && result.success) {
            alert("✅ Đã thêm vào giỏ hàng");
        } else {
            alert(result.message || "❌ Lỗi khi thêm vào giỏ");
        }
    } catch (err) {
        alert("Lỗi server: " + err.message);
    }
}

window.addToCart = addToCart

async function removeBookmark(bookId) {
    if (!confirm("Bạn chắc chắn muốn xoá sách này khỏi danh sách yêu thích?")) return;

    try {
        const res = await fetch(`${API_BASE}/book-mark/remove?bookId=${bookId}`, {
            method: "DELETE",
            headers: {Authorization: `Bearer ${accessToken}`}
        });

        const result = await res.json();

        if (res.ok && result.success) {
            alert("✅ Đã xoá khỏi yêu thích");
            fetchBookMarks(); // reload
        } else {
            alert(result.message || "❌ Lỗi khi xoá yêu thích");
        }
    } catch (err) {
        alert("Lỗi server: " + err.message);
    }
}
window.removeBookmark=removeBookmark
