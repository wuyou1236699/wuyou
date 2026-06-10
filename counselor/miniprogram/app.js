const auth = require('./utils/auth.js');

App({
  onLaunch() {
    console.log('咨询师端小程序启动');
    try {
      const accountInfo = wx.getAccountInfoSync();
      const envVersion = accountInfo.miniProgram.envVersion;
      if (envVersion === 'release') {
        this.globalData.BASE_URL = ''; // 上线前替换为正式域名，如 https://api.example.com
      }
    } catch (e) {
      console.warn('获取环境信息失败，使用默认地址');
    }
  },

  globalData: {
    BASE_URL: 'http://localhost:8080',
    userInfo: null
  }
});
