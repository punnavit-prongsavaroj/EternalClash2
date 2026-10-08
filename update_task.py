import re

with open(r'C:\Users\User\.gemini\antigravity\brain\4f1b7e91-8126-431a-be38-c612c4731948\task.md', 'r', encoding='utf-8') as f:
    task = f.read()

task = task.replace('- [ ] Create MapNode Entity (id, x, y, game).', '- [x] Create MapNode Entity (id, x, y, game). (Done via City entity with Coordinates)')
task = task.replace('- [ ] Create MapEdge Entity (id, game, node1, node2).', '- [x] Create MapEdge Entity (id, game, node1, node2).')
task = task.replace('- [ ] Update City Entity (remove 1-to-1 Player relationship, add Many-to-1 Player, Many-to-1 MapNode).', '- [x] Update City Entity (remove 1-to-1 Player relationship, add Many-to-1 Player, Many-to-1 MapNode).')
task = task.replace('- [ ] Move ood and soldiers from Player (or previous City structure) to be per-city.', '- [x] Move ood and soldiers from Player (or previous City structure) to be per-city.')
task = task.replace('- [ ] Update Army Entity (track movement via edges, change target to targetCity/targetNode).', '- [x] Update Army Entity (track movement via edges, change target to targetCity/targetNode).')
task = task.replace('- [ ] Update TurnAction Entity (add sourceCityId).', '- [x] Update TurnAction Entity (add sourceCityId).')

task = task.replace('- [ ] Write map generation logic: Create N*3 nodes, assign 2-3 connections each, place random coordinates.', '- [x] Write map generation logic: Create N*3 nodes, assign 2-3 connections each, place random coordinates.')
task = task.replace('- [ ] Add PLACEMENT GameStatus.', '- [x] Add PLACEMENT GameStatus.')

task = task.replace('- [ ] Update TurnActionService to process actions per-city instead of per-player.', '- [x] Update TurnActionService to process actions per-city instead of per-player.')
task = task.replace('- [ ] Enforce "all owned cities must act" before turn advances.', '- [x] Enforce "all owned cities must act" before turn advances.')
task = task.replace('- [ ] Implement shared food logic (DFS/BFS for contiguous cities, divide cost, remainder to source).', '- [x] Implement shared food logic (DFS/BFS for contiguous cities, divide cost, remainder to source).')
task = task.replace('- [ ] Update combat logic (capture neutral/enemy node, surviving soldiers stay at the captured node).', '- [x] Update combat logic (capture neutral/enemy node, surviving soldiers stay at the captured node).')
task = task.replace('- [ ] Update GameViewService to serialize nodes, edges, cities, and local food/soldiers.', '- [x] Update GameViewService to serialize nodes, edges, cities, and local food/soldiers.')

with open(r'C:\Users\User\.gemini\antigravity\brain\4f1b7e91-8126-431a-be38-c612c4731948\task.md', 'w', encoding='utf-8') as f:
    f.write(task)
