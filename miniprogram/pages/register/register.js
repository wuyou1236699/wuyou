const app = getApp();

Page({
  data: {
    username: '',
    nickname: '',
    password: '',
    confirmPassword: ''
  },

  onUsernameInput(e) {
    this.setData({ username: e.detail.value });
  },

  onNicknameInput(e) {
    this.setData({ nickname: e.detail.value });
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value });
  },

  onConfirmPasswordInput(e) {
    this.setData({ confirmPassword: e.detail.value });
  },

  handleRegister() {
    const { username, nickname, password, confirmPassword } = this.data;

    if (!username || !password) {
      wx.showToast({ title: '请输入用户名和密码', icon: 'none' });
      return;
    }

    if (password !== confirmPassword) {
      wx.showToast({ title: '两次密码输入不一致', icon: 'none' });
      return;
    }

    if (password.length < 6) {
      wx.showToast({ title: '密码至少6位', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '注册中...' });

    wx.request({
      url: app.globalData.BASE_URL + '/api/user/register',
      method: 'POST',
      data: { username, nickname, password },
      header: { 'Content-Type': 'application/json' },
      success: (res) => {
        wx.hideLoading();
        if (res.data.code === 200) {
          const data = res.data.data;
          wx.setStorageSync('token', data.token);
          wx.setStorageSync('userId', data.userId);
          wx.setStorageSync('username', data.username);
          wx.setStorageSync('nickname', data.nickname);
          wx.showToast({ title: '注册成功', icon: 'success' });
          setTimeout(() => {
            wx.reLaunch({ url: '/pages/index/index' });
          }, 1500);
        } else {
          wx.showToast({ title: res.data.msg || '注册失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({ title: '网络请求失败', icon: 'none' });
      }
    });
  },

  goToLogin() {
    wx.navigateBack();
  }
});
