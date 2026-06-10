const request = require('../../utils/request.js');
const wsClient = require('../../utils/websocket.js');
const app = getApp();

Page({
  data: {
    messages: [],
    inputText: '',
    targetUserId: null,
    targetUserName: null,
    userId: null,
    sending: false,
    isOnline: false,
    scrollTop: 0,
    page: 1,
    hasMore: false,
    loadingMore: false
  },

  onLoad(options) {
    if (!app.checkLogin()) {
      wx.reLaunch({ url: '/pages/login/login' });
      return;
    }

    const targetUserId = options.counselorId;
    const targetUserName = decodeURIComponent(options.name || '咨询师');
    const appointmentId = options.appointmentId ? parseInt(options.appointmentId) : 0;
    const userId = wx.getStorageSync('userId');

    if (!targetUserId) {
      wx.showToast({ title: '参数错误', icon: 'none' });
      return;
    }

    this.setData({
      targetUserId,
      targetUserName,
      userId,
      appointmentId
    });

    this.loadMessages();
    this.connectWebSocket();
  },

  connectWebSocket() {
    wsClient.offMessage(this.onWsMessage);
    wsClient.onMessage(this.onWsMessage);
    wsClient.connect(this.data.userId).catch(() => {
      this.startPolling();
    });
  },

  disconnectWebSocket() {
    wsClient.offMessage(this.onWsMessage);
    wsClient.disconnect();
  },

  onWsMessage(message) {
    if (message.type === 'new_message') {
      const senderId = message.sender === 'user' ? this.data.userId : this.data.targetUserId;
      const isRelevant =
        (message.sender === 'user' && senderId == this.data.userId) ||
        (message.sender === 'counselor' && senderId == this.data.targetUserId);

      if (!isRelevant) return;

      const lastMsg = this.data.messages[this.data.messages.length - 1];
      if (lastMsg && lastMsg.id === message.messageId) return;

      const newMsg = {
        id: message.messageId,
        role: message.sender === 'user' ? 'user' : 'other',
        content: message.content,
        time: message.time || this.formatTime(new Date())
      };

      const messages = [...this.data.messages, newMsg];
      this.setData({ messages });
      this.scrollToBottom();
      wx.vibrateShort({ type: 'medium' });
    }
  },

  startPolling() {
    this.pollingInterval = setInterval(() => {
      this.loadMessages(true);
    }, 5000);
  },

  stopPolling() {
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval);
      this.pollingInterval = null;
    }
  },

  scrollToBottom() {
    setTimeout(() => {
      this.setData({ scrollTop: 99999 });
    }, 100);
  },

  async loadMessages(isBackground = false) {
    try {
      const data = await request.get(
        `/api/chat/messages?userId=${this.data.userId}&counselorId=${this.data.targetUserId}&appointmentId=${this.data.appointmentId}&page=${this.data.page}&size=30`
      );
      if (data && data.messages) {
        const oldCount = this.data.messages.length;
        const messages = data.messages.reverse().map(msg => ({
          id: msg.id,
          role: msg.sender === 'user' ? 'user' : 'other',
          content: msg.content,
          time: msg.time
        }));
        this.setData({ messages, hasMore: data.hasMore });

        if (isBackground && messages.length > oldCount) {
          wx.vibrateShort({ type: 'medium' });
        }
        this.scrollToBottom();
      }
    } catch (err) {
      console.warn('加载消息失败:', err);
    }
  },

  async loadMore() {
    if (!this.data.hasMore || this.data.loadingMore) return;

    this.setData({ loadingMore: true });
    const nextPage = this.data.page + 1;

    try {
      const data = await request.get(
        `/api/chat/messages?userId=${this.data.userId}&counselorId=${this.data.targetUserId}&appointmentId=${this.data.appointmentId}&page=${nextPage}&size=30`
      );
      if (data && data.messages) {
        const olderMessages = data.messages.reverse().map(msg => ({
          id: msg.id,
          role: msg.sender === 'user' ? 'user' : 'other',
          content: msg.content,
          time: msg.time
        }));
        this.setData({
          messages: olderMessages.concat(this.data.messages),
          page: nextPage,
          hasMore: data.hasMore
        });
      }
    } catch (err) {
      console.warn('加载更多失败:', err);
    } finally {
      this.setData({ loadingMore: false });
    }
  },

  async onDeleteMsg(e) {
    const index = e.currentTarget.dataset.index;
    const msg = this.data.messages[index];
    if (!msg || !msg.id) return;

    wx.showModal({
      title: '删除消息',
      content: '确定删除这条消息吗？删除后仅自己不可见。',
      success: async (res) => {
        if (res.confirm) {
          try {
            await request.put(`/api/chat/messages/${msg.id}/delete`);
            const messages = [...this.data.messages];
            messages.splice(index, 1);
            this.setData({ messages });
          } catch (err) {
            wx.showToast({ title: '删除失败', icon: 'none' });
          }
        }
      }
    });
  },

  onInput(e) {
    this.setData({ inputText: e.detail.value });
  },

  async sendMessage() {
    const text = this.data.inputText.trim();
    if (!text) return;
    if (this._sendingLock) return;

    this._sendingLock = true;
    this.setData({ sending: true, inputText: '' });

    try {
      await request.post('/api/chat/send', {
        userId: this.data.userId,
        counselorId: this.data.targetUserId,
        appointmentId: this.data.appointmentId,
        content: text
      });
    } catch (err) {
      request.showToast('发送失败，请稍后重试');
    } finally {
      this._sendingLock = false;
      this.setData({ sending: false });
    }
  },

  formatTime(date) {
    if (!date) return '';
    const d = typeof date === 'number' ? new Date(date) : new Date(date);
    if (isNaN(d.getTime())) return '';
    const hours = d.getHours().toString().padStart(2, '0');
    const minutes = d.getMinutes().toString().padStart(2, '0');
    return `${hours}:${minutes}`;
  },

  goBack() {
    this.stopPolling();
    this.disconnectWebSocket();
    wx.navigateBack();
  },

  onShow() {
    if (this.data.targetUserId) {
      this.setData({ page: 1 });
      this.loadMessages();
      this.connectWebSocket();
    }
  },

  onHide() {
    this.stopPolling();
    this.disconnectWebSocket();
  },

  onUnload() {
    this.stopPolling();
    this.disconnectWebSocket();
  }
});
