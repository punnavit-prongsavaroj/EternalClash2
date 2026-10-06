import re

with open('src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

# Replace the entire renderMap function
new_renderMap = '''function renderMap() {
    const container = document.getElementById('castles-container');
    const targetSelect = document.getElementById('attack-target');
    container.innerHTML = '';
    targetSelect.innerHTML = '';
    
    const seasons = ['spring', 'summer', 'autumn', 'winter'];
    const timeOfDay = ['day', 'night'];
    const randomSeason = seasons[Math.floor(Math.random() * seasons.length)];
    const randomTime = timeOfDay[Math.floor(Math.random() * timeOfDay.length)];
    
    const bgMap = document.getElementById('dynamic-map-bg');
    if(randomSeason === 'winter') bgMap.style.filter = 'grayscale(0.6) brightness(1.2)';
    else if(randomTime === 'night') bgMap.style.filter = 'brightness(0.4)';
    else bgMap.style.filter = 'none';
    
    document.getElementById('current-turn-display').innerText = 'เทิร์นที่: 1 (' + randomSeason + ' - ' + randomTime + ')';
    
    mapData.forEach(player => {
        const node = document.createElement('div');
        node.className = 'castle-node' + (player.isMe ? ' my-castle' : '');
        node.style.left = player.x + '%';
        node.style.top = player.y + '%';
        
        const dx = 50 - player.x;
        const dy = 50 - player.y;
        let angle = Math.atan2(dy, dx) * (180 / Math.PI);
        
        let radarHTML = '';
        if (player.incoming > 0) {
            let dotsHTML = '';
            for(let i = 0; i < player.incoming; i++) {
                dotsHTML += '<div class="dot"></div>';
            }
            radarHTML = '<div class="radar-container" style="transform: translateY(-50%) rotate(' + angle + 'deg);">' + dotsHTML + '</div>';
        }
        
        // Use proper backticks inside to avoid string parsing issues
        node.innerHTML = radarHTML +
            '<div class="castle-icon" style="pointer-events: auto;" onclick="if(!'+player.isMe+') { document.getElementById(\\'attack-target\\').value=\\''+player.id+'\\'; openAttackModal(); }">🏰</div>' +
            '<div class="castle-name">' + player.name + '</div>';
                         
        container.appendChild(node);
        if (!player.isMe) {
            targetSelect.innerHTML += '<option value="' + player.id + '">ตีเมือง: ' + player.name + '</option>';
        }
    });
}'''

js = re.sub(r"function renderMap\(\) \{.*?\}(?=\n\nfunction submitAction)", new_renderMap, js, flags=re.DOTALL)

with open('src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)
