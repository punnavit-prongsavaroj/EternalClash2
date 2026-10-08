with open('code/src/main/java/com/eternalclash2/service/GameViewService.java', 'r', encoding='utf-8') as f:
    java = f.read()

target = '''            return new NodeSnapshot(
                city.getId(),
                city.getX(),
                city.getY(),'''

replacement = '''            return new NodeSnapshot(
                city.getId(),
                city.getName(),
                city.getX(),
                city.getY(),'''

java = java.replace(target, replacement)
with open('code/src/main/java/com/eternalclash2/service/GameViewService.java', 'w', encoding='utf-8') as f:
    f.write(java)
