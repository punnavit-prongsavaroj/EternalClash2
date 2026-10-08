with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

target = '''        let nodeIcon = owner ? '🏰' : '⚪';
        let nodeName = owner ? owner.name : 'ป่าเถื่อน';'''

replacement = '''        let nodeIcon = owner ? '🏰' : '⚪';
        let nodeName = node.name; // Use the name generated from backend (random historical names or owner)'''

js = js.replace(target, replacement)
with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)
