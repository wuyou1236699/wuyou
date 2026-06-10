// 工具函数
function formatDate(dateStr) {
    if (!dateStr) return '-';
    const d = new Date(dateStr);
    return d.getFullYear() + '-' + (d.getMonth()+1).toString().padStart(2,'0') + '-' + d.getDate().toString().padStart(2,'0');
}

function formatTime(dateStr) {
    if (!dateStr) return '-';
    const d = new Date(dateStr);
    return d.getHours().toString().padStart(2,'0') + ':' + d.getMinutes().toString().padStart(2,'0');
}

function formatDateTime(dateStr) {
    if (!dateStr) return '-';
    return formatDate(dateStr) + ' ' + formatTime(dateStr);
}

function showToast(msg, type) {
    // type: 'success' | 'error' | 'info'
    const bg = type === 'success' ? 'bg-green-500' : type === 'error' ? 'bg-red-500' : 'bg-blue-500';
    const toast = document.createElement('div');
    toast.className = `fixed top-5 left-1/2 -translate-x-1/2 ${bg} text-white px-6 py-3 rounded-lg shadow-lg z-50 transition-opacity text-sm`;
    toast.textContent = msg;
    document.body.appendChild(toast);
    setTimeout(() => { toast.style.opacity = '0'; setTimeout(() => toast.remove(), 300); }, 2000);
}

// 登录守卫
function checkLogin() {
    if (!localStorage.getItem('user_token')) {
        window.location.href = 'index.html';
        return false;
    }
    return true;
}

// 获取当前用户信息
function getUserInfo() {
    const info = localStorage.getItem('user_info');
    return info ? JSON.parse(info) : null;
}

// 从 URL 读取查询参数
function getQueryParam(name) {
    const params = new URLSearchParams(window.location.search);
    return params.get(name);
}

// 星级 HTML
function starRating(rating) {
    const r = Math.round(rating);
    let html = '';
    for (let i = 1; i <= 5; i++) {
        html += i <= r ? '<i class="fa fa-star text-yellow-500"></i>' : '<i class="fa fa-star text-gray-300"></i>';
    }
    html += ' <span class="text-sm text-gray-500">' + (typeof rating === 'number' ? rating.toFixed(1) : rating) + '</span>';
    return html;
}
