const API_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");

// --- Sách thường xuyên mua ---
async function fetchPaidManyBooks() {
    try {
        const response = await fetch(`${API_BASE}/book/paid-many`, {
            headers: { "Authorization": `Bearer ${accessToken}` }
        });

        const result = await response.json();

        if (result.success) {
            showBooks(result.data, "paidManyBooks");
        } else {
            alert("Không thể lấy danh sách sách thường mua: " + result.message);
        }
    } catch (error) {
        console.error("Lỗi khi lấy sách thường mua:", error);
        alert("Có lỗi xảy ra khi tải danh sách!");
    }
}

// --- Sách ngẫu nhiên có phân trang ---
let randomBooksPage = 0;
const randomBooksSize = 5;
let randomBooksTotalPages = 1;
let isLoadingRandomBooks = false;

async function fetchRandomBooks(page = 0, size = 5) {
    if (isLoadingRandomBooks || page >= randomBooksTotalPages) return;

    isLoadingRandomBooks = true;

    try {
        const response = await fetch(`${API_BASE}/book/random?page=${page}&size=${size}`, {
            headers: { "Authorization": `Bearer ${accessToken}` }
        });

        const result = await response.json();

        if (result.success) {
            const books = result.data.content; // lấy content từ Page<BookDTO>
            randomBooksTotalPages = result.data.totalPages;
            showBooks(books, "random-book", true);
            randomBooksPage++;
        } else {
            alert("Không thể lấy sách ngẫu nhiên: " + result.message);
        }
    } catch (error) {
        console.error("Lỗi khi lấy sách ngẫu nhiên:", error);
        alert("Có lỗi xảy ra khi tải sách ngẫu nhiên!");
    } finally {
        isLoadingRandomBooks = false;
    }
}

// --- Hiển thị sách (dùng chung cho cả 2 loại) ---
// append = true → thêm vào container thay vì xóa đi
function showBooks(books, containerId, append = false) {
    const container = document.getElementById(containerId);
    if (!append) container.innerHTML = "";

    if (!books || books.length === 0) {
        if (!append) container.innerHTML = "<p>Chưa có sách nào.</p>";
        return;
    }

    books.forEach(book => {
        const card = document.createElement("div");
        card.classList.add("book-card");

        const price = Number(book.price).toLocaleString("vi-VN");
        const salePrice = book.salePrice ? Number(book.salePrice).toLocaleString("vi-VN") : null;

        card.innerHTML = `
            <img src="${book.imgUrl || '/default-book.png'}" alt="${book.title}">
            <div class="book-title">${book.title}</div>
            <div>
                <span class="book-price">${salePrice ? salePrice : price} ₫</span>
                ${salePrice ? `<span class="book-sale">${price} ₫</span>` : ""}
            </div>
            <button class="add-cart-btn">🛒 Thêm vào giỏ hàng</button>
        `;

        const idToAdd = book.bookId ?? book.id;

        card.querySelector(".add-cart-btn").addEventListener("click", () => {
            addToCart(idToAdd, 1);
        });

        container.appendChild(card);
    });
}

// --- Thêm vào giỏ hàng ---
async function addToCart(bookId, quantity) {
    try {
        const response = await fetch(`${API_BASE}/cart/items`, {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${accessToken}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ bookId, quantity })
        });

        const result = await response.json();

        if (result.success) {
            alert(result.message || "Đã thêm vào giỏ hàng!");
        } else {
            alert("Không thể thêm vào giỏ: " + result.message);
        }
    } catch (error) {
        console.error("Lỗi khi thêm vào giỏ hàng:", error);
        alert("Có lỗi xảy ra khi thêm vào giỏ hàng!");
    }
}

// --- Load thêm sách khi cuộn gần cuối trang ---
window.addEventListener("scroll", () => {
    const nearBottom = window.innerHeight + window.scrollY >= document.body.offsetHeight - 200;
    if (nearBottom) fetchRandomBooks(randomBooksPage, randomBooksSize);
});

window.addEventListener("DOMContentLoaded", () => {
    fetchPaidManyBooks();
    fetchRandomBooks(randomBooksPage, randomBooksSize);
});
