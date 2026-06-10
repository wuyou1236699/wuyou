const request = require('../../utils/request.js');

Page({
  data: {
    articles: [],
    categories: [],
    activeCategory: '',
    page: 1,
    size: 10,
    loading: false,
    hasMore: true
  },

  onLoad() {
    this.loadCategories();
    this.loadArticles();
  },

  onShow() {
    // 每次显示页面都重新加载，确保数据最新
    this.setData({ page: 1, articles: [], hasMore: true });
    this.loadArticles();
  },

  onPullDownRefresh() {
    this.setData({ page: 1, articles: [], hasMore: true });
    this.loadArticles();
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadArticles();
    }
  },

  async loadCategories() {
    try {
      const data = await request.get('/api/science/categories', false);
      this.setData({ categories: [{ id: '', name: '全部' }, ...data] });
    } catch (err) {
      console.warn('加载分类失败:', err);
      request.showToast('加载分类失败，请检查网络');
    }
  },

  async loadArticles() {
    this.setData({ loading: true });
    
    try {
      const params = {
        page: this.data.page,
        size: this.data.size
      };
      if (this.data.activeCategory) {
        params.categoryId = this.data.activeCategory;
      }
      
      const data = await request.get('/api/science/articles', params, false);
      
      if (data.list && data.list.length > 0) {
        const newArticles = data.list.map(article => ({
          id: article.id,
          title: article.title,
          summary: article.summary,
          content: article.content,
          category: this.getCategoryName(article.categoryId),
          coverImage: article.cover || '',
          createTime: this.formatDate(article.createTime),
          readCount: article.viewCount || 0,
          likeCount: article.likeCount || 0
        }));
        
        let updatedArticles;
        if (this.data.page === 1) {
          updatedArticles = newArticles;
        } else {
          // 过滤掉已经存在的文章
          const existingIds = new Set(this.data.articles.map(a => a.id));
          const uniqueNewArticles = newArticles.filter(a => !existingIds.has(a.id));
          updatedArticles = [...this.data.articles, ...uniqueNewArticles];
        }
        
        const nextPage = this.data.page + 1;
        this.setData({
          articles: updatedArticles,
          hasMore: data.list.length === this.data.size,
          page: nextPage
        });
      } else {
        this.setData({ hasMore: false });
      }
    } catch (err) {
      console.warn('加载文章失败:', err);
      request.showToast('加载文章失败，请检查网络');
    } finally {
      this.setData({ loading: false });
      wx.stopPullDownRefresh();
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

  getCategoryName(categoryId) {
    const category = this.data.categories.find(c => c.id === categoryId);
    return category ? category.name : '';
  },

  onCategoryChange(e) {
    const categoryId = e.currentTarget.dataset.id;
    this.setData({ 
      activeCategory: categoryId,
      page: 1, 
      articles: [], 
      hasMore: true 
    });
    this.loadArticles();
  },

  goToDetail(e) {
    const article = e.currentTarget.dataset.article;
    wx.navigateTo({
      url: `/pages/science/detail?id=${article.id}&title=${encodeURIComponent(article.title)}`
    });
  }
});
