const auth = require('../../utils/auth.js');
const request = require('../../utils/request.js');

Page({
  data: {
    userInfo: {},
    stats: [],
    menuItems: [],
    loading: true,
    editing: false,
    editPhone: '',
    editEmail: '',
    maskedPhone: ''
  },

  maskPhone(phone) {
    if (!phone || phone.length < 8) return phone || '';
    return phone.substring(0, 3) + '****' + phone.substring(7);
  },

  onLoad() {
    this.loadUserInfo();
  },

  async loadUserInfo() {
    this.setData({ loading: true });
    const userInfo = wx.getStorageSync('userInfo') || {};

    try {
      const data = await request.get(`/api/counselors/${userInfo.id}`);
      this.setData({
        maskedPhone: this.maskPhone(data.phone),
        userInfo: {
          ...userInfo,
          ...data,
          avatar: data.avatar || '/images/default-avatar.png'
        },
        stats: [
          { label: '累计咨询', value: `${data.totalConsultations || 0}次`, icon: '💬' },
          { label: '好评率', value: `${data.positiveRate || 0}%`, icon: '⭐' },
          { label: '在线时长', value: `${data.onlineHours || 0}小时`, icon: '⏱️' },
          { label: '预约人数', value: `${data.appointmentCount || 0}人`, icon: '📅' }
        ],
        menuItems: [
          { label: '我的排班', icon: '📋', path: '/pages/schedule/schedule' },
          { label: '查看评价', icon: '⭐', path: '/pages/reviews/reviews' }
        ]
      });
    } catch (err) {
      console.warn('获取用户信息失败:', err);
      this.setData({
        userInfo: {
          ...userInfo,
          avatar: '/images/default-avatar.png'
        },
        stats: [
          { label: '累计咨询', value: '0次', icon: '💬' },
          { label: '好评率', value: '0%', icon: '⭐' },
          { label: '在线时长', value: '0小时', icon: '⏱️' },
          { label: '预约人数', value: '0人', icon: '📅' }
        ],
        menuItems: [
          { label: '我的排班', icon: '📋', path: '/pages/schedule/schedule' },
          { label: '查看评价', icon: '⭐', path: '/pages/reviews/reviews' }
        ]
      });
    } finally {
      this.setData({ loading: false });
    }
  },

  goToPage(e) {
    const path = e.currentTarget.dataset.path;
    if (path) {
      wx.navigateTo({ url: path });
    } else {
      wx.showToast({ title: '功能开发中', icon: 'none' });
    }
  },

  editProfile() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFiles[0].tempFilePath;
        wx.showLoading({ title: '上传中...', mask: true });
        request.upload(tempFilePath).then(uploadData => {
          const avatarUrl = uploadData.url;
          return request.put('/api/counselor/profile', { avatar: avatarUrl }).then(() => avatarUrl);
        }).then(avatarUrl => {
          wx.hideLoading();
          wx.showToast({ title: '头像更新成功', icon: 'success' });
          const updatedUserInfo = { ...this.data.userInfo, avatar: avatarUrl };
          this.setData({ userInfo: updatedUserInfo });
          wx.setStorageSync('userInfo', updatedUserInfo);
        }).catch(err => {
          wx.hideLoading();
          console.warn('头像上传失败:', err);
          wx.showToast({ title: err.msg || '上传失败', icon: 'none' });
        });
      }
    });
  },

  startEditContact() {
    this.setData({
      editing: true,
      editPhone: this.data.userInfo.phone || '',
      editEmail: this.data.userInfo.email || ''
    });
  },

  cancelEditContact() {
    this.setData({ editing: false });
  },

  onPhoneInput(e) {
    this.setData({ editPhone: e.detail.value });
  },

  onEmailInput(e) {
    this.setData({ editEmail: e.detail.value });
  },

  async saveContact() {
    try {
      await request.put('/api/counselor/profile', {
        phone: this.data.editPhone,
        email: this.data.editEmail
      });
      const userInfo = { ...this.data.userInfo, phone: this.data.editPhone, email: this.data.editEmail };
      this.setData({ userInfo, maskedPhone: this.maskPhone(this.data.editPhone), editing: false });
      wx.showToast({ title: '保存成功', icon: 'success' });
    } catch (err) {
      wx.showToast({ title: '保存失败', icon: 'none' });
    }
  },

  logout() {
    wx.showModal({
      title: '退出登录',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          auth.logout();
          wx.reLaunch({ url: '/pages/login/login' });
        }
      }
    });
  }
})