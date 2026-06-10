const app = getApp();

class WebSocketClient {
  constructor() {
    this.socket = null;
    this.url = '';
    this.messageListeners = [];
    this.connectionListeners = [];
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectDelay = 3000;
    this.isConnected = false;
  }

  connect(userId) {
    return new Promise((resolve, reject) => {
      if (this.socket && this.isConnected) {
        resolve(this.socket);
        return;
      }

      // 关闭旧 socket 避免重复连接
      if (this.socket) {
        try { this.socket.close(); } catch (e) {}
        this.socket = null;
        this.isConnected = false;
      }

      const protocol = app.globalData.BASE_URL.startsWith('https') ? 'wss' : 'ws';
      const baseUrl = app.globalData.BASE_URL.replace(/^https?\:\/\//, '');
      this.url = `${protocol}://${baseUrl}/api/chat/ws?userId=${userId}`;

      this.socket = wx.connectSocket({
        url: this.url,
        header: {
          'Authorization': `Bearer ${wx.getStorageSync('token') || ''}`
        },
        success: () => {
          console.log('WebSocket连接成功');
        },
        fail: (err) => {
          console.error('WebSocket连接失败:', err);
          reject(err);
        }
      });

      this.socket.onOpen(() => {
        this.isConnected = true;
        this.reconnectAttempts = 0;
        this.notifyConnection(true);
        resolve(this.socket);
      });

      this.socket.onMessage((res) => {
        try {
          const message = JSON.parse(res.data);
          this.notifyMessage(message);
        } catch (err) {
          console.error('解析消息失败:', err);
        }
      });

      this.socket.onClose((res) => {
        console.log('WebSocket连接关闭:', res);
        this.isConnected = false;
        this.notifyConnection(false);
        
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
          this.reconnectAttempts++;
          console.log(`尝试重新连接，第 ${this.reconnectAttempts} 次`);
          setTimeout(() => {
            this.connect(userId).catch(() => {});
          }, this.reconnectDelay * this.reconnectAttempts);
        }
      });

      this.socket.onError((err) => {
        console.error('WebSocket错误:', err);
        this.isConnected = false;
        this.notifyConnection(false);
      });
    });
  }

  disconnect() {
    if (this.socket) {
      this.socket.close();
      this.socket = null;
      this.isConnected = false;
    }
  }

  sendMessage(message) {
    return new Promise((resolve, reject) => {
      if (!this.socket || !this.isConnected) {
        reject(new Error('WebSocket未连接'));
        return;
      }

      this.socket.send({
        data: JSON.stringify(message),
        success: resolve,
        fail: reject
      });
    });
  }

  onMessage(callback) {
    if (typeof callback === 'function') {
      this.messageListeners.push(callback);
    }
  }

  offMessage(callback) {
    const index = this.messageListeners.indexOf(callback);
    if (index > -1) {
      this.messageListeners.splice(index, 1);
    }
  }

  onConnectionChange(callback) {
    if (typeof callback === 'function') {
      this.connectionListeners.push(callback);
    }
  }

  offConnectionChange(callback) {
    const index = this.connectionListeners.indexOf(callback);
    if (index > -1) {
      this.connectionListeners.splice(index, 1);
    }
  }

  notifyMessage(message) {
    this.messageListeners.forEach(listener => {
      try {
        listener(message);
      } catch (err) {
        console.error('消息回调失败:', err);
      }
    });
  }

  notifyConnection(isConnected) {
    this.connectionListeners.forEach(listener => {
      try {
        listener(isConnected);
      } catch (err) {
        console.error('连接状态回调失败:', err);
      }
    });
  }

  getStatus() {
    return {
      isConnected: this.isConnected,
      reconnectAttempts: this.reconnectAttempts,
      url: this.url
    };
  }
}

const wsClient = new WebSocketClient();

module.exports = wsClient;
