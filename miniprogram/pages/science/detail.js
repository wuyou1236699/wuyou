const request = require('../../utils/request.js');

Page({
  data: {
    article: {},
    loading: true
  },

  onLoad(options) {
    const id = options.id;
    if (id) {
      this.loadArticle(id);
    }
  },

  async loadArticle(id) {
    this.setData({ loading: true });
    
    try {
      const data = await request.get(`/api/science/articles/${id}`);
      this.setData({
        article: {
          id: data.id,
          title: data.title,
          summary: data.summary,
          content: data.content,
          coverImage: data.cover || '',
          createTime: this.formatDate(data.createTime),
          readCount: data.viewCount || 0,
          likeCount: data.likeCount || 0
        }
      });
    } catch (err) {
      console.warn('加载文章详情失败:', err);
      request.showToast('加载文章失败，请检查网络');
    } finally {
      this.setData({ loading: false });
    }
  },

  formatDate(date) {
    if (!date) return '';
    const d = new Date(date);
    const year = d.getFullYear();
    const month = (d.getMonth() + 1).toString().padStart(2, '0');
    const day = d.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
  },

  onShareAppMessage() {
    return {
      title: this.data.article.title,
      path: `/pages/science/detail?id=${this.data.article.id}`
    };
  }
});
