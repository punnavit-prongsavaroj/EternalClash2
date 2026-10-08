with open('code/src/main/java/com/eternalclash2/service/GameViewService.java', 'r', encoding='utf-8') as f:
    java = f.read()

java = java.replace('public record NodeSnapshot(Long nodeId, Double x, Double y, Long ownerId, Integer food, Integer soldiers, boolean actionUsedThisTurn) {}', 'public record NodeSnapshot(Long nodeId, String name, Double x, Double y, Long ownerId, Integer food, Integer soldiers, boolean actionUsedThisTurn) {}')

java = java.replace('new NodeSnapshot(\n                        city.getId(),\n                        city.getX(),\n                        city.getY(),\n                        city.getPlayer() != null ? city.getPlayer().getId() : null,\n                        isViewerOwner ? city.getFood() : null,\n                        isViewerOwner ? city.getSoldiers() : null,\n                        Boolean.TRUE.equals(city.getActionUsedThisTurn())\n                )', 'new NodeSnapshot(\n                        city.getId(),\n                        city.getName(),\n                        city.getX(),\n                        city.getY(),\n                        city.getPlayer() != null ? city.getPlayer().getId() : null,\n                        isViewerOwner ? city.getFood() : null,\n                        isViewerOwner ? city.getSoldiers() : null,\n                        Boolean.TRUE.equals(city.getActionUsedThisTurn())\n                )')

with open('code/src/main/java/com/eternalclash2/service/GameViewService.java', 'w', encoding='utf-8') as f:
    f.write(java)
