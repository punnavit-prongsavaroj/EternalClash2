import re

with open('src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

# Update log output logic
log_new = '''const actions = ['ทำฟาร์ม', 'เกณฑ์ทหาร', 'ส่งกองทัพโจมตี'];
      const enemyAAction = actions[Math.floor(Math.random()*actions.length)];
      const enemyBAction = actions[Math.floor(Math.random()*actions.length)];
      
      const newLog = "<div style='margin-bottom: 10px; line-height: 1.5;'>" +
                     "<strong style='color:#f39c12'>[สรุปแอคชัน เทิร์น " + (turnCounter-1) + "]</strong><br>" +
                     "🏰 ข้าพเจ้า เลือก: <span style='color:#2ecc71'>" + myLastAction + "</span><br>" +
                     "🏰 ศัตรู A เลือก: <span style='color:#e74c3c'>" + enemyAAction + "</span><br>" +
                     "🏰 ศัตรู B เลือก: <span style='color:#e74c3c'>" + enemyBAction + "</span>" +
                     "</div><hr style='border-color: #7f8c8d; margin: 10px 0;'>";'''

js = re.sub(r'const newLog = ".*?<hr style=\'border-color: #7f8c8d; margin: 10px 0;\'>";', log_new, js, flags=re.DOTALL)

with open('src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)
