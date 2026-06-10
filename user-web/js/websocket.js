// WebSocket 客户端 — 接收实时消息推送
const WS_BASE = 'ws://localhost:8080';

class WebSocketClient {
    constructor() {
        this.socket = null;
        this.isConnected = false;
        this.listeners = [];
        this.reconnectAttempts = 0;
        this.maxReconnect = 5;
        this.userId = null;
    }

    connect(userId) {
        if (this.socket && this.isConnected) return;
        this.userId = userId;

        const token = localStorage.getItem('user_token');
        // 通过 URL param 传 userId，token 通过子协议或 query 传（兼容）
        const url = WS_BASE + '/api/chat/ws?userId=' + userId + '&token=' + encodeURIComponent(token || '');
        this.socket = new WebSocket(url);

        this.socket.onopen = () => {
            this.isConnected = true;
            this.reconnectAttempts = 0;
            console.log('WebSocket connected');
        };

        this.socket.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                this.listeners.forEach(fn => fn(data));
            } catch (e) {
                console.error('WS parse error:', e);
            }
        };

        this.socket.onclose = () => {
            this.isConnected = false;
            if (this.reconnectAttempts < this.maxReconnect) {
                const delay = (this.reconnectAttempts + 1) * 3000;
                this.reconnectAttempts++;
                setTimeout(() => this.connect(this.userId), delay);
            }
        };

        this.socket.onerror = () => {
            this.isConnected = false;
        };
    }

    disconnect() {
        this.maxReconnect = 0; // 阻止自动重连
        if (this.socket) {
            this.socket.close();
            this.socket = null;
        }
        this.isConnected = false;
    }

    onMessage(fn) {
        this.listeners.push(fn);
    }

    offMessage(fn) {
        this.listeners = this.listeners.filter(f => f !== fn);
    }

    send(data) {
        if (this.socket && this.isConnected) {
            this.socket.send(JSON.stringify(data));
        }
    }
}

const wsClient = new WebSocketClient();
