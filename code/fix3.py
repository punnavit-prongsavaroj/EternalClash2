import re

# 1. Update HTML
with open('src/main/resources/static/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

# Wrap command buttons in a container
command_panel_old = '''<h3 style="color: #f39c12; text-align: center; margin-bottom: 20px; font-size: 1.5rem;">ออกคำสั่งประจำเทิร์น</h3>
                    <button class="action-btn produce-btn" onclick="submitAction('PRODUCE_FOOD')">🌾 ทำฟาร์ม (เพิ่มเสบียง)</button>
                    <button class="action-btn recruit-btn" onclick="submitAction('RECRUIT_SOLDIERS')">⚔️ เกณฑ์ทหาร (ใช้เสบียง)</button>
                    <button class="action-btn attack-btn" onclick="openAttackModal()">🚀 ส่งกองทัพโจมตี</button>'''
command_panel_new = '''<h3 style="color: #f39c12; text-align: center; margin-bottom: 20px; font-size: 1.5rem;">ออกคำสั่งประจำเทิร์น</h3>
                    <div id="action-buttons-container">
                        <button class="action-btn produce-btn" onclick="submitAction('PRODUCE_FOOD')">🌾 ทำฟาร์ม (เพิ่มเสบียง)</button>
                        <button class="action-btn recruit-btn" onclick="submitAction('RECRUIT_SOLDIERS')">⚔️ เกณฑ์ทหาร (ใช้เสบียง)</button>
                        <button class="action-btn attack-btn" onclick="openAttackModal()">🚀 ส่งกองทัพโจมตี</button>
                    </div>'''
html = html.replace(command_panel_old, command_panel_new)

# Add Log Button and Panel right before Attack Modal
log_ui = '''
        <!-- Log UI -->
        <button class="action-btn" style="position: absolute; bottom: 20px; right: 20px; z-index: 15; background: #34495e; padding: 10px 20px; font-size: 1.1rem; width: auto;" onclick="toggleLog()">📜 บันทึกสงคราม</button>
        <div id="log-panel" class="glass-panel" style="display: none; position: absolute; bottom: 70px; right: 20px; width: 350px; max-height: 400px; z-index: 15; flex-direction: column; padding: 20px;">
            <h3 style="color: #f39c12; margin-bottom: 10px; text-align: center;">เหตุการณ์ล่าสุด</h3>
            <div id="log-content" style="flex: 1; overflow-y: auto; color: white; font-size: 1rem; text-align: left; padding: 10px; background: rgba(0,0,0,0.5); border-radius: 8px; min-height: 200px;">
                <p style="color: #bdc3c7;"><i>ยังไม่มีเหตุการณ์...</i></p>
            </div>
        </div>
        
        <!-- Attack Modal -->'''
html = html.replace('<!-- Attack Modal -->', log_ui)

with open('src/main/resources/static/index.html', 'w', encoding='utf-8') as f:
    f.write(html)

# 2. Update JS
with open('src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

submit_action_old = '''function submitAction(actionType) {
    const buttons = document.querySelectorAll('.command-panel .action-btn');
    buttons.forEach(btn => btn.style.display = 'none');
    document.getElementById('action-locked-msg').style.display = 'block';
    alert('ส่งคำสั่ง: ' + actionType + ' เรียบร้อยแล้ว (รอระบบ API)');
}'''

submit_action_new = '''function toggleLog() {
    const panel = document.getElementById('log-panel');
    panel.style.display = (panel.style.display === 'none' || panel.style.display === '') ? 'flex' : 'none';
}

let turnCounter = 1;

function submitAction(actionType) {
    // ซ่อนปุ่มทั้งหมด
    document.getElementById('action-buttons-container').style.display = 'none';
    // แสดงข้อความรอ
    document.getElementById('action-locked-msg').style.display = 'block';
    
    // จำลองการรอผู้เล่นคนอื่น 5 วินาที ก่อนขึ้นเทิร์นใหม่
    setTimeout(simulateNextTurn, 5000);
}

function simulateNextTurn() {
    turnCounter++;
    
    // ซ่อนข้อความรอ และ นำปุ่มกลับมาแสดงใหม่
    document.getElementById('action-locked-msg').style.display = 'none';
    document.getElementById('action-buttons-container').style.display = 'block';
    
    // อัปเดต Log
    const logContent = document.getElementById('log-content');
    if(logContent.innerHTML.includes('ยังไม่มีเหตุการณ์')) logContent.innerHTML = '';
    
    const newLog = "<p style='margin-bottom: 8px;'><strong style='color:#f39c12'>[เทิร์น " + (turnCounter-1) + "]</strong> กองทัพศัตรูเคลื่อนไหว...</p>" +
                   "<p style='margin-bottom: 8px;'><strong style='color:#f39c12'>[เทิร์น " + (turnCounter-1) + "]</strong> เสบียงของคุณเพิ่มขึ้น/ลดลง</p><hr style='border-color: #7f8c8d; margin: 10px 0;'>";
    logContent.innerHTML = newLog + logContent.innerHTML;
    
    // เปิดหน้า Log ให้เด้งขึ้นมาดูอัตโนมัติ
    document.getElementById('log-panel').style.display = 'flex';
    
    // อัปเดตตัวเลขเทิร์นบน UI
    const seasons = ['spring', 'summer', 'autumn', 'winter'];
    const timeOfDay = ['day', 'night'];
    const s = seasons[Math.floor(Math.random() * seasons.length)];
    const t = timeOfDay[Math.floor(Math.random() * timeOfDay.length)];
    document.getElementById('current-turn-display').innerText = 'เทิร์นที่: ' + turnCounter + ' (' + s + ' - ' + t + ')';
}'''

js = js.replace(submit_action_old, submit_action_new)

with open('src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)
