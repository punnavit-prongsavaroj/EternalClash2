import re

with open('src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

old_radar = '''        let radarHTML = '';
        if (player.incoming > 0) {
            let showRadar = true;
            if (randomTime === 'night' && !player.isMe) {
                showRadar = false; // กลางคืนมองไม่เห็นเรดาร์คนอื่น
            }
            if (showRadar) {
                let dotsHTML = '';
                if (player.incoming === 2) {
                    dotsHTML = '<div class="dot white"></div><div class="dot red"></div>';
                } else {
                    dotsHTML = '<div class="dot red"></div><div class="dot white"></div>';
                }
                radarHTML = '<div class="radar-container" style="transform: translateY(-50%) rotate(' + angle + 'deg);">' + dotsHTML + '</div>';
            }
        }'''

new_radar = '''        let radarHTML = '';
        let showRadar = true;
        if (randomTime === 'night' && !player.isMe) {
            showRadar = false; // กลางคืนมองไม่เห็นเรดาร์คนอื่น
        }
        if (showRadar) {
            let dotsHTML = '';
            if (player.incoming === 2) {
                dotsHTML = '<div class="dot white"></div><div class="dot red"></div>';
            } else if (player.incoming === 1) {
                dotsHTML = '<div class="dot red"></div><div class="dot white"></div>';
            } else {
                dotsHTML = '<div class="dot white"></div><div class="dot white"></div>';
            }
            radarHTML = '<div class="radar-container" style="transform: translateY(-50%) rotate(' + angle + 'deg);">' + dotsHTML + '</div>';
        }'''

js = js.replace(old_radar, new_radar)

with open('src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)
