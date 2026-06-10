from datetime import date, timedelta

schedules = {
    1: [(1,'09:00','12:00'), (1,'14:00','17:00'), (3,'09:00','12:00'), (3,'14:00','17:00')],
    2: [(2,'09:00','12:00'), (2,'14:00','17:00'), (4,'09:00','12:00'), (4,'14:00','17:00')],
    3: [(3,'09:00','12:00'), (3,'14:00','17:00'), (5,'09:00','12:00'), (5,'14:00','17:00')],
    4: [(4,'09:00','12:00'), (6,'09:00','12:00')],
    5: [(1,'09:00','12:00'), (5,'14:00','17:00')],
    6: [(2,'09:00','12:00'), (4,'14:00','17:00')],
    7: [(3,'14:00','17:00'), (6,'09:00','12:00')],
    8: [(1,'14:00','17:00'), (5,'09:00','12:00')],
    9: [(2,'14:00','17:00'), (4,'09:00','12:00')],
    10: [(6,'14:00','17:00'), (7,'09:00','12:00')],
}

start = date(2026, 5, 18)
end = date(2026, 6, 14)

lines = []
lines.append("USE psychology_db;")
lines.append("")
lines.append("-- 全量周期性排班: 2026-05-18 至 2026-06-14 (4周)")
lines.append("DELETE FROM schedule;")
lines.append("")

for cid, slots in schedules.items():
    values = []
    d = start
    while d <= end:
        iso_day = d.isoweekday()
        for (day, st, et) in slots:
            if iso_day == day:
                values.append(f"({cid}, '{d}', '{st}:00', '{et}:00', 1)")
        d += timedelta(days=1)
    if values:
        lines.append(f"-- 咨询师{cid}")
        lines.append("INSERT INTO schedule (counselor_id, date, start_time, end_time, is_available) VALUES")
        lines.append(",\n".join(values) + ";")
        lines.append("")

f = open("C:/Users/20245/WeChatProjects/psychology-final/database/clean_weekly_schedule.sql", "w", encoding="utf-8")
f.write("\n".join(lines))
f.close()
print("Done. Generated SQL file.")
