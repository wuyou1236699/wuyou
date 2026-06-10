const request = require('../../utils/request.js');

Page({
  data: {
    activeTab: 'pending',
    pendingAppointments: [],
    confirmedAppointments: [],
    historyAppointments: [],
    loading: true,
    isOnline: false
  },

  onLoad() {
    this.loadData();
  },

  onShow() {
    this.loadData();
  },

  async loadData() {
    this.setData({ loading: true });
    try {
      await Promise.all([
        this.loadOnlineStatus(),
        this.loadPendingAppointments(),
        this.loadConfirmedAppointments(),
        this.loadHistoryAppointments()
      ]);
    } catch (err) {
      console.error('加载数据失败:', err);
      request.showToast(err.msg || '加载失败');
    } finally {
      this.setData({ loading: false });
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

  async loadPendingAppointments() {
    try {
      const data = await request.get('/api/appointments/upcoming');
      this.setData({
        pendingAppointments: data.map(item => ({
          ...item,
          dateText: request.formatDate(item.appointmentTime),
          timeText: request.formatTime(item.appointmentTime)
        }))
      });
    } catch (err) {
      console.warn('获取待处理预约失败:', err);
      this.setData({ pendingAppointments: [] });
    }
  },

  async loadConfirmedAppointments() {
    try {
      const data = await request.get('/api/appointments/confirmed');
      this.setData({
        confirmedAppointments: data.map(item => ({
          ...item,
          dateText: request.formatDate(item.appointmentTime),
          timeText: request.formatTime(item.appointmentTime)
        }))
      });
    } catch (err) {
      console.warn('获取已确认预约失败:', err);
      this.setData({ confirmedAppointments: [] });
    }
  },

  async loadHistoryAppointments() {
    try {
      const data = await request.get('/api/appointments/history');
      this.setData({
        historyAppointments: data.map(item => ({
          ...item,
          dateText: request.formatDate(item.appointmentTime),
          timeText: request.formatTime(item.appointmentTime)
        }))
      });
    } catch (err) {
      console.warn('获取历史记录失败:', err);
      this.setData({ historyAppointments: [] });
    }
  },

  switchTab(e) {
    this.setData({ activeTab: e.currentTarget.dataset.tab });
  },

  async goOnline(e) {
    const item = e.currentTarget.dataset.item;

    if (item.serviceType === 3) {
      wx.showToast({ title: '门诊预约请线下咨询', icon: 'none' });
      return;
    }

    if (!this.data.isOnline) {
      wx.showModal({
        title: '上线确认',
        content: '您当前离线，需要先上线才能开始咨询',
        showCancel: false
      });
      return;
    }

    wx.showModal({
      title: '开始咨询',
      content: `确认与${item.userName}开始咨询？`,
      confirmText: '开始',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm) {
          wx.navigateTo({
            url: `/pages/chat/chat-detail?userId=${item.userId}&userName=${item.userName}&appointmentId=${item.id}`
          });
        }
      }
    });
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/appointment-detail/appointment-detail?id=${id}` });
  },

  onPullDownRefresh() {
    this.loadData();
    wx.stopPullDownRefresh();
  }
})