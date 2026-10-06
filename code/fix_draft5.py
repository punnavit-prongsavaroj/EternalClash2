import re

with open('src/main/resources/static/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

# Forcefully insert the header before marshal-single-card if missing
if 'เลือกขุนพลคู่กาย' not in html:
    html = re.sub(
        r'(<div class="glass-panel" style="padding: 40px; text-align: center; border-radius: 12px; width: 80%; max-width: 900px; margin-top: 50px; z-index: 10;">\s*)(<div id="marshal-single-card")',
        r'\1<h2 style="color: #f39c12; font-size: 2.5rem; margin-bottom: 25px;">เลือกขุนพลคู่กาย</h2>\n            \2',
        html
    )
    with open('src/main/resources/static/index.html', 'w', encoding='utf-8') as f:
        f.write(html)
        print("Header forced via regex.")
