import re

# Update JS
with open('src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

# 1. Update the Radar rendering logic
renderMap_old = '''        let radarHTML = '';
        if (player.incoming > 0 && !player.isMe) {
            let dotsHTML = '';
            for(let i = 0; i < player.incoming; i++) {
                dotsHTML += '<div class="dot"></div>';
            }
            radarHTML = '<div class="radar-container" style="transform: translateY(-50%) rotate(' + angle + 'deg);">' + dotsHTML + '</div>';
        }'''

renderMap_new = '''        let radarHTML = '';
        if (player.incoming > 0) {
            let showRadar = true;
            if (randomTime === 'night' && !player.isMe) {
                showRadar = false; // กลางคืนมองไม่เห็นเรดาร์คนอื่น
            }
            if (showRadar) {
                let dotsHTML = '';
                if (player.incoming === 2) {
                    dotsHTML = '<div class="dot white"></div><div class="dot red"></div>';
                } else {
                    dotsHTML = '<div class="dot red"></div><div class="dot white"></div>';
                }
                radarHTML = '<div class="radar-container" style="transform: translateY(-50%) rotate(' + angle + 'deg);">' + dotsHTML + '</div>';
            }
        }'''
js = js.replace(renderMap_old, renderMap_new)

# 2. Update Submit Action to store action
js = js.replace("let turnCounter = 1;", "let turnCounter = 1;\nlet myLastAction = '';")
submit_old = "function submitAction(actionType) {"
submit_new = '''function submitAction(actionType) {
    if(actionType === 'PRODUCE_FOOD') myLastAction = 'ทำฟาร์ม';
    else if(actionType === 'RECRUIT_SOLDIERS') myLastAction = 'เกณฑ์ทหาร';
    else myLastAction = 'ส่งกองทัพโจมตี';'''
js = js.replace(submit_old, submit_new)

# 3. Update simulateNextTurn to log actions
log_old = '''const newLog = "<p style='margin-bottom: 8px;'><strong style='color:#f39c12'>[เทิร์น " + (turnCounter-1) + "]</strong> กองทัพศัตรูเคลื่อนไหว...</p>" +
                     "<p style='margin-bottom: 8px;'><strong style='color:#f39c12'>[เทิร์น " + (turnCounter-1) + "]</strong> เสบียงของคุณเพิ่มขึ้น/ลดลง</p><hr style='border-color: #7f8c8d; margin: 10px 0;'>";'''

log_new = '''const actions = ['ทำฟาร์ม', 'เกณฑ์ทหาร', 'ส่งกองทัพโจมตี'];
      const enemyAAction = actions[Math.floor(Math.random()*actions.length)];
      const enemyBAction = actions[Math.floor(Math.random()*actions.length)];
      
      const newLog = "<div style='margin-bottom: 10px; line-height: 1.5;'>" +
                     "<strong style='color:#f39c12'>[สรุปแอคชัน เทิร์น " + (turnCounter-1) + "]</strong><br>" +
                     "🏰 ข้าพเจ้า เลือก: <span style='color:#2ecc71'>" + myLastAction + "</span><br>" +
                     "🏰 ศัตรู A เลือก: <span style='color:#e74c3c'>" + enemyAAction + "</span><br>" +
                     "🏰 ศัตรู B เลือก: <span style='color:#e74c3c'>" + enemyBAction + "</span>" +
                     "</div><hr style='border-color: #7f8c8d; margin: 10px 0;'>";'''
js = js.replace(log_old, log_new)

with open('src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)
