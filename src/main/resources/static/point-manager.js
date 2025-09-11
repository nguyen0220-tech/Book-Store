const API_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");

async function searchPoint() {
    const username = document.getElementById("search-username").value.trim();
    if (!username) {
        alert("Vui lòng nhập username");
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/points/by-username`, {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${accessToken}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ username })
        });

        if (!res.ok) {
            throw new Error("Không tìm thấy point cho username: " + username);
        }

        const result = await res.json();
        const point = result.data;

        renderTable([point]); //
        document.getElementById("pagination").innerHTML = ""; //
    } catch (err) {
        console.error("Lỗi khi tìm point:", err);
        document.getElementById("point-table").innerHTML =
            `<tr><td colspan="4" style="color:red">${err.message}</td></tr>`;
    }
}
window.searchPoint=searchPoint

let currentPage = 0;
const pageSize = 5;

async function fetchPoints(page = 0) {
    try {
        const res = await fetch(`${API_BASE}/points/all?page=${page}&size=${pageSize}`, {
            headers: {
                "Authorization": `Bearer ${accessToken}`,
                "Content-Type": "application/json"
            }
        });

        if (!res.ok) {
            throw new Error("Không thể tải dữ liệu points");
        }

        const result = await res.json();
        const data = result.data;

        renderTable(data.content);
        renderPagination(data.totalPages, page);
    } catch (err) {
        console.error("Lỗi khi load points:", err);
        document.getElementById("point-table").innerHTML =
            `<tr><td colspan="5" style="color:red">${err.message}</td></tr>`;
    }
}

function renderTable(points) {
    const tbody = document.getElementById("point-table");
    tbody.innerHTML = "";

    if (!points || points.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4">Không có dữ liệu</td></tr>`;
        return;
    }

    points.forEach(p => {
        const row = `
            <tr>
                <td>${p.username}</td>
                <td>${p.userFullName}</td>
                <td>${p.point}</td>
                <td>${new Date(p.updatedAt).toLocaleString()}</td>
            </tr>
        `;
        tbody.innerHTML += row;
    });
}

function renderPagination(totalPages, current) {
    const pagination = document.getElementById("pagination");
    pagination.innerHTML = "";

    for (let i = 0; i < totalPages; i++) {
        const btn = document.createElement("button");
        btn.textContent = i + 1;
        if (i === current) {
            btn.disabled = true;
        }
        btn.addEventListener("click", () => {
            currentPage = i;
            fetchPoints(currentPage);
        });
        pagination.appendChild(btn);
    }
}

window.onload = () => {
    fetchPoints(currentPage);
};
