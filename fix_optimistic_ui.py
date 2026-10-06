import re

with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

old_submit = '''    try {
        const res = await fetch(API_BASE_URL + '/games/' + gameId + '/players/' + playerId + '/actions', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ actionType: actionType, targetPlayerId: target, soldierCount: soldiers })
        });
        
        if (!res.ok) {
            const err = await res.json();
            alert(err.message || 'คำสั่งล้มเหลว');
            return;
        }
        
        mySubmitted = true;
        document.getElementById('command-panel').style.display = 'none';'''

new_submit = '''    // 1. ตอบสนอง UI ทันทีไม่ต้องรอหลังบ้าน (Optimistic Update)
    mySubmitted = true;
    document.getElementById('command-panel').style.display = 'none';

    try {
        const res = await fetch(API_BASE_URL + '/games/' + gameId + '/players/' + playerId + '/actions', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ actionType: actionType, targetPlayerId: target, soldierCount: soldiers })
        });
        
        if (!res.ok) {
            const err = await res.json();
            alert(err.message || 'คำสั่งล้มเหลว');
            // คืนค่า UI ให้กดใหม่ถ้า Server แจ้งว่าทำไม่ได้ (เช่น ทหารไม่พอ)
            mySubmitted = false;
            document.getElementById('command-panel').style.display = 'flex';
            return;
        }'''

if 'Optimistic Update' not in js:
    js = js.replace(old_submit, new_submit)
    with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
        f.write(js)

print("Optimistic UI update added to submitAction")
