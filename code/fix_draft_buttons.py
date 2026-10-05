import re

with open('src/main/resources/static/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

# Fix draft-buttons-wrapper
html = re.sub(
    r'(<div style="margin-top: 30px; display: flex; justify-content: center; gap: 20px;">\s*<button class="action-btn produce-btn" onclick="confirmMarshal\(\)">)',
    r'<div id="draft-buttons-wrapper" style="margin-top: 30px; display: flex; justify-content: center; gap: 20px;">\n                  <button class="action-btn produce-btn" onclick="confirmMarshal()">',
    html
)

with open('src/main/resources/static/index.html', 'w', encoding='utf-8') as f:
    f.write(html)
