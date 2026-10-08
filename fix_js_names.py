with open('code/src/main/java/com/eternalclash2/service/GameService.java', 'r', encoding='utf-8') as f:
    java = f.read()

target = '''        // 1. Create nodes with random positions (using a simple grid-like logic to spread them out, or just random)
        for (int i = 0; i < nodeCount; i++) {
            City city = City.builder()
                .game(game)
                .name("Node " + (i + 1))
                .food(0)
                .soldiers(30)
                .x(10.0 + rand.nextDouble() * 80.0) // 10% to 90% width
                .y(10.0 + rand.nextDouble() * 80.0) // 10% to 90% height
                .build();
            nodes.add(cityRepository.save(city));
        }'''

replacement = '''        // 1. Create nodes with random positions
        List<String> cityNames = new ArrayList<>(Arrays.asList("ซือลี่", "อิวจิ๋ว", "กีจิ๋ว", "เป้งจิ๋ว", "เฉงจิ๋ว", "กุนจิ๋ว", "ชีจิ๋ว", "อื้อจิ๋ว", "สูจิ๋ว", "เหลียงจิ๋ว", "อี้จิ๋ว", "เกงจิ๋ว", "แยว่จิ๋ว", "เกาจิ๋ว", "ลิหยาง", "ลำกุ๋น", "เลนเหลง", "ฮุยแฮ", "บูเหล็ง", "ปาเสียว", "เองเฉวียน"));
        Collections.shuffle(cityNames, rand);
        for (int i = 0; i < nodeCount; i++) {
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

java = java.replace(target, replacement)
with open('code/src/main/java/com/eternalclash2/service/GameService.java', 'w', encoding='utf-8') as f:
    f.write(java)
