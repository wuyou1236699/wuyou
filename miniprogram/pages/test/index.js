const request = require('../../utils/request.js');

Page({
  data: {
    tests: [],
    loading: false
  },

  onLoad() {
    this.loadTests();
  },

  onShow() {
    if (this.data.tests.length === 0) {
      this.loadTests();
    }
  },

  onPullDownRefresh() {
    this.loadTests();
  },

  async loadTests() {
    this.setData({ loading: true });
    
    try {
      const data = await request.get('/api/tests', false);
      this.setData({
        tests: data.map(test => ({
          ...test,
          icon: this.getIcon(test.category),
          color: this.getColor(test.category)
        }))
      });
    } catch (err) {
      console.warn('加载测评失败:', err);
      request.showToast('加载测评失败，请检查网络');
    } finally {
      this.setData({ loading: false });
      wx.stopPullDownRefresh();
    }
  },

  getIcon(category) {
    const icons = {
      anxiety: '😰',
      depression: '😔',
      stress: '💼',
      emotion: '🎭',
      sleep: '😴',
      relationship: '👥'
    };
    return icons[category] || '🧠';
  },

  getColor(category) {
    const colors = {
      anxiety: '#EF4444',
      depression: '#3B82F6',
      stress: '#F59E0B',
      emotion: '#8B5CF6',
      sleep: '#06B6D4',
      relationship: '#10B981'
    };
    return colors[category] || '#6B7280';
  },

  goToTest(e) {
    const test = e.currentTarget.dataset.test;
    wx.navigateTo({
      url: `/pages/test/detail?id=${test.id}&title=${encodeURIComponent(test.title)}`
    });
  },

  goToHistory() {
    wx.navigateTo({ url: '/pages/test/history' });
  }
});
