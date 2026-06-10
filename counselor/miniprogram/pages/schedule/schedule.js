const request = require('../../utils/request.js');
const auth = require('../../utils/auth.js');

Page({
  data: {
    scheduleList: [],
    loading: true
  },

  onLoad() {
    this.loadSchedule();
  },

  onShow() {
    this.loadSchedule();
  },

  async loadSchedule() {
    this.setData({ loading: true });
    try {
      const userInfo = auth.getUserInfo();
      if (!userInfo || !userInfo.id) {
        throw new Error('未获取到咨询师信息');
      }

      const today = new Date();
      const startDate = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;

      const endDate = new Date(today);
      endDate.setDate(endDate.getDate() + 30);
      const endDateStr = `${endDate.getFullYear()}-${String(endDate.getMonth() + 1).padStart(2, '0')}-${String(endDate.getDate()).padStart(2, '0')}`;

      const data = await request.get(`/api/counselor/schedule?counselorId=${userInfo.id}&startDate=${startDate}&endDate=${endDateStr}`);

      const scheduleMap = {};
      data.forEach(item => {
        const dateStr = item.date ? (typeof item.date === 'string' ? item.date : item.date.toString()) : (item.dateStr || '');
        if (!dateStr) return;

        if (!scheduleMap[dateStr]) {
          scheduleMap[dateStr] = {
            date: dateStr,
            timeRanges: []
          };
        }

        const startTime = item.startTime ? (typeof item.startTime === 'string' ? item.startTime : item.startTime.toString()) : '';
        const endTime = item.endTime ? (typeof item.endTime === 'string' ? item.endTime : item.endTime.toString()) : '';

        const timeRange = `${startTime.substring(0, 5)}-${endTime.substring(0, 5)}`;
        if (!scheduleMap[dateStr].timeRanges.includes(timeRange)) {
          scheduleMap[dateStr].timeRanges.push(timeRange);
        }
      });

      const scheduleList = Object.values(scheduleMap).map(s => ({
        ...s,
        timeRanges: [...s.timeRanges].sort()
      }));

      this.setData({ scheduleList });
    } catch (err) {
      console.warn('获取排班失败:', err);
      this.setData({ scheduleList: [] });
    } finally {
      this.setData({ loading: false });
    }
  }
})
