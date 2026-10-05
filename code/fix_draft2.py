import re

# Update HTML card style (Put header back outside)
with open('src/main/resources/static/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

old_glass_panel_start = '''<div class="glass-panel" style="padding: 40px; text-align: center; border-radius: 12px; width: 80%; max-width: 900px; margin-top: 50px; z-index: 10;">
            <div id="marshal-single-card" class="marshal-card"'''

new_glass_panel_start = '''<div class="glass-panel" style="padding: 40px; text-align: center; border-radius: 12px; width: 80%; max-width: 900px; margin-top: 50px; z-index: 10;">
            <h2 style="color: #f39c12; font-size: 2.5rem; margin-bottom: 25px;">เลือกขุนพลคู่กาย</h2>
            <div id="marshal-single-card" class="marshal-card"'''

html = html.replace(old_glass_panel_start, new_glass_panel_start)

with open('src/main/resources/static/index.html', 'w', encoding='utf-8') as f:
    f.write(html)


# Update JS render function (Remove header from inside card)
with open('src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

old_render = '''function renderCurrentDraft() {
    const card = document.getElementById('marshal-single-card');
    const m = MARSHAL_POOL[currentDraftIndex];
    card.innerHTML = 
        '<img src="/Marshal/' + m.img + '" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; object-fit: cover; object-position: top; z-index: 1;">' +
        '<div style="position: absolute; top: 0; left: 0; width: 100%; padding: 25px 15px 40px; background: linear-gradient(to bottom, rgba(0,0,0,0.9) 10%, rgba(0,0,0,0.5) 60%, transparent); z-index: 2; box-sizing: border-box; text-align: center;">' +
            '<h2 style="color: #f39c12; font-size: 2rem; margin: 0 0 5px 0; text-shadow: 2px 2px 4px #000;">เลือกขุนพลคู่กาย</h2>' +
            '<p style="color: white; font-size: 1rem; margin: 0; text-shadow: 1px 1px 2px #000;">โปรดพิจารณาขุนพลที่จะนำทัพของคุณ</p>' +
        '</div>' +
        '<div style="position: absolute; bottom: 0; left: 0; width: 100%; padding: 50px 20px 20px; background: linear-gradient(to top, rgba(0,0,0,1) 10%, rgba(0,0,0,0.85) 60%, transparent); z-index: 2; box-sizing: border-box; text-align: center;">' +
            '<h3 style="font-size: 2.8rem; margin-bottom: 12px; color: #f39c12; text-shadow: 2px 2px 4px #000; letter-spacing: 2px;">' + m.name + '</h3>' +
            '<p style="font-size: 1.05rem; color: #2ecc71; text-shadow: 1px 1px 3px #000; margin-bottom: 6px;">✅ จุดเด่น: ' + m.skill + '</p>' +
            '<p style="font-size: 1.05rem; color: #e74c3c; text-shadow: 1px 1px 3px #000; margin: 0;">⚠️ ข้อเสีย: ' + m.drawback + '</p>' +
        '</div>';
}'''

new_render = '''function renderCurrentDraft() {
    const card = document.getElementById('marshal-single-card');
    const m = MARSHAL_POOL[currentDraftIndex];
    card.innerHTML = 
        '<img src="/Marshal/' + m.img + '" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; object-fit: cover; object-position: top; z-index: 1;">' +
        '<div style="position: absolute; bottom: 0; left: 0; width: 100%; padding: 50px 20px 20px; background: linear-gradient(to top, rgba(0,0,0,1) 10%, rgba(0,0,0,0.85) 60%, transparent); z-index: 2; box-sizing: border-box; text-align: center;">' +
            '<h3 style="font-size: 2.8rem; margin-bottom: 12px; color: #f39c12; text-shadow: 2px 2px 4px #000; letter-spacing: 2px;">' + m.name + '</h3>' +
            '<p style="font-size: 1.05rem; color: #2ecc71; text-shadow: 1px 1px 3px #000; margin-bottom: 6px;">✅ จุดเด่น: ' + m.skill + '</p>' +
            '<p style="font-size: 1.05rem; color: #e74c3c; text-shadow: 1px 1px 3px #000; margin: 0;">⚠️ ข้อเสีย: ' + m.drawback + '</p>' +
        '</div>';
}'''

js = js.replace(old_render, new_render)

with open('src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)
