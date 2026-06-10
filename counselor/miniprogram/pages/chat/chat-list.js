const request = require('../../utils/request.js');

Page({
  data: {
    chatList: [],
    searchKey: '',
    filterType: 'all',
    conditionFilter: 'all',
    filteredChatList: [],
    totalUnread: 0,
    totalRead: 0,
    loading: true
  },

  onLoad: function() {
    this.loadData();
  },

  onShow: function() {
    this.loadData();
  },

  async loadData() {
    this.setData({ loading: true });
    try {
      await this.loadChatList();
      this.updateStats();
      this.updateFilteredList();
    } catch (err) {
      console.error('加载数据失败:', err);
      request.showToast(err.msg || '加载失败');
    } finally {
      this.setData({ loading: false });
    }
  },

  async loadChatList() {
    try {
      const data = await request.get('/api/chat/list');
      var list = (data && data.data) ? data.data : (data || []);
      // Add initial letter for avatar placeholder
      list = list.map(function(item) {
        var name = item.userName || '';
        item.initial = name.charAt(0) || '用';
        return item;
      });
      this.setData({ chatList: list });
    } catch (err) {
      console.warn('获取聊天列表失败:', err);
      this.setData({ chatList: [] });
    }
  },

  updateStats: function() {
    var list = this.data.chatList;
    var unreadConv = 0;

    for (var i = 0; i < list.length; i++) {
      if ((list[i].unreadCount || 0) > 0) {
        unreadConv++;
      }
    }
    var readConv = list.length - unreadConv;

    this.setData({
      totalUnread: unreadConv,
      totalRead: readConv
    });
  },

  updateFilteredList: function() {
    var list = this.data.chatList;
    var searchKey = this.data.searchKey;
    var filterType = this.data.filterType;
    var conditionFilter = this.data.conditionFilter;

    var filtered = list.filter(function(item) {
      var matchSearch = !searchKey || (item.userName && item.userName.indexOf(searchKey) !== -1) || (item.lastMessage && item.lastMessage.indexOf(searchKey) !== -1);
      var matchFilter = true;
      var matchCondition = conditionFilter === 'all' || (item.condition || '其他') === conditionFilter;

      if (filterType === 'read') {
        matchFilter = (item.unreadCount || 0) === 0;
      } else if (filterType === 'unread') {
        matchFilter = (item.unreadCount || 0) > 0;
      }

      return matchSearch && matchFilter && matchCondition;
    });

    this.setData({ filteredChatList: filtered });
  },

  onSearch: function(e) {
    this.setData({ searchKey: e.detail.value });
    this.updateFilteredList();
  },

  clearSearch: function() {
    this.setData({ searchKey: '' });
    this.updateFilteredList();
  },

  switchFilter: function(e) {
    var type = e.currentTarget.dataset.type;
    this.setData({ filterType: type });
    this.updateFilteredList();
  },

  switchCondition: function(e) {
    var cond = e.currentTarget.dataset.cond;
    this.setData({ conditionFilter: cond });
    this.updateFilteredList();
  },

  enterChat: function(e) {
    var userId = e.currentTarget.dataset.userid;
    var userName = e.currentTarget.dataset.username;
    var appointmentId = e.currentTarget.dataset.appointmentid || '';
    var isDirect = e.currentTarget.dataset.isdirect;

    var url = '/pages/chat/chat-detail?userId=' + userId + '&userName=' + encodeURIComponent(userName || '');
    if (appointmentId) {
      url += '&appointmentId=' + appointmentId;
    }
    wx.navigateTo({ url: url });
  },

  onPullDownRefresh() {
    this.loadData();
    wx.stopPullDownRefresh();
  }
});
