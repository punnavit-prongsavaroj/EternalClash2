import os
import re

# 1. Fix GameService.java
gs_path = 'code/src/main/java/com/eternalclash2/service/GameService.java'
with open(gs_path, 'r', encoding='utf-8') as f:
    gs = f.read()

# Make sure imports exist
if 'import org.springframework.data.domain.Page;' not in gs:
    gs = gs.replace('import java.util.List;', 'import java.util.List;\nimport org.springframework.data.domain.Page;\nimport org.springframework.data.domain.Pageable;')

old_findall = 'public List<Game> findAll() { return gameRepository.findAll(); }'
new_findall = '''public List<Game> findAll() { return gameRepository.findAll(); }

    @Transactional(readOnly = true)
    public Page<Game> findAll(Pageable pageable) { return gameRepository.findAll(pageable); }'''

if new_findall not in gs:
    gs = gs.replace(old_findall, new_findall)
    with open(gs_path, 'w', encoding='utf-8') as f:
        f.write(gs)

# 2. Fix @Builder.Default warnings
entity_dir = 'code/src/main/java/com/eternalclash2/domain/entity'
for filename in os.listdir(entity_dir):
    if filename.endswith('.java'):
        filepath = os.path.join(entity_dir, filename)
        with open(filepath, 'r', encoding='utf-8') as f:
            lines = f.readlines()
        
        modified = False
        import_builder_default = False
        for i, line in enumerate(lines):
            # Check if line is a private field with initialization
            if re.search(r'private\s+[\w<>]+\s+\w+\s*=\s*.+;', line):
                # Ensure the previous line isn't already @Builder.Default
                if i > 0 and '@Builder.Default' not in lines[i-1] and '@Builder.Default' not in line:
                    lines[i] = '    @Builder.Default\n' + line
                    modified = True
                    import_builder_default = True
        
        if import_builder_default:
            # Lombok's @Builder.Default is usually inside Lombok or imported if needed.
            # Actually, it's just lombok.Builder.Default, but usually @Builder.Default works if lombok.Builder is imported or just natively since @Builder is on the class.
            pass
            
        if modified:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.writelines(lines)

print("GameService and Entities fixed")
