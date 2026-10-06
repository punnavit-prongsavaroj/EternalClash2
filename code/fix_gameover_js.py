import re

with open('src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

# Make sure hideAllScreens hides game-over-screen
js = js.replace('document.getElementById("game-screen").style.display = "none";', 'document.getElementById("game-screen").style.display = "none";\n    document.getElementById("game-over-screen").style.display = "none";')

# Update FINISHED handler
old_finished = '''    } else if (snapshot.status === 'FINISHED') {
        if (lastStatus !== 'FINISHED') {
            lastStatus = 'FINISHED';
            alert('เกมจบแล้ว!');
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
            } else {
                document.getElementById('winner-name-display').innerText = 'ไม่มีผู้รอดชีวิต (เสมอ)';
                document.getElementById('winner-name-display').style.color = '#e74c3c';
                document.getElementById('winner-marshal-display').innerText = '-';
            }
        }
    }'''
js = js.replace(old_finished, new_finished)

# Add returnToLobby function
return_to_lobby = '''
function returnToLobby() {
    localStorage.removeItem("eternalClashGameId");
    localStorage.removeItem("eternalClashPlayerId");
    if(pollingInterval) clearInterval(pollingInterval);
    checkLoginState();
}
'''
js += return_to_lobby

with open('src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)
