import re

with open('src/main/resources/static/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

new_login_bg = '''    <div id="login-screen" class="screen">
        <div class="lobby-bg"></div>'''

old_login_bg = '''    <div id="login-screen" class="screen">
        <video autoplay muted loop playsinline id="bg-video">
            <source src="/gif/1004.mp4" type="video/mp4">
            เบราว์เซอร์ของคุณไม่รองรับวิดีโอนี้
        </video>'''

html = html.replace(new_login_bg, old_login_bg)

with open('src/main/resources/static/index.html', 'w', encoding='utf-8') as f:
    f.write(html)
