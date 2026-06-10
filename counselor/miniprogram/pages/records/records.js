const request = require('../../utils/request.js');

Page({
  data: {
    records: [],
    loading: true,
    filterStatus: 'all',
    filterOptions: [
      { value: 'all', label: '全部' },
      { value: 'draft', label: '草稿' },
      { value: 'completed', label: '已完成' }
    ],
    filterLabels: ['全部', '草稿', '已完成'],
    filterIndex: 0
  },

  onLoad() {
    this.loadRecords();
  },

  onShow() {
    this.loadRecords();
  },

  async loadRecords() {
    this.setData({ loading: true });
    try {
      const params = {};
      if (this.data.filterStatus === 'draft') {
        params.status = 0;
      } else if (this.data.filterStatus === 'completed') {
        params.status = 1;
      }
      
      const data = await request.get('/api/records', params);
      const serviceMap = {1: '电话咨询', 2: '网络咨询', 3: '门诊咨询'};
      const records = (data.records || []).map(r => ({ ...r, serviceTypeText: serviceMap[r.serviceType] || '未知' }));
      this.setData({ records });
    } catch (err) {
      console.warn('获取记录失败:', err);
      this.setData({ records: [] });
    } finally {
      this.setData({ loading: false });
    }
  },

  onFilterChange(e) {
    const index = e.detail.value;
    const options = this.data.filterOptions;
    this.setData({ 
      filterIndex: index,
      filterStatus: options[index].value 
    }, () => {
      this.loadRecords();
    });
  },

  viewDetail(e) {
    const id = e.currentTarget.dataset.id;
    const appointmentId = e.currentTarget.dataset.appointmentId;
    if (id) {
      wx.navigateTo({ url: `/pages/records/record-detail?id=${id}` });
    } else if (appointmentId) {
      wx.navigateTo({ url: `/pages/records/record-detail?appointmentId=${appointmentId}` });
    }
  },

  onPullDownRefresh() {
    this.loadRecords();
    wx.stopPullDownRefresh();
  }
})
