const request = require('../../utils/request.js');

Page({
  data: {
    record: {},
    loading: true,
    isEditing: false,
    isNew: false
  },

  onLoad(options) {
    if (options.id) {
      this.loadRecord(options.id);
    } else if (options.appointmentId) {
      this.initFromAppointment(options.appointmentId);
    }
  },

  async initFromAppointment(appointmentId) {
    this.setData({ loading: true });
    try {
      const appt = await request.get(`/api/appointments/${appointmentId}`);
      const serviceMap = {1: '电话咨询', 2: '网络咨询', 3: '门诊咨询'};
      this.setData({
        record: {
          appointmentId: appt.id,
          userId: appt.userId,
          counselorId: appt.counselorId,
          userName: appt.userName,
          serviceType: appt.serviceType,
          serviceTypeText: serviceMap[appt.serviceType] || '未知',
          problem: appt.problem || '',
          diagnosis: '',
          suggestions: '',
          duration: 30,
          fee: 0,
          status: 0
        },
        isNew: true,
        isEditing: true
      });
    } catch (err) {
      request.showToast('加载预约信息失败');
    } finally {
      this.setData({ loading: false });
    }
  },

  async loadRecord(id) {
    this.setData({ loading: true });
    try {
      const data = await request.get(`/api/records/${id}`);
      const serviceMap = {1: '电话咨询', 2: '网络咨询', 3: '门诊咨询'};
      this.setData({ record: { ...data, serviceTypeText: serviceMap[data.serviceType] || '未知' } });
    } catch (err) {
      console.warn('获取记录详情失败:', err);
      this.setData({ record: {} });
    } finally {
      this.setData({ loading: false });
    }
  },

  toggleEdit() {
    this.setData({ isEditing: !this.data.isEditing });
  },

  async saveRecord() {
    try {
      const record = { ...this.data.record };
      if (this.data.isNew) {
        record.id = null;
      }
      console.log('保存咨询记录，数据:', JSON.stringify(record, null, 2));
      const result = await request.post('/api/records', record);
      console.log('保存成功，返回:', result);
      request.showToast('保存成功', 'success');
      this.setData({ isEditing: false, isNew: false });
    } catch (err) {
      console.error('保存失败，错误:', JSON.stringify(err));
      if (err && err.msg) {
        request.showToast('保存失败: ' + err.msg);
      } else {
        request.showToast('保存失败');
      }
    }
  },

  onDiagnosisInput(e) {
    this.setData({ 'record.diagnosis': e.detail.value });
  },

  onSuggestionsInput(e) {
    this.setData({ 'record.suggestions': e.detail.value });
  },

  onDurationInput(e) {
    this.setData({ 'record.duration': parseInt(e.detail.value) || 0 });
  },

  onFeeInput(e) {
    this.setData({ 'record.fee': parseFloat(e.detail.value) || 0 });
  },

  async markCompleted() {
    try {
      const record = { ...this.data.record };
      record.status = 1;
      if (this.data.isNew) {
        record.id = null;
      }
      await request.post('/api/records', record);
      request.showToast('已标记为完成', 'success');
      this.setData({
        isEditing: false,
        isNew: false,
        record: record
      });
    } catch (err) {
      request.showToast('操作失败');
    }
  }
})
