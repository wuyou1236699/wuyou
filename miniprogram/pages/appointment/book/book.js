const request = require('../../../utils/request.js');
const app = getApp();

Page({
  data: {
    activeTab: 'condition',
    problemTypes: ['抑郁', '焦虑', '婚恋', '情绪压力', '青少年', '职场', '人际关系', '睡眠障碍'],
    selectedProblem: '',
    counselors: [],
    filteredCounselors: [],
    matchedList: [],
    matchDone: false,
    timeMatchedList: [],
    timeMatchDone: false,
    keyword: '',
    selectedDate: '',
    selectedPeriod: '',
    loading: false
  },

  onLoad() {
    if (!app.checkLogin()) {
      wx.reLaunch({ url: '/pages/login/login' });
      return;
    }
    this.loadCounselors();
    this.initDate();
  },

  initDate() {
    const today = new Date();
    const y = today.getFullYear();
    const m = String(today.getMonth() + 1).padStart(2, '0');
    const d = String(today.getDate()).padStart(2, '0');
    this.setData({ selectedDate: `${y}-${m}-${d}` });
  },

  async loadCounselors() {
    this.setData({ loading: true });
    try {
      const res = await request.get('/api/counselor/list?page=1&size=100', false);
      const list = (res.records || []).map(function(c) {
        c.displayPrice = c.price != null ? '¥' + c.price + '/次' : '面议';
        return c;
      });
      this.setData({ counselors: list, filteredCounselors: list });
    } catch (err) {
      console.warn('加载咨询师失败:', err);
    } finally {
      this.setData({ loading: false });
    }
  },

  switchTab(e) {
    this.setData({ activeTab: e.currentTarget.dataset.tab });
  },

  // ===== 按病情匹配 =====
  selectProblem(e) {
    this.setData({ selectedProblem: e.currentTarget.dataset.type });
  },

  async matchByProblem() {
    const type = this.data.selectedProblem;
    if (!type) return;
    this.setData({ loading: true });
    try {
      const res = await request.get('/api/counselors/recommend?problemType=' + encodeURIComponent(type), false);
      const raw = Array.isArray(res) ? res : (res.data || res || []);
      const list = raw.map(function(c) {
        c.displayPrice = c.price != null ? '¥' + c.price + '/次' : '面议';
        return c;
      });
      this.setData({ matchedList: list, matchDone: true, loading: false });
    } catch (err) {
      console.warn('匹配失败:', err);
      this.setData({ loading: false });
      request.showToast('匹配失败，请重试');
    }
  },

  // ===== 按咨询师预约 =====
  onKeywordInput(e) {
    const keyword = e.detail.value.trim();
    this.setData({ keyword });
    if (!keyword) {
      this.setData({ filteredCounselors: this.data.counselors });
      return;
    }
    const lower = keyword.toLowerCase();
    const filtered = this.data.counselors.filter(c =>
      (c.name && c.name.includes(keyword)) ||
      (c.expertise && c.expertise.toLowerCase().includes(lower))
    );
    this.setData({ filteredCounselors: filtered });
  },

  // ===== 按时间预约 =====
  onDateChange(e) {
    this.setData({ selectedDate: e.detail.value });
  },

  selectPeriod(e) {
    this.setData({ selectedPeriod: e.currentTarget.dataset.period });
  },

  async matchByTime() {
    const { selectedDate, selectedPeriod } = this.data;
    if (!selectedDate || !selectedPeriod) return;

    this.setData({ loading: true });
    try {
      // 根据时段映射时间范围
      const periodMap = { morning: ['09:00', '12:00'], afternoon: ['14:00', '18:00'], evening: ['18:00', '21:00'] };
      const range = periodMap[selectedPeriod];
      const allSchedules = await request.get(
        `/api/counselor/schedule?counselorId=0&startDate=${selectedDate}&endDate=${selectedDate}`,
        false
      ).catch(() => []);

      const availableIds = new Set();
      (allSchedules || []).forEach(s => {
        const st = s.startTime ? String(s.startTime).substring(0, 5) : '';
        if (st >= range[0] && st < range[1]) {
          availableIds.add(s.counselorId);
        }
      });

      const list = this.data.counselors.filter(c => availableIds.has(c.id));
      this.setData({ timeMatchedList: list, timeMatchDone: true });
    } catch (err) {
      console.warn('匹配失败:', err);
    } finally {
      this.setData({ loading: false });
    }
  },

  // ===== 通用 =====
  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/counselor/detail/detail?id=${id}` });
  }
});
