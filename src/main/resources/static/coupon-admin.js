const API_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");

let editingCode = null; // nếu khác null -> đang sửa

async function loadCoupons() {
    const res = await fetch(`${API_BASE}/coupon`, {
        headers: { Authorization: `Bearer ${accessToken}` }
    });
    const data = await res.json();

    if (data.success) {
        const list = data.data.map(c => {
            const discountText = c.percentDiscount
                ? `${c.discountPercent}%`
                : `${c.discountAmount.toLocaleString()}₩`;

            const expiredDate = c.expired?.split("T")[0] || "Không rõ";

            return `
                <div style="border-bottom: 1px solid #ccc; padding: 0.5rem 0;">
                    <b>🎁 ${c.couponCode}</b> - <span>${discountText}</span><br/>
                    💰 Tối thiểu: ${c.minimumAmount.toLocaleString()}₩ |
                    🔁 Dùng nhiều lần: ${c.usage ? "✅" : "❌"} |
                    📊 Đã dùng: <b>${c.usageCount}/${c.maxUsage}</b> |
                    🕒 Hết hạn: ${expiredDate}<br/>
                    📝 <i>${c.description || "Không có mô tả"}</i>
                    <div class="actions" style="margin-top: 0.3rem;">
                        <button onclick="editCoupon('${c.couponCode}')">✏️ Sửa</button>
                        <button onclick="deleteCoupon('${c.couponCode}')">❌ Xoá</button>
                    </div>
                </div>
            `;
        }).join("");

        document.getElementById("couponList").innerHTML = list;
    } else {
        alert(data.message || "Không thể tải danh sách coupon");
    }
}

async function submitCoupon() {
    const request = {
        couponCode: document.getElementById("couponCode").value.trim(),
        discountAmount: parseFloat(document.getElementById("discountAmount").value),
        percentDiscount: document.getElementById("percentDiscount").checked,
        discountPercent: parseFloat(document.getElementById("discountPercent").value),
        minimumAmount: parseFloat(document.getElementById("minimumAmount").value),
        active: document.getElementById("active").checked,
        description: document.getElementById("description").value.trim(),
        expired: document.getElementById("expired").value + ":00",
        usage: document.getElementById("usage").checked,
        maxUsage: parseInt(document.getElementById("maxUsage").value),
        usageCount: parseInt(document.getElementById("usageCount").value)
    };


    const url = editingCode
        ? `${API_BASE}/coupon/${editingCode}` // update
        : `${API_BASE}/coupon/add`;           // create
    const method = editingCode ? "PUT" : "POST";

    const res = await fetch(url, {
        method,
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${accessToken}`
        },
        body: JSON.stringify(request)
    });

    const data = await res.json();
    if (data.success) {
        alert(editingCode ? "✅ Cập nhật thành công" : "✅ Tạo coupon thành công");
        editingCode = null;
        document.getElementById("couponForm").reset();
        loadCoupons();
    } else {
        alert(data.message || "Thao tác thất bại");
    }
}
window.submitCoupon=submitCoupon

async function deleteCoupon(code) {
    if (!confirm(`Xoá coupon "${code}"?`)) return;
    const res = await fetch(`${API_BASE}/coupon/${code}`, {
        method: "DELETE",
        headers: {Authorization: `Bearer ${accessToken}`}
    });
    const data = await res.json();
    if (data.success) {
        alert("🗑️ Đã xoá coupon");
        loadCoupons();
    } else {
        alert(data.message || "Không thể xoá");
    }
}
window.deleteCoupon=deleteCoupon

async function editCoupon(code) {
    const res = await fetch(`${API_BASE}/coupon/code/${code}`, {
        headers: {Authorization: `Bearer ${accessToken}`}
    });
    const data = await res.json();
    if (data.success) {
        const c = data.data;
        document.getElementById("couponCode").value = c.couponCode;
        document.getElementById("discountAmount").value = c.discountAmount || 0;
        document.getElementById("percentDiscount").checked = c.percentDiscount;
        toggleDiscountType();
        document.getElementById("discountPercent").value = c.discountPercent || 0;
        document.getElementById("minimumAmount").value = c.minimumAmount || 0;
        document.getElementById("active").checked = c.active;
        document.getElementById("description").value = c.description || "";
        document.getElementById("expired").value = c.expired ? c.expired.split(".")[0] : "";
        document.getElementById("usage").checked = c.usage;
        document.getElementById("maxUsage").value = c.maxUsage || 0;
        document.getElementById("usageCount").value = c.usageCount || 0;

        editingCode = c.couponCode;
        alert(`✏️ Đang chỉnh sửa coupon: ${editingCode}`);
    } else {
        alert(data.message || "Không thể tải dữ liệu coupon");
    }
}
window.editCoupon=editCoupon

function toggleDiscountType() {
    const isPercent = document.getElementById("percentDiscount").checked;
    document.getElementById("fixedDiscountGroup").style.display = isPercent ? "none" : "block";
    document.getElementById("percentDiscountGroup").style.display = isPercent ? "block" : "none";
}

window.onload = () => {
    loadCoupons();
    toggleDiscountType(); // đảm bảo hiển thị đúng khi reload
};


