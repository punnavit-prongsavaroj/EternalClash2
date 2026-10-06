import re

with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

# Declare globals for season tracking at top
if 'let lastSeason = null;' not in js:
    js = js.replace('let lastStatus = "";', 'let lastStatus = "";\nlet lastSeason = null;\nlet lastDaytime = null;')

# Update handleSnapshot
old_game = '''          if(lastStatus !== 'IN_PROGRESS') {
              lastStatus = 'IN_PROGRESS';
              playCloudTransition(() => {
                  hideAllScreens();
                  document.getElementById('game-screen').style.display = 'flex';
                  
                  // เปลี่ยนเพลงเป็นเพลงในเกม
                  const bgMusic = document.getElementById('bg-music');
                  if (bgMusic.src.includes('menu_bgm.mp3')) {
                      bgMusic.src = '/sound/game_bgm.mp3';
                      if (isMusicPlaying) {
                          bgMusic.play().catch(e => console.log(e));
                      }
                  }
              });
          }
          
          updateGameUI(snapshot);'''

new_game = '''          if(lastStatus !== 'IN_PROGRESS') {
              lastStatus = 'IN_PROGRESS';
              playCloudTransition(() => {
                  hideAllScreens();
                  document.getElementById('game-screen').style.display = 'flex';
                  
                  // เปลี่ยนเพลงเป็นเพลงในเกม
                  const bgMusic = document.getElementById('bg-music');
                  if (bgMusic.src.includes('menu_bgm.mp3')) {
                      bgMusic.src = '/sound/game_bgm.mp3';
                      if (isMusicPlaying) {
                          bgMusic.play().catch(e => console.log(e));
                      }
                  }
                  
                  lastSeason = snapshot.season;
                  lastDaytime = snapshot.daytime;
                  updateGameUI(snapshot);
              });
              return;
          }
          
          if (lastSeason !== null && lastDaytime !== null && (lastSeason !== snapshot.season || lastDaytime !== snapshot.daytime)) {
              playCloudTransition(() => {
                  lastSeason = snapshot.season;
                  lastDaytime = snapshot.daytime;
                  updateGameUI(snapshot);
              });
              return;
          }
          
          lastSeason = snapshot.season;
          lastDaytime = snapshot.daytime;
          updateGameUI(snapshot);'''

if "lastSeason !== snapshot.season" not in js:
    js = js.replace(old_game, new_game)
    with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
        f.write(js)

print("Cloud logic updated to trigger on day/night and season changes")
