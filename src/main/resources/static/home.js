const API_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");

let currentPage = 0;
let currentSearchType = "";
let currentKeyword = "";
let totalPages = 0;

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



async function fetchRandomBooks(page = 0) {
    currentPage = page;
    currentSearchType = "random";
    currentKeyword = ""; // Không cần từ khóa

    try {
        const url = new URL(`${API_BASE}/book/random`);
        url.searchParams.append("page", page);
        url.searchParams.append("size", 5);

        const res = await fetch(url, {
            headers: { Authorization: `Bearer ${accessToken}` }
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

function showBooks(books) {
    const html =
        books.length === 0
            ? "<p>Không có kết quả</p>"
            : books
                .map(
                    (b) => `
    <div style="display: flex; justify-content: space-between; border:1px solid #ccc; margin:10px; padding:10px;">

      <!-- LEFT: BOOK INFO -->
      <div style="flex: 1;">
        <b>Title:</b> ${b.title} <br/>
        <b>Author:</b> ${b.author} <br/>
        <b>Price:</b> ${b.price} <br/>
        <b>Stock:</b> ${b.stock} <br/>
        <b>Description:</b> ${b.description} <br/>
        <b>Category:</b> ${b.categoryName} <br/>
        <img src="${b.imgUrl}" alt="${b.title}" style="max-width:100px; max-height:100px;" /><br/>
      </div>

      <!-- RIGHT: ACTIONS -->
      <div style="flex-shrink: 0; display: flex; flex-direction: column; align-items: flex-end; justify-content: center; gap: 6px;">
        <label for="qty-${b.id}">Số lượng:</label>
        <input type="number" id="qty-${b.id}" value="1" min="1" max="${b.stock}" style="width: 60px;"/>
        <button onclick="addToCart(${b.id})">🛒 Thêm vào giỏ</button>
      </div>
    </div>
  `
                )
                .join("");
    document.getElementById("bookList").innerHTML = html;
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
            body: JSON.stringify({ bookId, quantity })  // ❌ Bỏ userId
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

//khi app khoi dong
window.onload = () => {
    fetchRandomBooks();

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
