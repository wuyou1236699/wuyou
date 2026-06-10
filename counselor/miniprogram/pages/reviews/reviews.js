const request = require('../../utils/request.js');

Page({
  data: {
    items: [],
    loading: true
  },

  onLoad() {
    this.loadData();
  },

  async loadData() {
    this.setData({ loading: true });
    try {
      const userInfo = wx.getStorageSync('userInfo') || {};
      const counselorId = userInfo.id;

      // 同时获取评价和已完成记录
      const [reviews, recordsData] = await Promise.all([
        request.get(`/api/review/list?counselorId=${counselorId}&page=1&size=100`).catch(() => []),
        request.get('/api/records').catch(() => ({ records: [] }))
      ]);

      const records = recordsData.records || [];

      // 建立评价映射：appointmentId -> review
      const reviewMap = {};
      (reviews || []).forEach(r => {
        reviewMap[r.appointmentId] = r;
      });

      // 合并：已有评价的 + 未评价的记录
      const items = [];
      const seenAppointmentIds = new Set();

      // 先放有评价的
      (reviews || []).forEach(r => {
        const record = records.find(rec => rec.appointmentId === r.appointmentId) || {};
        items.push({
          id: r.id,
          userName: record.userName || r.userName || '匿名用户',
          rating: r.rating,
          content: r.content,
          createTime: r.createTime,
          date: record.date || '',
          hasReview: true
        });
        seenAppointmentIds.add(r.appointmentId);
      });

      // 再放未评价的已完成记录
      records.forEach(rec => {
        if (!seenAppointmentIds.has(rec.appointmentId)) {
          items.push({
            id: rec.id || rec.appointmentId,
            userName: rec.userName || '匿名用户',
            rating: 0,
            content: '',
            createTime: '',
            date: rec.date || '',
            hasReview: false
          });
        }
      });

      this.setData({ items });
    } catch (err) {
      console.warn('加载评价失败:', err);
    } finally {
      this.setData({ loading: false });
    }
  }
});