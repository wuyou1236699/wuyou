"""Generate diagrams for Chapter 4: architecture, module structure, state, sequence, activity, ER."""
import os

OUT = r'D:\claude memory\毕业论文文档'
FONT = 'SimHei,Microsoft YaHei,sans-serif'

def svg(w, h):
    return f'<svg xmlns="http://www.w3.org/2000/svg" width="{w}" height="{h}" viewBox="0 0 {w} {h}" style="font-family:{FONT}">\n  <rect width="{w}" height="{h}" fill="white"/>'

def end():
    return '</svg>'

def rect(x, y, w, h, fill, stroke, label='', font_size=13, color='#333', rx=4):
    r = f'  <rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{rx}" fill="{fill}" stroke="{stroke}" stroke-width="1.5"/>'
    if label:
        lines = label.split('\n')
        line_h = font_size + 4
        start_y = y + (h - len(lines) * line_h) // 2 + font_size - 2
        for i, line in enumerate(lines):
            r += f'\n  <text x="{x + w//2}" y="{start_y + i*line_h}" text-anchor="middle" font-size="{font_size}" fill="{color}">{line}</text>'
    return r

def arrow_line(x1, y1, x2, y2, color='#555'):
    return f'  <line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" stroke="{color}" stroke-width="1.5" marker-end="url(#a)"/>'

def dashed_line(x1, y1, x2, y2):
    return f'  <line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" stroke="#999" stroke-width="1" stroke-dasharray="6,4"/>'

def text(x, y, content, size=13, color='#333', anchor='middle'):
    return f'  <text x="{x}" y="{y}" text-anchor="{anchor}" font-size="{size}" fill="{color}">{content}</text>'

def arrow_def():
    return '  <defs><marker id="a" markerWidth="8" markerHeight="6" refX="8" refY="3" orient="auto"><polygon points="0 0, 8 3, 0 6" fill="#555"/></marker></defs>'

# ============================================================
# 4.1 系统架构图 - Three-tier
# ============================================================
def gen_architecture():
    r = [svg(800, 550), arrow_def()]

    # Title
    r.append(text(400, 28, '系统总体架构', 16, '#333'))

    # Presentation layer
    r.append(rect(40, 55, 720, 95, '#E3F2FD', '#1565C0', '', 8))
    r.append(text(400, 80, '前端展示层', 14, '#1565C0', 'middle'))
    # Three client boxes
    r.append(rect(70, 100, 190, 35, '#BBDEFB', '#1976D2', '用户端（微信小程序）', 12, '#1565C0'))
    r.append(rect(305, 100, 190, 35, '#BBDEFB', '#1976D2', '咨询师端（微信小程序）', 12, '#1565C0'))
    r.append(rect(540, 100, 190, 35, '#BBDEFB', '#1976D2', '管理端（Web页面）', 12, '#1565C0'))

    # Arrows down
    r.append(arrow_line(400, 155, 400, 195))

    # Business layer
    r.append(rect(40, 200, 720, 150, '#E8F5E9', '#2E7D32', '', 8))
    r.append(text(400, 225, '业务逻辑层（Spring Boot）', 14, '#2E7D32', 'middle'))

    # Components
    comps = ['Controller\n控制层', 'Service\n业务层', 'Mapper\n数据访问', 'WebSocket\n实时通信', 'JWT\n认证']
    comp_w = 120
    gap = 15
    total_w = len(comps) * comp_w + (len(comps)-1) * gap
    start_x = (800 - total_w) // 2
    for i, c in enumerate(comps):
        cx = start_x + i * (comp_w + gap)
        r.append(rect(cx, 245, comp_w, 90, '#C8E6C9', '#43A047', c, 11, '#1B5E20'))

    # Arrows down
    r.append(arrow_line(400, 355, 400, 390))

    # Data layer
    r.append(rect(40, 395, 720, 65, '#FFF3E0', '#E65100', '', 8))
    r.append(text(400, 420, '数据持久层', 14, '#E65100', 'middle'))
    r.append(rect(260, 430, 120, 25, '#FFE0B2', '#EF6C00', 'MySQL 数据库', 12, '#BF360C'))
    r.append(rect(420, 430, 120, 25, '#FFE0B2', '#EF6C00', '文件存储', 12, '#BF360C'))

    # Side labels
    r.append(text(755, 340, 'RESTful\nAPI', 11, '#888', 'start'))
    r.append(text(755, 460, 'JDBC/\nMyBatis', 11, '#888', 'start'))

    r.append(end())
    return '\n'.join(r)


# ============================================================
# 4.2 模块结构图
# ============================================================
def gen_module_structure():
    r = [svg(900, 650), arrow_def()]
    r.append(text(450, 25, '系统功能模块结构', 16, '#333'))

    # Root
    r.append(rect(325, 45, 250, 35, '#1565C0', '#0D47A1', '心理咨询管理系统', 14, 'white', 6))

    # Level 1 - Three branches
    l1_y = 110
    branches = [
        (90, '用户端功能'),
        (350, '咨询师端功能'),
        (610, '管理端功能'),
    ]
    for bx, bl in branches:
        r.append(rect(bx, l1_y, 200, 32, '#E3F2FD', '#1976D2', bl, 13, '#1565C0', 5))

    # Lines from root to branches
    r.append(arrow_line(380, 80, 190, 110))
    r.append(arrow_line(450, 80, 450, 110))
    r.append(arrow_line(520, 80, 710, 110))

    # Level 2 - Specific modules under each branch
    user_mods = ['账号管理', '咨询师浏览', '预约操作', '在线聊天', '心理测评', '科普阅读', '服务评价']
    counselor_mods = ['首页概览', '预约处理', '在线聊天', '消息管理', '记录管理', '排班查看']
    admin_mods = ['人员管理', '预约管理', '排班管理', '统计分析', '内容管理', '记录查看']

    for col_x, mods in [(90, user_mods), (350, counselor_mods), (610, admin_mods)]:
        for i, m in enumerate(mods):
            my = 165 + i * 35
            r.append(rect(col_x, my, 200, 28, '#F5F5F5', '#BDBDBD', m, 11, '#555', 4))
            r.append(arrow_line(col_x+100, 142, col_x+100, 165))

    r.append(end())
    return '\n'.join(r)


# ============================================================
# 4.3a 预约状态图
# ============================================================
def gen_appointment_state():
    r = [svg(800, 520), arrow_def()]
    r.append(text(400, 22, '预约状态图', 16, '#333'))

    def state_box(x, y, w, h, name, color):
        r.append(f'  <rect x="{x}" y="{y}" width="{w}" height="{h}" rx="8" fill="#FAFBFC" stroke="{color}" stroke-width="2"/>')
        r.append(f'  <line x1="{x}" y1="{y+30}" x2="{x+w}" y2="{y+30}" stroke="{color}" stroke-width="1"/>')
        r.append(f'  <text x="{x+w//2}" y="{y+20}" text-anchor="middle" font-size="13" fill="{color}">{name}</text>')
        return (x, y, w, h)

    def label(pos_x, pos_y, txt, c='#555'):
        r.append(f'  <text x="{pos_x}" y="{pos_y}" text-anchor="middle" font-size="11" fill="{c}">{txt}</text>')

    def tline(x1, y1, x2, y2):
        r.append(f'  <line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" stroke="#555" stroke-width="1.2" marker-end="url(#a)"/>')

    # Initial
    r.append(f'  <circle cx="95" cy="145" r="10" fill="#333"/>')

    # States — well spaced
    s1 = state_box(140, 120, 110, 50, '待确认', '#E65100')
    s2 = state_box(360, 70, 110, 50, '已确认', '#1565C0')
    s3 = state_box(580, 70, 110, 50, '已完成', '#7B1FA2')
    s4 = state_box(580, 240, 110, 50, '已取消', '#C62828')
    s5 = state_box(360, 240, 110, 50, '咨询中', '#2E7D32')

    # Final states — bullseye, one for each terminal path
    r.append(f'  <circle cx="635" cy="380" r="14" fill="none" stroke="#333" stroke-width="1.5"/>')
    r.append(f'  <circle cx="635" cy="380" r="7" fill="#333"/>')
    r.append(f'  <circle cx="690" cy="380" r="14" fill="none" stroke="#333" stroke-width="1.5"/>')
    r.append(f'  <circle cx="690" cy="380" r="7" fill="#333"/>')

    # ── Transitions ──
    # init → 待确认
    tline(105, 145, 140, 145)
    # 待确认 → 已确认
    tline(250, 130, 360, 95)
    label(305, 100, '确认')
    # 待确认 → 已取消
    tline(195, 170, 510, 255)
    label(350, 230, '取消')
    # 已确认 → 已完成
    tline(470, 95, 580, 95)
    label(525, 82, '完成')
    # 已确认 → 咨询中
    tline(415, 120, 415, 240)
    label(435, 180, '开始咨询')
    # 已确认 → 已取消
    tline(430, 120, 580, 240)
    label(525, 175, '取消')
    # 咨询中 → 已完成
    tline(450, 260, 580, 120)
    label(540, 185, '完成')
    # 咨询中 → 已取消
    tline(470, 265, 580, 265)
    label(525, 282, '取消')
    # 已完成 → final
    tline(635, 120, 635, 366)
    # 已取消 → final
    tline(690, 290, 690, 366)

    r.append(end())
    return '\n'.join(r)


# ============================================================
# 4.3b 预约时序图
# ============================================================
def gen_booking_sequence():
    r = [svg(850, 520), arrow_def()]
    r.append(text(425, 22, '预约业务流程时序图', 16, '#333'))

    # Lifelines: user, ui, server, counselor
    cols = {'用户': 80, '用户端': 280, '系统服务端': 480, '咨询师端': 680}
    lifeline_top = 50
    for name, x in cols.items():
        r.append(rect(x-55, lifeline_top, 110, 30, '#E3F2FD', '#1976D2', name, 12, '#1565C0'))
        r.append(dashed_line(x, 80, x, 490))

    def msg(y, fr, to, label, is_return=False):
        fx = cols[fr]
        tx = cols[to]
        style = 'stroke-dasharray="6,3"' if is_return else ''
        arrow = ' marker-end="url(#a)"' if not is_return else ''
        r.append(f'  <line x1="{fx}" y1="{y}" x2="{tx}" y2="{y}" stroke="{"#999" if is_return else "#555"}" stroke-width="1.5" {style}{arrow}/>')
        # Label above line
        mx = (fx + tx) // 2
        r.append(f'  <rect x="{mx-90}" y="{y-18}" width="180" height="16" fill="white"/>')
        r.append(f'  <text x="{mx}" y="{y-6}" text-anchor="middle" font-size="11" fill="{"#777" if is_return else "#333"}">{label}</text>')

    y = 100
    step = 38
    msg(y, '用户', '用户端', '1. 浏览咨询师详情'); y += step
    msg(y, '用户', '用户端', '2. 选择病情标签与预约时间'); y += step
    msg(y, '用户端', '系统服务端', '3. POST 提交预约请求'); y += step
    msg(y, '系统服务端', '系统服务端', '4. 校验排班与时段冲突'); y += step - 5  # self-call shorter
    msg(y+15, '系统服务端', '咨询师端', '5. 推送预约通知'); y += step
    msg(y, '咨询师端', '系统服务端', '6. 确认预约'); y += step
    msg(y, '系统服务端', '用户端', '7. 返回预约确认结果', True); y += step
    msg(y, '用户端', '用户', '8. 展示预约状态更新'); y += step

    r.append(end())
    return '\n'.join(r)


# ============================================================
# 4.4 聊天活动图
# ============================================================
def gen_chat_activity():
    r = [svg(550, 680), arrow_def()]
    r.append(text(275, 22, '聊天消息收发活动图', 16, '#333'))

    # Start
    r.append(f'  <circle cx="275" cy="50" r="10" fill="#333"/>')
    r.append(text(275, 70, '开始', 11))

    cx = 275  # center x
    bw = 210  # box width
    bh = 36   # box height

    # All activity nodes in a clean vertical column
    steps = [
        ('用户/咨询师进入聊天页面', '#E3F2FD', '#1565C0'),
        ('加载历史消息记录', '#E3F2FD', '#1565C0'),
        ('输入消息内容', '#FFF8E1', '#F57F17'),
        ('点击发送按钮', '#FFF8E1', '#F57F17'),
    ]

    # Draw nodes
    positions = []
    ny = 90
    for label, fill, stroke in steps:
        rect_x = cx - bw//2
        r.append(f'  <rect x="{rect_x}" y="{ny}" width="{bw}" height="{bh}" rx="6" fill="{fill}" stroke="{stroke}" stroke-width="1.5"/>')
        r.append(f'  <text x="{cx}" y="{ny + bh//2 + 4}" text-anchor="middle" font-size="12" fill="{stroke}">{label}</text>')
        positions.append((cx, ny, ny+bh))
        ny += bh + 20

    # Decision diamond: 预约有效?
    d_cx, d_cy = cx, ny + 25
    d_rx, d_ry = 55, 30
    r.append(f'  <polygon points="{d_cx},{d_cy-d_ry} {d_cx+d_rx},{d_cy} {d_cx},{d_cy+d_ry} {d_cx-d_rx},{d_cy}" fill="#FFF8E1" stroke="#F57F17" stroke-width="1.5"/>')
    r.append(f'  <text x="{d_cx}" y="{d_cy+4}" text-anchor="middle" font-size="11" fill="#F57F17">预约有效?</text>')
    d_bottom = d_cy + d_ry

    # Rest of nodes after decision
    first_rest_y = d_bottom + 20
    next_y = first_rest_y
    rest_steps = [
        ('系统存储消息并入数据库', '#E8F5E9', '#2E7D32'),
        ('即时推送通知给接收方', '#E8F5E9', '#2E7D32'),
        ('接收方页面展示新消息', '#F3E5F5', '#7B1FA2'),
    ]
    for label, fill, stroke in rest_steps:
        rect_x = cx - bw//2
        r.append(f'  <rect x="{rect_x}" y="{next_y}" width="{bw}" height="{bh}" rx="6" fill="{fill}" stroke="{stroke}" stroke-width="1.5"/>')
        r.append(f'  <text x="{cx}" y="{next_y + bh//2 + 4}" text-anchor="middle" font-size="12" fill="{stroke}">{label}</text>')
        positions.append((cx, next_y, next_y+bh))
        next_y += bh + 20

    # End node
    end_y = next_y + 5
    r.append(f'  <circle cx="{cx}" cy="{end_y}" r="10" fill="#333"/>')
    r.append(f'  <circle cx="{cx}" cy="{end_y}" r="16" fill="none" stroke="#333" stroke-width="1.5"/>')
    r.append(text(cx, end_y + 28, '结束', 11))

    # All flow arrows
    # Start → first node
    r.append(arrow_line(cx, 50, cx, 90))
    # Between nodes before decision
    for i in range(len(steps)-1):
        _, y1_bottom = positions[i][1], positions[i][2]
        _, y2_top = positions[i+1][1], positions[i+1][1]
        r.append(arrow_line(cx, y1_bottom, cx, y2_top))
    # Last step → decision
    r.append(arrow_line(cx, positions[3][2], d_cx, d_cy - d_ry))
    # Decision → first rest step
    r.append(arrow_line(d_cx, d_cy + d_ry, cx, first_rest_y))
    # Between rest steps
    for i in range(4, len(positions)-1):
        r.append(arrow_line(cx, positions[i][2], cx, positions[i+1][1]))
    # Last rest step → end
    r.append(arrow_line(cx, positions[-1][2], cx, end_y - 16))

    # "否" branch from decision (out right side)
    r.append(f'  <line x1="{d_cx + d_rx}" y1="{d_cy}" x2="{d_cx + d_rx + 40}" y2="{d_cy}" stroke="#888" stroke-width="1" stroke-dasharray="4,3"/>')
    r.append(f'  <text x="{d_cx + d_rx + 44}" y="{d_cy + 4}" font-size="10" fill="#888">否，返回提示</text>')

    r.append(end())
    return '\n'.join(r)


# ============================================================
# 4.5 推荐算法流程图
# ============================================================
def gen_recommend_flow():
    r = [svg(630, 600), arrow_def()]
    r.append(text(315, 22, '咨询师推荐算法流程', 16, '#333'))

    steps = [
        (215, 50, 200, 32, '输入：用户病情标签', '#E3F2FD', '#1565C0'),
        (215, 100, 200, 32, '第一轮遍历咨询师\n求 maxRating, maxLogCount', '#FFF8E1', '#F57F17'),
        (215, 155, 200, 32, '第二轮遍历：\n计算匹配度 (命中/总数)×50', '#FFF8E1', '#F57F17'),
        (215, 210, 200, 32, '计算评分 (avg/max)×30', '#FFF8E1', '#F57F17'),
        (215, 265, 200, 32, '计算经验 log(1+cnt)/max×20', '#FFF8E1', '#F57F17'),
        (215, 325, 200, 32, '综合得分=匹配+评分+经验', '#E8F5E9', '#2E7D32'),
        (215, 385, 200, 32, '降序排列，返回推荐列表', '#F3E5F5', '#7B1FA2'),
    ]
    for sx, sy, sw, sh, sl, sf, sc in steps:
        r.append(f'  <rect x="{sx}" y="{sy}" width="{sw}" height="{sh}" rx="6" fill="{sf}" stroke="{sc}" stroke-width="1.5"/>')
        lines = sl.split('\n')
        lh = 16
        tsy = sy + (sh - len(lines)*lh)//2 + 12
        for i, line in enumerate(lines):
            r.append(f'  <text x="{sx+sw//2}" y="{tsy + i*lh}" text-anchor="middle" font-size="12" fill="{sc}">{line}</text>')

    # Arrows
    for i in range(len(steps)-1):
        y1 = steps[i][1] + steps[i][3]
        y2 = steps[i+1][1]
        r.append(arrow_line(315, y1, 315, y2))

    # Formula side note
    r.append(rect(430, 150, 175, 200, '#FAFAFA', '#E0E0E0', '', 8))
    r.append(text(517, 175, '权重配比', 13, '#333'))
    r.append(text(517, 200, '匹配度 50%', 12, '#1565C0'))
    r.append(text(517, 225, '评分   30%', 12, '#2E7D32'))
    r.append(text(517, 250, '经验   20%', 12, '#E65100'))
    r.append(text(517, 290, '总分范围', 11, '#888'))
    r.append(text(517, 310, '约 0 ~ 100', 11, '#888'))

    r.append(end())
    return '\n'.join(r)


# ============================================================
# 4.6 E-R图
# ============================================================
def gen_er():
    r = [svg(950, 600), arrow_def()]
    r.append(text(475, 20, '数据库E-R图（核心实体）', 16, '#333'))

    def er_entity(x, y, name, fields, color='#1565C0'):
        """Draw entity box with fields."""
        fh = 20
        h = 30 + len(fields) * fh + 10
        # Body rect
        r.append(f'  <rect x="{x}" y="{y}" width="160" height="{h}" fill="white" stroke="{color}" stroke-width="1.5"/>')
        # Header rect
        r.append(f'  <rect x="{x}" y="{y}" width="160" height="30" fill="{color}"/>')
        r.append(f'  <text x="{x+80}" y="{y+20}" text-anchor="middle" font-size="13" fill="white">{name}</text>')
        # Fields
        for j, (fname, fkey) in enumerate(fields):
            fy = y + 35 + j * fh
            r.append(f'  <text x="{x+8}" y="{fy+14}" font-size="11" fill="#555">{fname}</text>')
            if fkey:
                r.append(f'  <text x="{x+150}" y="{fy+14}" text-anchor="end" font-size="10" fill="#999">{fkey}</text>')
        return (x, y, 160, h)

    # Entities
    user = er_entity(40, 50, 'user', [
        ('id BIGINT (PK)', ''),
        ('openid VARCHAR', ''),
        ('nickname VARCHAR', ''),
        ('phone VARCHAR', ''),
        ('create_time DATETIME', ''),
    ])

    counselor = er_entity(410, 50, 'counselor', [
        ('id BIGINT (PK)', ''),
        ('name VARCHAR', ''),
        ('expertise VARCHAR', ''),
        ('qualification VARCHAR', ''),
        ('status TINYINT', ''),
    ])

    appointment = er_entity(200, 280, 'appointment', [
        ('id BIGINT (PK)', ''),
        ('user_id BIGINT (FK)', '→ user'),
        ('counselor_id BIGINT (FK)', '→ counselor'),
        ('appointment_time DATETIME', ''),
        ('problem TEXT', ''),
        ('status TINYINT', ''),
    ])

    review = er_entity(40, 420, 'review', [
        ('id BIGINT (PK)', ''),
        ('user_id BIGINT (FK)', ''),
        ('counselor_id BIGINT (FK)', ''),
        ('appointment_id BIGINT (FK)', ''),
        ('rating INT', ''),
        ('content TEXT', ''),
    ])

    chat = er_entity(460, 280, 'chat_message', [
        ('id BIGINT (PK)', ''),
        ('user_id BIGINT (FK)', ''),
        ('counselor_id BIGINT (FK)', ''),
        ('appointment_id BIGINT (FK)', ''),
        ('sender VARCHAR', ''),
        ('content TEXT', ''),
    ])

    rec = er_entity(440, 440, 'consultation_record', [
        ('id BIGINT (PK)', ''),
        ('appointment_id BIGINT (FK)', ''),
        ('user_id BIGINT (FK)', ''),
        ('counselor_id BIGINT (FK)', ''),
        ('diagnosis TEXT', ''),
        ('suggestions TEXT', ''),
    ])

    schedule = er_entity(700, 160, 'schedule', [
        ('id BIGINT (PK)', ''),
        ('counselor_id BIGINT (FK)', '→ counselor'),
        ('date DATE', ''),
        ('start_time TIME', ''),
        ('end_time TIME', ''),
    ])

    # Relationships
    def rel_line(x1, y1, x2, y2, lab, lab_y=None):
        r.append(f'  <line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" stroke="#888" stroke-width="1"/>')
        if lab_y is None:
            lab_y = (y1 + y2) // 2
        mx = (x1 + x2) // 2
        # Offset label slightly from midpoint to avoid overlapping entity boxes
        r.append(f'  <text x="{mx+8}" y="{lab_y-6}" text-anchor="middle" font-size="9" fill="#888">{lab}</text>')

    # User → Appointment
    rel_line(120, 230, 280, 280, '1:N', 260)
    # User → Review
    rel_line(120, 280, 120, 420, '1:N', 370)
    # Counselor → Appointment
    rel_line(490, 230, 360, 280, '1:N', 260)
    # Counselor → Schedule
    rel_line(490, 230, 700, 200, '1:N', 210)
    # Appointment → Review
    rel_line(150, 360, 120, 420, '1:N', 400)
    # Appointment → Chat
    rel_line(360, 320, 460, 320, '1:N', 305)
    # Appointment → Record
    rel_line(360, 360, 440, 440, '1:N', 415)
    # Counselor → Chat
    rel_line(570, 230, 540, 280, '1:N', 260)
    # User → Chat
    rel_line(120, 280, 460, 320, '1:N', 310)

    r.append(end())
    return '\n'.join(r)


if __name__ == '__main__':
    diagrams = {
        'architecture.svg': gen_architecture,
        'module_structure.svg': gen_module_structure,
        'appointment_state.svg': gen_appointment_state,
        'booking_sequence.svg': gen_booking_sequence,
        'chat_activity.svg': gen_chat_activity,
        'recommend_flow.svg': gen_recommend_flow,
        'er_diagram.svg': gen_er,
    }
    for name, fn in diagrams.items():
        path = os.path.join(OUT, name)
        with open(path, 'w', encoding='utf-8') as f:
            f.write(fn())
        print(f'Generated: {name}')
