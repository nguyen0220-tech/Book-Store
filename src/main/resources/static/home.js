const API_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");

let currentPage = 0;
let currentSearchType = "";
let currentKeyword = "";
let totalPages = 0;
let bookmarkedBookIds = [];

// Nếu chưa đăng nhập
if (!accessToken) {
    document.getElementById("logoutBtn").style.display = "none";
    document.getElementById("profileBtn").style.display = "none";
    document.getElementById("book-markBtn").style.display = "none";
    document.getElementById("cartBtn").style.display = "none";
    document.getElementById("adminBtn").style.display = "none";
    document.getElementById("loginBtn").style.display = "inline-block";
    document.getElementById("signupBtn").style.display = "inline-block";
    document.getElementById("searchHistorySection").style.display = "none";
} else {
    document.getElementById("logoutBtn").style.display = "inline-block";
    document.getElementById("loginBtn").style.display = "none";
    document.getElementById("signupBtn").style.display = "none";
}

// Xử lý nút Logout
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
            window.location.href = "/home.html";
        } else {
            document.getElementById("logoutMessage").textContent = result.message || "Logout thất bại!";
        }
    } catch (err) {
        document.getElementById("logoutMessage").textContent = "Lỗi server: " + err.message;
    }
});

// Xử lý nút Login
document.getElementById("loginBtn").addEventListener("click", () => {
    window.location.href = "/auth.html";
});

// Xử lý nút Signup
document.getElementById("signupBtn").addEventListener("click", async () => {
    window.location.href = "/auth.html";
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
    if (!accessToken)
        return
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
    await saveSearchHistory("by-author", keyword);
}

window.searchByAuthor = searchByAuthor

async function searchByTitle() {
    const keyword = document.getElementById("searchKeyword").value.trim();
    if (!keyword) return alert("Vui lòng nhập từ khóa tìm kiếm");

    await searchBooks("by-title", keyword);
    await saveSearchHistory("by-title", keyword);
}

window.searchByTitle = searchByTitle

async function searchByCategory() {
    const keyword = document.getElementById("searchKeyword").value.trim();
    if (!keyword) return alert("Vui lòng nhập từ khóa tìm kiếm");

    await searchBooks("by-category", keyword);
    await saveSearchHistory("by-category", keyword);
}

window.searchByCategory = searchByCategory

// --- Search History ---
const searchHistoryListEl = document.getElementById("searchHistoryList");
const clearSearchHistoryBtn = document.getElementById("clearSearchHistoryBtn");

// Lấy danh sách lịch sử tìm kiếm từ server và hiển thị
async function loadSearchHistory() {
    if (!accessToken)
        return
    try {
        const res = await fetch(`${API_BASE}/search-history`, {
            headers: {Authorization: `Bearer ${accessToken}`}
        });
        const result = await res.json();
        if (res.ok && result.success) {
            renderSearchHistory(result.data);
        } else {
            console.warn("Không tải được lịch sử tìm kiếm");
            searchHistoryListEl.innerHTML = "<i>Không có lịch sử tìm kiếm</i>";
        }
    } catch (err) {
        console.error("Lỗi tải lịch sử tìm kiếm:", err.message);
        searchHistoryListEl.innerHTML = "<i>Lỗi khi tải lịch sử tìm kiếm</i>";
    }
}

// Hiển thị danh sách lịch sử tìm kiếm
function renderSearchHistory(historyArray) {
    if (!historyArray || historyArray.length === 0) {
        searchHistoryListEl.innerHTML = "<i>Chưa có lịch sử tìm kiếm</i>";
        return;
    }

    searchHistoryListEl.innerHTML = "";

    historyArray.forEach(item => {
        // item: { id, search, searchAt, type }

        const div = document.createElement("div");
        div.style.display = "flex";
        div.style.justifyContent = "space-between";
        div.style.alignItems = "center";
        div.style.padding = "6px 0";
        div.style.borderBottom = "1px solid #eee";

        const infoDiv = document.createElement("div");

        // Hiển thị loại tìm kiếm tiếng Việt
        const typeLabel = translateSearchType(item.type);

        const keywordSpan = document.createElement("span");
        keywordSpan.textContent = `📚 [${typeLabel}] ${item.search}`;
        keywordSpan.style.fontWeight = "bold";
        keywordSpan.style.cursor = "pointer";

        // Click để tự điền và tìm lại
        keywordSpan.addEventListener("click", () => {
            document.getElementById("searchKeyword").value = item.search;

            // Gọi đúng hàm tìm kiếm theo type
            const type = item.type?.toLowerCase(); // "CATEGORY" → "category"
            if (type === "author") searchByAuthor();
            else if (type === "title") searchByTitle();
            else if (type === "category") searchByCategory();
        });

        const timeSpan = document.createElement("div");
        timeSpan.style.fontSize = "0.8em";
        timeSpan.style.color = "gray";
        timeSpan.textContent = "🕘 " + formatTime(item.searchAt);

        infoDiv.appendChild(keywordSpan);
        infoDiv.appendChild(timeSpan);

        const deleteBtn = document.createElement("button");
        deleteBtn.textContent = "🗑️";
        deleteBtn.style.marginLeft = "10px";
        deleteBtn.title = "Xóa lịch sử này";

        deleteBtn.addEventListener("click", async () => {
            await deleteSearchHistory(item.search);
        });

        div.appendChild(infoDiv);
        div.appendChild(deleteBtn);
        searchHistoryListEl.appendChild(div);
    });
}

// Format ISO time: "2025-07-18 18:50"
function formatTime(isoString) {
    const date = new Date(isoString);
    const yyyy = date.getFullYear();
    const MM = String(date.getMonth() + 1).padStart(2, '0');
    const dd = String(date.getDate()).padStart(2, '0');
    const hh = String(date.getHours()).padStart(2, '0');
    const mm = String(date.getMinutes()).padStart(2, '0');
    return `${yyyy}-${MM}-${dd} ${hh}:${mm}`;
}

function translateSearchType(type) {
    switch (type?.toUpperCase()) {
        case "AUTHOR":
            return "Tác giả";
        case "TITLE":
            return "Tiêu đề";
        case "CATEGORY":
            return "Thể loại";
        default:
            return type;
    }
}

async function saveSearchHistory(type, keyword) {
    if (!accessToken) return;

    let searchType;
    switch (type) {
        case "by-author":
            searchType = "AUTHOR";
            break;
        case "by-title":
            searchType = "TITLE";
            break;
        case "by-category":
            searchType = "CATEGORY";
            break;
        default:
            return;
    }

    try {
        const res = await fetch(`${API_BASE}/search-history/add`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${accessToken}`
            },
            body: JSON.stringify({
                keyword: keyword,
                type: searchType
            })
        });

        const result = await res.json();
        if (!res.ok || !result.success) {
            console.warn("Không thể lưu lịch sử tìm kiếm:", result.message);
        }
    } catch (err) {
        console.error("Lỗi khi lưu lịch sử tìm kiếm:", err.message);
    }
}

window.saveSearchHistory = saveSearchHistory

async function deleteSearchHistory(keyword) {
    if (!confirm(`Bạn có chắc muốn xóa lịch sử tìm kiếm: "${keyword}"?`)) return;

    try {
        const url = new URL(`${API_BASE}/search-history/delete`);
        url.searchParams.append("keyword", keyword);

        const res = await fetch(url, {
            method: "DELETE",
            headers: {Authorization: `Bearer ${accessToken}`}
        });
        const result = await res.json();
        if (res.ok && result.success) {
            loadSearchHistory();
        } else {
            alert(result.message || "Xóa lịch sử tìm kiếm thất bại");
        }
    } catch (err) {
        alert("Lỗi khi xóa lịch sử tìm kiếm: " + err.message);
    }
}

window.deleteSearchHistory = deleteSearchHistory

clearSearchHistoryBtn.addEventListener("click", async () => {
    if (!confirm("Bạn có chắc muốn xóa toàn bộ lịch sử tìm kiếm?")) return;

    try {
        const res = await fetch(`${API_BASE}/search-history/delete-all`, {
            method: "DELETE",
            headers: {Authorization: `Bearer ${accessToken}`}
        });
        const result = await res.json();
        if (res.ok && result.success) {
            loadSearchHistory();
        } else {
            alert(result.message || "Xóa toàn bộ lịch sử tìm kiếm thất bại");
        }
    } catch (err) {
        alert("Lỗi khi xóa toàn bộ lịch sử tìm kiếm: " + err.message);
    }
});


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
    currentBooks = books;
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
        <b>Price:</b> ${
                    b.salePrice && b.salePrice < b.price
                        ? `<span style="text-decoration: line-through; color: gray;">${b.price}</span> 
                   <span style="color: red; font-weight: bold;">${b.salePrice}</span>`
                        : `${b.price}`
                } <br/>
<!--        <b>Stock:</b> ${b.stock} <br/>-->
        <b>Description:</b> ${truncateToSentences(b.description, 2)} <br/>
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

function truncateToSentences(text, maxSentences) {
    if (!text) return "";

    // Tách theo các dấu kết thúc câu: ".", "!", "?" theo tiếng Việt/Anh
    const sentences = text.match(/[^.!?]+[.!?]+/g);

    if (!sentences) return text; // Không tìm thấy câu nào

    // Lấy tối đa `maxSentences` câu và nối lại
    return sentences.slice(0, maxSentences).join(' ').trim();
}

function getCurrentBooks() {
    return currentBooks;
}

async function toggleBookmark(bookId) {
    if (!accessToken) {
        alert("Vui lòng đăng nhập để sử dụng tính năng này");
        window.location.href = "/auth.html";
        return;
    }

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

window.toggleBookmark = toggleBookmark

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

window.toggleReviews = toggleReviews

async function addToCart(bookId) {
    if (!accessToken) {
        alert("Vui lòng đăng nhập");
        return window.location.href = "/auth.html";

    }

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

window.addToCart = addToCart

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

let friendSuggestCurrentPage = 0;
let friendSuggestTotalPages = 0;

async function loadFriendSuggestBooks(page = 0) {
    const container = document.getElementById("friendSuggestBooks");
    if (!accessToken) {
        container.innerHTML = "<i>🔑 Đăng nhập để sử dụng tính năng này</i>";
        return;
    }
    try {
        const url = new URL(`${API_BASE}/book/suggest-from-friend`);
        url.searchParams.append("page", page);
        url.searchParams.append("size", 5);

        const res = await fetch(url, {
            headers: {Authorization: `Bearer ${accessToken}`}
        });

        const result = await res.json();
        if (res.ok && result.success) {
            const data = result.data;
            renderFriendSuggestBooks(data.content || []);
            friendSuggestCurrentPage = data.number;
            friendSuggestTotalPages = data.totalPages;
            renderFriendSuggestPagination();
        } else {
            alert(result.message || "Không thể tải gợi ý sách từ bạn bè");
        }
    } catch (err) {
        alert("Lỗi server: " + err.message);
    }
}

window.loadFriendSuggestBooks = loadFriendSuggestBooks;

function renderFriendSuggestBooks(books) {
    const container = document.getElementById("friendSuggestBooks");
    container.innerHTML = "";

    if (!books || books.length === 0) {
        container.innerHTML = "<i>Không có gợi ý sách nào từ bạn bè</i>";
        return;
    }

    books.forEach(b => {
        const div = document.createElement("div");
        div.classList.add("book-item");

        // Tính hiển thị giá: nếu có salePrice > 0, show giá sale + gạch ngang giá cũ
        let priceHtml = "";
        if (b.salePrice && b.salePrice > 0) {
            priceHtml = `<div>💵 <span style="text-decoration: line-through; color:#888;">${b.price} đ</span> <span style="color:red; font-weight:bold;">${b.salePrice} đ</span></div>`;
        } else {
            priceHtml = `<div>💵 ${b.price ? b.price + " đ" : "Liên hệ"}</div>`;
        }

        div.innerHTML = `
            <img src="${b.imgUrl || '/default-book.png'}" alt="${b.title}" style="width:60px;height:80px;object-fit:cover;">
            <div>
                <div><b>${b.title}</b></div>
                <div>✍️ ${b.author || "Không rõ"}</div>
                ${priceHtml}
                <div>👤 Gợi ý từ: <b>${b.friendName}</b></div>
                <button class="add-to-cart-btn" data-book-id="${b.id}">🛒 Thêm vào giỏ</button>
            </div>
        `;

        container.appendChild(div);

        // Event listener nút thêm vào giỏ
        const addBtn = div.querySelector(".add-to-cart-btn");
        addBtn.addEventListener("click", async () => {
            try {
                const res = await fetch(`${API_BASE}/cart/items`, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        "Authorization": `Bearer ${accessToken}`
                    },
                    body: JSON.stringify({
                        bookId: b.id,
                        quantity: 1
                    })
                });

                const result = await res.json();
                if (res.ok && result.success) {
                    alert("Đã thêm vào giỏ hàng 🎉");
                } else {
                    alert(result.message || "Không thể thêm vào giỏ hàng");
                }
            } catch (err) {
                alert("Lỗi server: " + err.message);
            }
        });
    });
}

function renderFriendSuggestPagination() {
    const container = document.getElementById("friendSuggestPagination");
    container.innerHTML = "";

    const prevBtn = document.createElement("button");
    prevBtn.textContent = "« Prev";
    prevBtn.disabled = friendSuggestCurrentPage === 0;
    prevBtn.onclick = () => loadFriendSuggestBooks(friendSuggestCurrentPage - 1);
    container.appendChild(prevBtn);

    for (let i = 0; i < friendSuggestTotalPages; i++) {
        const btn = document.createElement("button");
        btn.textContent = i + 1;
        if (i === friendSuggestCurrentPage) btn.classList.add("active");
        btn.onclick = () => loadFriendSuggestBooks(i);
        container.appendChild(btn);
    }

    const nextBtn = document.createElement("button");
    nextBtn.textContent = "Next »";
    nextBtn.disabled = friendSuggestCurrentPage === friendSuggestTotalPages - 1;
    nextBtn.onclick = () => loadFriendSuggestBooks(friendSuggestCurrentPage + 1);
    container.appendChild(nextBtn);
}

async function fetchTopNewBooks() {
    try {
        const res = await fetch(`${API_BASE}/book/top-new`, {
            headers: {Authorization: `Bearer ${accessToken}`}
        });
        const result = await res.json();

        if (res.ok && result.success) {
            const books = result.data;
            const html = books.map(b => {
                const hasSale = b.salePrice !== null && b.salePrice < b.price;
                const priceHtml = hasSale
                    ? `<span>Giá: <s>${b.price.toLocaleString()}₩</s> <b style="color:red">${b.salePrice.toLocaleString()}₩</b></span>`
                    : `<span>Giá: ${b.price.toLocaleString()}₩</span>`;

                return `
                    <div style="border: 1px solid #ccc; padding: 10px; margin: 5px;">
                        <a href="book-detail.html?bookId=${b.bookId}"><b>${b.title}</b></a><br/>
                        <img src="${b.imgUrl}" style="max-width: 80px;" /><br/>
                        ${priceHtml}<br/>
                        <label>Số lượng:</label>
                        <input type="number" id="qty-new-${b.bookId}" value="1" min="1" style="width: 60px;"/>
                        <button onclick="addTopToCart(${b.bookId}, 'new')">🛒 Thêm vào giỏ</button>
                    </div>
                `;
            }).join("");
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
            headers: {Authorization: `Bearer ${accessToken}`}
        });
        const result = await res.json();

        if (!res.ok || !result.success) {
            document.getElementById("topBooks").innerHTML = "<p>Không thể tải top sách.</p>";
            return;
        }

        const books = result.data;
        const html = books.map((b, idx) => {
            const hasSale = b.salePrice !== null && b.salePrice < b.price;
            const priceHtml = hasSale
                ? `<span>Giá: <s>${b.price.toLocaleString()}₩</s> <b style="color:red">${b.salePrice.toLocaleString()}₩</b></span>`
                : `<span>Giá: ${b.price.toLocaleString()}₩</span>`;

            return `
                <div style="border: 1px solid #ccc; padding: 10px; margin: 5px;">
                    <b>#${idx + 1} <a href="book-detail.html?bookId=${b.bookId}">${b.title}</a></b><br/>
                    <img src="${b.imgUrl}" style="max-width: 80px;" /><br/>
                    ${priceHtml}<br/>
                    <span>Đã bán: ${b.totalSold} cuốn</span><br/>
                    <label>Số lượng:</label>
                    <input type="number" id="qty-top-${b.bookId}" value="1" min="1" style="width: 60px;"/>
                    <button onclick="addTopToCart(${b.bookId}, 'top')">🛒 Thêm vào giỏ</button>
                </div>
            `;
        }).join("");

        document.getElementById("topBooks").innerHTML = html;
    } catch (err) {
        document.getElementById("topBooks").innerHTML = "<p>Lỗi server: " + err.message + "</p>";
    }
}

async function addTopToCart(bookId, type) {
    if (!accessToken) {
        alert("Vui lòng đăng nhập");
        return window.location.href = "/auth.html";
    }

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
            body: JSON.stringify({bookId, quantity})
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

window.addTopToCart = addTopToCart

let postCurrentPage = 0;
const postPageSize = 5;

async function fetchUserPosts(page = 0, size = postPageSize) {
    try {
        const response = await fetch(`${API_BASE}/post/all?page=${page}&size=${size}`, {
            headers: {
                "Authorization": `Bearer ${accessToken}`
            }
        });

        if (!response.ok) {
            throw new Error("Không thể tải bài viết");
        }

        const json = await response.json();
        const dataPage = json.data; // Đây là Page<PostDTO>

        const posts = dataPage.content || [];
        renderUserPosts(posts);
        renderPostPagination(dataPage);

        postCurrentPage = dataPage.number;
    } catch (error) {
        console.error(error);
        document.getElementById("userPostList").innerHTML = `<p style="color:red;">Lỗi tải bài viết: ${error.message}</p>`;
    }
}

window.fetchUserPosts = fetchUserPosts

function renderUserPosts(posts) {
    const container = document.getElementById("userPostList");
    if (posts.length === 0) {
        container.innerHTML = "<p>Chưa có bài viết nào.</p>";
        return;
    }

    container.innerHTML = `
        <div style="display: flex; flex-direction: column; align-items: center;">
            ${posts.map(post => `
                <div style="width: 100%; max-width: 600px; border:1px solid #ccc; padding: 1rem; margin-bottom: 1rem; border-radius: 8px; background-color: #f9f9f9;">
                    <p><strong>${post.username || "Ẩn danh"}</strong> | ${new Date(post.postDate).toLocaleString()}</p>
                    <p>${post.content}</p>
                    ${post.imageUrl ? `<img src="${post.imageUrl}" style="max-width: 100%; max-height: 300px; border-radius: 6px;" />` : ""}
                    <p><em>Chế độ: ${post.postShare === 'PUBLIC' ? 'Công khai' : post.postShare === 'FRIEND' ? 'Bạn bè' : 'Riêng tư'}</em></p>
                    
                    <!-- Cảm xúc -->
                    <div id="emotions-${post.id}" style="margin-top:10px;"></div>

                    <!-- Bình luận -->
                    <div id="comments-${post.id}" style="margin-top: 10px; background-color: #fff; padding: 10px; border-radius: 6px; border: 1px solid #eee;">
                        <p><strong>Bình luận:</strong></p>
                        <p>Đang tải bình luận...</p>
                    </div>

                    <!-- Form nhập bình luận -->
                    <div style="margin-top: 10px;">
                        <input id="comment-input-${post.id}" type="text" placeholder="Viết bình luận..." style="width: 80%; padding: 6px; border-radius: 4px; border: 1px solid #ccc;" />
                        <button onclick="submitComment(${post.id})" style="padding: 6px 10px; border-radius: 4px; background-color: #007bff; color: white; border: none;">Gửi</button>
                    </div>
                </div>
            `).join("")}
        </div>
    `;

    // Gọi fetchComments & fetchPostEmotions sau khi đã render khung
    posts.forEach(post => {
        fetchComments(post.id);
        fetchPostEmotions(post.id);
    });
}


function renderPostPagination(pageData) {
    const container = document.getElementById("postPagination");
    const totalPages = pageData.totalPages;
    if (totalPages <= 1) {
        container.innerHTML = "";
        return;
    }

    let buttonsHtml = "";
    for (let i = 0; i < totalPages; i++) {
        buttonsHtml += `<button style="margin-right:5px; ${i === postCurrentPage ? "font-weight:bold;" : ""}" onclick="fetchUserPosts(${i})">${i + 1}</button>`;
    }
    container.innerHTML = buttonsHtml;
}

async function fetchComments(postId) {
    try {
        const response = await fetch(`${API_BASE}/comment?postId=${postId}&page=0&size=5`, {
            headers: {
                "Authorization": `Bearer ${accessToken}`
            }
        });

        if (!response.ok) {
            throw new Error("Không thể tải bình luận");
        }

        const json = await response.json();
        const comments = json.data.content || [];

        renderComments(postId, comments);
    } catch (error) {
        console.error(error);
        const commentContainer = document.getElementById(`comments-${postId}`);
        if (commentContainer) {
            commentContainer.innerHTML = `<p style="color:red;">Lỗi tải bình luận: ${error.message}</p>`;
        }
    }
}

function renderComments(postId, comments) {
    const commentContainer = document.getElementById(`comments-${postId}`);
    if (!commentContainer) return;

    if (comments.length === 0) {
        commentContainer.innerHTML = "<p>Chưa có bình luận nào.</p>";
        return;
    }

    const token = localStorage.getItem("accessToken");
    const payload = parseJwt(token);
    const currentUserId = payload?.userId;

    commentContainer.innerHTML = comments.map(comment => {
        const canDelete = comment.userId === currentUserId || comment.postUserId === currentUserId;
        return `
            <div style="padding: 4px 8px; border-bottom: 1px solid #ddd;">
                <strong>${comment.username}</strong>: ${comment.commentContent}
                <br/>
                <small style="color:gray;">${new Date(comment.createdAt).toLocaleString()}</small>
                ${canDelete ? `
                    <button onclick="deleteComment(${postId}, ${comment.id})"
                        style="margin-left: 10px; color: red; border: none; background: none; cursor: pointer;">
                        🗑️
                    </button>` : ""}
            </div>
        `;
    }).join("");
}

async function deleteComment(postId, commentId) {
    if (!accessToken) {
        alert("Vui lòng đăng nhập để sử dụng tính năng này");
        return;
    }

    if (!confirm("Bạn có chắc muốn xóa bình luận này không?")) return;

    try {
        const response = await fetch(`${API_BASE}/comment/${commentId}`, {
            method: "DELETE",
            headers: {
                "Authorization": `Bearer ${accessToken}`
            }
        });

        const result = await response.json();

        if (!response.ok || !result.success) {
            alert(result.message || "Xóa bình luận thất bại");
            return;
        }

        fetchComments(postId); // Refresh lại danh sách comment
    } catch (error) {
        console.error("Lỗi khi xóa bình luận:", error);
        alert("Lỗi xóa bình luận: " + error.message);
    }
}

window.deleteComment = deleteComment

async function submitComment(postId) {
    if (!accessToken) {
        alert("Vui lòng đăng nhập để sử dụng tính năng này");
        window.location.href = "/auth.html";
        return;
    }
    const input = document.getElementById(`comment-input-${postId}`);
    const content = input.value.trim();

    if (!content) {
        alert("Vui lòng nhập bình luận.");
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/comment`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${accessToken}`
            },
            body: JSON.stringify({
                postId: postId,
                comment: content
            })
        });

        const result = await response.json(); // Đọc JSON dù là success hay error

        if (!response.ok || !result.success) {
            alert(result.message || "Không thể gửi bình luận");
            return;
        }

        input.value = "";
        fetchComments(postId);
    } catch (error) {
        console.error("Lỗi gửi bình luận:", error);
        alert("Lỗi khi gửi bình luận: " + error.message);
    }
}

window.submitComment = submitComment

// hàm load cảm xúc cho mỗi bài viết
async function fetchPostEmotions(postId) {
    try {
        const res = await fetch(`${API_BASE}/posts/${postId}/emotions`, {
            headers: {"Authorization": `Bearer ${accessToken}`}
        });
        if (!res.ok) throw new Error("Không thể tải cảm xúc");
        const json = await res.json();
        renderPostEmotions(postId, json.data || []);
    } catch (err) {
        console.error(err);
        document.getElementById(`emotions-${postId}`).innerHTML = `<p style="color:red;">Lỗi tải cảm xúc</p>`;
    }
}

window.fetchPostEmotions = fetchPostEmotions

async function renderPostEmotions(postId, emotions) {
    const container = document.getElementById(`emotions-${postId}`);
    if (!container) return;

    const emotionIcons = {
        LIKE: "👍",
        DISLIKE: "👎",
        LOVE: "❤️",
        WOW: "😮"
    };

    const payload = parseJwt(accessToken);
    const currentUserId = payload?.id || payload?.userId;

    // Lấy danh sách user cảm xúc từ API filter
    let filterList = [];
    let myEmotion = null;

    try {
        const filterRes = await fetch(`${API_BASE}/posts/${postId}/emotions/filter`, {
            headers: {"Authorization": `Bearer ${accessToken}`}
        });
        const filterData = await filterRes.json();
        filterList = filterData.data || [];
        myEmotion = filterList.find(e => e.userId === currentUserId);
    } catch (e) {
        console.error("Error fetching user emotion:", e);
    }

    // Render nút + danh sách người dùng
    container.innerHTML = Object.keys(emotionIcons).map(status => {
        const count = emotions.find(e => e.emotionStatus === status)?.count || 0;
        const isSelected = myEmotion && myEmotion.emotionStatus === status;

        // Lấy danh sách tên user của cảm xúc này
        const userNames = filterList
            .filter(e => e.emotionStatus === status)
            .map(e => e.username)
            .join(", ");

        const buttonStyle = isSelected
            ? "margin-right:5px; background-color: gold; color: black; border: none; border-radius: 4px;"
            : "margin-right:5px; border-radius: 4px;";

        // Thêm tooltip hover để xem ai đã bấm
        return `<button onclick="toggleEmotion(${postId}, '${status}')" style="${buttonStyle}" title="${userNames || 'Chưa ai'}">
                    ${emotionIcons[status]} ${count}
                </button>`;
    }).join("");
}

window.renderPostEmotions = renderPostEmotions

async function toggleEmotion(postId, status) {
    try {
        // Lấy cảm xúc hiện tại sau mỗi thao tác thành công
        async function getCurrentUserEmotion() {
            const filterRes = await fetch(`${API_BASE}/posts/${postId}/emotions/filter`, {
                headers: {"Authorization": `Bearer ${accessToken}`}
            });
            if (!filterRes.ok) throw new Error("Không thể tải cảm xúc hiện tại");
            const filterData = await filterRes.json();
            console.log("Filter API data:", filterData.data);

            // Sửa lấy đúng key 'id' trong payload token
            const currentUserId = parseJwt(accessToken)?.id;
            console.log("Current user id from token:", currentUserId);

            const myEmotion = filterData.data.find(e => {
                console.log("Checking emotion entry:", e);
                return e.userId === currentUserId;
            });

            console.log("Current user emotion found:", myEmotion);
            return myEmotion;
        }

        let myEmotion = await getCurrentUserEmotion();

        console.log("Current user emotion:", myEmotion);
        console.log("Clicked status:", status);

        let method, url, body;

        if (!accessToken) {
            alert("Vui lòng đăng nhập để sử dụng tính năng này");
            window.location.href = "/auth.html";
            return;
        }

        if (!myEmotion) {
            method = "POST";
            url = `${API_BASE}/posts/${postId}/emotions`;
            body = {emotionStatus: status};
        } else if (myEmotion.emotionStatus === status) {
            method = "DELETE";
            url = `${API_BASE}/posts/${postId}/emotions`;
            body = null;
        } else {
            method = "PUT";
            url = `${API_BASE}/posts/${postId}/emotions`;
            body = {emotionStatus: status};
        }

        console.log("Sending request", method, url, body);

        const fetchOptions = {
            method,
            headers: {
                "Authorization": `Bearer ${accessToken}`
            }
        };

        if (method !== "DELETE") {
            fetchOptions.headers["Content-Type"] = "application/json";
            fetchOptions.body = JSON.stringify(body);
        }

        const res = await fetch(url, fetchOptions);

        const result = await res.json();

        if (!result.success) {
            if (result.message === "Bạn đã thể hiện cảm xúc bài viết rồi" && method === "POST") {
                console.log("Retrying update instead of create");
                const retryRes = await fetch(url, {
                    method: "PUT",
                    headers: {
                        "Authorization": `Bearer ${accessToken}`,
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({emotionStatus: status})
                });
                const retryResult = await retryRes.json();
                if (!retryResult.success) {
                    alert(retryResult.message || "Lỗi khi cập nhật cảm xúc");
                    return;
                }
                await fetchPostEmotions(postId);
                return;
            }
            alert(result.message || "Lỗi khi xử lý cảm xúc");
            return;
        }

        // Cập nhật lại cảm xúc sau khi thành công
        await fetchPostEmotions(postId);

    } catch (err) {
        console.error(err);
        alert("Lỗi xử lý cảm xúc");
    }
}

window.toggleEmotion = toggleEmotion

//khi app khoi dong
window.onload = async () => {
    await loadSearchHistory()
    await fetchBookMarks();
    await fetchRandomBooks();
    await loadFriendSuggestBooks(0)
    await fetchTopBooks()
    await fetchTopNewBooks()
    await fetchUserPosts(0)

    const token = localStorage.getItem("accessToken");
    const payload = parseJwt(token);
    const roles = payload?.roles || [];

    // if (!roles.includes("ROLE_ADMIN")) {
    //     // Ẩn chức năng quản lý
    //     document.querySelector('a[href="book.html"]').style.display = "none";
    //     document.querySelector('a[href="user.html"]').style.display = "none";
    //     document.querySelector('a[href="order-manager.html"]').style.display = "none";
    //     document.querySelector('a[href="coupon-admin.html"]').style.display = "none";
    //     document.querySelector('a[href="system-notification.html"]').style.display = "none";
    // }
};
