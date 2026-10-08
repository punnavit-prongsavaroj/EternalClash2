import re

with open('code/src/test/java/com/eternalclash2/controller/GameEndpointTest.java', 'r', encoding='utf-8') as f:
    java = f.read()

# 1. Fix player(city) removal from getPlayerSerialisesWithoutFollowingEntityRelations
old_test1 = '''    @Test
    void getPlayerSerialisesWithoutFollowingEntityRelations() throws Exception {
        Game game = game(10L, GameStatus.IN_PROGRESS, 3);
        Player player = player(1L, game);
        player.setCity(City.builder().id(5L).player(player).name("Pizza's City").food(50).soldiers(0).build());
        given(playerService.findById(1L)).willReturn(player);

        mockMvc.perform(get("/api/players/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(10))
                .andExpect(jsonPath("$.name").value("Pizza"))
                .andExpect(jsonPath("$.marshal.name").value("โจโฉ"))
                .andExpect(jsonPath("$.food").value(50))
                .andExpect(jsonPath("$.game").doesNotExist())
                .andExpect(jsonPath("$.city").doesNotExist());
    }'''

new_test1 = '''    @Test
    void getPlayerSerialisesWithoutFollowingEntityRelations() throws Exception {
        Game game = game(10L, GameStatus.IN_PROGRESS, 3);
        Player player = player(1L, game);
        given(playerService.findById(1L)).willReturn(player);

        mockMvc.perform(get("/api/players/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(10))
                .andExpect(jsonPath("$.name").value("Pizza"))
                .andExpect(jsonPath("$.marshal.name").value("โจโฉ"))
                .andExpect(jsonPath("$.game").doesNotExist())
                .andExpect(jsonPath("$.city").doesNotExist());
    }'''

java = java.replace(old_test1, new_test1)

# 2. Fix submitActionRecordsTheCommandResult
old_test2 = '''    @Test
    void submitActionRecordsTheCommandResult() throws Exception {
        Game game = game(1L, GameStatus.IN_PROGRESS, 3);
        Player player = player(2L, game);
        TurnAction action = TurnAction.builder().id(9L).game(game).turnNumber(3).player(player)
                .actionType(ActionType.SEND_ARMY).foodBefore(50).foodAfter(25).soldiersBefore(100).soldiersAfter(80)
                .army(Army.builder().id(11L).owner(player).target(player(3L, game)).soldiers(20).build()).build();
        given(turnActionService.performAction(eq(1L), eq(2L), eq(ActionType.SEND_ARMY), eq(3L), eq(20)))
                .willReturn(action);

        mockMvc.perform(post("/api/games/1/players/2/actions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\\"actionType\\":\\"SEND_ARMY\\",\\"targetPlayerId\\":3,\\"soldierCount\\":20}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.turnNumber").value(3))
                .andExpect(jsonPath("$.actionType").value("SEND_ARMY"))
                .andExpect(jsonPath("$.armyId").value(11))
                .andExpect(jsonPath("$.soldiersAfter").value(80));
    }'''

new_test2 = '''    @Test
    void submitActionRecordsTheCommandResult() throws Exception {
        Game game = game(1L, GameStatus.IN_PROGRESS, 3);
        Player player = player(2L, game);
        City sourceCity = City.builder().id(10L).player(player).name("Source").build();
        City targetCity = City.builder().id(3L).player(player(4L, game)).name("Target").build();
        TurnAction action = TurnAction.builder().id(9L).game(game).turnNumber(3).player(player).city(sourceCity)
                .actionType(ActionType.SEND_ARMY).foodBefore(50).foodAfter(25).soldiersBefore(100).soldiersAfter(80)
                .army(Army.builder().id(11L).owner(player).sourceCity(sourceCity).targetCity(targetCity).soldiers(20).build()).build();
        given(turnActionService.performAction(eq(1L), eq(2L), eq(10L), eq(ActionType.SEND_ARMY), eq(3L), eq(20)))
                .willReturn(action);

        mockMvc.perform(post("/api/games/1/players/2/actions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\\"cityId\\":10,\\"actionType\\":\\"SEND_ARMY\\",\\"targetCityId\\":3,\\"soldierCount\\":20}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.turnNumber").value(3))
                .andExpect(jsonPath("$.actionType").value("SEND_ARMY"))
                .andExpect(jsonPath("$.armyId").value(11))
                .andExpect(jsonPath("$.soldiersAfter").value(80));
    }'''

java = java.replace(old_test2, new_test2)

# 3. Fix battlesAreLimitedToOneGameAndOneTurn (army target -> targetCity)
old_test3 = '''    @Test
    void battlesAreLimitedToOneGameAndOneTurn() throws Exception {
        Game first = game(1L, GameStatus.IN_PROGRESS, 4);
        Player attacker = player(2L, first);
        Player defender = player(3L, first);
        Army army = Army.builder().id(11L).owner(attacker).target(defender).soldiers(50).build();'''

new_test3 = '''    @Test
    void battlesAreLimitedToOneGameAndOneTurn() throws Exception {
        Game first = game(1L, GameStatus.IN_PROGRESS, 4);
        Player attacker = player(2L, first);
        Player defender = player(3L, first);
        City targetCity = City.builder().id(3L).player(defender).name("DefCity").build();
        Army army = Army.builder().id(11L).owner(attacker).targetCity(targetCity).soldiers(50).build();'''

java = java.replace(old_test3, new_test3)

with open('code/src/test/java/com/eternalclash2/controller/GameEndpointTest.java', 'w', encoding='utf-8') as f:
    f.write(java)
