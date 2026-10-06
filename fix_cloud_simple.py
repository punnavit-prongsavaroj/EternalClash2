import re

with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

new_transition = '''
// --- ระบบ Transition ก้อนเมฆแบบเสถียร 100% ---
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
    // สร้างกลุ่มเมฆสีขาวแบบทึบ ป้องกันอาการหน่วงจาก blur
    for (let i = 0; i < 30; i++) {
        const cloud = document.createElement('div');
        const size = Math.random() * 400 + 300; // ใหญ่ๆ ไปเลย
        cloud.style.position = 'absolute';
        cloud.style.width = size + 'px';
        cloud.style.height = (size * 0.7) + 'px';
        cloud.style.backgroundColor = '#ffffff';
        cloud.style.borderRadius = '50%';
        cloud.style.opacity = '1';
        
        // เริ่มจากนอกจอด้านขวา
        cloud.style.top = (Math.random() * 120 - 10) + 'vh';
        cloud.style.left = '120vw';
        
        cloud.style.transition = 'left 0.8s ease-in-out';
        
        overlay.appendChild(cloud);
        clouds.push(cloud);
    }

    // กระตุ้นให้เบราว์เซอร์รับรู้
    setTimeout(() => {
        // ให้เมฆลอยมาตรงกลางจอเพื่อบัง
        clouds.forEach(c => {
            c.style.left = (Math.random() * 80) + 'vw';
            // ปรับตำแหน่งให้อยู่กลางจอมากขึ้น
            if (parseInt(c.style.left) > 60) c.style.left = '50vw';
        });
        
        // บังเพิ่มความชัวร์ด้วยจอกลายเป็นสีขาว
        overlay.style.transition = 'background-color 0.5s ease-in-out';
        overlay.style.backgroundColor = 'rgba(255,255,255,0.9)';
    }, 50);

    // เปลี่ยนฉากหลังจากเมฆบังมิด
    setTimeout(() => {
        if (callback) callback();
        
        // เอาฉากขาวออก
        overlay.style.backgroundColor = 'transparent';
        
        // เมฆลอยออกไปทางซ้าย
        clouds.forEach(c => {
            c.style.left = '-150vw';
        });

        // ลบเมฆทิ้ง
        setTimeout(() => {
            overlay.remove();
        }, 1000);
        
    }, 900);
}
'''

# Remove the old playCloudTransition completely
js = re.sub(r'// --- ระบบ Transition ก้อนเมฆ ---[\s\S]*?(?=function hideAllScreens)', new_transition + '\n', js)

with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)

print("Cloud refactored")
