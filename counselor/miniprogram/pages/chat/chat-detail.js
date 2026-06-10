const request = require('../../utils/request.js');
const app = getApp();

Page({
  data: {
    userId: '',
    userName: '',
    counselorId: '',
    appointmentId: null,
    messages: [],
    inputMessage: '',
    isOnline: false,
    scrollTop: 0,
    loading: true,
    page: 1,
    hasMore: false,
    loadingMore: false,
    canReply: true,
    replyNotice: ''
  },

  onLoad: function(options) {
    const userInfo = wx.getStorageSync('userInfo');
    this.setData({
      userId: options.userId || '',
      userName: decodeURIComponent(options.userName || '用户'),
      counselorId: userInfo?.id || '',
      appointmentId: options.appointmentId ? parseInt(options.appointmentId) : null
    });

    // 检查回复权限
    if (!this.data.appointmentId) {
      this.setData({ canReply: false, replyNotice: '直接消息不可回复，请等待用户预约' });
    } else {
      this.checkAppointmentStatus();
    }

    this.markAsRead();
    this.loadData();
    this.startListening();
  },

  onShow: function() {
    this.startListening();
  },

  onHide: function() {
    this.stopListening();
  },

  onUnload: function() {
    this.stopListening();
  },

  async markAsRead() {
    const userId = this.data.userId;
    if (!userId) return;
    try {
      await request.put('/api/chat/read', { userId: parseInt(userId), counselorId: this.data.counselorId });
    } catch (err) {
      console.warn('标记已读失败:', err);
    }
  },

  async loadData() {
    this.setData({ loading: true, page: 1 });
    try {
      await Promise.all([
        this.loadOnlineStatus(),
        this.loadMessages()
      ]);
    } catch (err) {
      console.error('加载数据失败:', err);
    } finally {
      this.setData({ loading: false });
    }
  },

  async checkAppointmentStatus() {
    try {
      const data = await request.get('/api/appointments/' + this.data.appointmentId);
      if (data && (data.status === 2 || data.status === 3)) {
        this.setData({
          canReply: false,
          replyNotice: data.status === 2 ? '预约已完成，不能继续发送消息' : '预约已取消，不能继续发送消息'
        });
      }
    } catch (err) {
      console.warn('检查预约状态失败:', err);
    }
  },

  async loadOnlineStatus() {
    try {
      const data = await request.get('/api/counselors/online-status');
      this.setData({ isOnline: data.isOnline });
    } catch (err) {
      console.warn('获取在线状态失败:', err);
    }
  },

  async loadMessages() {
    const userId = this.data.userId;
    const counselorId = this.data.counselorId;
    if (!userId || !counselorId) return;

    try {
      const apptId = this.data.appointmentId ? `&appointmentId=${this.data.appointmentId}` : '';
      const data = await request.get(`/api/chat/messages?userId=${userId}&counselorId=${counselorId}${apptId}&page=${this.data.page}&size=30`);
      if (data && data.messages) {
        this.setData({
          messages: data.messages.reverse(),
          hasMore: data.hasMore
        });
      }
      this.scrollToBottom();
    } catch (err) {
      console.warn('获取消息失败:', err);
    }
  },

  async loadMore() {
    if (!this.data.hasMore || this.data.loadingMore) return;

    this.setData({ loadingMore: true });
    const nextPage = this.data.page + 1;

    try {
      const data = await request.get(
        `/api/chat/messages?userId=${this.data.userId}&counselorId=${this.data.counselorId}${this.data.appointmentId ? '&appointmentId=' + this.data.appointmentId : ''}&page=${nextPage}&size=30`
      );
      if (data && data.messages) {
        const olderMessages = data.messages.reverse();
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

  startListening() {
    if (this._listenInterval) clearInterval(this._listenInterval);
    this._listenInterval = setInterval(() => {
      this.loadMessages();
    }, 5000);
  },

  stopListening() {
    if (this._listenInterval) {
      clearInterval(this._listenInterval);
      this._listenInterval = null;
    }
  },

  async sendMessage() {
    const text = this.data.inputMessage.trim();
    if (!text) return;
    if (this._sendingLock) return;

    this._sendingLock = true;
    this.setData({ inputMessage: '' });

    try {
      await request.post('/api/chat/send', {
        userId: this.data.userId,
        counselorId: this.data.counselorId,
        appointmentId: this.data.appointmentId,
        content: text
      });
      await this.loadMessages();
    } catch (err) {
      console.error('发送消息失败:', err);
      request.showToast('发送失败');
    } finally {
      this._sendingLock = false;
    }
  },

  scrollToBottom() {
    setTimeout(() => {
      this.setData({ scrollTop: 99999 });
    }, 100);
  },

  getCurrentTime() {
    const now = new Date();
    return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`;
  },

  onInput(e) {
    this.setData({ inputMessage: e.detail.value });
  }
});
