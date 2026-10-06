import os
import glob

# 1. Move and update GameClock.java
old_path = 'src/main/java/com/eternalclash2/domain/service/GameClock.java'
new_path = 'src/main/java/com/eternalclash2/service/GameClock.java'

with open(old_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('package com.eternalclash2.domain.service;', 'package com.eternalclash2.service;')

with open(new_path, 'w', encoding='utf-8') as f:
    f.write(content)

os.remove(old_path)

# 2. Update imports in all Java files
for file_path in glob.glob('src/main/java/com/eternalclash2/**/*.java', recursive=True):
    with open(file_path, 'r', encoding='utf-8') as f:
        file_content = f.read()
    
    if 'com.eternalclash2.domain.service.GameClock' in file_content:
        file_content = file_content.replace('import com.eternalclash2.domain.service.GameClock;', 'import com.eternalclash2.service.GameClock;')
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(file_content)

# 3. Clean up directory if empty
try:
    os.rmdir('src/main/java/com/eternalclash2/domain/service')
except:
    pass
