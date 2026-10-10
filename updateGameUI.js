function updateGameUI(snapshot) {
    // ฤดูกาล / กลางวันกลางคืน
    const bgMap = document.getElementById('dynamic-map-bg');
    let bgUrl = '';
    const seasonId = snapshot.season.toLowerCase();
    const isDay = snapshot.daytime;
    
    if(seasonId === 'summer') {
        bgUrl = isDay ? '/Map/sunny.jpg' : '/Map/Summer-night.jpg';
    } else if(seasonId === 'rainy') {
        bgUrl = isDay ? '/Map/rainy-day.jpg' : '/Map/rainy-night.jpg';
    } else if(seasonId === 'winter') {
        bgUrl = isDay ? '/Map/Snow-day.jpg' : '/Map/Snow-night.jpg';
    } else {
        bgUrl = '/Map/sunny.jpg';
    }
    bgMap.style.backgroundImage = 'url("' + bgUrl + '")';
    bgMap.style.filter = 'none';
    
    let seasonTh = seasonId === 'summer' ? 'ฤดูร้อน' : (seasonId === 'rainy' ? 'ฤดูฝน' : 'ฤดูหนาว');
    document.getElementById('current-turn-display').innerText = 'เทิร์นที่: ' + snapshot.currentTurn + ' (' + seasonTh + ' - ' + (isDay ? 'กลางวัน' : 'กลางคืน') + ')';
    
    // อัปเดตเมืองผู้เล่น
    const container = document.getElementById('castles-container');
    const targetSelect = document.getElementById('attack-target');
    container.innerHTML = '';
    targetSelect.innerHTML = '';
    
    // จำลองตำแหน่งบนแผนที่ คงที่ตาม ID ผู้เล่น
    const positions = [
        { x: 50, y: 80 }, { x: 20, y: 30 }, { x: 80, y: 40 }, { x: 20, y: 70 }, { x: 80, y: 70 }
    ];
    
    snapshot.players.filter(p => p.alive).forEach((player, index) => {
        if(player.isViewer) {
            document.getElementById('ui-marshal').innerText = player.marshalName || '-';
            document.getElementById('ui-city-name').innerText = player.name;
            document.getElementById('ui-food').innerText = player.food || 0;
            document.getElementById('ui-soldiers').innerText = player.citySoldiers || 0;
        }
        
        const pos = positions[index % positions.length];
        const node = document.createElement('div');
        node.className = 'castle-node' + (player.isViewer ? ' my-castle' : '');
        node.style.left = pos.x + '%';
        node.style.top = pos.y + '%';
        
        const dx = 50 - pos.x;
        const dy = 50 - pos.y;
        let angle = Math.atan2(dy, dx) * (180 / Math.PI);
        
        let radarHTML = '';
        let showRadar = true;
        if (!isDay && !player.isViewer) {
            showRadar = false;
        }
        if (showRadar) {
            // หา incoming armies ไปหา player นี้
            let armies = snapshot.visibleArmies.filter(a => a.visibleTargetPlayerId === player.playerId);
            let hasT2 = armies.some(a => (a.arrivalTurn - snapshot.currentTurn) === 2);
            // กรณีศัตรูประชิดถึงเทิร์นนี้เลย
            let hasT0 = armies.some(a => (a.arrivalTurn - snapshot.currentTurn) <= 0);
            let hasT1 = armies.some(a => (a.arrivalTurn - snapshot.currentTurn) === 1);
            
            let dot1 = '<div class="dot white"></div>';
            let dot2 = '<div class="dot white"></div>';
            
            if (hasT0) {
                // ห่าง 0: จุดในแดง จุดนอกขาว
                dot1 = '<div class="dot red"></div>';
            } else if (hasT1) {
                // ห่าง 1: จุดในขาว จุดนอกแดง
                dot2 = '<div class="dot red"></div>';
            }
            // ห่าง 2 ขึ้นไป: ขาวคู่ (ไม่เห็น)
            
            radarHTML = '<div class="radar-container" style="transform: translateY(-50%) rotate(' + angle + 'deg);">' + dot1 + dot2 + '</div>';
        }
        
        node.innerHTML = radarHTML +
            '<div class="castle-icon" style="pointer-events: auto;" onclick="if(!'+player.isViewer+') { document.getElementById(\'attack-target\').value=\''+player.playerId+'\'; openAttackModal(); }">🏰</div>' +
            '<div class="castle-name">' + player.name + '</div>';
                         
        container.appendChild(node);
        if (!player.isViewer) {
            targetSelect.innerHTML += '<option value="' + player.playerId + '">ตีเมือง: ' + player.name + '</option>';
        }
    });
}

async function submitAction