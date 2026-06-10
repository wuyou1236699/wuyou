USE psychology_db;

-- 为科普文章添加封面图片
UPDATE science_article SET cover = 'https://images.unsplash.com/photo-1506126613408-eca07ce370ec?w=800&h=600&fit=crop' WHERE id = 1;
UPDATE science_article SET cover = 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=800&h=600&fit=crop' WHERE id = 2;
UPDATE science_article SET cover = 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=800&h=600&fit=crop' WHERE id = 3;
UPDATE science_article SET cover = 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=800&h=600&fit=crop' WHERE id = 4;
UPDATE science_article SET cover = 'https://images.unsplash.com/photo-1497215728101-856f4ea42174?w=800&h=600&fit=crop' WHERE id = 5;
UPDATE science_article SET cover = 'https://images.unsplash.com/photo-1506126613408-eca07ce370ec?w=800&h=600&fit=crop' WHERE id = 6;
UPDATE science_article SET cover = 'https://images.unsplash.com/photo-1516550893923-42d28e5677af?w=800&h=600&fit=crop' WHERE id = 7;
UPDATE science_article SET cover = 'https://images.unsplash.com/photo-1523419409548-9c8431f63f1b?w=800&h=600&fit=crop' WHERE id = 8;
UPDATE science_article SET cover = 'https://images.unsplash.com/photo-1511632765486-a01980e01a18?w=800&h=600&fit=crop' WHERE id = 9;
UPDATE science_article SET cover = 'https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=800&h=600&fit=crop' WHERE id = 10;

-- 验证
SELECT id, title, cover FROM science_article;
