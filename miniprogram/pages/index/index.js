const request = require('../../utils/request.js');
const app = getApp();

Page({
  data: {
    counselors: [],
    loading: true,
    page: 1,
    size: 10,
    hasMore: true,
    onlineStatusMap: {},
    isOnline: false,
    userId: null,
    announcements: []
  },

  onLoad() {
    if (!app.checkLogin()) {
      wx.reLaunch({ url: '/pages/login/login' });
      return;
    }
    const userId = wx.getStorageSync('userId');
    this.setData({ userId });
    this.loadCounselors();
    this.loadUserOnlineStatus();
    this.loadAnnouncements();
  },

  onShow() {
    if (!app.checkLogin()) {
      wx.reLaunch({ url: '/pages/login/login' });
      return;
    }
    if (this.data.page === 1) {
      this.loadUserOnlineStatus();
      this.loadCounselors();
    }
  },

  async loadUserOnlineStatus() {
    const userId = this.data.userId;
    if (!userId) return;

    try {
      const data = await request.get(`/api/user/online-status/${userId}`, null, false);
      this.setData({ isOnline: data.isOnline });
    } catch (err) {
      console.warn('获取用户在线状态失败:', err);
    }
  },

  async loadCounselors(reset = true) {
    if (reset) {
      this.setData({ page: 1, counselors: [], hasMore: true });
    }
    if (!this.data.hasMore) return;

    this.setData({ loading: true });

    try {
      const res = await request.get(`/api/counselor/list?page=${this.data.page}&size=${this.data.size}`, false);
      const newList = (res.records || []).map(function(c) {
        c.displayPrice = c.price != null ? '¥' + c.price + '/次' : '面议';
        return c;
      });
      await this.loadOnlineStatus();
      this.setData({
        counselors: this.data.page === 1 ? newList : this.data.counselors.concat(newList),
        hasMore: newList.length === this.data.size,
        loading: false
      });
    } catch (err) {
      console.warn('API请求失败:', err);
      this.setData({ loading: false });
      request.showToast(err.msg || '加载失败');
    }
  },

  async loadOnlineStatus() {
    try {
      const data = await request.get('/api/counselors/online-status-list', false);
      this.setData({ onlineStatusMap: data });
    } catch (err) {
      console.warn('获取在线状态失败:', err);
    }
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
    wx.showModal({
      title: '公告详情',
      content: this.data.announcements[0]?.content || '暂无内容',
      showCancel: false
    });
  },

  getOnlineStatus(counselorId) {
    return this.data.onlineStatusMap[counselorId] || false;
  },


  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.setData({ page: this.data.page + 1 }, () => this.loadCounselors(false));
    }
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/counselor/detail/detail?id=${id}` });
  },

  goToBook() {
    wx.navigateTo({ url: '/pages/appointment/book/book' });
  },

  goToAppointment() {
    wx.navigateTo({ url: '/pages/appointment/my/my' });
  },

  goToChat(e) {
    const counselorId = e.currentTarget.dataset.id;
    const name = e.currentTarget.dataset.name;
    if (!counselorId) {
      wx.showToast({
        title: '请先选择一位咨询师',
        icon: 'none'
      });
      return;
    }
    wx.navigateTo({
      url: `/pages/chat/chat?counselorId=${counselorId}&name=${encodeURIComponent(name)}`
    });
  },

  goToUser() {
    wx.switchTab({ url: '/pages/user/user' });
  },

  goToScience() {
    wx.switchTab({ url: '/pages/science/index' });
  },

  goToTest() {
    wx.switchTab({ url: '/pages/test/index' });
  },

  onPullDownRefresh() {
    this.setData({ page: 1 });
    this.loadCounselors();
    wx.stopPullDownRefresh();
  }
});
