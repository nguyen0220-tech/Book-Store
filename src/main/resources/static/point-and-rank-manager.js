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

        renderPointTable([point]);
        document.getElementById("pagination-points").innerHTML = ""; // reset phân trang
    } catch (err) {
        console.error("Lỗi khi tìm point:", err);
        document.getElementById("point-table").innerHTML =
            `<tr><td colspan="4" style="color:red">${err.message}</td></tr>`;
    }
}
window.searchPoint = searchPoint;

// ----------------- PHÂN TRANG DÙNG CHUNG -----------------
function renderPagination(containerId, totalPages, current, onPageClick) {
    const pagination = document.getElementById(containerId);
    pagination.innerHTML = "";

    for (let i = 0; i < totalPages; i++) {
        const btn = document.createElement("button");
        btn.textContent = i + 1;
        if (i === current) btn.disabled = true;
        btn.addEventListener("click", () => onPageClick(i));
        pagination.appendChild(btn);
    }
}

// ----------------- POINT -----------------
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

        if (!res.ok) throw new Error("Không thể tải dữ liệu points");

        const result = await res.json();
        const data = result.data;

        renderPointTable(data.content);
        renderPagination("pagination-points", data.totalPages, page, (i) => {
            currentPage = i;
            fetchPoints(currentPage);
        });
    } catch (err) {
        console.error("Lỗi khi load points:", err);
        document.getElementById("point-table").innerHTML =
            `<tr><td colspan="5" style="color:red">${err.message}</td></tr>`;
    }
}

function renderPointTable(points) {
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

// ----------------- RANK -----------------
let currentRankPage = 0;
const rankPageSize = 5;

async function fetchRanks(page = 0) {
    try {
        const res = await fetch(`${API_BASE}/rank?page=${page}&size=${rankPageSize}`, {
            headers: { "Authorization": `Bearer ${accessToken}` }
        });

        if (!res.ok) throw new Error("Không thể tải dữ liệu rank");

        const result = await res.json();
        const data = result.data;

        renderRankTable(data.content);
        renderPagination("pagination-rank", data.totalPages, page, (i) => {
            currentRankPage = i;
            fetchRanks(currentRankPage);
        });
    } catch (err) {
        console.error("Lỗi khi load rank:", err);
        document.getElementById("rank-table").innerHTML =
            `<tr><td colspan="4" style="color:red">${err.message}</td></tr>`;
    }
}

function renderRankTable(ranks) {
    const tbody = document.getElementById("rank-table");
    tbody.innerHTML = "";

    if (!ranks || ranks.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4">Không có dữ liệu</td></tr>`;
        return;
    }

    ranks.forEach(r => {
        const tr = `
            <tr>
                <td>${r.username}</td>
                <td>${r.fullUsername}</td>
                <td>${r.rank}</td>
                <td>${new Date(r.updatedAt).toLocaleString()}</td>
            </tr>
        `;
        tbody.innerHTML += tr;
    });
}

// ----------------- LOAD LẦN ĐẦU -----------------
window.onload = () => {
    fetchPoints(currentPage);
    fetchRanks(currentRankPage);
};
