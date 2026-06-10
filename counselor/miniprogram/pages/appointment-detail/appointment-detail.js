const request = require('../../utils/request.js');

Page({
  data: {
    appointmentId: '',
    appointment: {},
    loading: true
  },

  onLoad: function(options) {
    this.setData({
      appointmentId: options.id
    });
    this.loadAppointmentDetail();
  },

  async loadAppointmentDetail() {
    this.setData({ loading: true });
    try {
      const data = await request.get(`/api/appointments/${this.data.appointmentId}`);
      if (data) {
        const statusMap = { 0: '待确认', 1: '已确认', 2: '已完成', 3: '已取消' };
        const serviceMap = { 1: '电话咨询', 2: '网络咨询', 3: '门诊咨询' };

        this.setData({
          appointment: {
            ...data,
            statusText: statusMap[data.status] || '未知',
            serviceTypeText: serviceMap[data.serviceType] || '未知',
            dateText: data.appointmentTime ? this.formatDate(data.appointmentTime) : '',
            timeText: data.appointmentTime ? this.formatTime(data.appointmentTime) : ''
          }
        });
      }
    } catch (err) {
      console.error('获取预约详情失败:', err);
      request.showToast('获取详情失败');
    } finally {
      this.setData({ loading: false });
    }
  },

  formatDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  },

  formatTime(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
  },

  startConsultation() {
    const appointment = this.data.appointment;
    wx.showModal({
      title: '开始咨询',
      content: `确认与${appointment.userName}开始咨询？`,
      confirmText: '开始',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm) {
          this.doStartConsultation(appointment);
        }
      }
    });
  },

  doStartConsultation(appointment) {
    wx.navigateTo({
      url: `/pages/chat/chat-detail?userId=${appointment.userId}&userName=${encodeURIComponent(appointment.userName || '用户')}&appointmentId=${this.data.appointmentId}`
    });
  },

  async completeConsultation() {
    wx.showModal({
      title: '完成咨询',
      content: '确定要完成这次咨询吗？完成后将生成咨询记录。',
      success: async (res) => {
        if (res.confirm) {
          try {
            await request.put(`/api/appointments/${this.data.appointmentId}/complete`);
            request.showToast('咨询已完成', 'success');
            setTimeout(() => {
              this.loadAppointmentDetail();
            }, 1500);
          } catch (err) {
            request.showToast('操作失败');
          }
        }
      }
    });
  },

  async cancelAppointment() {
    wx.showModal({
      title: '取消预约',
      content: '确定要取消这个预约吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await request.put(`/api/appointments/${this.data.appointmentId}/cancel`);
            request.showToast('预约已取消', 'success');
            setTimeout(() => {
              wx.navigateBack();
            }, 1500);
          } catch (err) {
            request.showToast('操作失败');
          }
        }
      }
    });
  }
});