package com.eternalclash2.service;

import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.Marshal;
import com.eternalclash2.domain.entity.MarshalCandidate;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.exception.BusinessLogicException;
import com.eternalclash2.exception.ResourceNotFoundException;
import com.eternalclash2.repository.GameRepository;
import com.eternalclash2.repository.MarshalCandidateRepository;
import com.eternalclash2.repository.MarshalRepository;
import com.eternalclash2.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarshalDraftServiceTest {

    @Mock
    private GameRepository gameRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private MarshalRepository marshalRepository;
    @Mock
    private MarshalCandidateRepository candidateRepository;

    @InjectMocks
    private MarshalDraftService marshalDraftService;

    private Game game;
    private Player player1;
    private Player player2;

    @BeforeEach
    void buildFixtures() {
        game = Game.builder().id(1L).status(GameStatus.MARSHAL_SELECTION).currentTurnNumber(0).build();
        player1 = Player.builder().id(1L).name("Player1").game(game).isAlive(true).rerollCount(0).build();
        player2 = Player.builder().id(2L).name("Player2").game(game).isAlive(true).rerollCount(0).build();
    }

    private Marshal marshal(Long id) {
        return Marshal.builder().id(id).name("Marshal" + id).build();
    }

    private MarshalCandidate candidate(Long id, Player player, Marshal marshal, int slot) {
        return MarshalCandidate.builder().id(id).player(player).marshal(marshal).slotNumber(slot).isSelected(false).build();
    }

    /** The three cards a draftee is offered, as selectCurrentCandidate and reroll expect them. */
    private List<MarshalCandidate> threeCandidatesFor(Player player) {
        return List.of(candidate(11L, player, marshal(101L), 1),
                candidate(12L, player, marshal(102L), 2),
                candidate(13L, player, marshal(103L), 3));
    }

    @Test
    void tc01_prepareNextPlayer_offersThreeFreshCandidatesToTheFirstPlayerWithoutAMarshal() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findByGame_IdOrderById(1L)).thenReturn(List.of(player1, player2));
        when(candidateRepository.findByPlayer_IdOrderBySlotNumber(1L)).thenReturn(List.of());
        when(marshalRepository.findAll()).thenReturn(List.of(marshal(1L), marshal(2L), marshal(3L), marshal(4L), marshal(5L)));
        when(candidateRepository.save(any(MarshalCandidate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<MarshalCandidate> offered = marshalDraftService.prepareNextPlayer(1L);

        assertEquals(3, offered.size());
        assertEquals(List.of(1, 2, 3), offered.stream().map(MarshalCandidate::getSlotNumber).toList());
        assertTrue(offered.stream().allMatch(c -> Boolean.FALSE.equals(c.getIsSelected())));
        assertTrue(offered.stream().allMatch(c -> c.getPlayer() == player1));
    }

    @Test
    void tc02_prepareNextPlayer_returnsTheExistingHandInsteadOfReshuffling() {
        List<MarshalCandidate> existing = threeCandidatesFor(player1);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findByGame_IdOrderById(1L)).thenReturn(List.of(player1, player2));
        when(candidateRepository.findByPlayer_IdOrderBySlotNumber(1L)).thenReturn(existing);

        List<MarshalCandidate> offered = marshalDraftService.prepareNextPlayer(1L);

        assertEquals(3, offered.size());
        verify(marshalRepository, never()).findAll();
        verify(candidateRepository, never()).save(any());
    }

    @Test
    void tc03_prepareNextPlayer_onceEveryMarshalIsChosen_movesIntoPlacement() {
        player1.setMarshal(marshal(1L));
        player2.setMarshal(marshal(2L));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findByGame_IdOrderById(1L)).thenReturn(List.of(player1, player2));

        List<MarshalCandidate> offered = marshalDraftService.prepareNextPlayer(1L);

        assertTrue(offered.isEmpty());
        assertEquals(GameStatus.PLACEMENT, game.getStatus());
        verify(gameRepository).save(game);
    }

    @Test
    void tc04_prepareNextPlayer_outsideTheSelectionPhase_isRejected() {
        game.setStatus(GameStatus.IN_PROGRESS);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> marshalDraftService.prepareNextPlayer(1L));

        assertEquals("Game is not in marshal selection", exception.getMessage());
    }

    @Test
    void tc05_prepareNextPlayer_withUnknownGame_throwsResourceNotFoundException() {
        when(gameRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> marshalDraftService.prepareNextPlayer(99L));

        assertEquals("Game not found with id: 99", exception.getMessage());
    }

    @Test
    void tc06_prepareNextPlayer_withEveryMarshalAlreadyTaken_isRejected() {
        player2.setMarshal(marshal(1L));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findByGame_IdOrderById(1L)).thenReturn(List.of(player1, player2));
        when(candidateRepository.findByPlayer_IdOrderBySlotNumber(1L)).thenReturn(List.of());
        when(marshalRepository.findAll()).thenReturn(List.of(marshal(1L)));

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> marshalDraftService.prepareNextPlayer(1L));

        assertEquals("No marshals are available for this draft", exception.getMessage());
    }

    @Test
    void tc07_prepareNextPlayer_neverOffersAMarshalAnotherPlayerAlreadyHolds() {
        player2.setMarshal(marshal(1L));
        Player player3 = Player.builder().id(3L).name("Player3").game(game).isAlive(true).marshal(marshal(2L)).build();
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findByGame_IdOrderById(1L)).thenReturn(List.of(player1, player2, player3));
        when(candidateRepository.findByPlayer_IdOrderBySlotNumber(1L)).thenReturn(List.of());
        when(marshalRepository.findAll()).thenReturn(List.of(marshal(1L), marshal(2L), marshal(3L), marshal(4L)));
        when(candidateRepository.save(any(MarshalCandidate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<MarshalCandidate> offered = marshalDraftService.prepareNextPlayer(1L);

        assertEquals(2, offered.size());
        assertTrue(offered.stream().noneMatch(c -> c.getMarshal().getId() == 1L || c.getMarshal().getId() == 2L));
    }

    @Test
    void tc08_reroll_movesToTheNextCardAndCountsItself() {
        List<MarshalCandidate> hand = threeCandidatesFor(player1);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));
        when(playerRepository.findByGame_IdOrderById(1L)).thenReturn(List.of(player1, player2));
        when(candidateRepository.findByPlayer_IdOrderBySlotNumber(1L)).thenReturn(hand);

        MarshalCandidate result = marshalDraftService.reroll(1L);

        assertSame(hand.get(1), result);
        assertEquals(1, player1.getRerollCount());
        verify(playerRepository).save(player1);
    }

    @Test
    void tc09_reroll_afterTheThirdCard_isRejected() {
        player1.setRerollCount(2);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));
        when(playerRepository.findByGame_IdOrderById(1L)).thenReturn(List.of(player1, player2));
        when(candidateRepository.findByPlayer_IdOrderBySlotNumber(1L)).thenReturn(threeCandidatesFor(player1));

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> marshalDraftService.reroll(1L));

        assertEquals("No rerolls remain", exception.getMessage());
    }

    @Test
    void tc10_reroll_outOfTurn_isRejected() {
        when(playerRepository.findById(2L)).thenReturn(Optional.of(player2));
        when(playerRepository.findByGame_IdOrderById(1L)).thenReturn(List.of(player1, player2));

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> marshalDraftService.reroll(2L));

        assertEquals("It is not this player's draft turn", exception.getMessage());
    }

    @Test
    void tc11_reroll_outsideTheSelectionPhase_isRejected() {
        game.setStatus(GameStatus.IN_PROGRESS);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> marshalDraftService.reroll(1L));

        assertEquals("Game is not in marshal selection", exception.getMessage());
    }

    @Test
    void tc12_selectCurrentCandidate_marksTheFirstCardAndHandsOverToTheNextPlayer() {
        List<MarshalCandidate> hand = threeCandidatesFor(player1);
        List<MarshalCandidate> nextHand = threeCandidatesFor(player2);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));
        when(playerRepository.findByGame_IdOrderById(1L)).thenReturn(List.of(player1, player2));
        when(candidateRepository.findByPlayer_IdOrderBySlotNumber(1L)).thenReturn(hand);
        when(candidateRepository.findByPlayer_IdOrderBySlotNumber(2L)).thenReturn(nextHand);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        Marshal chosen = marshalDraftService.selectCurrentCandidate(1L);

        assertSame(hand.get(0).getMarshal(), chosen);
        assertTrue(hand.get(0).getIsSelected());
        assertFalse(hand.get(1).getIsSelected());
        assertSame(hand.get(0).getMarshal(), player1.getMarshal());
        verify(candidateRepository).save(hand.get(0));
        verify(candidateRepository).findByPlayer_IdOrderBySlotNumber(2L);
    }

    @Test
    void tc13_selectCurrentCandidate_afterOneReroll_takesTheSecondCard() {
        player1.setRerollCount(1);
        List<MarshalCandidate> hand = threeCandidatesFor(player1);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));
        when(playerRepository.findByGame_IdOrderById(1L)).thenReturn(List.of(player1, player2));
        when(candidateRepository.findByPlayer_IdOrderBySlotNumber(1L)).thenReturn(hand);
        when(candidateRepository.findByPlayer_IdOrderBySlotNumber(2L)).thenReturn(threeCandidatesFor(player2));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        Marshal chosen = marshalDraftService.selectCurrentCandidate(1L);

        assertSame(hand.get(1).getMarshal(), chosen);
        assertSame(hand.get(1), hand.stream().filter(MarshalCandidate::getIsSelected).findFirst().orElseThrow());
    }

    @Test
    void tc14_selectCurrentCandidate_byTheLastPlayerInDraft_startsPlacement() {
        player1.setMarshal(marshal(1L));
        List<MarshalCandidate> hand = threeCandidatesFor(player2);
        when(playerRepository.findById(2L)).thenReturn(Optional.of(player2));
        when(playerRepository.findByGame_IdOrderById(1L)).thenReturn(List.of(player1, player2));
        when(candidateRepository.findByPlayer_IdOrderBySlotNumber(2L)).thenReturn(hand);

        Marshal chosen = marshalDraftService.selectCurrentCandidate(2L);

        assertSame(hand.get(0).getMarshal(), chosen);
        assertEquals(GameStatus.PLACEMENT, game.getStatus());
        verify(gameRepository).save(game);
        verify(gameRepository, never()).findById(1L);
    }

    @Test
    void tc15_selectCurrentCandidate_withoutAnyCandidate_isRejected() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));
        when(playerRepository.findByGame_IdOrderById(1L)).thenReturn(List.of(player1, player2));
        when(candidateRepository.findByPlayer_IdOrderBySlotNumber(1L)).thenReturn(List.of());

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> marshalDraftService.selectCurrentCandidate(1L));

        assertEquals("No marshal candidates are available", exception.getMessage());
        verify(candidateRepository, never()).save(any());
    }
}
