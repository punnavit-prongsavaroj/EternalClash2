with open('src/main/resources/static/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

import re
matches = re.search(r'<div class="glass-panel" style="padding: 40px; text-align: center; border-radius: 12px; width: 80%; max-width: 900px; margin-top: 50px; z-index: 10;">.*?(<h2.*?>.*?</h2>)?.*?<div id="marshal-single-card"', html, re.DOTALL)
print(matches.group(0))
