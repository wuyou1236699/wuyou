"""Generate use case diagrams as SVG - v3 with arrows and clean routing."""
import os

OUT = r'D:\claude memory\毕业论文文档'

def svg_header(w, h):
    return f'''<svg xmlns="http://www.w3.org/2000/svg" width="{w}" height="{h}" viewBox="0 0 {w} {h}" style="font-family:SimHei,Microsoft YaHei,sans-serif">
  <rect width="{w}" height="{h}" fill="white"/>
  <defs>
    <marker id="arr" markerWidth="7" markerHeight="5" refX="7" refY="2.5" orient="auto">
      <polygon points="0 0, 7 2.5, 0 5" fill="#555"/>
    </marker>
    <marker id="rel-arrow" markerWidth="7" markerHeight="5" refX="7" refY="2.5" orient="auto">
      <polygon points="0 0, 7 2.5, 0 5" fill="#888"/>
    </marker>
  </defs>'''

def svg_footer():
    return '</svg>'

def actor(x, y, label):
    return f'''  <circle cx="{x}" cy="{y-30}" r="10" fill="none" stroke="#333" stroke-width="1.8"/>
  <line x1="{x}" y1="{y-20}" x2="{x}" y2="{y+5}" stroke="#333" stroke-width="1.8"/>
  <line x1="{x-22}" y1="{y-10}" x2="{x+22}" y2="{y-10}" stroke="#333" stroke-width="1.8"/>
  <line x1="{x}" y1="{y+5}" x2="{x-18}" y2="{y+30}" stroke="#333" stroke-width="1.8"/>
  <line x1="{x}" y1="{y+5}" x2="{x+18}" y2="{y+30}" stroke="#333" stroke-width="1.8"/>
  <text x="{x}" y="{y+50}" text-anchor="middle" font-size="13" fill="#333">{label}</text>'''

def usecase(x, y, label, width=100):
    rx, ry = width // 2, 20
    return f'''  <ellipse cx="{x}" cy="{y}" rx="{rx}" ry="{ry}" fill="#E8F0FE" stroke="#1A73E8" stroke-width="1.5"/>
  <text x="{x}" y="{y+5}" text-anchor="middle" font-size="12" fill="#1A73E8">{label}</text>'''

def system_box(x, y, w, h, label):
    return f'''  <rect x="{x}" y="{y}" width="{w}" height="{h}" rx="8" fill="none" stroke="#999" stroke-width="1.5" stroke-dasharray="6,3"/>
  <text x="{x+10}" y="{y+18}" font-size="11" fill="#666">{label}</text>'''

# ── Connectors ──

def conn_h(x1, y1, x2, y2, arrow=False):
    """Horizontal line from edge of one ellipse to edge of another at same y."""
    am = ' marker-end="url(#arr)"' if arrow else ''
    return f'  <line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" stroke="#555" stroke-width="1.2"{am}/>'

def conn_rel_v(x, y1, y2, label):
    """Vertical dashed relationship line: runs BETWEEN two use case ellipses, label to the right."""
    return f'''  <line x1="{x}" y1="{y1}" x2="{x}" y2="{y2}" stroke="#888" stroke-width="1" stroke-dasharray="5,3" marker-end="url(#rel-arrow)"/>
  <text x="{x+10}" y="{(y1+y2)//2 - 4}" font-size="10" fill="#888">{label}</text>'''

def conn_rel_r(sx, sy, tx, ty, label):
    """L-shaped route from (sx,sy) rightward, then up/down to (tx,ty). tx must be > sx."""
    mid_x = max(sx, tx) + 4  # push slightly past the farther edge
    return f'''  <polyline points="{sx},{sy} {mid_x},{sy} {mid_x},{ty} {tx},{ty}" fill="none" stroke="#888" stroke-width="1" stroke-dasharray="5,3" marker-end="url(#rel-arrow)"/>
  <text x="{mid_x+6}" y="{(sy+ty)//2 - 4}" font-size="10" fill="#888">{label}</text>'''

def conn_rel_l(x1, y1, y2, label):
    """Route from main column leftwards to a use case on the left side."""
    mid_x = x1 - 80
    return f'''  <polyline points="{x1},{y1} {mid_x},{y1} {mid_x},{y2}" fill="none" stroke="#888" stroke-width="1" stroke-dasharray="5,3" marker-end="url(#rel-arrow)"/>
  <text x="{mid_x+6}" y="{(y1+y2)//2 - 4}" font-size="10" fill="#888">{label}</text>'''


# ============================================================
# 用户端
# ============================================================
def gen_user_usecase():
    parts = [svg_header(780, 820)]
    parts.append(system_box(220, 25, 540, 770, '心理咨询管理系统（用户端）'))
    parts.append(actor(85, 150, '普通用户'))

    cx = 370   # main column
    rx = 570   # right column for extend

    cases = [
        (cx, 70,  '注册账号', 85),
        (cx, 140, '登录系统', 85),
        (cx, 210, '浏览咨询师', 100),
        (cx, 275, '查看咨询师详情', 115),
        (cx, 345, '预约咨询', 85),
        (rx, 320, '取消预约', 85),     # extend from 预约
        (cx, 415, '在线聊天', 85),
        (cx, 485, '心理测评', 85),
        (cx, 555, '浏览科普文章', 105),
        (cx, 625, '评价咨询师', 90),    # extend from 预约 via left route
        (cx, 700, '管理个人信息', 105),
    ]
    for (ux, uy, label, *rest) in cases:
        w = rest[0] if rest else 90
        parts.append(usecase(ux, uy, label, w))

    # Actor connections
    parts.append(conn_h(170, 150, 255, 150, True))
    for (ux, uy, _, *rest) in cases:
        if ux == cx:
            w = rest[0] if rest else 90
            parts.append(conn_h(270, uy, ux - w//2 - 5, uy, True))
    # Connect to right-column 取消预约
    parts.append(conn_h(270, 320, rx - 45, 320, True))

    # Relationships (run alongside use cases, not through them)
    parts.append(conn_rel_v(cx+65,  210, 275, '《include》'))  # 浏览→详情
    parts.append(conn_rel_r(cx+48, 365, rx-45, 320, '《extend》'))   # 预约→取消(right)
    # 预约→评价: route left first then down
    parts.append(f'''  <polyline points="{cx-48},{365} {cx-120},{365} {cx-120},{625}" fill="none" stroke="#888" stroke-width="1" stroke-dasharray="5,3" marker-end="url(#rel-arrow)"/>
  <text x="{cx-114}" y="490" font-size="10" fill="#888">《extend》</text>''')

    parts.append(svg_footer())
    return '\n'.join(parts)


# ============================================================
# 咨询师端
# ============================================================
def gen_counselor_usecase():
    parts = [svg_header(800, 750)]
    parts.append(system_box(220, 25, 560, 700, '心理咨询管理系统（咨询师端）'))
    parts.append(actor(85, 160, '咨询师'))

    cx = 370   # main column
    rx = 590   # right column (取消预约)

    cases = [
        (cx, 65,  '登录系统', 85),
        (cx, 130, '查看首页', 85),
        (cx, 195, '查看预约列表', 110),
        (cx, 260, '确认预约', 85),
        (cx, 330, '开始咨询', 85),
        (rx, 275, '取消预约', 85),      # extend from 确认
        (cx, 400, '完成咨询', 85),
        (cx, 470, '在线聊天', 85),
        (cx, 540, '查看咨询记录', 110),
        (cx, 610, '查看消息列表', 110),
        (cx, 680, '查看排班', 85),
    ]
    for (ux, uy, label, *rest) in cases:
        w = rest[0] if rest else 90
        parts.append(usecase(ux, uy, label, w))

    # Actor connections
    parts.append(conn_h(172, 160, 255, 160, True))
    for (ux, uy, _, *rest) in cases:
        if ux == cx or ux == rx:
            w = rest[0] if rest else 90
            parts.append(conn_h(270, uy, ux - w//2 - 5, uy, True))

    # ── Relationships: all lines run to the right of main column ──
    rel_x = cx + 65   # vertical channel for relationship arrows

    parts.append(conn_rel_v(rel_x, 195, 260, '《include》'))   # 查看预约→确认
    parts.append(conn_rel_r(cx+48, 282, rx-45, 275, '《extend》'))  # 确认→取消(right)

    # 确认→开始: simple vertical
    parts.append(conn_rel_v(rel_x, 260, 330, '《extend》'))

    # 确认→完成: route around 开始咨询
    # Go right from 确认 bottom, past the start of 开始, then down along right side to 完成 top
    bypass_x = cx + 110
    parts.append(f'''  <polyline points="{cx+48},{282} {bypass_x},{282} {bypass_x},{380} {cx+48},{380}" fill="none" stroke="#888" stroke-width="1" stroke-dasharray="5,3" marker-end="url(#rel-arrow)"/>
  <text x="{bypass_x+6}" y="330" font-size="10" fill="#888">《extend》</text>''')

    # 开始→聊天: simple vertical
    parts.append(conn_rel_v(rel_x, 330, 470, '《extend》'))

    # 完成→记录: simple vertical
    parts.append(conn_rel_v(rel_x, 400, 540, '《extend》'))

    parts.append(svg_footer())
    return '\n'.join(parts)


# ============================================================
# 管理端
# ============================================================
def gen_admin_usecase():
    parts = [svg_header(780, 860)]
    parts.append(system_box(220, 25, 540, 810, '心理咨询管理系统（管理端）'))
    parts.append(actor(85, 180, '管理员'))

    cx = 370
    cases = [
        (cx, 70,  '登录系统', 85),
        (cx, 135, '查看数据看板', 105),
        (cx, 200, '管理用户', 85),
        (cx, 265, '管理咨询师', 95),
        (cx, 330, '管理预约', 85),
        (cx, 395, '查看咨询记录', 105),
        (cx, 460, '管理排班', 85),
        (cx, 525, '统计分析', 85),
        (cx, 590, '管理科普文章', 105),
        (cx, 655, '管理公告', 85),
        (cx, 720, '管理测评', 85),
        (cx, 785, '系统通知', 85),
    ]
    for (ux, uy, label, *rest) in cases:
        w = rest[0] if rest else 90
        parts.append(usecase(ux, uy, label, w))

    parts.append(conn_h(170, 180, 255, 180, True))
    for (ux, uy, _, *rest) in cases:
        w = rest[0] if rest else 90
        parts.append(conn_h(270, uy, ux - w//2 - 5, uy, True))

    rel_x = cx + 65
    parts.append(conn_rel_v(rel_x, 70, 200, '《include》'))
    parts.append(conn_rel_v(rel_x, 460, 395, '《include》'))

    parts.append(svg_footer())
    return '\n'.join(parts)


if __name__ == '__main__':
    for name, fn in [('usecase_user.svg', gen_user_usecase),
                      ('usecase_counselor.svg', gen_counselor_usecase),
                      ('usecase_admin.svg', gen_admin_usecase)]:
        path = os.path.join(OUT, name)
        with open(path, 'w', encoding='utf-8') as f:
            f.write(fn())
        print(f'Generated: {name}')
