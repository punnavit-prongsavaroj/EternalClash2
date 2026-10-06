import re

with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

# Remove the generic event sound
old_generic = "if(events.length > 0) playSound('event-sfx');"
js = js.replace(old_generic, "")

# Add dynamic sound inside the loop
old_loop_start = "events.forEach(ev => {"
new_loop_start = '''events.forEach(ev => {
                    // เล่นเสียงเฉพาะของแต่ละอีเวนต์
                    const evSound = new Audio('/sound/' + ev.eventType.toLowerCase() + '.mp3');
                    let playPromise = evSound.play();
                    if (playPromise !== undefined) playPromise.catch(e => {});
'''

if "new Audio('/sound/' + ev.eventType.toLowerCase()" not in js:
    js = js.replace(old_loop_start, new_loop_start)
    with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
        f.write(js)

print("Dynamic Event SFX logic added")
