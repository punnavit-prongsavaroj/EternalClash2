import re

with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

# Add mapping
video_mapping = '''
const MARSHAL_WIN_VIDEOS = {
    'ขงเบ้ง': 'kongming.mp4',
    'จูล่ง': 'jurong.mp4',
    'จิวยี่': 'jilyi.mp4',
    'โจโฉ': 'sungon.mp4',
    'เล่าปี่': 'laopi.mp4',
    'ลิโป้': 'Lubu.mp4',
    'ซุนกวน': 'songun.mp4'
};
'''
if 'MARSHAL_WIN_VIDEOS' not in js:
    js = js.replace('const MARSHAL_IMGS = {', video_mapping + '\nconst MARSHAL_IMGS = {')

# Modify FINISHED block
old_finished = '''    } else if (snapshot.status === 'FINISHED') {
        if (lastStatus !== 'FINISHED') {
            lastStatus = 'FINISHED';
            hideAllScreens();
            document.getElementById('game-over-screen').style.display = 'flex';
            
            // ปิดเพลงหรือเปลี่ยนเพลงตอนจบ (ถ้าต้องการ)
            // document.getElementById('bg-music').pause();
            
            const winner = snapshot.players.find(p => p.alive);
            document.getElementById('winner-name-display').innerText = winner ? winner.name : 'ไม่มีใครรอด';
            if(winner && winner.marshalName) {
                document.getElementById('winner-marshal-display').innerText = winner.marshalName;
                document.getElementById('winner-marshal-img').src = '/Marshal/' + MARSHAL_IMGS[winner.marshalName];
                document.getElementById('winner-marshal-img').style.display = 'block';
            }
        }
    }'''

new_finished = '''    } else if (snapshot.status === 'FINISHED') {
        if (lastStatus !== 'FINISHED') {
            lastStatus = 'FINISHED';
            hideAllScreens();
            document.getElementById('game-over-screen').style.display = 'block';
            document.getElementById('win-ui').style.display = 'none'; // ซ่อน UI ไว้ก่อน
            
            // ปิดเพลงฉาก
            const bgMusic = document.getElementById('bg-music');
            if (bgMusic) bgMusic.pause();
            
            const winner = snapshot.players.find(p => p.alive);
            document.getElementById('winner-name-display').innerText = winner ? winner.name : 'ไม่มีใครรอด';
            if(winner && winner.marshalName) {
                document.getElementById('winner-marshal-display').innerText = winner.marshalName;
                document.getElementById('winner-marshal-img').src = '/Marshal/' + MARSHAL_IMGS[winner.marshalName];
                document.getElementById('winner-marshal-img').style.display = 'block';
                
                // เล่นวิดีโออนิเมชั่นชนะ
                const winVideo = document.getElementById('win-video');
                const videoFile = MARSHAL_WIN_VIDEOS[winner.marshalName];
                if (videoFile && winVideo) {
                    winVideo.src = '/SkillAction/win%20animation/' + videoFile;
                    // ปิดเสียงหรือเปิดเสียงวิดีโอแล้วแต่ต้องการ (ผมไม่ได้ใส่ muted ไว้ใน html เพื่อให้เปิดเสียงได้ถ้าไฟล์มีเสียง)
                    if (typeof isMusicPlaying !== 'undefined' && isMusicPlaying) {
                        winVideo.muted = false;
                    } else {
                        winVideo.muted = true;
                    }
                    
                    winVideo.onended = () => {
                        // เมื่อวิดีโอจบ ให้แสดง UI
                        document.getElementById('win-ui').style.display = 'flex';
                    };
                    
                    // ป้องกันเผื่อวิดีโอโหลดไม่ขึ้นหรือมีปัญหา
                    winVideo.onerror = () => { document.getElementById('win-ui').style.display = 'flex'; };
                    
                    winVideo.play().catch(e => {
                        // ถ้า autoplay โดนบล็อก
                        document.getElementById('win-ui').style.display = 'flex';
                    });
                } else {
                    document.getElementById('win-ui').style.display = 'flex';
                }
            } else {
                document.getElementById('win-ui').style.display = 'flex';
            }
        }
    }'''

js = js.replace(old_finished, new_finished)

with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)

print("Game Over video logic added")
