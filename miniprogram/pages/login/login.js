const app = getApp();

Page({
  data: {
    username: '',
    password: ''
  },

  onLoad() {
    const token = wx.getStorageSync('token');
    if (token) {
      wx.reLaunch({ url: '/pages/index/index' });
    }
  },

  onUsernameInput(e) {
    this.setData({ username: e.detail.value });
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value });
  },

  handleLogin() {
    const { username, password } = this.data;

    if (!username || !password) {
      wx.showToast({ title: '请输入用户名和密码', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '登录中...' });

    wx.request({
      url: app.globalData.BASE_URL + '/api/user/login',
      method: 'POST',
      data: { username, password },
      header: { 'Content-Type': 'application/json' },
      success: (res) => {
        wx.hideLoading();
        if (res.data.code === 200) {
          const data = res.data.data;
          wx.setStorageSync('token', data.token);
          wx.setStorageSync('userId', data.userId);
          wx.setStorageSync('username', data.username);
          wx.setStorageSync('nickname', data.nickname);
          wx.showToast({ title: '登录成功', icon: 'success' });
          setTimeout(() => {
            wx.reLaunch({ url: '/pages/index/index' });
          }, 1500);
        } else {
          wx.showToast({ title: res.data.msg || '登录失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({ title: '网络请求失败', icon: 'none' });
      }
    });
  },

  goToRegister() {
    wx.navigateTo({ url: '/pages/register/register' });
  }
});
