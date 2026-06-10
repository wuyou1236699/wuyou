const app = getApp();

const MAX_RETRY = 2;
const TIMEOUT = 45000;

const request = (url, method, data, needAuth = true, retryCount = 0) => {
  return new Promise((resolve, reject) => {
    const header = { 'Content-Type': 'application/json' };
    if (needAuth) {
      const token = wx.getStorageSync('token');
      if (!token) {
        reject({ code: 401, msg: '未登录，请先登录' });
        return;
      }
      header['Authorization'] = `Bearer ${token}`;
    }
    
    doRequest();
    
    function doRequest() {
      wx.request({
        url: app.globalData.BASE_URL + url,
        method,
        data,
        header,
        timeout: TIMEOUT,
        success(res) {
          if (res.statusCode === 200) {
            if (res.data.code === 200) {
              resolve(res.data.data);
            } else if (res.data.code === 401) {
              if (retryCount < MAX_RETRY) {
                wx.removeStorageSync('token');
                request(url, method, data, needAuth, retryCount + 1)
                  .then(resolve)
                  .catch(reject);
              } else {
                wx.removeStorageSync('token');
                wx.removeStorageSync('userInfo');
                wx.reLaunch({ url: '/pages/login/login' });
                reject({ code: 401, msg: '登录已过期，请重新登录' });
              }
            } else {
              reject(res.data);
            }
          } else {
            reject({ code: res.statusCode, msg: '请求失败' });
          }
        },
        fail(err) {
          console.warn('网络请求失败:', err);
          
          if (retryCount < MAX_RETRY) {
            console.log(`重试第 ${retryCount + 1} 次...`);
            setTimeout(() => {
              request(url, method, data, needAuth, retryCount + 1)
                .then(resolve)
                .catch(reject);
            }, 1000 * (retryCount + 1));
          } else {
            const isTimeout = err.errMsg && err.errMsg.includes('timeout');
            const isConnectionFailed = err.errMsg && err.errMsg.includes('fail');
            
            if (isTimeout) {
              reject({ code: -1, msg: '服务器响应超时，已重试2次，请稍后再试' });
            } else if (isConnectionFailed) {
              reject({ code: -2, msg: '无法连接到服务器，请检查网络' });
            } else {
              reject({ code: -3, msg: '网络请求失败，请稍后重试' });
            }
          }
        }
      });
    }
  });
};

const get = (url, data, needAuth = true) => {
  if (typeof data === 'boolean') {
    needAuth = data;
    data = null;
  }
  
  let fullUrl = url;
  if (data && typeof data === 'object') {
    const params = Object.keys(data)
      .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(data[key])}`)
      .join('&');
    fullUrl += (url.includes('?') ? '&' : '?') + params;
  }
  
  return request(fullUrl, 'GET', null, needAuth);
};

const post = (url, data, needAuth = true) => request(url, 'POST', data, needAuth);
const put = (url, data, needAuth = true) => request(url, 'PUT', data, needAuth);
const del = (url, needAuth = true) => request(url, 'DELETE', null, needAuth);

const upload = (filePath) => {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token');
    if (!token) {
      reject({ code: 401, msg: '未登录，请先登录' });
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

const formatDate = (date) => {
  if (!date) return '';
  const d = new Date(date);
  const year = d.getFullYear();
  const month = (d.getMonth() + 1).toString().padStart(2, '0');
  const day = d.getDate().toString().padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const formatDateTime = (date) => {
  if (!date) return '';
  const d = new Date(date);
  const year = d.getFullYear();
  const month = (d.getMonth() + 1).toString().padStart(2, '0');
  const day = d.getDate().toString().padStart(2, '0');
  const hour = d.getHours().toString().padStart(2, '0');
  const minute = d.getMinutes().toString().padStart(2, '0');
  return `${year}-${month}-${day} ${hour}:${minute}`;
};

const formatTime = (date) => {
  if (!date) return '';
  const d = new Date(date);
  const hours = d.getHours().toString().padStart(2, '0');
  const minutes = d.getMinutes().toString().padStart(2, '0');
  return `${hours}:${minutes}`;
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

module.exports = {
  request,
  get,
  post,
  put,
  del,
  upload,
  formatDate,
  formatDateTime,
  formatTime,
  showToast,
  showLoading,
  hideLoading
};
