import re

with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

# 1. Add playCloudTransition function
cloud_func = '''
// --- ระบบ Transition ก้อนเมฆ ---
function playCloudTransition(callback) {
    const overlay = document.createElement('div');
    overlay.style.position = 'fixed';
    overlay.style.top = '0';
    overlay.style.left = '0';
    overlay.style.width = '100vw';
    overlay.style.height = '100vh';
    overlay.style.zIndex = '99999';
    overlay.style.pointerEvents = 'none';
    overlay.style.overflow = 'hidden';
    document.body.appendChild(overlay);

    const clouds = [];
    // สร้างเมฆ 50 ก้อน ขนาดเล็กใหญ่ปนกัน
    for (let i = 0; i < 50; i++) {
        const cloud = document.createElement('div');
        const size = Math.random() * 400 + 200; // 200px ถึง 600px
        cloud.style.position = 'absolute';
        cloud.style.width = size + 'px';
        cloud.style.height = (size * 0.7) + 'px';
        cloud.style.backgroundColor = '#ecf0f1';
        cloud.style.borderRadius = '50%';
        cloud.style.filter = 'blur(15px)';
        cloud.style.opacity = '0.95';
        cloud.style.boxShadow = '0 10px 30px rgba(0,0,0,0.1)';
        
        // เริ่มจากนอกจอด้านขวา
        cloud.style.top = (Math.random() * 120 - 10) + 'vh';
        cloud.style.left = '120vw';
        
        // แรนด้อมความเร็ว
        const duration = 0.8 + Math.random() * 0.5;
        cloud.style.transition = 'left ' + duration + 's cubic-bezier(0.25, 1, 0.5, 1)';
        
        overlay.appendChild(cloud);
        clouds.push(cloud);
    }

    // สั่ง Reflow ให้เบราว์เซอร์รับรู้ค่าเริ่มต้นก่อน animate
    void overlay.offsetWidth;

    // Phase 1: เมฆลอยเข้ามาบังเต็มจอ
    clouds.forEach(c => {
        c.style.left = (Math.random() * 100 - 10) + 'vw';
    });

    // Phase 2: เปลี่ยนฉากตอนที่เมฆบังมิด (ประมาณ 1.1 วิ)
    setTimeout(() => {
        if (callback) callback();
        
        // Phase 3: เมฆลอยออกไปทางซ้าย
        clouds.forEach(c => {
            const duration = 0.8 + Math.random() * 0.5;
            c.style.transition = 'left ' + duration + 's cubic-bezier(0.5, 0, 0.75, 0)';
            c.style.left = '-100vw';
        });

        // ลบเมฆทิ้งเมื่อลอยออกหมดแล้ว
        setTimeout(() => {
            overlay.remove();
        }, 1500);
        
    }, 1100);
}
'''
if 'playCloudTransition' not in js:
    js = js.replace('function hideAllScreens', cloud_func + '\nfunction hideAllScreens')

# 2. Wrap screen functions
old_showLobby = 'function showLobby(playerName) { hideAllScreens(); document.getElementById("lobby-screen").style.display = "flex"; document.getElementById("display-name").innerText = playerName; }'
new_showLobby = 'function showLobby(playerName) { playCloudTransition(() => { hideAllScreens(); document.getElementById("lobby-screen").style.display = "flex"; document.getElementById("display-name").innerText = playerName; }); }'
js = js.replace(old_showLobby, new_showLobby)

old_showRoom = 'function showRoom(gameId, roomCode, playerName) { hideAllScreens(); document.getElementById("room-screen").style.display = "flex"; document.getElementById("current-room-id").innerText = roomCode; document.getElementById("current-player-name").innerText = playerName; }'
new_showRoom = 'function showRoom(gameId, roomCode, playerName) { playCloudTransition(() => { hideAllScreens(); document.getElementById("room-screen").style.display = "flex"; document.getElementById("current-room-id").innerText = roomCode; document.getElementById("current-player-name").innerText = playerName; }); }'
js = js.replace(old_showRoom, new_showRoom)

# 3. Wrap handleSnapshot transitions
old_marshal = '''          if(lastStatus !== 'MARSHAL_SELECTION') {
              lastStatus = 'MARSHAL_SELECTION';
              hideAllScreens();
              document.getElementById('draft-screen').style.display = 'flex';
          }'''
new_marshal = '''          if(lastStatus !== 'MARSHAL_SELECTION') {
              lastStatus = 'MARSHAL_SELECTION';
              playCloudTransition(() => {
                  hideAllScreens();
                  document.getElementById('draft-screen').style.display = 'flex';
              });
          }'''
js = js.replace(old_marshal, new_marshal)

old_game = '''          if(lastStatus !== 'IN_PROGRESS') {
              lastStatus = 'IN_PROGRESS';
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
          }'''
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
              });
          }'''
js = js.replace(old_game, new_game)

with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)

print("Cloud transition injected")
