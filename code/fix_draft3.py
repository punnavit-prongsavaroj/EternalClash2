import re

with open('src/main/resources/static/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

# Make sure the header is added back
if 'เลือกขุนพลคู่กาย' not in html:
    old_start = '''<div class="glass-panel" style="padding: 40px; text-align: center; border-radius: 12px; width: 80%; max-width: 900px; margin-top: 50px; z-index: 10;">
            <div id="marshal-single-card" class="marshal-card"'''
    
    new_start = '''<div class="glass-panel" style="padding: 40px; text-align: center; border-radius: 12px; width: 80%; max-width: 900px; margin-top: 50px; z-index: 10;">
            <h2 style="color: #f39c12; font-size: 2.5rem; margin-bottom: 25px;">เลือกขุนพลคู่กาย</h2>
            <div id="marshal-single-card" class="marshal-card"'''
    
    html = html.replace(old_start, new_start)
    
    with open('src/main/resources/static/index.html', 'w', encoding='utf-8') as f:
        f.write(html)
        print("Header restored.")
else:
    print("Header already exists.")
