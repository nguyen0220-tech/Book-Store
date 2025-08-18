//User Manager js
const API_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");

// =================== LOGOUT ===================
document.getElementById("logoutBtn").addEventListener("click", async () => {
    const refreshToken = localStorage.getItem("refreshToken");

    if (!refreshToken) {
        document.getElementById("logoutMessage").textContent = "Không tìm thấy refresh token!";
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/auth/logout`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
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

async function fetchAllUsers() {
    const res = await fetch(`${API_BASE}/user/users`, {
        headers: { "Authorization": `Bearer ${accessToken}` }
    });
    const result = await res.json();
    showUsers(result.data || []);
}
window.fetchAllUsers=fetchAllUsers

async function searchByName() {
    const keyword = document.getElementById("nameKeyword").value;
    const res = await fetch(`${API_BASE}/user/search-name?keyword=${keyword}`, {
        headers: { "Authorization": `Bearer ${accessToken}` }
    });
    const result = await res.json();
    showUsers(result.data.content || []);
}
window.searchByName=searchByName

async function filterByRole() {
    const role = document.getElementById("roleKeyword").value;
    const res = await fetch(`${API_BASE}/user/filter-role?role=${role}`, {
        headers: { "Authorization": `Bearer ${accessToken}` }
    });
    const result = await res.json();
    showUsers(result.data.content || []);
}
window.filterByRole=filterByRole

async function getById() {
    const id = document.getElementById("userId").value;
    const res = await fetch(`${API_BASE}/user/userid?id=${id}`, {
        headers: { "Authorization": `Bearer ${accessToken}` }
    });
    const result = await res.json();
    if (result.data) showUsers([result.data]);
    else document.getElementById("userResult").innerHTML = "<p>Không tìm thấy người dùng</p>";
}
window.getById=getById

async function deleteUser(id) {
    if (!confirm(`Xoá user ID: ${id}?`)) return;

    const res = await fetch(`${API_BASE}/user/${id}`, {
        method: "DELETE",
        headers: {
            "Authorization": `Bearer ${accessToken}`
        }
    });
    const result = await res.json();
    alert(result.message || "Đã xoá");
    fetchAllUsers(); // refresh list
}
window.deleteUser=deleteUser

async function addUser() {
    const username = document.getElementById("addUsername").value;
    const password = document.getElementById("addPassword").value;

    if (!username || !password) {
        document.getElementById("addUserMessage").textContent = "Vui lòng nhập username và password";
        return;
    }

    const res = await fetch(`${API_BASE}/user/add`, {
        method: "POST",
        headers: {
            "Authorization": `Bearer ${accessToken}`,
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ username, password })
    });

    const result = await res.json();

    if (res.ok && result.success) {
        document.getElementById("addUserMessage").textContent = "Thêm thành công!";
        fetchAllUsers();
    } else {
        document.getElementById("addUserMessage").textContent = result.message || "Lỗi khi thêm người dùng";
    }
}
window.addUser=addUser

async function editUser(id, oldUsername, oldRoles) {
    const newUsername = prompt("Nhập username mới:", oldUsername);
    if (!newUsername) return;

    const roleInput = prompt("Nhập các role (phân cách bằng dấu phẩy):", oldRoles);
    if (!roleInput) return;

    const roles = roleInput.split(",").map(r => ({ name: r.trim() }));

    try {
        const res = await fetch(`${API_BASE}/user/${id}`, {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${accessToken}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ id, username: newUsername, roles })
        });

        const result = await res.json();

        if (res.ok && result.success) {
            alert("Cập nhật thành công!");
            fetchAllUsers();
        } else {
            alert(result.message || "Lỗi khi cập nhật!");
        }
    } catch (err) {
        alert("Lỗi server: " + err.message);
    }
}
window.editUser=editUser

// =================== Display ===================
function showUsers(users) {
    const html = users.map((u, index) => {
        const roles = u.roles.map(r => r.name).join(", ");
        const lockBtnLabel = u.enabled ? "🔒 Khoá" : "🔓 Mở khoá";

        return `
        <div style="border:1px solid #ccc; padding:10px; margin:10px;">
            <b>STT:</b> ${index + 1}<br/>
            <b>Username:</b> ${u.username}<br/>
            <b>Roles:</b> ${roles}<br/>
            <b>Id:</b> ${u.id}<br/>
            <b>Enabled:</b> ${u.enabled}<br/>
            <button onclick="deleteUser(${u.id})">❌ Xoá</button>
            <button onclick="editUser(${u.id}, '${u.username}', '${roles}')">✏️ Cập nhật</button>
            <button onclick="toggleUserLock(${u.id}, ${u.enabled})">${lockBtnLabel}</button>
        </div>
        `;
    }).join("");

    document.getElementById("userResult").innerHTML = html || "<p>Không có kết quả</p>";
}

async function toggleUserLock(userId, currentEnabled) {
    let url;
    if (currentEnabled) {
        url = `${API_BASE}/user/lock/${userId}`;
    } else {
        url = `${API_BASE}/user/un-lock/${userId}`; // lưu ý backend là "un-lock"
    }

    try {
        const res = await fetch(url, {
            method: "PUT",
            headers: { "Authorization": `Bearer ${accessToken}` }
        });

        const data = await res.json();

        alert(data.message || "Đã xảy ra lỗi");

        if (data.success) {
            fetchAllUsers();
        }

    } catch (err) {
        alert("Lỗi server: " + err.message);
    }
}
window.toggleUserLock = toggleUserLock;
