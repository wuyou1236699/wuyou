"""SVG use case diagrams - proper UML: individual actor lines, direct ellipse-to-ellipse relationships."""
import os

OUT = r'D:\claude memory\毕业论文文档'

UC_RX = '60'
UC_RY = '24'
FONT = 'SimHei,Microsoft YaHei,sans-serif'

def svg(w, h):
    return f'''<svg xmlns="http://www.w3.org/2000/svg" width="{w}" height="{h}" viewBox="0 0 {w} {h}" style="font-family:{FONT}">
  <defs>
    <marker id="arr" markerWidth="8" markerHeight="6" refX="8" refY="3" orient="auto"><polygon points="0 0, 8 3, 0 6" fill="#555"/></marker>
    <marker id="darr" markerWidth="8" markerHeight="6" refX="8" refY="3" orient="auto"><polygon points="0 0, 8 3, 0 6" fill="#888"/></marker>
  </defs>
  <rect width="{w}" height="{h}" fill="white"/>'''

def actor(x, y, label):
    return f'''  <circle cx="{x}" cy="{y-32}" r="10" fill="none" stroke="#333" stroke-width="2"/>
  <line x1="{x}" y1="{y-22}" x2="{x}" y2="{y+5}" stroke="#333" stroke-width="2"/>
  <line x1="{x-22}" y1="{y-12}" x2="{x+22}" y2="{y-12}" stroke="#333" stroke-width="2"/>
  <line x1="{x}" y1="{y+5}" x2="{x-16}" y2="{y+28}" stroke="#333" stroke-width="2"/>
  <line x1="{x}" y1="{y+5}" x2="{x+16}" y2="{y+28}" stroke="#333" stroke-width="2"/>
  <text x="{x}" y="{y+48}" text-anchor="middle" font-size="14" fill="#333">{label}</text>'''

def usecase(x, y, label):
    return f'''  <ellipse cx="{x}" cy="{y}" rx="{UC_RX}" ry="{UC_RY}" fill="#EBF3FC" stroke="#2B75C6" stroke-width="2"/>
  <text x="{x}" y="{y+5}" text-anchor="middle" font-size="13" fill="#2B75C6">{label}</text>'''

def system_box(x, y, w, h, label):
    return f'''  <rect x="{x}" y="{y}" width="{w}" height="{h}" rx="8" fill="none" stroke="#AAA" stroke-width="1" stroke-dasharray="6,3"/>
  <text x="{x+12}" y="{y-6}" font-size="11" fill="#888">{label}</text>'''

def actor_line(ax, ay, cx, cy):
    """Arrow from actor right-side to left edge of use case."""
    return f'''  <line x1="{ax}" y1="{ay}" x2="{cx-60}" y2="{cy}" stroke="#555" stroke-width="1.8" marker-end="url(#arr)"/>'''

def rel_v(fx, fy, tx, ty, label):
    """Vertical dashed arrow from bottom of upper UC to top of lower UC (same column)."""
    r = ''
    r += f'  <line x1="{fx}" y1="{fy+24}" x2="{fx}" y2="{ty-24}" stroke="#888" stroke-width="1" stroke-dasharray="5,3" marker-end="url(#darr)"/>'
    r += f'\n  <rect x="{fx+8}" y="{(fy+ty)//2 - 10}" width="70" height="16" fill="white"/>'
    r += f'\n  <text x="{fx+12}" y="{(fy+ty)//2 + 3}" font-size="10" fill="#888">{label}</text>'
    return r

def rel_r(fx, fy, tx, ty, label):
    """Horizontal dashed arrow from right edge of from_uc to left edge of to_uc (similar y)."""
    mid = fx + 60 + (tx - fx) // 2
    r = ''
    r += f'  <line x1="{fx+60}" y1="{fy}" x2="{tx-60}" y2="{ty}" stroke="#888" stroke-width="1" stroke-dasharray="5,3" marker-end="url(#darr)"/>'
    r += f'\n  <rect x="{mid-30}" y="{fy-16}" width="70" height="14" fill="white"/>'
    r += f'\n  <text x="{mid-26}" y="{fy-5}" font-size="10" fill="#888">{label}</text>'
    return r

def rel_l(fx, fy, tx, ty, label, mid_x):
    """L-shaped left route from source left edge, down, then into target left edge."""
    r = ''
    r += f'  <polyline points="{fx-60},{fy} {mid_x},{fy} {mid_x},{ty} {tx+60},{ty}" fill="none" stroke="#888" stroke-width="1" stroke-dasharray="5,3" marker-end="url(#darr)"/>'
    r += f'\n  <rect x="{mid_x+6}" y="{(fy+ty)//2 - 10}" width="70" height="14" fill="white"/>'
    r += f'\n  <text x="{mid_x+10}" y="{(fy+ty)//2 + 2}" font-size="10" fill="#888">{label}</text>'
    return r


# ============================================================
def gen_user():
    parts = [svg(1000, 700)]
    parts.append(system_box(240, 30, 520, 640, '心理咨询管理系统（用户端）'))
    parts.append(actor(100, 150, '普通用户'))

    cx, rx = 420, 640
    poses = [
        (cx,  70, '账号管理'),
        (cx, 145, '浏览咨询师'),
        (cx, 225, '预约操作'),
        (cx, 310, '在线聊天'),
        (cx, 395, '心理测评'),
        (cx, 480, '科普阅读'),
        (rx, 375, '服务评价'),
        (cx, 565, '个人中心'),
    ]
    for (px, py, label) in poses:
        parts.append(usecase(px, py, label))

    # Individual actor lines
    for (px, py, _) in poses:
        parts.append(actor_line(145, 150, px, py))

    # Relationships
    parts.append(rel_v(cx, 145, cx, 225, 'include'))   # 浏览→预约
    parts.append(rel_l(cx, 225, rx, 375, 'extend', 540))  # 预约→评价

    parts.append('</svg>')
    return '\n'.join(parts)


def gen_counselor():
    parts = [svg(1000, 700)]
    parts.append(system_box(240, 30, 520, 640, '心理咨询管理系统（咨询师端）'))
    parts.append(actor(100, 150, '咨询师'))

    cx, rx = 420, 640
    poses = [
        (cx,  70, '登录系统'),
        (cx, 145, '首页概览'),
        (cx, 225, '预约处理'),
        (cx, 310, '在线聊天'),
        (cx, 395, '消息管理'),
        (rx, 375, '咨询记录管理'),
        (cx, 480, '排班查看'),
        (cx, 565, '个人中心'),
    ]
    for (px, py, label) in poses:
        parts.append(usecase(px, py, label))

    for (px, py, _) in poses:
        parts.append(actor_line(145, 150, px, py))

    parts.append(rel_v(cx, 145, cx, 225, 'include'))    # 首页→预约
    parts.append(rel_v(cx, 225, cx, 310, 'extend'))      # 预约→聊天
    parts.append(rel_l(cx, 225, rx, 375, 'extend', 540)) # 预约→记录

    parts.append('</svg>')
    return '\n'.join(parts)


def gen_admin():
    parts = [svg(1000, 700)]
    parts.append(system_box(240, 30, 520, 640, '心理咨询管理系统（管理端）'))
    parts.append(actor(100, 150, '管理员'))

    cx, rx = 420, 640
    poses = [
        (cx,  70, '登录系统'),
        (cx, 145, '数据看板'),
        (cx, 225, '人员管理'),
        (cx, 305, '预约管理'),
        (rx, 305, '排班管理'),
        (cx, 390, '统计分析'),
        (cx, 475, '内容管理'),
        (cx, 560, '咨询记录查看'),
    ]
    for (px, py, label) in poses:
        parts.append(usecase(px, py, label))

    for (px, py, _) in poses:
        parts.append(actor_line(145, 150, px, py))

    # 登录→人员: 绕过数据看板走右边
    parts.append(f'''  <polyline points="{cx+60},90 {cx+120},90 {cx+120},195 {cx-60},195" fill="none" stroke="#888" stroke-width="1" stroke-dasharray="5,3" marker-end="url(#darr)"/>
  <rect x="{cx+80}" y="130" width="70" height="14" fill="white"/>
  <text x="{cx+84}" y="141" font-size="10" fill="#888">include</text>''')
    # 预约→排班 (horizontal, same row)
    parts.append(rel_r(cx, 305, rx, 305, 'include'))

    parts.append('</svg>')
    return '\n'.join(parts)


if __name__ == '__main__':
    for name, fn in [('usecase_user.svg', gen_user),
                      ('usecase_counselor.svg', gen_counselor),
                      ('usecase_admin.svg', gen_admin)]:
        path = os.path.join(OUT, name)
        with open(path, 'w', encoding='utf-8') as f:
            f.write(fn())
        print(f'Generated: {name}')
