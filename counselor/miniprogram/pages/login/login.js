const auth = require('../../utils/auth.js');

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

  async handleLogin() {
    const { username, password } = this.data;

    if (!username || !password) {
      wx.showToast({ title: '请输入用户名和密码', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '登录中...' });

    try {
      const res = await new Promise((resolve, reject) => {
        wx.request({
          url: app.globalData.BASE_URL + '/api/counselor/login',
          method: 'POST',
          data: { username, password },
          header: { 'Content-Type': 'application/json' },
          success(result) {
            if (result.data.code === 200) {
              resolve(result.data.data);
            } else {
              reject(result.data);
            }
          },
          fail(err) {
            reject({ msg: '网络请求失败' });
          }
        });
      });

      wx.setStorageSync('token', res.token);
      const counselorInfo = res.counselor || res;
      wx.setStorageSync('counselorInfo', counselorInfo);
      wx.setStorageSync('userInfo', counselorInfo);
      auth.setRole('counselor');

      wx.showToast({ title: '登录成功', icon: 'success' });

      setTimeout(() => {
        wx.reLaunch({ url: '/pages/index/index' });
      }, 1500);
    } catch (err) {
      wx.hideLoading();
      wx.showToast({ title: err.msg || '登录失败', icon: 'none' });
    }
  }
});