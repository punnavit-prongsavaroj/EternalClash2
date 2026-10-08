with open('code/src/main/java/com/eternalclash2/service/GameService.java', 'r', encoding='utf-8') as f:
    java = f.read()

target = '''        for (int i = 0; i < nodeCount; i++) {
            String cName = i < cityNames.size() ? cityNames.get(i) : "Node " + (i + 1);
            City city = City.builder()
                .game(game)
                .name(cName)
                .food(0)
                .soldiers(30)
                .x(10.0 + rand.nextDouble() * 80.0) // 10% to 90% width
                .y(10.0 + rand.nextDouble() * 80.0) // 10% to 90% height
                .build();
            nodes.add(cityRepository.save(city));
        }'''

replacement = '''        List<Double> xCoords = new ArrayList<>();
        List<Double> yCoords = new ArrayList<>();
        for (int i = 0; i < nodeCount; i++) {
            String cName = i < cityNames.size() ? cityNames.get(i) : "Node " + (i + 1);
            
            double x = 0, y = 0;
            boolean valid = false;
            int attempts = 0;
            while (!valid && attempts < 100) {
                x = 10.0 + rand.nextDouble() * 80.0;
                y = 10.0 + rand.nextDouble() * 80.0;
                valid = true;
                for (int j = 0; j < xCoords.size(); j++) {
                    double dx = xCoords.get(j) - x;
                    double dy = yCoords.get(j) - y;
                    if (Math.sqrt(dx * dx + dy * dy) < 12.0) { // minimum distance of 12% between nodes
                        valid = false;
                        break;
                    }
                }
                attempts++;
            }
            xCoords.add(x);
            yCoords.add(y);

            City city = City.builder()
                .game(game)
                .name(cName)
                .food(0)
                .soldiers(30)
                .x(x)
                .y(y)
                .build();
            nodes.add(cityRepository.save(city));
        }'''

java = java.replace(target, replacement)
with open('code/src/main/java/com/eternalclash2/service/GameService.java', 'w', encoding='utf-8') as f:
    f.write(java)
