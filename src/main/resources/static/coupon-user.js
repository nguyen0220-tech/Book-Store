const API_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");

async function loadMyCoupons() {
    const res = await fetch(`${API_BASE}/coupon/user`, {
        headers: { Authorization: `Bearer ${accessToken}` }
    });
    const data = await res.json();

    if (data.success) {
        document.getElementById("myCoupons").innerHTML = data.data.map(c => {
            const discountText = c.percentDiscount
                ? `🔻 ${c.discountPercent}%`
                : `💸 ${c.discountAmount}₩`;
;

            const expiredDate = c.expired?.split("T")[0] || "Không rõ";

            return `
                <div class="coupon-box">
                    <b>🎫 ${c.couponCode}</b> - Giảm: <strong>${discountText}</strong><br/>
                    💬 Mô tả: ${c.description}<br/>
                    💵 Áp dụng cho đơn từ: ${c.minimumAmount}₩<br/>
                    ⏰ Hạn sử dụng: ${expiredDate}
                </div>
            `;
        }).join("");
    } else {
        document.getElementById("myCoupons").innerHTML = "<p>Không thể tải coupon</p>";
    }
}

async function claimCoupon() {
    const request = { couponCode: document.getElementById("claimCode").value };
    const res = await fetch(`${API_BASE}/coupon/claim`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${accessToken}`
        },
        body: JSON.stringify(request)
    });
    const data = await res.json();
    if (data.success) {
        alert("🎉 Nhận thành công!");
        loadMyCoupons();
    } else alert(data.message || "Nhận thất bại");
}
window.claimCoupon=claimCoupon

window.onload = loadMyCoupons;