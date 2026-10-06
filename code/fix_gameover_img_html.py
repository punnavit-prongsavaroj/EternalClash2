import re

with open('src/main/resources/static/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

old_game_over = '''<h2 style="color: white; font-size: 2.5rem; margin-bottom: 10px;">ผู้ชนะรอดชีวิต: <span id="winner-name-display" style="color: #2ecc71;">-</span></h2>
        <h3 style="color: #bdc3c7; font-size: 1.8rem; margin-bottom: 50px;">นำทัพโดย: <span id="winner-marshal-display" style="color: #f39c12;">-</span></h3>'''

new_game_over = '''<h2 style="color: white; font-size: 2.5rem; margin-bottom: 10px;">ผู้ชนะรอดชีวิต: <span id="winner-name-display" style="color: #2ecc71;">-</span></h2>
        <div style="position: relative; width: 250px; height: 350px; border: 3px solid #f39c12; border-radius: 12px; overflow: hidden; margin-bottom: 15px; box-shadow: 0 10px 30px rgba(0,0,0,0.8);">
            <img id="winner-marshal-img" src="" style="width: 100%; height: 100%; object-fit: cover; object-position: top; display: none;">
            <div style="position: absolute; bottom: 0; width: 100%; padding: 10px; background: rgba(0,0,0,0.8); text-align: center; box-sizing: border-box;">
                <h3 style="color: #bdc3c7; font-size: 1.3rem; margin: 0;">ขุนพล: <span id="winner-marshal-display" style="color: #f39c12;">-</span></h3>
            </div>
        </div>
        <div style="margin-bottom: 40px;"></div>'''

html = html.replace(old_game_over, new_game_over)

with open('src/main/resources/static/index.html', 'w', encoding='utf-8') as f:
    f.write(html)
