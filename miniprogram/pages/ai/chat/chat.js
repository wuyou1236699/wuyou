const request = require('../../../utils/request.js');
const app = getApp();

Page({
  data: {
    messages: [],
    inputValue: '',
    scrollToView: '',
    sending: false,
    showTyping: false
  },

  onLoad() {
    if (!app.checkLogin()) {
      wx.reLaunch({ url: '/pages/login/login' });
      return;
    }
    this.loadHistory();
  },

  async loadHistory() {
    try {
      const history = await request.get('/api/ai/history?limit=20');
      if (history && history.length > 0) {
        const messages = [];
        history.forEach(item => {
          messages.push({ role: item.role, content: item.content, time: this.formatTime(item.createTime) });
        });
        this.setData({ messages });
        this.scrollToBottom();
      }
    } catch (err) {
      console.warn('加载历史记录失败:', err);
    }
  },

  onInput(e) {
    this.setData({ inputValue: e.detail.value });
  },

  async sendMessage() {
    const content = this.data.inputValue.trim();
    if (!content || this.data.sending) return;

    this.setData({ sending: true, inputValue: '', showTyping: true });

    const userMsg = { role: 'user', content, time: this.formatTime(new Date()) };
    this.setData({ messages: [...this.data.messages, userMsg] });
    this.scrollToBottom();

    try {
      const response = await request.post('/api/ai/chat', { message: content });
      const aiMsg = { role: 'ai', content: response, time: this.formatTime(new Date()) };
      this.setData({
        messages: [...this.data.messages, aiMsg],
        showTyping: false,
        sending: false
      });
      this.scrollToBottom();
    } catch (err) {
      this.setData({
        showTyping: false,
        sending: false
      });
      request.showToast('发送失败');
    }
  },

  scrollToBottom() {
    this.setData({ scrollToView: 'bottom' });
  },

  formatTime(date) {
    if (!date) return '';
    if (typeof date === 'string') date = new Date(date);
    if (!date || isNaN(date.getTime())) return '';
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${hours}:${minutes}`;
  },

  clearHistory() {
    wx.showModal({
      title: '清除历史记录',
      content: '确定要清除所有聊天历史记录吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await request.del('/api/ai/history');
            this.setData({ messages: [] });
            wx.showToast({ title: '历史记录已清除', icon: 'success' });
          } catch (err) {
            wx.showToast({ title: '清除失败，请稍后重试', icon: 'none' });
          }
        }
      }
    });
  }
});
