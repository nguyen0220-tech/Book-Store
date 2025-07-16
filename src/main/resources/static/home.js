const API_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");

let currentPage = 0;
let currentSearchType = "";
let currentKeyword = "";
let totalPages = 0;
let bookmarkedBookIds = [];

document.getElementById("logoutBtn").addEventListener("click", async () => {
    const refreshToken = localStorage.getItem("refreshToken");
    if (!refreshToken) {
        document.getElementById("logoutMessage").textContent = "Không tìm thấy refresh token!";
        return;
    }
    try {
        const res = await fetch(`${API_BASE}/auth/logout`, {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({refreshToken})
        });
        const result = await res.json();
        if (res.ok && result.success) {
            localStorage.removeItem("accessToken");
            localStorage.removeItem("refreshToken");
            localStorage.removeItem("userId");
            window.location.href = "/auth.html";
        } else {
            document.getElementById("logoutMessage").textContent = result.message || "Logout thất bại!";
        }
    } catch (err) {
        document.getElementById("logoutMessage").textContent = "Lỗi server: " + err.message;
    }
});

//giai ma TOKEN
function parseJwt(token) {
    try {
        const payload = token.split('.')[1];
        const decoded = atob(payload); // decode base64
        return JSON.parse(decoded);
    } catch (e) {
        return null;
    }
}

async function fetchBookMarks() {
    try {
        const res = await fetch(`${API_BASE}/book-mark`, {
            headers: {Authorization: `Bearer ${accessToken}`}
        });
        const result = await res.json();
        if (res.ok && result.success) {
            bookmarkedBookIds = result.data.map(b => b.bookId);
        } else {
            console.warn("Không thể tải danh sách bookmark");
        }
    } catch (err) {
        console.error("Lỗi khi tải bookmark:", err.message);
    }
}


async function fetchRandomBooks(page = 0) {
    currentPage = page;
    currentSearchType = "random";
    currentKeyword = ""; // Không cần từ khóa

    try {
        const url = new URL(`${API_BASE}/book/random`);
        url.searchParams.append("page", page);
        url.searchParams.append("size", 5);

        const res = await fetch(url, {
            headers: {Authorization: `Bearer ${accessToken}`}
        });
        const result = await res.json();

        if (res.ok && result.success) {
            const books = result.data.content || [];
            totalPages = result.data.totalPages || 1;
            showBooks(books);
            renderPagination();
        } else {
            alert(result.message || "Không thể lấy sách ngẫu nhiên");
        }
    } catch (err) {
        alert("Lỗi server: " + err.message);
    }
}


async function searchByAuthor() {
    const keyword = document.getElementById("searchKeyword").value.trim();
    if (!keyword) return alert("Vui lòng nhập từ khóa tìm kiếm");
    await searchBooks("by-author", keyword);
}

async function searchByTitle() {
    const keyword = document.getElementById("searchKeyword").value.trim();
    if (!keyword) return alert("Vui lòng nhập từ khóa tìm kiếm");
    await searchBooks("by-title", keyword);
}

async function searchByCategory() {
    const keyword = document.getElementById("searchKeyword").value.trim();
    if (!keyword) return alert("Vui lòng nhập từ khóa tìm kiếm");
    await searchBooks("by-category", keyword);
}

async function searchBooks(type, keyword, page = 0) {
    currentPage = page;
    currentSearchType = type;
    currentKeyword = keyword;

    try {
        const url = new URL(`${API_BASE}/book/${type}`);
        url.searchParams.append("page", page);
        url.searchParams.append("size", 5);
        url.searchParams.append(
            type === "by-category" ? "category" : type === "by-author" ? "author" : "title",
            keyword
        );

        const res = await fetch(url, {
            headers: {Authorization: `Bearer ${accessToken}`}
        });
        const result = await res.json();

        if (res.ok && result.success) {
            const books = result.data.content || [];
            totalPages = result.data.totalPages || 1;
            showBooks(books);
            renderPagination();
        } else {
            alert(result.message || "Lỗi khi tìm kiếm sách");
        }
    } catch (err) {
        alert("Lỗi server: " + err.message);
    }
}

let currentBooks = [];

function showBooks(books) {
    currentBooks = books
    const html =
        books.length === 0
            ? "<p>Không có kết quả</p>"
            : books.map((b) => {
                const isBookmarked = bookmarkedBookIds.includes(b.id);
                return `
    <div style="display: flex; justify-content: space-between; border:1px solid #ccc; margin:10px; padding:10px;">
      <div style="flex: 1;">
        <b>Title:</b> <a href="book-detail.html?bookId=${b.id}">${b.title}</a> <br/>
        <b>Author:</b> ${b.author} <br/>
        <b>Price:</b> ${b.price} <br/>
        <b>Stock:</b> ${b.stock} <br/>
        <b>Description:</b> ${b.description} <br/>
        <b>Category:</b> ${b.categoryName} <br/>
        <img src="${b.imgUrl}" alt="${b.title}" style="max-width:100px; max-height:100px;" /><br/>
        <button onclick="toggleReviews(${b.id})">👁️ Xem đánh giá</button>
        <button onclick="toggleBookmark(${b.id})" style="margin-left:10px;">
            ${isBookmarked ? '💔 Bỏ yêu thích' : '❤️ Yêu thích'}
        </button>

        <div id="reviews-${b.id}" style="display:none; margin-top:10px;"></div>
      </div>

      <div style="flex-shrink: 0; display: flex; flex-direction: column; align-items: flex-end; justify-content: center; gap: 6px;">
        <label for="qty-${b.id}">Số lượng:</label>
        <input type="number" id="qty-${b.id}" value="1" min="1" max="${b.stock}" style="width: 60px;"/>
        <button onclick="addToCart(${b.id})">🛒 Thêm vào giỏ</button>
      </div>
    </div>`;
            }).join("");

    document.getElementById("bookList").innerHTML = html;
}

function getCurrentBooks() {
    return currentBooks;
}

async function toggleBookmark(bookId) {
    const isBookmarked = bookmarkedBookIds.includes(bookId);
    const method = isBookmarked ? 'DELETE' : 'POST';
    const url = `${API_BASE}/book-mark/${isBookmarked ? 'remove' : 'add'}?bookId=${bookId}`;

    try {
        const res = await fetch(url, {
            method: method,
            headers: {
                Authorization: `Bearer ${accessToken}`
            }
        });
        const result = await res.json();
        if (res.ok && result.success) {
            if (isBookmarked) {
                bookmarkedBookIds = bookmarkedBookIds.filter(id => id !== bookId);
            } else {
                bookmarkedBookIds.push(bookId);
            }
            showBooks(await getCurrentBooks()); // reload UI
        } else {
            alert(result.message || "Lỗi khi cập nhật bookmark");
        }
    } catch (err) {
        alert("Lỗi server: " + err.message);
    }
}

async function toggleReviews(bookId) {
    const container = document.getElementById(`reviews-${bookId}`);
    if (container.style.display === "none") {
        try {
            const res = await fetch(`${API_BASE}/review/book?bookId=${bookId}`, {
                headers: {Authorization: `Bearer ${accessToken}`}
            });

            const result = await res.json();

            if (!res.ok || !result.success) {
                container.innerHTML = `<p style="color:red;">Không thể tải đánh giá</p>`;
                container.style.display = "block";
                return;
            }

            const reviews = result.data;
            if (!reviews || reviews.length === 0) {
                container.innerHTML = `<p>Chưa có đánh giá nào cho sách này.</p>`;
            } else {
                container.innerHTML = reviews.map(r => `
                    <div style="border-top:1px solid #ccc; margin-top:5px; padding-top:5px;">
                        🧑 <b>${r.username}</b> - 🕒 ${new Date(r.createdAt).toLocaleDateString("ko-KR")}<br/>
                        ✍️ ${r.content}
                    </div>
                `).join('');
            }

            container.style.display = "block";
        } catch (err) {
            container.innerHTML = `<p style="color:red;">Lỗi server: ${err.message}</p>`;
            container.style.display = "block";
        }
    } else {
        container.style.display = "none";
    }
}


async function addToCart(bookId) {
    if (!accessToken) return alert("Vui lòng đăng nhập");

    const qtyInput = document.getElementById(`qty-${bookId}`);
    const quantity = parseInt(qtyInput.value);
    const maxStock = parseInt(qtyInput.max);

    if (isNaN(quantity) || quantity <= 0) {
        alert("Số lượng không hợp lệ");
        return;
    }

    if (quantity > maxStock) {
        alert(`⚠️ Số lượng vượt quá số lượng còn lại trong kho (${maxStock})`);
        qtyInput.value = maxStock;
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/cart/items`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${accessToken}`
            },
            body: JSON.stringify({bookId, quantity})  // ❌ Bỏ userId
        });
        const result = await res.json();
        if (res.ok && result.success) {
            alert("✅ Đã thêm vào giỏ");
        } else {
            alert(result.message || "Thêm thất bại");
        }
    } catch (err) {
        alert("Lỗi: " + err.message);
    }
}

function renderPagination() {
    const container = document.getElementById("paginationControls");
    container.innerHTML = "";

    if (totalPages <= 1) return;

    for (let i = 0; i < totalPages; i++) {
        const btn = document.createElement("button");
        btn.textContent = i + 1;
        if (i === currentPage) {
            btn.classList.add("active");
        }
        btn.onclick = () => goToPage(i);
        container.appendChild(btn);
    }
}

function goToPage(page) {
    searchBooks(currentSearchType, currentKeyword, page);
}

async function fetchTopNewBooks() {
    try {
        const res = await fetch(`${API_BASE}/book/top-new`, {
            headers: { Authorization: `Bearer ${accessToken}` }
        });
        const result = await res.json();

        if (res.ok && result.success) {
            const books = result.data;
            const html = books.map(b => `
                <div style="border: 1px solid #ccc; padding: 10px; margin: 5px;">
                    <a href="book-detail.html?bookId=${b.bookId}"><b>${b.title}</b></a><br/>
                    <img src="${b.imgUrl}" style="max-width: 80px;" /><br/>
                    <span>Giá: ${b.price.toLocaleString()} ₩</span><br/>
                    <label>Số lượng:</label>
                    <input type="number" id="qty-new-${b.bookId}" value="1" min="1" style="width: 60px;"/>
                    <button onclick="addTopToCart(${b.bookId}, 'new')">🛒 Thêm vào giỏ</button>
                </div>
            `).join("");
            document.getElementById("newBookList").innerHTML = html;
        } else {
            alert(result.message || "Không thể lấy sách mới");
        }
    } catch (err) {
        alert("Lỗi server: " + err.message);
    }
}

async function fetchTopBooks() {
    try {
        const res = await fetch(`${API_BASE}/book/top-book`, {
            headers: { Authorization: `Bearer ${accessToken}` }
        });
        const result = await res.json();

        if (!res.ok || !result.success) {
            document.getElementById("topBooks").innerHTML = "<p>Không thể tải top sách.</p>";
            return;
        }

        const books = result.data;
        const html = books.map((b, idx) => `
            <div style="border: 1px solid #ccc; padding: 10px; margin: 5px;">
                <b>#${idx + 1} <a href="book-detail.html?bookId=${b.bookId}">${b.title}</a></b><br/>
                <img src="${b.imgUrl}" style="max-width: 80px;" /><br/>
                <span>Giá: ${b.price.toLocaleString()} ₩</span><br/>
                <span>Đã bán: ${b.totalSold} cuốn</span><br/>
                <label>Số lượng:</label>
                <input type="number" id="qty-top-${b.bookId}" value="1" min="1" style="width: 60px;"/>
                <button onclick="addTopToCart(${b.bookId}, 'top')">🛒 Thêm vào giỏ</button>
            </div>
        `).join("");

        document.getElementById("topBooks").innerHTML = html;
    } catch (err) {
        document.getElementById("topBooks").innerHTML = "<p>Lỗi server: " + err.message + "</p>";
    }
}

async function addTopToCart(bookId, type) {
    if (!accessToken) return alert("Vui lòng đăng nhập");

    const qtyInput = document.getElementById(`qty-${type}-${bookId}`);
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
            body: JSON.stringify({ bookId, quantity })
        });
        const result = await res.json();
        if (res.ok && result.success) {
            alert("✅ Đã thêm vào giỏ");
        } else {
            alert(result.message || "Thêm thất bại");
        }
    } catch (err) {
        alert("Lỗi: " + err.message);
    }
}

//khi app khoi dong
window.onload = async () => {
    await fetchBookMarks();
    await fetchRandomBooks();
    await fetchTopBooks()
    await fetchTopNewBooks()

    const token = localStorage.getItem("accessToken");
    const payload = parseJwt(token);
    const roles = payload?.roles || [];

    if (!roles.includes("ROLE_ADMIN")) {
        // Ẩn chức năng quản lý
        document.querySelector('a[href="book.html"]').style.display = "none";
        document.querySelector('a[href="user.html"]').style.display = "none";
        document.querySelector('a[href="order-manager.html"]').style.display = "none";
        document.querySelector('a[href="coupon-admin.html"]').style.display = "none";
    }
};
