// auth-utils.js
const API_BASE = window.location.origin;

// ----------------------------- AUTH + FETCH WRAPPER -----------------------------

function parseJwt(token) {
    try {
        const payload = token.split('.')[1];
        const decoded = atob(payload);
        return JSON.parse(decoded);
    } catch (e) {
        return null;
    }
}

function getAccessToken() {
    return localStorage.getItem("accessToken");
}

async function tryRefreshToken() {
    const refreshToken = localStorage.getItem("refreshToken");
    if (!refreshToken) throw new Error("Không có refresh token");

    const res = await fetch(`${API_BASE}/auth/refresh`, {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({ refreshToken })
    });

    if (!res.ok) throw new Error("Refresh thất bại");

    const result = await res.json();
    if (result.success && result.data?.accessToken) {
        localStorage.setItem("accessToken", result.data.accessToken);
    } else {
        throw new Error(result.message || "Lỗi khi refresh token");
    }
}

async function secureFetch(url, options = {}, retry = true) {
    const token = getAccessToken();
    const headers = options.headers || {};
    headers["Authorization"] = `Bearer ${token}`;
    options.headers = headers;

    const res = await fetch(url, options);

    if (res.status === 401 && retry) {
        await tryRefreshToken();
        return await secureFetch(url, options, false);
    }

    return res;
}
