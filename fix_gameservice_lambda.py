import re

with open('code/src/main/java/com/eternalclash2/service/GameService.java', 'r', encoding='utf-8') as f:
    java = f.read()

old_code = '''                    boolean exists = edges.stream().anyMatch(e -> 
                        (e.getCity1().getId().equals(nodes.get(i).getId()) && e.getCity2().getId().equals(nodes.get(target).getId())) ||
                        (e.getCity2().getId().equals(nodes.get(i).getId()) && e.getCity1().getId().equals(nodes.get(target).getId()))
                    );'''

new_code = '''                    final City c1 = nodes.get(i);
                    final City c2 = nodes.get(target);
                    boolean exists = edges.stream().anyMatch(e -> 
                        (e.getCity1().getId().equals(c1.getId()) && e.getCity2().getId().equals(c2.getId())) ||
                        (e.getCity2().getId().equals(c1.getId()) && e.getCity1().getId().equals(c2.getId()))
                    );'''

with open('code/src/main/java/com/eternalclash2/service/GameService.java', 'w', encoding='utf-8') as f:
    f.write(java.replace(old_code, new_code))
