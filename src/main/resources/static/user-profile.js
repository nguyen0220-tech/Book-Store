const API_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");

window.onload = async () => {
    try {
        const res = await fetch(`${API_BASE}/user/my-profile`, {
            headers: { "Authorization": `Bearer ${accessToken}` }
        });
        const data = await res.json();
        if (data.success) {
            const user = data.data;
            document.getElementById("username").value = user.username;
            document.getElementById("fullName").value = user.fullName || "";
            document.getElementById("address").value = user.address || "";
            document.getElementById("liking").value = user.liking || "";
            document.getElementById("phone").value = user.phone || "";
        }
    } catch (err) {
        showMessage("Không thể tải thông tin người dùng", true);
    }
};

async function updateProfile() {
    const request = {
        fullName: document.getElementById("fullName").value,
        address: document.getElementById("address").value,
        liking: document.getElementById("liking").value,
        phone: document.getElementById("phone").value
    };

    try {
        const res = await fetch(`${API_BASE}/user/my-profile/update`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${accessToken}`
            },
            body: JSON.stringify(request)
        });


        const data = await res.json();
        if (data.success) {
            showMessage("Cập nhật thông tin thành công!");
        } else {
            showMessage(data.message, true);
        }
    } catch (err) {
        showMessage("Lỗi khi cập nhật thông tin", true);
    }
}

async function changePassword() {
    const request = {
        oldPassword: document.getElementById("oldPassword").value,
        newPassword: document.getElementById("newPassword").value,
        confirmPassword: document.getElementById("confirmPassword").value
    };

    try {
        const res = await fetch(`${API_BASE}/user/my-profile/setup-pass`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${accessToken}`
            },
            body: JSON.stringify(request)
        });


        const data = await res.json();
        if (data.success) {
            showMessage("Đổi mật khẩu thành công!");
        } else {
            showMessage(data.message, true);
        }
    } catch (err) {
        showMessage("Lỗi khi đổi mật khẩu", true);
    }
}

function showMessage(msg, isError = false) {
    const messageEl = document.getElementById("message");
    messageEl.innerText = msg;
    messageEl.className = isError ? "error" : "success";
}