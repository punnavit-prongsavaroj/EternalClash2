import re

with open('code/src/main/resources/static/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

pattern = r'<div id="game-over-screen".*?</div>\s*</body>'

new_game_over = '''<div id="game-over-screen" style="display: none; position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: black; z-index: 100;">
        <!-- วิดีโอพื้นหลังที่เล่นครั้งเดียวและค้างที่เฟรมสุดท้าย -->
        <video id="win-video" style="position: absolute; top: 0; left: 0; width: 100vw; height: 100vh; object-fit: cover; z-index: 1;"></video>
        
        <!-- UI ที่จะถูกซ่อนไว้ก่อนจนกว่าวิดีโอจะเล่นจบ -->
        <div id="win-ui" style="display: none; position: relative; z-index: 2; width: 100%; height: 100%; flex-direction: column; align-items: center; justify-content: center; background: rgba(0,0,0,0.4);">
            <h1 style="color: #f1c40f; font-size: 5rem; text-shadow: 0 0 30px #f39c12; margin-bottom: 20px;">จบสงคราม!</h1>
            <h2 style="color: white; font-size: 2.5rem; margin-bottom: 10px;">ผู้ชนะรอดชีวิต: <span id="winner-name-display" style="color: #2ecc71;">-</span></h2>
            <div style="position: relative; width: 250px; height: 350px; border: 3px solid #f39c12; border-radius: 12px; overflow: hidden; margin-bottom: 15px; box-shadow: 0 10px 30px rgba(0,0,0,0.8);">
                <img id="winner-marshal-img" src="" style="width: 100%; height: 100%; object-fit: cover; object-position: top; display: none;">
                <div style="position: absolute; bottom: 0; width: 100%; padding: 10px; background: rgba(0,0,0,0.8); text-align: center; box-sizing: border-box;">
                    <h3 style="color: #bdc3c7; font-size: 1.3rem; margin: 0;">ขุนพล: <span id="winner-marshal-display" style="color: #f39c12;">-</span></h3>
                </div>
            </div>
            <div style="margin-bottom: 40px;"></div>
            <div style="display: flex; gap: 20px;">
                <button class="action-btn recruit-btn" style="font-size: 1.2rem; padding: 15px 30px;" onclick="returnToLobby()">🏠 กลับล็อบบี้ (หาห้องใหม่)</button>
                <button class="action-btn produce-btn" style="font-size: 1.2rem; padding: 15px 30px;" onclick="logout()">🚪 กลับหน้าเมนูหลัก</button>
            </div>
        </div>
    </div>
</body>'''

html = re.sub(pattern, new_game_over, html, flags=re.DOTALL)

with open('code/src/main/resources/static/index.html', 'w', encoding='utf-8') as f:
    f.write(html)
        
print("HTML video element fixed")
