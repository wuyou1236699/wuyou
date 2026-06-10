const request = require('../../utils/request.js');
const app = getApp();

Page({
  data: {
    records: [],
    loading: true,
    activeTab: 'all'
  },

  onLoad() {
    if (!app.checkLogin()) {
      wx.reLaunch({ url: '/pages/login/login' });
      return;
    }
    this.loadRecords();
  },

  onShow() {
    this.loadRecords();
  },

  async loadRecords() {
    this.setData({ loading: true });
    
    try {
      const status = this.data.activeTab === 'all' ? null : 
                     this.data.activeTab === 'completed' ? 1 : 0;
      
      let url = '/api/records?page=1&size=50';
      if (status !== null) {
        url += '&status=' + status;
      }

      const data = await request.get(url);
      const records = data.records || [];
      
      const formattedRecords = records.map(item => ({
        ...item,
        createTimeText: this.formatDateTime(item.createTime),
        displayFee: item.fee != null ? '¥' + item.fee : '未填写'
      }));
      
      this.setData({ records: formattedRecords });
    } catch (err) {
      console.warn('加载咨询记录失败:', err);
      request.showToast('加载失败');
    } finally {
      this.setData({ loading: false });
    }
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    this.setData({ activeTab: tab }, () => {
      this.loadRecords();
    });
  },

  formatDateTime(dateStr) {
    if (!dateStr) return '';
    try {
      const date = new Date(dateStr);
      const year = date.getFullYear();
      const month = (date.getMonth() + 1).toString().padStart(2, '0');
      const day = date.getDate().toString().padStart(2, '0');
      const hour = date.getHours().toString().padStart(2, '0');
      const minute = date.getMinutes().toString().padStart(2, '0');
      return `${year}-${month}-${day} ${hour}:${minute}`;
    } catch (e) {
      return dateStr;
    }
  }
});
