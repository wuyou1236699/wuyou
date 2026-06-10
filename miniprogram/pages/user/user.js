const request = require('../../utils/request.js');

Page({
  data: {
    userInfo: null,
    loading: true
  },

  onShow() {
    this.loadUserInfo();
  },

  async loadUserInfo() {
    this.setData({ loading: true });

    try {
      const data = await request.get('/api/user/info');
      this.setData({ userInfo: data });
    } catch (err) {
      console.warn('加载用户信息失败:', err);
      request.showToast('加载用户信息失败');
    } finally {
      this.setData({ loading: false });
    }
  },

  goToMyAppointments() {
    wx.navigateTo({ url: '/pages/appointment/my/my' });
  },

  goToMyConsultations() {
    wx.navigateTo({ url: '/pages/consultation-record/consultation-record' });
  },

  goToMyReports() {
    wx.navigateTo({ url: '/pages/test/history' });
  },

  onAvatarTap() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFiles[0].tempFilePath;
        wx.showLoading({ title: '上传中...', mask: true });
        request.upload(tempFilePath).then(uploadData => {
          const avatarUrl = uploadData.url;
          return request.put('/api/user/profile', { avatar: avatarUrl }).then(() => avatarUrl);
        }).then(avatarUrl => {
          wx.hideLoading();
          wx.showToast({ title: '头像更新成功', icon: 'success' });
          const updatedInfo = { ...this.data.userInfo, avatar: avatarUrl };
          this.setData({ userInfo: updatedInfo });
        }).catch(err => {
          wx.hideLoading();
          console.warn('头像上传失败:', err);
          wx.showToast({ title: err.msg || '上传失败', icon: 'none' });
        });
      }
    });
  },

  logout() {
    wx.showModal({
      title: '退出登录',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync('token');
          wx.removeStorageSync('userId');
          wx.removeStorageSync('username');
          wx.removeStorageSync('nickname');
          wx.reLaunch({ url: '/pages/login/login' });
        }
      }
    });
  }
});
