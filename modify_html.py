import re

with open('code/src/main/resources/static/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

# Add placement phase panel
placement_panel = '''
        <!-- Placement Phase Panel -->
        <div id="placement-panel" class="glass-panel" style="display: none; position: absolute; top: 10px; left: 50%; transform: translateX(-50%); z-index: 25; flex-direction: column; align-items: center; padding: 20px;">
            <h2 style="color: #f39c12; margin-bottom: 10px; text-align: center; text-shadow: 1px 1px 2px black;">ช่วงเตรียมการรบ: เลือกอณาเขตตั้งฐานทัพ</h2>
            <p style="color: white; font-size: 1.1rem; text-shadow: 1px 1px 2px black;">โปรดคลิกเลือกเมืองที่เป็นอณาเขตว่าง (สีเทา) บนแผนที่เพื่อตั้งเป็นฐานหลักของท่าน</p>
            <p id="placement-waiting-msg" style="color: #2ecc71; font-weight: bold; margin-top: 10px; display: none;">ท่านได้เลือกฐานแล้ว รอผู้เล่นท่านอื่น...</p>
        </div>
'''

html = html.replace('<!-- แผงคำสั่ง (ซ้าย) -->', placement_panel + '\n                <!-- แผงคำสั่ง (ซ้าย) -->')

# Add edge container
edge_container = '''
        <!-- แผนที่แบบเต็มจอ สำหรับวาดเส้นเชื่อม -->
        <svg id="edges-container" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; z-index: 4; pointer-events: none;">
            <!-- เส้นขอบจะถูกวาดที่นี่ด้วย JS -->
        </svg>
'''
html = html.replace('<!-- แผนที่แบบเต็มจอ สำหรับวางปราสาท -->', edge_container + '\n        <!-- แผนที่แบบเต็มจอ สำหรับวางปราสาท -->')

with open('code/src/main/resources/static/index.html', 'w', encoding='utf-8') as f:
    f.write(html)
