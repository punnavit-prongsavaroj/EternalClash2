import re

with open('src/main/resources/static/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

# Make command-panel absolute center and give it an ID
old_panel = '''<div class="command-panel glass-panel" style="pointer-events: auto;">
                    <h3 style="color: #f39c12; text-align: center; margin-bottom: 20px; font-size: 1.5rem;">ออกคำสั่งประจำเทิร์น</h3>
                    <div id="action-buttons-container">
                        <button class="action-btn produce-btn" onclick="submitAction('PRODUCE_FOOD')">🌾 ทำฟาร์ม (เพิ่มเสบียง)</button>
                        <button class="action-btn recruit-btn" onclick="submitAction('RECRUIT_SOLDIERS')">⚔️ เกณฑ์ทหาร (ใช้เสบียง)</button>
                        <button class="action-btn attack-btn" onclick="openAttackModal()">🚀 ส่งกองทัพโจมตี</button>
                    </div>
                    <div id="action-locked-msg" style="display: none; color: #f39c12; margin-top: 20px; text-align: center; font-size: 1.2rem; font-weight: bold;">
                        ส่งคำสั่งแล้ว รอเทิร์นถัดไป...
                    </div>
                </div>'''

new_panel = '''<div id="command-panel" class="command-panel glass-panel" style="pointer-events: auto; position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); z-index: 20;">
                    <h3 style="color: #f39c12; text-align: center; margin-bottom: 20px; font-size: 1.5rem;">ออกคำสั่งประจำเทิร์น</h3>
                    <div id="action-buttons-container">
                        <button class="action-btn produce-btn" onclick="submitAction('PRODUCE_FOOD')">🌾 ทำฟาร์ม (เพิ่มเสบียง)</button>
                        <button class="action-btn recruit-btn" onclick="submitAction('RECRUIT_SOLDIERS')">⚔️ เกณฑ์ทหาร (ใช้เสบียง)</button>
                        <button class="action-btn attack-btn" onclick="openAttackModal()">🚀 ส่งกองทัพโจมตี</button>
                    </div>
                </div>'''

html = html.replace(old_panel, new_panel)

with open('src/main/resources/static/index.html', 'w', encoding='utf-8') as f:
    f.write(html)
    
# Update JS
with open('src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

# 1. Hide the whole command panel instead of just buttons
js = js.replace("document.getElementById('action-buttons-container').style.display = 'none';", "document.getElementById('command-panel').style.display = 'none';")
js = js.replace("document.getElementById('action-locked-msg').style.display = 'block';", "")

# 2. Show the whole command panel on new turn
js = js.replace("document.getElementById('action-locked-msg').style.display = 'none';", "")
js = js.replace("document.getElementById('action-buttons-container').style.display = 'block';", "document.getElementById('command-panel').style.display = 'flex';")

# 3. Remove dots from own castle
js = js.replace("if (player.incoming > 0) {", "if (player.incoming > 0 && !player.isMe) {")

with open('src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)
