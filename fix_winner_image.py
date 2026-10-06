import re

with open('code/src/main/resources/static/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

# Replace the marshal card with a simple text element
old_card = '''<div style="position: relative; width: 250px; height: 350px; border: 3px solid #f39c12; border-radius: 12px; overflow: hidden; margin-bottom: 15px; box-shadow: 0 10px 30px rgba(0,0,0,0.8);">
                <img id="winner-marshal-img" src="" style="width: 100%; height: 100%; object-fit: cover; object-position: top; display: none;">
                <div style="position: absolute; bottom: 0; width: 100%; padding: 10px; background: rgba(0,0,0,0.8); text-align: center; box-sizing: border-box;">
                    <h3 style="color: #bdc3c7; font-size: 1.3rem; margin: 0;">ขุนพล: <span id="winner-marshal-display" style="color: #f39c12;">-</span></h3>
                </div>
            </div>
            <div style="margin-bottom: 40px;"></div>'''

new_text = '''<h3 style="color: #f39c12; font-size: 2rem; margin-bottom: 40px; text-shadow: 0 2px 10px rgba(0,0,0,0.8);">ขุนพลคู่กาย: <span id="winner-marshal-display" style="color: white;">-</span></h3>'''

if '<img id="winner-marshal-img"' in html:
    html = html.replace(old_card, new_text)
    with open('code/src/main/resources/static/index.html', 'w', encoding='utf-8') as f:
        f.write(html)
    print("HTML updated")
else:
    print("HTML already updated or card not found")

with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

# Remove JS logic that updates the image
old_js = '''                const imgName = MARSHAL_IMGS[winner.marshalName] || 'Jo-Sho.jpg';
                document.getElementById('winner-marshal-img').src = '/Marshal/' + imgName;
                document.getElementById('winner-marshal-img').style.display = 'block';'''

if "document.getElementById('winner-marshal-img')" in js:
    js = js.replace(old_js, '')
    with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
        f.write(js)
    print("JS updated")
else:
    print("JS already updated")
