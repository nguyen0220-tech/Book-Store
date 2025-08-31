const API_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");
let currentPage = 0;
const pageSize = 5;
let totalPages = 0;
let lowStockCurrentPage = 0;
let lowStockTotalPages = 0;


document.getElementById("logoutBtn").addEventListener("click", async () => {
    const refreshToken = localStorage.getItem("refreshToken");
    if (!refreshToken) {
        document.getElementById("logoutMessage").textContent = "Không tìm thấy refresh token!";
        return;
    }
    try {
        const res = await fetch(`${API_BASE}/auth/logout`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ refreshToken })
        });
        const result = await res.json();
        if (res.ok && result.success) {
            localStorage.removeItem("accessToken");
            localStorage.removeItem("refreshToken");
            window.location.href = "/auth.html";
        } else {
            document.getElementById("logoutMessage").textContent = result.message || "Logout thất bại!";
        }
    } catch (err) {
        document.getElementById("logoutMessage").textContent = "Lỗi server: " + err.message;
    }
});

async function loadBookCount() {
    try {
        const res = await fetch(`${API_BASE}/book/count`, {
            headers: { "Authorization": `Bearer ${accessToken}` }
        });
        const result = await res.json();
        if (res.ok && result.success) {
            document.getElementById("bookCount").textContent =
                `Tổng số sách: ${result.data}`;
        } else {
            document.getElementById("bookCount").textContent =
                result.message || "Không thể lấy số lượng sách";
        }
    } catch (err) {
        document.getElementById("bookCount").textContent =
            "Lỗi server: " + err.message;
    }
}


async function loadAllBooks(page = 0) {
    try {
        const url = new URL(`${API_BASE}/book`);
        url.searchParams.append("page", page);
        url.searchParams.append("size", pageSize);

        const res = await fetch(url, {
            headers: { "Authorization": `Bearer ${accessToken}` }
        });
        const result = await res.json();
        if (res.ok && result.success) {
            showBooks(result.data.content || []);
            totalPages = result.data.totalPages;
            currentPage = result.data.number;
            renderPagination();
        } else {
            alert(result.message || "Lỗi khi tải sách");
        }
    } catch (err) {
        alert("Lỗi server: " + err.message);
    }
}
window.loadAllBooks=loadAllBooks
window.currentPage=currentPage

function renderPagination(loadFn = loadAllBooks) {
    const container = document.getElementById("paginationControls");
    container.innerHTML = "";

    const prev = document.createElement("button");
    prev.textContent = "« Prev";
    prev.disabled = currentPage === 0;
    prev.onclick = () => loadFn(currentPage - 1);
    container.appendChild(prev);

    for (let i = 0; i < totalPages; i++) {
        const btn = document.createElement("button");
        btn.textContent = i + 1;
        if (i === currentPage) btn.classList.add("active");
        btn.onclick = () => loadFn(i);
        container.appendChild(btn);
    }

    const next = document.createElement("button");
    next.textContent = "Next »";
    next.disabled = currentPage === totalPages - 1;
    next.onclick = () => loadFn(currentPage + 1);
    container.appendChild(next);
}

function showBooks(books) {
    const tbody = document.querySelector("#bookTable tbody");
    tbody.innerHTML = "";
    if (books.length === 0) {
        tbody.innerHTML = "<tr><td colspan='11'>Không có sách nào</td></tr>";
        return;
    }
    books.forEach(b => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td>${b.id}</td>
            <td>${b.title}</td>
            <td>${b.author}</td>
            <td>${b.price}</td>
            <td>${b.salePrice !== null ? b.salePrice : "-"}</td>
            <td>${b.stock}</td>
            <td>${b.description}</td>
            <td><img src="${b.imgUrl}" alt="${b.title}"/></td>
            <td>${b.categoryId || ""}</td>
            <td>${b.categoryName || ""}</td>
            <td>
                <button onclick="deleteBook(${b.id})">Xóa</button>
                <button onclick="editBook(${b.id})">Sửa</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

async function addBook() {
    const title = document.getElementById("addTitle").value.trim();
    const author = document.getElementById("addAuthor").value.trim();
    const price = parseFloat(document.getElementById("addPrice").value);
    const salePriceInput = document.getElementById("addSalePrice").value;
    const salePrice = salePriceInput ? parseFloat(salePriceInput) : null;
    const stock = parseInt(document.getElementById("addStock").value);
    const description = document.getElementById("addDescription").value.trim();
    const imgUrl = document.getElementById("addImgUrl").value.trim();
    const categoryId = parseInt(document.getElementById("addCategoryId").value);

    if (!title || !author || isNaN(price) || isNaN(stock) || !description || !imgUrl || isNaN(categoryId)) {
        document.getElementById("addBookMessage").textContent = "Vui lòng nhập đầy đủ thông tin hợp lệ";
        return;
    }
    document.getElementById("addBookMessage").textContent = "";

    const body = { title, author, price, stock, description, imgUrl, categoryId };
    if (salePrice !== null && !isNaN(salePrice)) {
        body.salePrice = salePrice;
    }

    try {
        const res = await fetch(`${API_BASE}/book/add`, {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${accessToken}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify(body)
        });
        const result = await res.json();
        if (res.ok && result.success) {
            alert("Thêm sách thành công");
            loadAllBooks(currentPage);
            clearAddForm();
        } else {
            document.getElementById("addBookMessage").textContent = result.message || "Lỗi khi thêm sách";
        }
    } catch (err) {
        document.getElementById("addBookMessage").textContent = "Lỗi server: " + err.message;
    }
}
window.addBook=addBook

function clearAddForm() {
    document.getElementById("addTitle").value = "";
    document.getElementById("addAuthor").value = "";
    document.getElementById("addPrice").value = "";
    document.getElementById("addSalePrice").value = "";
    document.getElementById("addStock").value = "";
    document.getElementById("addDescription").value = "";
    document.getElementById("addImgUrl").value = "";
    document.getElementById("addCategoryId").value = "";
}

async function deleteBook(id) {
    if (!confirm(`Bạn có chắc muốn xóa sách ID=${id}?`)) return;
    try {
        const res = await fetch(`${API_BASE}/book/${id}`, {
            method: "DELETE",
            headers: { "Authorization": `Bearer ${accessToken}` }
        });
        const result = await res.json();
        if (res.ok && result.success) {
            alert("Xóa sách thành công");
            loadAllBooks(currentPage);
        } else {
            alert(result.message || "Lỗi khi xóa sách");
        }
    } catch (err) {
        alert("Lỗi server: " + err.message);
    }
}
window.deleteBook=deleteBook

function editBook(id) {
    fetch(`${API_BASE}/book?page=0&size=100`, {
        headers: { "Authorization": `Bearer ${accessToken}` }
    })
        .then(res => res.json())
        .then(result => {
            if (result.success) {
                const book = result.data.content.find(b => b.id === id);
                if (!book) return alert("Không tìm thấy sách");

                const title = prompt("Title:", book.title);
                if (!title) return;
                const author = prompt("Author:", book.author);
                if (!author) return;
                const price = prompt("Price:", book.price);
                if (isNaN(price)) return alert("Giá không hợp lệ");
                const salePrice = prompt("Sale Price:", book.salePrice ?? "");
                if (salePrice && isNaN(salePrice)) return alert("Sale Price không hợp lệ");
                const stock = prompt("Stock:", book.stock);
                if (isNaN(stock)) return alert("Stock không hợp lệ");
                const description = prompt("Description:", book.description);
                const imgUrl = prompt("Image URL:", book.imgUrl);
                const categoryId = parseInt(prompt("Category ID:", book.categoryId));
                if (isNaN(categoryId)) return alert("Category ID không hợp lệ");

                const updatedBook = {
                    id,
                    title,
                    author,
                    price: parseFloat(price),
                    salePrice: salePrice ? parseFloat(salePrice) : null,
                    stock: parseInt(stock),
                    description,
                    imgUrl,
                    categoryId
                };

                fetch(`${API_BASE}/book/${id}`, {
                    method: "POST",
                    headers: {
                        "Authorization": `Bearer ${accessToken}`,
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify(updatedBook)
                })
                    .then(res => res.json())
                    .then(res => {
                        if (res.success) {
                            alert("Cập nhật sách thành công");
                            loadAllBooks(currentPage);
                            loadLowStockBooks(lowStockCurrentPage);
                        } else {
                            alert(res.message || "Lỗi khi cập nhật sách");
                        }
                    })
                    .catch(err => alert("Lỗi server: " + err.message));
            } else {
                alert(result.message || "Lỗi khi lấy danh sách sách");
            }
        })
        .catch(err => alert("Lỗi server: " + err.message));
}
window.editBook=editBook

async function searchBooksByTitle(page = 0) {
    const title = document.getElementById("searchTitleInput").value.trim();
    if (!title) {
        loadAllBooks(); // fallback nếu rỗng
        return;
    }

    try {
        const url = new URL(`${API_BASE}/book/by-title`);
        url.searchParams.append("page", page);
        url.searchParams.append("size", pageSize);
        url.searchParams.append("title", title);

        const res = await fetch(url, {
            headers: { "Authorization": `Bearer ${accessToken}` }
        });

        const result = await res.json();
        if (res.ok && result.success) {
            showBooks(result.data.content || []);
            totalPages = result.data.totalPages;
            currentPage = result.data.number;
            renderPagination(() => searchBooksByTitle); // truyền callback tìm kiếm lại
        } else {
            alert(result.message || "Không tìm thấy sách phù hợp");
        }
    } catch (err) {
        alert("Lỗi server: " + err.message);
    }
}
window.searchBooksByTitle=searchBooksByTitle

async function loadLowStockBooks(page = 0) {
    try {
        const url = new URL(`${API_BASE}/book/stock-max50`);
        url.searchParams.append("page", page);
        url.searchParams.append("size", pageSize);

        const res = await fetch(url, {
            headers: { "Authorization": `Bearer ${accessToken}` }
        });

        const result = await res.json();
        if (res.ok && result.success) {
            const data = result.data;
            showLowStockBooks(data.content || []);
            lowStockTotalPages = data.totalPages;
            lowStockCurrentPage = data.number;
            renderLowStockPagination();
        } else {
            alert(result.message || "Lỗi khi tải kho sách tồn thấp");
        }
    } catch (err) {
        alert("Lỗi server: " + err.message);
    }
}
window.loadLowStockBooks = loadLowStockBooks;

function showLowStockBooks(books) {
    const tbody = document.querySelector("#lowStockTable tbody");
    tbody.innerHTML = "";

    if (books.length === 0) {
        tbody.innerHTML = "<tr><td colspan='5'>Không có sách nào</td></tr>";
        return;
    }

    books.forEach(b => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td>${b.bookId}</td>
            <td>${b.title}</td>
            <td>${b.author}</td>
            <td style="color:${b.stock < 20 ? 'red' : 'orange'}; font-weight:bold;">${b.stock}</td>
            <td><img src="${b.imgUrl}" alt="${b.title}" /></td>
            <td>
                <button onclick="editBook(${b.bookId})">Sửa</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}


function renderLowStockPagination() {
    const container = document.getElementById("lowStockPagination");
    container.innerHTML = "";

    const prev = document.createElement("button");
    prev.textContent = "« Prev";
    prev.disabled = lowStockCurrentPage === 0;
    prev.onclick = () => loadLowStockBooks(lowStockCurrentPage - 1);
    container.appendChild(prev);

    for (let i = 0; i < lowStockTotalPages; i++) {
        const btn = document.createElement("button");
        btn.textContent = i + 1;
        if (i === lowStockCurrentPage) btn.classList.add("active");
        btn.onclick = () => loadLowStockBooks(i);
        container.appendChild(btn);
    }

    const next = document.createElement("button");
    next.textContent = "Next »";
    next.disabled = lowStockCurrentPage === lowStockTotalPages - 1;
    next.onclick = () => loadLowStockBooks(lowStockCurrentPage + 1);
    container.appendChild(next);
}

loadBookCount()
loadAllBooks();
loadLowStockBooks(0)
