import re

with open('src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

# Update mapData to use arrays for incoming
old_map_data = '''const mapData = [
    { id: 1, name: localStorage.getItem("eternalClashPlayerName") || 'ข้าพเจ้าเอง', isMe: true, x: 50, y: 80, incoming: 0 },
    { id: 2, name: 'ศัตรู A', isMe: false, x: 20, y: 30, incoming: 1 },
    { id: 3, name: 'ศัตรู B', isMe: false, x: 80, y: 40, incoming: 2 }
];'''

new_map_data = '''const mapData = [
    { id: 1, name: localStorage.getItem("eternalClashPlayerName") || 'ข้าพเจ้าเอง', isMe: true, x: 50, y: 80, incoming: [] },
    { id: 2, name: 'ศัตรู A', isMe: false, x: 20, y: 30, incoming: [1] }, // 1 เทิร์นถึง
    { id: 3, name: 'ศัตรู B', isMe: false, x: 80, y: 40, incoming: [1, 2] } // ซ้อนกัน ทั้ง 1 และ 2 เทิร์น
];'''

js = js.replace(old_map_data, new_map_data)

# Update the radar logic
old_radar_logic = '''        let radarHTML = '';
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

new_radar_logic = '''        let radarHTML = '';
        let showRadar = true;
        if (randomTime === 'night' && !player.isMe) {
            showRadar = false; // กลางคืนมองไม่เห็นเรดาร์คนอื่น
        }
        if (showRadar) {
            // ระบบจุดเรดาร์: จุดแรก(ซ้าย) = 1 เทิร์น, จุดสอง(ขวา) = 2 เทิร์น
            let hasT1 = Array.isArray(player.incoming) && player.incoming.includes(1);
            let hasT2 = Array.isArray(player.incoming) && player.incoming.includes(2);
            
            let dot1 = hasT1 ? '<div class="dot red"></div>' : '<div class="dot white"></div>';
            let dot2 = hasT2 ? '<div class="dot red"></div>' : '<div class="dot white"></div>';
            
            radarHTML = '<div class="radar-container" style="transform: translateY(-50%) rotate(' + angle + 'deg);">' + dot1 + dot2 + '</div>';
        }'''

js = js.replace(old_radar_logic, new_radar_logic)

with open('src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)
