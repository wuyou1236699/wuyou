const request = require('../../utils/request.js');
const app = getApp();

Page({
  data: {
    appointmentId: '',
    counselorId: '',
    rating: 0,
    content: ''
  },
  onLoad(options) {
    if (!app.checkLogin()) {
      wx.reLaunch({ url: '/pages/login/login' });
      return;
    }
    this.setData({
      appointmentId: options.appointmentId,
      counselorId: options.counselorId
    });
  },
  selectRating(e) {
    const rating = e.currentTarget.dataset.rating;
    this.setData({ rating });
  },
  onContentInput(e) {
    this.setData({ content: e.detail.value });
  },
  async submitReview() {
    if (this.data.rating === 0) {
      wx.showToast({ title: '请选择评分', icon: 'none' });
      return;
    }
    wx.showLoading({ title: '提交中...', mask: true });
    try {
      await request.post('/api/review/add', {
        appointmentId: this.data.appointmentId,
        counselorId: this.data.counselorId,
        rating: this.data.rating,
        content: this.data.content
      });
      wx.hideLoading();
      wx.showToast({ title: '评价成功', icon: 'success' });
      // 刷新上一页（我的预约页）的数据
      const pages = getCurrentPages();
      const prevPage = pages[pages.length - 2];
      if (prevPage && prevPage.loadAppointments) {
        prevPage.loadAppointments();
      }
      setTimeout(() => wx.navigateBack(), 1500);
    } catch (err) {
      wx.hideLoading();
      wx.showToast({ title: err.msg || '评价失败', icon: 'none' });
    }
  }
});