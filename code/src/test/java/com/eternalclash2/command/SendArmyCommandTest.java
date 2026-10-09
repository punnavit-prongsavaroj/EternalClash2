package com.eternalclash2.command;

import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.Season;
import com.eternalclash2.repository.GameEventRepository;
import com.eternalclash2.service.ArmyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SendArmyCommandTest {

    @Mock
    private ArmyService armyService;
    
    @Mock
    private GameEventRepository gameEventRepository;

    @Test
    void execute_CallsArmyService() {
        Game game = Game.builder().id(1L).build();
        Player player = Player.builder().id(100L).game(game).build();
        City sourceCity = City.builder().id(10L).player(player).food(100).soldiers(50).build();
        
        SendArmyCommand command = new SendArmyCommand(armyService, sourceCity, 20L, 20, 1, gameEventRepository, game);

        command.execute();

        verify(armyService).sendArmy(10L, 20L, 20, 1);
    }
}
