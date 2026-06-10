const { get, post, showToast, showLoading, hideLoading, formatDateTime } = require('../../../utils/request.js');

// 如果需要直接使用完整URL，取消注释下面一行
// const BASE_URL = 'http://172.20.10.5:8080';

Page({
  data: {
    counselor: null,
    loading: true,
    showBookForm: false,
    serviceType: 1,
    problem: '',
    conditionTags: ['抑郁', '焦虑', '婚恋', '情绪压力', '青少年', '职场', '人际关系', '睡眠障碍', '学业压力', '自我成长', '原生家庭', '其他'],
    selectedCondition: '',
    schedules: [],
    availableDates: [],
    availableTimes: [],
    selectedDate: '',
    selectedTime: '',
    avgRating: 0,
    reviews: [],
    cases: []
  },

  onLoad(options) {
    const id = options.id;
    if (id) {
      this.loadData(id);
    } else {
      wx.showToast({ title: '参数错误', icon: 'none' });
      this.setData({ loading: false });
    }
  },

  async loadData(id) {
    showLoading('加载中...');
    
    try {
      const [counselor, schedules, avgRating, reviews, cases] = await Promise.all([
        this.loadCounselor(id),
        this.loadSchedules(id),
        this.loadAvgRating(id),
        this.loadReviews(id),
        this.loadCases(id)
      ]);

      console.log('所有数据加载完成:', { counselor, schedules, avgRating, reviews, cases });

      const dates = [...new Set(schedules.map(s => s.date))];

      this.setData({
        counselor,
        schedules,
        availableDates: dates,
        avgRating,
        reviews,
        cases,
        loading: false
      });
    } catch (err) {
      console.log('API调用失败:', err);
      showToast('加载失败，请检查网络');
      this.setData({ loading: false });
    } finally {
      hideLoading();
    }
  },

  async loadCounselor(id) {
    return await get(`/api/counselor/detail/${id}`);
  },

  async loadSchedules(counselorId) {
    const today = new Date();
    const startDate = this.formatDate(today);
    const endDate = this.formatDate(new Date(today.getTime() + 30 * 24 * 3600 * 1000));
    return await get(`/api/counselor/schedule?counselorId=${counselorId}&startDate=${startDate}&endDate=${endDate}`);
  },

  async loadAvgRating(counselorId) {
    const avg = await get(`/api/review/avg?counselorId=${counselorId}`);
    return avg || 0;
  },

  async loadReviews(counselorId) {
    const reviews = await get(`/api/review/list?counselorId=${counselorId}&page=1&size=5`);
    console.log('获取到的评价数据:', reviews);
    return reviews;
  },

  async loadCases(counselorId) {
    const cases = await get(`/api/records/counselor/${counselorId}`);
    console.log('获取到的咨询案例:', cases);
    return cases || [];
  },

  onDateChange(e) {
    const selectedDate = e.detail.value;
    const schedulesOfDay = this.data.schedules.filter(s => s.date === selectedDate);
    let times = [];
    schedulesOfDay.forEach(schedule => {
      const slots = this.generateTimeSlots(schedule.startTime, schedule.endTime, 30);
      times = times.concat(slots);
    });
    times = [...new Set(times)].sort();
    this.setData({
      selectedDate,
      availableTimes: times,
      selectedTime: ''
    });
  },

  onTimeChange(e) {
    const index = e.detail.value;
    const time = this.data.availableTimes[index];
    this.setData({ selectedTime: time });
  },

  onServiceTypeChange(e) {
    this.setData({ serviceType: parseInt(e.detail.value) });
  },

  onProblemInput(e) {
    this.setData({ problem: e.detail.value });
  },

  selectCondition(e) {
    var tag = e.currentTarget.dataset.tag;
    this.setData({ selectedCondition: tag === this.data.selectedCondition ? '' : tag });
  },

  showBookDialog() {
    if (this.data.availableDates.length === 0) {
      wx.showToast({ title: '暂无排班，请稍后再试', icon: 'none' });
      return;
    }
    this.setData({ showBookForm: true });
  },

  hideBookDialog() {
    this.setData({ showBookForm: false });
  },

  async submitBooking() {
    if (!this.data.selectedDate || !this.data.selectedTime) {
      wx.showToast({ title: '请选择预约时间和日期', icon: 'none' });
      return;
    }

    if (!this.data.selectedCondition && !this.data.problem) {
      wx.showToast({ title: '请选择病情或填写问题描述', icon: 'none' });
      return;
    }

    showLoading('提交中...');

    try {
      // 组合病情标签和问题描述
      var fullProblem = '';
      if (this.data.selectedCondition) {
        fullProblem += '【' + this.data.selectedCondition + '】';
      }
      if (this.data.problem) {
        fullProblem += (fullProblem ? ' ' : '') + this.data.problem;
      }

      const appointmentData = {
        counselorId: this.data.counselor.id,
        serviceType: this.data.serviceType,
        appointmentTime: `${this.data.selectedDate}T${this.data.selectedTime}:00`,
        problem: fullProblem
      };

      await post('/api/appointment/create', appointmentData);
      
      hideLoading();
      showToast('预约成功', 'success');
      this.setData({ 
        showBookForm: false,
        problem: '',
        selectedCondition: '',
        selectedDate: '',
        selectedTime: ''
      });
      
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    } catch (err) {
      hideLoading();
      console.error('预约失败:', err);
      showToast(err.msg || '预约失败，请稍后重试', 'none');
    }
  },

  formatDate(date) {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
  },

  formatReviewTime(timeStr) {
    if (!timeStr) return '';
    const date = new Date(timeStr);
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}`;
  },

  generateTimeSlots(startTime, endTime, interval) {
    const slots = [];
    const [startH, startM] = startTime.split(':').map(Number);
    const [endH, endM] = endTime.split(':').map(Number);
    let current = startH * 60 + startM;
    const end = endH * 60 + endM;
    while (current < end) {
      const h = Math.floor(current / 60);
      const m = current % 60;
      slots.push(`${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}`);
      current += interval;
    }
    return slots;
  }
});
