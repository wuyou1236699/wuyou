const request = require('../../../utils/request.js');
const app = getApp();

Page({
  data: {
    appointments: [],
    loading: true,
    page: 1,
    size: 10,
    hasMore: true,
    statusMap: ['待确认', '已确认', '已完成', '已取消']
  },

  onShow() {
    if (!app.checkLogin()) {
      wx.reLaunch({ url: '/pages/login/login' });
      return;
    }
    this.setData({ page: 1, hasMore: true });
    this.loadAppointments();
  },

  async loadAppointments() {
    if (!this.data.hasMore) return;
    this.setData({ loading: true });
    try {
      const res = await request.get(`/api/appointment/my?page=${this.data.page}&size=${this.data.size}`);
      const newList = res.records || [];
      this.setData({
        appointments: this.data.page === 1 ? newList : this.data.appointments.concat(newList),
        hasMore: newList.length === this.data.size,
        loading: false
      });
    } catch (err) {
      this.setData({ loading: false });
      request.showToast('加载失败');
    }
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.setData({ page: this.data.page + 1 }, () => this.loadAppointments());
    }
  },

  goToChat(e) {
    const counselorId = e.currentTarget.dataset.id;
    const name = e.currentTarget.dataset.name || '咨询师';
    const appointmentId = e.currentTarget.dataset.appointment;
    wx.navigateTo({ url: `/pages/chat/chat?counselorId=${counselorId}&name=${encodeURIComponent(name)}&appointmentId=${appointmentId}` });
  },

  goToReview(e) {
    const appointmentId = e.currentTarget.dataset.id;
    const counselorId = e.currentTarget.dataset.counselor;
    wx.navigateTo({ 
      url: `/pages/review/review?appointmentId=${appointmentId}&counselorId=${counselorId}` 
    });
  },

  viewReview(e) {
    const counselorId = e.currentTarget.dataset.counselor;
    wx.navigateTo({ url: `/pages/counselor/detail/detail?id=${counselorId}` });
  },

  async cancelAppointment(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '提示',
      content: '确认取消预约吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await request.put(`/api/appointment/cancel/${id}`);
            request.showToast('已取消', 'success');
            this.setData({ page: 1, hasMore: true });
            this.loadAppointments();
          } catch (err) {
            request.showToast(err.msg || '取消失败');
          }
        }
      }
    });
  },

  onPullDownRefresh() {
    this.setData({ page: 1, hasMore: true });
    this.loadAppointments();
    wx.stopPullDownRefresh();
  }
});