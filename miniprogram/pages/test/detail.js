const request = require('../../utils/request.js');
const app = getApp();

Page({
  data: {
    testId: null,
    testTitle: '',
    questions: [],
    currentIndex: 0,
    answers: {},
    loading: true,
    showResult: false,
    result: {},
    resultColor: '#10B981'
  },

  onLoad(options) {
    if (!app.checkLogin()) {
      wx.reLaunch({ url: '/pages/login/login' });
      return;
    }
    const id = options.id;
    const title = options.title ? decodeURIComponent(options.title) : '心理测评';

    this.setData({ testId: id, testTitle: title });
    this.loadQuestions(id);
  },

  async loadQuestions(id) {
    this.setData({ loading: true });
    
    try {
      const data = await request.get(`/api/tests/${id}/questions`);
      this.setData({ questions: data });
    } catch (err) {
      console.warn('加载题目失败:', err);
      request.showToast('加载题目失败，请检查网络');
    } finally {
      this.setData({ loading: false });
    }
  },

  onOptionSelect(e) {
    const questionId = e.currentTarget.dataset.questionId;
    const optionId = e.currentTarget.dataset.optionId;
    
    const answers = { ...this.data.answers };
    answers[questionId] = optionId;
    this.setData({ answers });
  },

  nextQuestion() {
    if (this.data.currentIndex < this.data.questions.length - 1) {
      this.setData({ currentIndex: this.data.currentIndex + 1 });
    }
  },

  prevQuestion() {
    if (this.data.currentIndex > 0) {
      this.setData({ currentIndex: this.data.currentIndex - 1 });
    }
  },

  goToQuestion(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({ currentIndex: index });
  },

  async submitTest() {
    const answeredCount = Object.keys(this.data.answers).length;
    if (answeredCount < this.data.questions.length) {
      wx.showToast({ title: '请完成所有题目', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '提交中...' });

    try {
      const response = await request.post('/api/tests/submit', {
        testId: this.data.testId,
        testName: this.data.testTitle,
        answers: this.data.answers
      });

      wx.hideLoading();

      const levelColorMap = {
        '正常': '#10B981',
        '轻度': '#3B82F6',
        '中度': '#F59E0B',
        '重度': '#EF4444'
      };

      this.setData({
        showResult: true,
        resultColor: levelColorMap[response.level] || '#10B981',
        result: {
          score: response.score,
          level: response.level,
          advice: response.suggestions,
          testTitle: this.data.testTitle,
          maxScore: response.maxScore
        }
      });
    } catch (err) {
      wx.hideLoading();
      console.warn('提交失败:', err);
      this.calculateResult();
    }
  },

  calculateResult() {
    let totalScore = 0;
    const questions = this.data.questions;
    const maxScore = questions.length * 5;

    questions.forEach(q => {
      const selectedOptionId = this.data.answers[q.id];
      if (selectedOptionId) {
        const option = q.options.find(opt => opt.id === selectedOptionId);
        if (option) {
          totalScore += option.score;
        }
      }
    });

    let level, advice, resultColor;
    const ratio = totalScore / maxScore;

    if (ratio <= 0.25) {
      level = '正常';
      advice = '您的心理状态良好！继续保持健康的生活方式和积极的心态。';
      resultColor = '#10B981';
    } else if (ratio <= 0.40) {
      level = '轻度';
      advice = '您可能有轻微的心理困扰，可以尝试通过运动、冥想等方式进行自我调节。';
      resultColor = '#3B82F6';
    } else if (ratio <= 0.55) {
      level = '中度';
      advice = '您的心理困扰较为明显，建议寻求专业心理咨询师的帮助。';
      resultColor = '#F59E0B';
    } else {
      level = '重度';
      advice = '您的心理困扰比较严重，强烈建议尽快联系心理医生进行专业评估和治疗。';
      resultColor = '#EF4444';
    }

    this.setData({
      showResult: true,
      resultColor: resultColor,
      result: {
        score: totalScore,
        level,
        advice,
        testTitle: this.data.testTitle,
        maxScore
      }
    });
  },

  goBack() {
    wx.navigateBack();
  },

  shareResult() {
    wx.showToast({ title: '分享功能开发中', icon: 'none' });
  },

  retakeTest() {
    this.setData({
      currentIndex: 0,
      answers: {},
      showResult: false,
      result: {}
    });
  }
});
