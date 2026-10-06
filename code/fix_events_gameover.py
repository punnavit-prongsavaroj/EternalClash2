import re

with open('src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

# 1. Update Game Over Marshal Image
old_finished = '''    } else if (snapshot.status === 'FINISHED') {
        if (lastStatus !== 'FINISHED') {
            lastStatus = 'FINISHED';
            hideAllScreens();
            document.getElementById('game-over-screen').style.display = 'flex';
            if(pollingInterval) clearInterval(pollingInterval);
            
            const winner = snapshot.players.find(p => p.alive);
            if (winner) {
                document.getElementById('winner-name-display').innerText = winner.name;
                document.getElementById('winner-marshal-display').innerText = winner.marshalName || 'ไม่ได้เลือก';
            } else {
                document.getElementById('winner-name-display').innerText = 'ไม่มีผู้รอดชีวิต (เสมอ)';
                document.getElementById('winner-name-display').style.color = '#e74c3c';
                document.getElementById('winner-marshal-display').innerText = '-';
            }
        }
    }'''

new_finished = '''    } else if (snapshot.status === 'FINISHED') {
        if (lastStatus !== 'FINISHED') {
            lastStatus = 'FINISHED';
            hideAllScreens();
            document.getElementById('game-over-screen').style.display = 'flex';
            if(pollingInterval) clearInterval(pollingInterval);
            
            const winner = snapshot.players.find(p => p.alive);
            if (winner) {
                document.getElementById('winner-name-display').innerText = winner.name;
                document.getElementById('winner-marshal-display').innerText = winner.marshalName || 'ไม่ได้เลือก';
                
                const imgName = MARSHAL_IMGS[winner.marshalName] || 'Jo-Sho.jpg';
                document.getElementById('winner-marshal-img').src = '/Marshal/' + imgName;
                document.getElementById('winner-marshal-img').style.display = 'block';
            } else {
                document.getElementById('winner-name-display').innerText = 'ไม่มีผู้รอดชีวิต (เสมอ)';
                document.getElementById('winner-name-display').style.color = '#e74c3c';
                document.getElementById('winner-marshal-display').innerText = '-';
            }
        }
    }'''
js = js.replace(old_finished, new_finished)

# 2. Update Log function to fetch events and battles
old_log = '''function updateLog(snapshot) {
    const logContent = document.getElementById('log-content');
    if(logContent.innerHTML.includes('ยังไม่มีเหตุการณ์')) logContent.innerHTML = '';
    
    let actionLines = '';
    snapshot.visibleActions.forEach(act => {
        let actStr = act.actionType;
        if(actStr === 'PRODUCE_FOOD') actStr = '<span style="color:#2ecc71">ทำฟาร์ม</span>';
        else if(actStr === 'RECRUIT_SOLDIERS') actStr = '<span style="color:#3498db">เกณฑ์ทหาร</span>';
        else if(actStr === 'SEND_ARMY') actStr = '<span style="color:#e74c3c">ส่งกองทัพโจมตี</span>';
        else actStr = 'พักผ่อน';
        
        const p = snapshot.players.find(p => p.playerId === act.playerId);
        const name = p ? p.name : 'ศัตรู';
        actionLines += "🏰 " + name + " เลือก: " + actStr + "<br>";
    });
    
    if(actionLines === '') actionLines = '<i>ไม่มีความเคลื่อนไหวที่มองเห็นได้</i>';
    
    const newLog = "<div style='margin-bottom: 10px; line-height: 1.5;'>" +
                   "<strong style='color:#f39c12'>[สรุปแอคชัน เทิร์น " + (lastTurn-1) + "]</strong><br>" +
                   actionLines +
                   "</div><hr style='border-color: #7f8c8d; margin: 10px 0;'>";
                   
    logContent.innerHTML = newLog + logContent.innerHTML;
    document.getElementById('log-panel').style.display = 'flex';
}'''

new_log = '''async function updateLog(snapshot) {
    const logContent = document.getElementById('log-content');
    if(logContent.innerHTML.includes('ยังไม่มีเหตุการณ์')) logContent.innerHTML = '';
    
    const gameId = localStorage.getItem('eternalClashGameId');
    const prevTurn = lastTurn - 1;
    
    let extraLines = '';
    if (prevTurn > 0) {
        try {
            const resE = await fetch(API_BASE_URL + '/games/' + gameId + '/events?turnNumber=' + prevTurn);
            if (resE.ok) {
                const events = await resE.json();
                events.forEach(ev => {
                    extraLines += "<span style='color:#9b59b6'>⚡ อีเวนต์: " + (ev.description || "เกิดเหตุการณ์ลึกลับ") + "</span><br>";
                });
            }
            const resB = await fetch(API_BASE_URL + '/games/' + gameId + '/battles?turnNumber=' + prevTurn);
            if (resB.ok) {
                const battles = await resB.json();
                battles.forEach(b => {
                    const atk = snapshot.players.find(p => p.playerId === b.attackerPlayerId);
                    const def = snapshot.players.find(p => p.playerId === b.defenderPlayerId);
                    const atkName = atk ? atk.name : 'ศัตรู';
                    const defName = def ? def.name : 'ศัตรู';
                    
                    extraLines += "⚔️ <b style='color:#e74c3c'>" + atkName + " ปะทะ " + defName + "</b> (สูญเสีย: รุก " + b.attackerCasualties + ", รับ " + b.defenderCasualties + ")<br>";
                    if (b.cityDestroyed) {
                        extraLines += "💥 <b style='color:#c0392b; font-size:1.1rem;'>เมืองของ " + defName + " แตกพ่าย!</b><br>";
                    }
                });
            }
        } catch(e) {}
    }
    
    let actionLines = '';
    snapshot.visibleActions.forEach(act => {
        let actStr = act.actionType;
        if(actStr === 'PRODUCE_FOOD') actStr = '<span style="color:#2ecc71">ทำฟาร์ม</span>';
        else if(actStr === 'RECRUIT_SOLDIERS') actStr = '<span style="color:#3498db">เกณฑ์ทหาร</span>';
        else if(actStr === 'SEND_ARMY') actStr = '<span style="color:#e74c3c">ส่งกองทัพโจมตี</span>';
        else actStr = 'พักผ่อน';
        
        const p = snapshot.players.find(p => p.playerId === act.playerId);
        const name = p ? p.name : 'ศัตรู';
        actionLines += "🏰 " + name + " เลือก: " + actStr + "<br>";
    });
    
    if(actionLines === '' && extraLines === '') actionLines = '<i>ไม่มีความเคลื่อนไหวที่มองเห็นได้</i><br>';
    
    const newLog = "<div style='margin-bottom: 10px; line-height: 1.5;'>" +
                   "<strong style='color:#f39c12'>[สรุปเหตุการณ์ เทิร์น " + prevTurn + "]</strong><br>" +
                   actionLines + extraLines +
                   "</div><hr style='border-color: #7f8c8d; margin: 10px 0;'>";
                   
    logContent.innerHTML = newLog + logContent.innerHTML;
    document.getElementById('log-panel').style.display = 'flex';
}'''

js = js.replace(old_log, new_log)

with open('src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)
