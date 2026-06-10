const request = require('../../utils/request.js');
const app = getApp();

Page({
  data: {
    reviews: [],
    loading: true
  },

  onLoad() {
    if (!app.checkLogin()) {
      wx.reLaunch({ url: '/pages/login/login' });
      return;
    }
    this.loadMyReviews();
  },

  onShow() {
    this.loadMyReviews();
  },

  async loadMyReviews() {
    this.setData({ loading: true });
    try {
      const res = await request.get('/api/review/my');
      this.setData({
        reviews: res.data || res || [],
        loading: false
      });
    } catch (err) {
      wx.showToast({ title: '加载失败', icon: 'none' });
      this.setData({ loading: false });
    }
  },

  goToCounselor(e) {
    const id = e.currentTarget.dataset.counselorId;
    wx.navigateTo({ url: `/pages/counselor/detail/detail?id=${id}` });
  },

  formatTime(time) {
    if (!time) return '';
    return time.substring(0, 10);
  }
});
