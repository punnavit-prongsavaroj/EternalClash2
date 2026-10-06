import re

with open('src/main/resources/static/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

# Update buttons wrapper
old_buttons = '''<div style="margin-top: 30px; display: flex; justify-content: center; gap: 20px;">
                  <button class="action-btn produce-btn" onclick="confirmMarshal()">✅ เลือกขุนพลคนนี้</button>
                  <button class="action-btn recruit-btn" onclick="rerollMarshal()">🔄 สุ่มเปลี่ยนคนใหม่</button>
              </div>'''

new_buttons = '''<div id="draft-buttons-wrapper" style="margin-top: 30px; display: flex; justify-content: center; gap: 20px;">
                  <button class="action-btn produce-btn" onclick="confirmMarshal()">✅ เลือกขุนพลคนนี้</button>
                  <button class="action-btn recruit-btn" onclick="rerollMarshal()">🔄 สุ่มเปลี่ยนคนใหม่</button>
              </div>'''

html = html.replace(old_buttons, new_buttons)

# Update draft waiting
old_waiting = '<div id="draft-waiting" style="display: none; margin-top: 20px; color: #f39c12; font-size: 1.2rem;">รอผู้เล่นอื่นเลือกขุนพล...</div>'
new_waiting = '<div id="draft-waiting" style="display: none; margin-top: 20px; color: #f39c12; font-size: 1.5rem; text-align: center;">รอผู้เล่นอื่นเลือกขุนพล...</div>'
html = html.replace(old_waiting, new_waiting)

with open('src/main/resources/static/index.html', 'w', encoding='utf-8') as f:
    f.write(html)
