// HTTP 请求封装 — 自动 token 注入、401 处理
const BASE_URL = 'http://localhost:8080';

function getToken() {
    return localStorage.getItem('counselor_token');
}

async function request(method, path, body) {
    const headers = { 'Content-Type': 'application/json' };
    const token = getToken();
    if (token) headers['Authorization'] = 'Bearer ' + token;

    const config = { method, headers };
    if (body && method !== 'GET') {
        config.body = JSON.stringify(body);
    }

    const res = await fetch(BASE_URL + path, config);
    if (res.status === 401) {
        localStorage.removeItem('counselor_token');
        localStorage.removeItem('user_info');
        window.location.href = 'index.html';
        throw new Error('未登录');
    }
    return res.json();
}

const api = {
    get: (path) => request('GET', path),
    post: (path, body) => request('POST', path, body),
    put: (path, body) => request('PUT', path, body),
    del: (path) => request('DELETE', path)
};
