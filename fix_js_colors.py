import re
with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

target = '''        let badgeHTML = '';
        if (node.soldiers != null) {
            badgeHTML = <div style="position:absolute; top:-10px; right:-10px; background:red; color:white; font-size:0.8rem; font-weight:bold; padding:2px 6px; border-radius:10px; border:2px solid white; pointer-events:none; z-index:10;"> ⚔️</div>;
        }'''

replacement = '''        let badgeHTML = '';
        if (node.soldiers != null) {
            let badgeColor = '#555';
            if (owner) {
                const PLAYER_COLORS = ['#e74c3c', '#3498db', '#2ecc71', '#9b59b6', '#e67e22', '#1abc9c', '#34495e'];
                let pIndex = snapshot.players.findIndex(p => p.playerId === owner.playerId);
                if (pIndex >= 0) badgeColor = PLAYER_COLORS[pIndex % PLAYER_COLORS.length];
            }
            badgeHTML = <div style="position:absolute; top:-10px; right:-10px; background:; color:white; font-size:0.8rem; font-weight:bold; padding:2px 6px; border-radius:10px; border:2px solid white; pointer-events:none; z-index:10;"> ⚔️</div>;
        }'''

js = js.replace(target, replacement)
with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)
