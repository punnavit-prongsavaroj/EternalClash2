import re

with open('src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

# Fix the innerHTML string assignment
js = re.sub(r"node\.innerHTML = radarHTML \+.*?;", 
    '''node.innerHTML = radarHTML +
                     '<div class="castle-icon" style="pointer-events: auto;" onclick="if(!' + player.isMe + ') { document.getElementById(\\'attack-target\\').value=' + player.id + '; openAttackModal(); }">🏰</div>' +
                     '<div class="castle-name">' + player.name + '</div>';''', js, flags=re.DOTALL)

with open('src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)
