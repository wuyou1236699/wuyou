const app = getApp();

const request = (url, method, data, needAuth = true) => {
  return new Promise((resolve, reject) => {
    const header = { 'Content-Type': 'application/json' };
    if (needAuth) {
      const token = wx.getStorageSync('token');
      if (!token) {
        reject({ code: 401, msg: '未登录' });
        return;
      }
      header['Authorization'] = `Bearer ${token}`;
    }
    wx.request({
      url: app.globalData.BASE_URL + url,
      method,
      data,
      header,
      timeout: 30000,
      success(res) {
        if (res.statusCode === 200) {
          if (res.data.code === 200) {
            resolve(res.data.data);
          } else if (res.data.code === 401) {
            wx.removeStorageSync('token');
            wx.removeStorageSync('userInfo');
            wx.removeStorageSync('counselorInfo');
            wx.reLaunch({ url: '/pages/login/login' });
            reject(res.data);
          } else {
            reject(res.data);
          }
        } else {
          reject({ code: res.statusCode, msg: '请求失败' });
        }
      },
      fail(err) {
        console.warn('网络请求失败:', err);
        reject({ code: -1, msg: '网络异常，请稍后重试' });
      }
    });
  });
};

const get = (url, params, needAuth = true) => {
  if (params && typeof params === 'object') {
    const query = Object.keys(params)
      .filter(k => params[k] !== undefined && params[k] !== null)
      .map(k => encodeURIComponent(k) + '=' + encodeURIComponent(params[k]))
      .join('&');
    if (query) url += (url.includes('?') ? '&' : '?') + query;
    return request(url, 'GET', null, needAuth);
  }
  if (typeof params === 'boolean') {
    return request(url, 'GET', null, params);
  }
  return request(url, 'GET', null, needAuth);
};
const post = (url, data, needAuth = true) => request(url, 'POST', data, needAuth);
const put = (url, data, needAuth = true) => request(url, 'PUT', data, needAuth);
const del = (url, needAuth = true) => request(url, 'DELETE', null, needAuth);

const upload = (filePath) => {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token');
    if (!token) {
      reject({ code: 401, msg: '未登录' });
      return;
    }
    wx.uploadFile({
      url: app.globalData.BASE_URL + '/api/common/upload',
      filePath: filePath,
      name: 'file',
      header: {
        'Authorization': 'Bearer ' + token
      },
      timeout: 30000,
      success(res) {
        if (res.statusCode === 200) {
          try {
            const data = JSON.parse(res.data);
            if (data.code === 200) {
              resolve(data.data);
            } else if (data.code === 401) {
              wx.removeStorageSync('token');
              wx.removeStorageSync('userInfo');
              wx.removeStorageSync('counselorInfo');
              wx.reLaunch({ url: '/pages/login/login' });
              reject(data);
            } else {
              reject(data);
            }
          } catch (e) {
            reject({ code: -1, msg: '解析响应失败' });
          }
        } else {
          reject({ code: res.statusCode, msg: '上传失败' });
        }
      },
      fail(err) {
        reject({ code: -1, msg: '网络异常' });
      }
    });
  });
};

const showToast = (title, icon = 'none') => {
  wx.showToast({ title, icon, duration: 2000 });
};

const showLoading = (title = '加载中...') => {
  wx.showLoading({ title, mask: true });
};

const hideLoading = () => {
  wx.hideLoading();
};

const formatDate = (dateStr) => {
  const date = new Date(dateStr);
  const today = new Date();
  const tomorrow = new Date(today);
  tomorrow.setDate(tomorrow.getDate() + 1);
  
  if (date.toDateString() === today.toDateString()) {
    return '今天';
  } else if (date.toDateString() === tomorrow.toDateString()) {
    return '明天';
  } else {
    return `${date.getMonth() + 1}月${date.getDate()}日`;
  }
};

const formatTime = (timeStr) => {
  if (!timeStr) return timeStr;
  if (timeStr.includes('T')) {
    const d = new Date(timeStr);
    if (isNaN(d.getTime())) return timeStr;
    return `${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}`;
  }
  if (timeStr.includes('-') && timeStr.length <= 11) {
    return timeStr.split('-')[0];
  }
  return timeStr;
};

module.exports = {
  request,
  get,
  post,
  put,
  del,
  upload,
  showToast,
  showLoading,
  hideLoading,
  formatDate,
  formatTime
};
