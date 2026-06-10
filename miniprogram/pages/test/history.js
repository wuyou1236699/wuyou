const request = require('../../utils/request.js');
const app = getApp();

Page({
  data: {
    history: [],
    loading: false
  },

  onLoad() {
    if (!app.checkLogin()) {
      wx.reLaunch({ url: '/pages/login/login' });
      return;
    }
    this.loadHistory();
  },

  onShow() {
    this.loadHistory();
  },

  onPullDownRefresh() {
    this.loadHistory();
  },

  async loadHistory() {
    this.setData({ loading: true });
    
    try {
      const data = await request.get('/api/tests/history');
      this.setData({
        history: data.map(item => ({
          ...item,
          createTime: this.formatDate(item.createTime),
          levelColor: this.getLevelColor(item.result),
          suggestionsArray: item.suggestions ? item.suggestions.split('\n').filter(s => s.trim()) : []
        }))
      });
    } catch (err) {
      console.warn('加载历史记录失败:', err);
      request.showToast('加载历史记录失败，请检查网络');
    } finally {
      this.setData({ loading: false });
      wx.stopPullDownRefresh();
    }
  },

  formatDate(date) {
    if (!date) return '';
    const d = new Date(date);
    const year = d.getFullYear();
    const month = (d.getMonth() + 1).toString().padStart(2, '0');
    const day = d.getDate().toString().padStart(2, '0');
    const hour = d.getHours().toString().padStart(2, '0');
    const minute = d.getMinutes().toString().padStart(2, '0');
    return `${year}-${month}-${day} ${hour}:${minute}`;
  },

  getLevelColor(result) {
    if (!result) return '#6B7280';
    if (result.includes('重度')) return '#DC2626';
    if (result.includes('中度')) return '#EF4444';
    if (result.includes('轻度')) return '#F59E0B';
    return '#10B981';
  },

  goToDetail(e) {
    const record = e.currentTarget.dataset.record;
    wx.showModal({
      title: record.testName,
      content: `测试结果：${record.result}\n\n总分：${record.totalScore}分\n\n建议：${record.suggestions || '暂无'}`,
      showCancel: false,
      confirmText: '知道了'
    });
  },

  goBack() {
    wx.navigateBack();
  }
});
