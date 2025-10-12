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

            // Hiển thị avatar
            const avatarImg = document.getElementById("avatarImg");
            if (user.avatarUrl) {
                avatarImg.src = user.avatarUrl;
            } else {
                avatarImg.src = "/icon/default-avatar.png"; // ảnh mặc định nếu chưa có
            }

            document.getElementById("username").value = user.username;
            document.getElementById("fullName").value = user.fullName || "";
            document.getElementById("address").value = user.address || "";
            document.getElementById("liking").value = user.liking || "";
            document.getElementById("phone").value = user.phone || "";
            document.getElementById("sex").value = user.sex || "UNKNOWN";

            document.getElementById("yob").value = user.yearOfBirth || "";
            document.getElementById("mob").value = user.monthOfBirth || "";
            document.getElementById("dob").value = user.dayOfBirth || "";
        }
    } catch (err) {
        showMessage("Không thể tải thông tin người dùng", true);
    }
};

async function uploadAvatar() {
    const fileInput = document.getElementById("avatarFile");
    if (!fileInput.files.length) {
        showMessage("Chọn file trước khi upload!", true);
        return;
    }

    const formData = new FormData();
    formData.append("file", fileInput.files[0]);

    try {
        const res = await fetch(`${API_BASE}/upload/avatar`, {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${accessToken}`
            },
            body: formData
        });

        const data = await res.json();
        if (data.success) {
            // Cập nhật avatar trên FE ngay
            document.getElementById("avatarImg").src = data.data;
            showMessage("Upload avatar thành công!");
        } else {
            showMessage(data.message || "Upload thất bại", true);
        }
    } catch (err) {
        showMessage("Lỗi khi upload avatar", true);
    }
}
window.uploadAvatar = uploadAvatar;

let avatarPage = 0;
const avatarSize = 5;
let totalAvatarPages = 1;

async function loadAvatarList() {
    avatarPage = 0; // reset về trang đầu
    document.getElementById("avatarList").innerHTML = ""; // clear danh sách cũ
    await fetchAvatarPage();
}
window.loadAvatarList=loadAvatarList

async function loadMoreAvatars() {
    await fetchAvatarPage();
}
window.loadMoreAvatars=loadMoreAvatars

async function fetchAvatarPage() {
    if (avatarPage >= totalAvatarPages) {
        showMessage("Không còn ảnh nào nữa");
        document.getElementById("loadMoreAvatarBtn").style.display = "none";
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/upload?page=${avatarPage}&size=${avatarSize}`, {
            headers: { "Authorization": `Bearer ${accessToken}` }
        });

        const data = await res.json();
        if (data.success) {
            const pageData = data.data;
            totalAvatarPages = pageData.totalPages;

            const listContainer = document.getElementById("avatarList");
            pageData.content.forEach(img => {
                const imgEl = document.createElement("img");
                imgEl.src = img.imageUrl;
                imgEl.width = 100;
                imgEl.height = 100;
                imgEl.style.cursor = "pointer";
                imgEl.title = `Upload: ${new Date(img.uploadAt).toLocaleString()}`;

                imgEl.onclick = async () => {
                    const confirmSet = confirm("Đặt ảnh này làm avatar?");
                    if (confirmSet) {
                        await setAsCurrentAvatar(img.id);
                    }
                };

                listContainer.appendChild(imgEl);
            });

            avatarPage++;

            // Hiện hoặc ẩn nút "Xem thêm"
            document.getElementById("loadMoreAvatarBtn").style.display =
                avatarPage < totalAvatarPages ? "inline-block" : "none";
        } else {
            showMessage(data.message || "Không tải được avatar", true);
        }
    } catch (err) {
        showMessage("Lỗi khi tải danh sách avatar", true);
    }
}

async function setAsCurrentAvatar(imageId) {
    try {
        const res = await fetch(`${API_BASE}/upload/avatar/change?imageId=${imageId}`, {
            method: "PUT",
            headers: { "Authorization": `Bearer ${accessToken}` }
        });

        const data = await res.json();
        if (data.success) {
            // Cập nhật ảnh đại diện hiện tại
            document.getElementById("avatarImg").src = data.data;
            showMessage("Đặt lại avatar thành công!");
        } else {
            showMessage(data.message || "Không thể đổi avatar", true);
        }
    } catch (err) {
        showMessage("Lỗi khi đặt lại avatar", true);
    }
}

async function updateProfile() {
    const request = {
        fullName: document.getElementById("fullName").value,
        address: document.getElementById("address").value,
        liking: document.getElementById("liking").value,
        phone: document.getElementById("phone").value,
        sex: document.getElementById("sex").value
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
        if (res.ok && data.success) {
            showMessage("Cập nhật thông tin thành công!");
        } else if (data.errors) {
            const errorMessages = Object.values(data.errors).join("\n");
            showMessage(errorMessages, true);
        } else {
            showMessage(data.message || "Cập nhật thất bại", true);
        }
    } catch (err) {
        showMessage("Lỗi khi cập nhật thông tin", true);
    }
}
window.updateProfile=updateProfile

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
window.changePassword=changePassword

function showMessage(msg, isError = false) {
    const messageEl = document.getElementById("message");
    messageEl.innerText = msg;
    messageEl.className = isError ? "error" : "success";
}