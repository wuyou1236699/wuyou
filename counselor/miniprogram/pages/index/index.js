const request = require('../../utils/request.js');
const auth = require('../../utils/auth.js');

Page({
  data: {
    upcomingAppointments: [],
    isOnline: false,
    stats: {
      todayAppointments: 0,
      pendingMessages: 0,
      monthConsultations: 0,
      rating: 0
    },
    userInfo: {},
    loading: true,
    announcements: []
  },

  heartbeatTimer: null,

  onLoad: function() {
    this.loadData();
  },

  onShow: function() {
    this.loadData();
  },

  onHide: function() {
    this.stopHeartbeat();
  },

  onUnload: function() {
    this.stopHeartbeat();
  },

  startHeartbeat() {
    this.stopHeartbeat();
    this.heartbeatTimer = setInterval(() => {
      request.put('/api/counselors/heartbeat', {}).catch(() => {});
    }, 180000);
  },

  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  },

  async loadData() {
    this.setData({ loading: true });
    try {
      await Promise.all([
        this.loadUserInfo(),
        this.loadOnlineStatus(),
        this.loadStats(),
        this.loadUpcomingAppointments(),
        this.loadAnnouncements()
      ]);
    } catch (err) {
      console.error('加载数据失败:', err);
      request.showToast(err.msg || '加载失败');
    } finally {
      this.setData({ loading: false });
    }
  },

  async loadUserInfo() {
    const userInfo = wx.getStorageSync('userInfo') || {};
    if (userInfo.id) {
      try {
        const data = await request.get(`/api/counselors/${userInfo.id}`);
        this.setData({ userInfo: data });
        if (data && data.name) {
          wx.setStorageSync('userInfo', { ...userInfo, ...data });
        }
      } catch (err) {
        this.setData({ userInfo });
      }
    } else {
      this.setData({ userInfo });
    }
  },

  async loadOnlineStatus() {
    try {
      const data = await request.get('/api/counselors/online-status');
      this.setData({ isOnline: data.isOnline });
      if (data.isOnline) {
        this.startHeartbeat();
      } else {
        await this.setOnline(true);
      }
    } catch (err) {
      console.warn('获取在线状态失败:', err);
    }
  },

  async setOnline(isOnline) {
    try {
      await request.put('/api/counselors/online-status', { isOnline });
      this.setData({ isOnline });
      if (isOnline) {
        this.startHeartbeat();
      } else {
        this.stopHeartbeat();
      }
    } catch (err) {
      console.warn('设置在线状态失败:', err);
    }
  },

  async loadStats() {
    try {
      const data = await request.get('/api/counselors/stats');
      this.setData({ stats: data });
    } catch (err) {
      console.warn('获取统计数据失败:', err);
    }
  },

  async loadUpcomingAppointments() {
    try {
      const data = await request.get('/api/appointments/upcoming');
      this.setData({
        upcomingAppointments: data.map(item => ({
          ...item,
          dateText: request.formatDate(item.appointmentTime),
          timeText: request.formatTime(item.appointmentTime)
        }))
      });
    } catch (err) {
      console.warn('获取预约列表失败:', err);
    }
  },

  async toggleOnlineStatus() {
    const newStatus = !this.data.isOnline;
    this.setData({ isOnline: newStatus });

    try {
      await request.put('/api/counselors/online-status', { isOnline: newStatus });
      if (newStatus) {
        this.startHeartbeat();
        request.showToast('已上线，开始接待', 'success');
      } else {
        this.stopHeartbeat();
        request.showToast('已离线', 'success');
      }
    } catch (err) {
      this.setData({ isOnline: !newStatus });
      request.showToast('操作失败，请重试');
    }
  },

  goToAppointmentDetail(e) {
    const appointmentId = e.currentTarget.dataset.id;
    if (appointmentId) {
      wx.navigateTo({
        url: `/pages/appointment-detail/appointment-detail?id=${appointmentId}`
      });
    } else {
      request.showToast('无法获取预约信息');
    }
  },

  goToChat(e) {
    const userId = e.currentTarget.dataset.userid;
    const name = e.currentTarget.dataset.username;

    if (userId) {
      wx.navigateTo({
        url: `/pages/chat/chat-detail?userId=${userId}&userName=${encodeURIComponent(name || '用户')}`
      });
    } else {
      request.showToast('无法获取用户信息');
    }
  },

  goToAppointments() {
    wx.switchTab({ url: '/pages/appointments/appointments' });
  },

  goToChatList() {
    wx.navigateTo({ url: '/pages/chat/chat-list' });
  },

  goToRecords() {
    wx.switchTab({ url: '/pages/records/records' });
  },

  async loadAnnouncements() {
    try {
      const data = await request.get('/api/public/announcements', false);
      this.setData({ announcements: data.records || [] });
    } catch (err) {
      console.warn('获取公告失败:', err);
    }
  },

  goToAnnouncement(e) {
    const id = e.currentTarget.dataset.id;
    const announcement = this.data.announcements.find(a => a.id == id);
    wx.showModal({
      title: announcement?.title || '公告',
      content: announcement?.content || '暂无内容',
      showCancel: false
    });
  },

  goToSchedule() {
    wx.navigateTo({ url: '/pages/schedule/schedule' });
  },

  onPullDownRefresh() {
    this.loadData();
    wx.stopPullDownRefresh();
  }
});
