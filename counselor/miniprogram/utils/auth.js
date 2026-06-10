const ROLES = {
  ADMIN: 'admin',
  COUNSELOR: 'counselor'
};

const auth = {
  ROLES,

  isLoggedIn() {
    return !!wx.getStorageSync('userInfo');
  },

  getRole() {
    return wx.getStorageSync('role') || ROLES.COUNSELOR;
  },

  setRole(role) {
    wx.setStorageSync('role', role);
  },

  getUserInfo() {
    return wx.getStorageSync('userInfo');
  },

  logout() {
    wx.removeStorageSync('userInfo');
    wx.removeStorageSync('role');
    wx.removeStorageSync('token');
    wx.removeStorageSync('counselorInfo');
  }
};

module.exports = auth;
