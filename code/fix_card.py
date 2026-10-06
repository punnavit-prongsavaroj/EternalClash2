import re

# Update HTML card style
with open('src/main/resources/static/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

old_card = 'id="marshal-single-card" class="marshal-card" style="margin: 0 auto; width: 300px; cursor: default;"'
new_card = 'id="marshal-single-card" class="marshal-card" style="margin: 0 auto; width: 350px; height: 500px; padding: 0; overflow: hidden; position: relative; border: 4px solid #f39c12; border-radius: 12px; cursor: default; box-shadow: 0 10px 30px rgba(0,0,0,0.8);"'
html = html.replace(old_card, new_card)

with open('src/main/resources/static/index.html', 'w', encoding='utf-8') as f:
    f.write(html)

# Update JS render function
with open('src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

old_render = '''function renderCurrentDraft() {
    const card = document.getElementById('marshal-single-card');
    const m = MARSHAL_POOL[currentDraftIndex];
    card.innerHTML = '<img src="/Marshal/' + m.img + '" style="width: 100%; height: 250px; object-fit: cover; object-position: top; border-radius: 8px; margin-bottom: 15px; border: 2px solid #34495e;">' +
                     '<h3 style="font-size: 1.8rem;">' + m.name + '</h3><p style="font-size: 1.1rem;">สกิล: ' + m.skill + '</p>';
}'''

new_render = '''function renderCurrentDraft() {
    const card = document.getElementById('marshal-single-card');
    const m = MARSHAL_POOL[currentDraftIndex];
    card.innerHTML = '<img src="/Marshal/' + m.img + '" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; object-fit: cover; object-position: top; z-index: 1;">' +
                     '<div style="position: absolute; bottom: 0; left: 0; width: 100%; padding: 40px 20px 20px; background: linear-gradient(to top, rgba(0,0,0,1) 10%, rgba(0,0,0,0.7) 60%, transparent); z-index: 2; box-sizing: border-box; text-align: center;">' +
                     '<h3 style="font-size: 2.8rem; margin-bottom: 5px; color: #f39c12; text-shadow: 2px 2px 4px #000; letter-spacing: 2px;">' + m.name + '</h3>' +
                     '<p style="font-size: 1.3rem; color: #ecf0f1; text-shadow: 1px 1px 3px #000;">สกิล: ' + m.skill + '</p>' +
                     '</div>';
}'''

js = js.replace(old_render, new_render)

with open('src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)
