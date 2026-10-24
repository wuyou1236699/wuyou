App({
  onLaunch() {
    console.log('用户端小程序启动');
    // 检测当前环境，自动切换 BASE_URL
    const accountInfo = wx.getAccountInfoSync();
    const envVersion = accountInfo.miniProgram.envVersion;
    if (envVersion === 'develop') {
      // 开发版：优先使用开发者工具 localhost
      // 真机调试时需改为电脑局域网IP，如 http://192.168.x.x:8080
      this.globalData.BASE_URL = 'http://localhost:8080';
    } else if (envVersion === 'trial') {
      // 体验版：使用测试服务器地址
      this.globalData.BASE_URL = 'http://localhost:8080';
    } else {
      // 正式版：使用生产服务器地址（发布前需替换为真实域名）
      this.globalData.BASE_URL = 'https://your-domain.com';
    }
  },

  globalData: {
    BASE_URL: 'http://localhost:8080',
    userInfo: null
  },

  checkLogin() {
    const token = wx.getStorageSync('token');
    return !!token;
  }
});
