"""Use case diagrams - proper UML style: individual actor lines, direct ellipse-to-ellipse relationships."""
import math, os
from PIL import Image, ImageDraw, ImageFont

OUT = r'D:\claude memory\毕业论文文档'
FONT_PATH = 'C:/Windows/Fonts/simhei.ttf'

def load_font(size):
    return ImageFont.truetype(FONT_PATH, size)

class Dia:
    def __init__(self, w, h, title):
        self.w, self.h = w, h
        self.img = Image.new('RGB', (w, h), 'white')
        self.d = ImageDraw.Draw(self.img)
        self.f14 = load_font(14)
        self.f12 = load_font(12)
        self.f11 = load_font(11)
        self.f10 = load_font(10)
        # 用例椭圆参数：半宽/半高
        self.rx = 62
        self.ry = 24

    def actor(self, x, y, label):
        d = self.d
        d.ellipse((x-11, y-44, x+11, y-22), outline='#333', width=2)
        d.line((x, y-22, x, y+6), fill='#333', width=2)
        d.line((x-24, y-12, x+24, y-12), fill='#333', width=2)
        d.line((x, y+6, x-16, y+30), fill='#333', width=2)
        d.line((x, y+6, x+16, y+30), fill='#333', width=2)
        bbox = d.textbbox((0,0), label, font=self.f14)
        tw = bbox[2]-bbox[0]
        d.text((x-tw//2, y+38), label, fill='#333', font=self.f14)

    def uc(self, x, y, label):
        """Draw use case ellipse. Returns (cx, cy, rx, ry) for connection math."""
        d = self.d
        d.ellipse((x-self.rx, y-self.ry, x+self.rx, y+self.ry),
                  fill='#EBF3FC', outline='#2B75C6', width=2)
        bbox = d.textbbox((0,0), label, font=self.f12)
        tw = bbox[2]-bbox[0]
        d.text((x-tw//2, y-9), label, fill='#2B75C6', font=self.f12)
        return (x, y, self.rx, self.ry)

    def box(self, x, y, w, h, label):
        d = self.d
        d.rectangle((x, y, x+w, y+h), outline='#AAA', width=1)
        d.text((x+14, y-16), label, fill='#888', font=self.f11)

    def line_actor(self, ax, ay, cx, cy, rx):
        """Line from actor center-right to left edge of use case ellipse, with arrow."""
        d = self.d
        x2 = cx - rx   # 椭圆左边缘
        y2 = cy
        d.line((ax, ay, x2, y2), fill='#555', width=2)
        # Arrowhead at ellipse edge
        ang = math.atan2(y2-ay, x2-ax)
        al = 10
        ax1, ay1 = x2 - al*math.cos(ang-0.5), y2 - al*math.sin(ang-0.5)
        ax2, ay2 = x2 - al*math.cos(ang+0.5), y2 - al*math.sin(ang+0.5)
        d.polygon(((x2, y2), (ax1, ay1), (ax2, ay2)), fill='#555')

    def rel_connect(self, from_uc, to_uc, label, side='right'):
        """Dashed line from edge of from_uc to edge of to_uc, with arrow at to_uc.
        side='right': line exits from_uc on the right and enters to_uc from above or right.
        side='left': line exits from_uc on the left.
        side='top': line exits from_uc on top and enters to_uc from bottom."""
        d = self.d
        fx, fy, frx, fry = from_uc
        tx, ty, trx, try_ = to_uc

        # Route: start from right edge of from_uc, go right, then up/down to to_uc
        if side == 'right':
            # Exit right edge, go out a bit, then route to target
            sx = fx + frx
            sy = fy
            # Go right to mid_x, then to target y, then into target
            mid_x = max(fx + frx, tx) + 50
            # L-shape: out right → vertical → into target from left
            points = [(sx, sy), (mid_x, sy), (mid_x, ty), (tx - trx, ty)]
        elif side == 'left':
            # Exit left, route around left side
            sx = fx - frx
            sy = fy
            mid_x = min(fx - frx, tx) - 50
            points = [(sx, sy), (mid_x, sy), (mid_x, ty), (tx + trx, ty)]
        elif side == 'top':
            # Exit top, route upward
            sx = fx
            sy = fy - fry
            points = [(sx, sy), (sx, ty + try_)]
        else:
            points = [(fx, fy), (tx, ty)]

        # Draw dashed polyline
        pts = points
        for i in range(len(pts)-1):
            x1, y1 = pts[i]
            x2, y2 = pts[i+1]
            dx, dy = x2-x1, y2-y1
            dist = math.sqrt(dx*dx + dy*dy)
            if dist < 1: continue
            ux, uy = dx/dist, dy/dist
            seg, gap = 6, 4
            drawn = 0.0
            while drawn < dist - seg:
                sx2 = x1+ux*drawn
                sy2 = y1+uy*drawn
                ex2 = x1+ux*min(drawn+seg, dist)
                ey2 = y1+uy*min(drawn+seg, dist)
                d.line((sx2, sy2, ex2, ey2), fill='#888', width=1)
                drawn += seg + gap

        # Arrowhead at last point
        last = pts[-1]
        prev = pts[-2]
        ang = math.atan2(last[1]-prev[1], last[0]-prev[0])
        al = 8
        ax1, ay1 = last[0] - al*math.cos(ang-0.45), last[1] - al*math.sin(ang-0.45)
        ax2, ay2 = last[0] - al*math.cos(ang+0.45), last[1] - al*math.sin(ang+0.45)
        d.polygon(((last[0], last[1]), (ax1, ay1), (ax2, ay2)), fill='#888')

        # Label near midpoint of first horizontal segment
        if side in ('right', 'left'):
            lx = (pts[0][0] + pts[1][0]) // 2 + 5
            ly = pts[0][1] - 14
        else:
            lx = pts[0][0] - 30
            ly = (pts[0][1] + pts[1][1]) // 2 - 5
        bbox = d.textbbox((0,0), label, font=self.f10)
        tw, th = bbox[2]-bbox[0], bbox[3]-bbox[1]
        d.rectangle((lx-3, ly-2, lx+tw+3, ly+th+2), fill='white')
        d.text((lx, ly), label, fill='#888', font=self.f10)

    def save(self, name):
        path = os.path.join(OUT, name)
        self.img.save(path, 'PNG', dpi=(150, 150))
        print(f'Saved: {name} ({self.w}x{self.h})')


# ============================================================
# 用户端
# ============================================================
def user():
    d = Dia(1050, 750, '')
    d.box(260, 30, 560, 690, '心理咨询管理系统（用户端）')
    # Actor at left, near vertical center
    ax, ay = 110, 200
    d.actor(ax, ay, '普通用户')

    # Use cases - two columns
    cx, rx = 420, 650  # left column, right column

    # Left column
    uc1 = d.uc(cx, 80,  '账号管理')
    uc2 = d.uc(cx, 160, '浏览咨询师')
    uc3 = d.uc(cx, 250, '预约操作')
    uc4 = d.uc(cx, 340, '在线聊天')
    uc5 = d.uc(cx, 430, '心理测评')
    uc6 = d.uc(cx, 520, '科普阅读')
    # Right column
    uc7 = d.uc(rx, 350, '服务评价')
    uc8 = d.uc(cx, 610, '个人中心')

    # Actor to each use case - individual lines
    for (ux, uy, urx, _) in [uc1, uc2, uc3, uc4, uc5, uc6, uc8]:
        d.line_actor(ax+45, ay, ux, uy, urx)
    d.line_actor(ax+45, ay, rx, 350, d.rx)  # for uc7 (right column)

    # Relationships - direct ellipse to ellipse
    d.rel_connect(uc2, uc3, 'include', 'right')      # 浏览→预约
    d.rel_connect(uc3, uc7, 'extend', 'right')        # 预约→评价

    d.save('usecase_user.png')


# ============================================================
# 咨询师端
# ============================================================
def counselor():
    d = Dia(1050, 750, '')
    d.box(260, 30, 560, 690, '心理咨询管理系统（咨询师端）')
    ax, ay = 110, 200
    d.actor(ax, ay, '咨询师')

    cx, rx = 420, 650
    uc1 = d.uc(cx, 80,  '登录系统')
    uc2 = d.uc(cx, 160, '首页概览')
    uc3 = d.uc(cx, 240, '预约处理')
    uc4 = d.uc(cx, 330, '在线聊天')
    uc5 = d.uc(cx, 420, '消息管理')
    uc6 = d.uc(rx, 380, '咨询记录管理')
    uc7 = d.uc(cx, 510, '排班查看')
    uc8 = d.uc(cx, 600, '个人中心')

    # Actor lines
    for (ux, uy, urx, _) in [uc1, uc2, uc3, uc4, uc5, uc7, uc8]:
        d.line_actor(ax+45, ay, ux, uy, urx)
    d.line_actor(ax+45, ay, rx, 380, d.rx)

    # Relationships
    d.rel_connect(uc2, uc3, 'include', 'right')     # 首页→预约
    d.rel_connect(uc3, uc4, 'extend', 'right')       # 预约→聊天
    d.rel_connect(uc3, uc6, 'extend', 'right')       # 预约→记录

    d.save('usecase_counselor.png')


# ============================================================
# 管理端
# ============================================================
def admin():
    d = Dia(1050, 750, '')
    d.box(260, 30, 560, 690, '心理咨询管理系统（管理端）')
    ax, ay = 110, 200
    d.actor(ax, ay, '管理员')

    cx, rx = 420, 650
    uc1 = d.uc(cx, 80,  '登录系统')
    uc2 = d.uc(cx, 160, '数据看板')
    uc3 = d.uc(cx, 240, '人员管理')
    uc4 = d.uc(cx, 320, '预约管理')
    uc5 = d.uc(rx, 320, '排班管理')
    uc6 = d.uc(cx, 410, '统计分析')
    uc7 = d.uc(cx, 500, '内容管理')
    uc8 = d.uc(cx, 590, '咨询记录查看')

    for (ux, uy, urx, _) in [uc1, uc2, uc3, uc4, uc6, uc7, uc8]:
        d.line_actor(ax+45, ay, ux, uy, urx)
    d.line_actor(ax+45, ay, rx, 320, d.rx)

    # Relationships
    d.rel_connect(uc1, uc3, 'include', 'right')     # 登录→人员
    d.rel_connect(uc5, uc6, 'include', 'left')       # 排班→统计

    d.save('usecase_admin.png')


if __name__ == '__main__':
    user()
    counselor()
    admin()
    print('All done.')
