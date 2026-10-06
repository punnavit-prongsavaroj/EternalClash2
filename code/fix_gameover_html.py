import re

with open('src/main/resources/static/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

# Append Game Over screen before body ends
game_over_html = '''
    <div id="game-over-screen" style="display: none; position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; flex-direction: column; align-items: center; justify-content: center; background: rgba(0,0,0,0.95); z-index: 100;">
        <h1 style="color: #f1c40f; font-size: 5rem; text-shadow: 0 0 30px #f39c12; margin-bottom: 20px;">จบสงคราม!</h1>
        <h2 style="color: white; font-size: 2.5rem; margin-bottom: 10px;">ผู้ชนะรอดชีวิต: <span id="winner-name-display" style="color: #2ecc71;">-</span></h2>
        <h3 style="color: #bdc3c7; font-size: 1.8rem; margin-bottom: 50px;">นำทัพโดย: <span id="winner-marshal-display" style="color: #f39c12;">-</span></h3>
        <div style="display: flex; gap: 20px;">
            <button class="action-btn recruit-btn" style="font-size: 1.2rem; padding: 15px 30px;" onclick="returnToLobby()">🏠 กลับล็อบบี้ (หาห้องใหม่)</button>
            <button class="action-btn produce-btn" style="font-size: 1.2rem; padding: 15px 30px;" onclick="logout()">🚪 กลับหน้าเมนูหลัก</button>
        </div>
    </div>
</body>
</html>'''

html = html.replace('</body>\n</html>', game_over_html)
html = html.replace('</body></html>', game_over_html)

with open('src/main/resources/static/index.html', 'w', encoding='utf-8') as f:
    f.write(html)
