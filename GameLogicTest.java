import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameLogicTest {
    GameLogic gameLogic;
    Player player1;
    Player player2;

        @BeforeEach
        void setUp() {
            gameLogic = new GameLogic();
            // Use concrete subclasses of Player
            Player player1 = new HumanPlayer(true);  // Assuming HumanPlayer is a concrete subclass of Player
            Player player2 = new RandomAI(false);
            gameLogic.setPlayers(player1, player2); // Initialize players
            gameLogic.reset();
        }

        @Test
        void locate_disc() {
            // Initial setup: Place a disc in a valid position
            Position position = new Position(4, 2); // A valid position for the first move
            Disc disc = new SimpleDisc(gameLogic.getFirstPlayer());

            assertTrue(gameLogic.locate_disc(position, disc), "Disc should be placed successfully.");
            assertEquals(disc, gameLogic.getDiscAtPosition(position), "The disc at the position should match the placed disc.");
        }



    @Test
    void getDiscAtPosition() {
            Position position = new Position(4, 2);
            Disc disc = new SimpleDisc(gameLogic.getFirstPlayer());
            gameLogic.locate_disc(position, disc);
            assertEquals(gameLogic.getDiscAtPosition(position),disc, "Disc should be placed successfully.");

    }

    @Test
    void validMoves() {
            int count = 0;
            Position validPos1 = new Position(4, 2);
            Position validPos2 = new Position(2, 4);
            Position validPos3 = new Position(5, 3);
            Position validPos4 = new Position(3, 5);
            List<Position>  theValidPos = new ArrayList<Position>();
            theValidPos.add(validPos1);
            theValidPos.add(validPos2);
            theValidPos.add(validPos3);
            theValidPos.add(validPos4);
        List<Position> actualValidMoves = gameLogic.ValidMoves();
        for(Position pos : actualValidMoves)
        {
            for (Position pos2 : theValidPos)
            {
                if(pos.getCol() == pos2.getCol() && pos.getRow() == pos2.getRow())
                {
                    count++;
                }

            }
        }
            assertEquals(actualValidMoves.size(),count, "The valid moves should match.");

    }

    @Test
    void countFlips() {
        // Arrange: Setup a complex scenario with multiple flips
        gameLogic.locate_disc(new Position(4, 2), new SimpleDisc(gameLogic.getFirstPlayer()));
        gameLogic.locate_disc(new Position(3, 2), new SimpleDisc(gameLogic.getSecondPlayer()));
        gameLogic.locate_disc(new Position(2, 4), new SimpleDisc(gameLogic.getFirstPlayer()));
        gameLogic.locate_disc(new Position(3, 5), new SimpleDisc(gameLogic.getSecondPlayer()));
        gameLogic.locate_disc(new Position(2, 2), new SimpleDisc(gameLogic.getFirstPlayer()));

        Position testPosition = new Position(3, 1);
        int flips = gameLogic.countFlips(testPosition);

        assertEquals(2, flips, "The move at (3, 1) should flip exactly 2 discs.");
    }

    @Test
    void setPlayers() {
        // Arrange: Create two players
        Player firstPlayer = new HumanPlayer(true); // Player 1
        Player secondPlayer = new HumanPlayer(false); // Player 2

        // Act: Set the players in the GameLogic instance
        gameLogic.setPlayers(firstPlayer, secondPlayer);

        // Assert: Verify the players are set correctly
        assertEquals(firstPlayer, gameLogic.getFirstPlayer(), "Player 1 should be set correctly.");
        assertEquals(secondPlayer, gameLogic.getSecondPlayer(), "Player 2 should be set correctly.");

    }

    @Test
    void isGameFinished() {
        // Test Case 1: Game is not finished initially
        assertFalse(gameLogic.isGameFinished(), "The game should not be finished at the start.");

        // Test Case 2: Verify winner
        int firstPlayerDiscs = 0;
        int secondPlayerDiscs = 0;

        while(!gameLogic.ValidMoves().isEmpty())
        {
            Position pos = gameLogic.ValidMoves().getFirst();
            Disc disc = new SimpleDisc(gameLogic.getCurrentPlayer());
            gameLogic.locate_disc(pos, disc);

            if (disc.get_owner().equals(gameLogic.getFirstPlayer())) {
                firstPlayerDiscs++;
            }
            else
            {
                secondPlayerDiscs++;
            }
        }

        if (firstPlayerDiscs > secondPlayerDiscs) {
            assertEquals(1, gameLogic.getFirstPlayer().getWins(), "Player 1 should be declared the winner.");
        } else {
            assertEquals(0, gameLogic.getSecondPlayer().getWins(), "Player 2 should be declared the winner.");
        }
    }




    @Test
    void reset() {
        gameLogic.locate_disc(new Position(4, 2), new SimpleDisc(gameLogic.getFirstPlayer()));
        gameLogic.reset();
        assertNull(gameLogic.getDiscAtPosition(new Position(4, 2)));

    }

    @Test
    void undoLastMove() {
        // Arrange: Set up the initial game and perform moves
        Position move1 = new Position(4, 2);
        Position move2 = new Position(3, 2);

        assertTrue(gameLogic.locate_disc(move1, new SimpleDisc(gameLogic.getFirstPlayer())), "First move should be valid.");
        assertTrue(gameLogic.locate_disc(move2, new SimpleDisc(gameLogic.getSecondPlayer())), "Second move should be valid.");

        // Assert: Moves were placed correctly
        assertNotNull(gameLogic.getDiscAtPosition(move1), "First move should be placed.");
        assertNotNull(gameLogic.getDiscAtPosition(move2), "Second move should be placed.");

        // Act & Assert: Undo second move
        gameLogic.undoLastMove();
        assertNull(gameLogic.getDiscAtPosition(move2), "Second move should be undone.");
        assertNotNull(gameLogic.getDiscAtPosition(move1), "First move should still be present.");

        // Act & Assert: Undo first move
        gameLogic.undoLastMove();
        assertNull(gameLogic.getDiscAtPosition(move1), "First move should be undone.");

        // Act & Assert: Excessive undo attempts
        gameLogic.undoLastMove();
        for (int row = 0; row < gameLogic.getBoardSize(); row++) {
            for (int col = 0; col < gameLogic.getBoardSize(); col++) {
                if ((row == 3 && col == 3) || (row == 3 && col == 4) ||
                        (row == 4 && col == 3) || (row == 4 && col == 4)) {
                    assertNotNull(gameLogic.getDiscAtPosition(new Position(row, col)),
                            "Initial center discs should remain on the board.");
                } else {
                    assertNull(gameLogic.getDiscAtPosition(new Position(row, col)),
                            "Other board positions should be empty.");
                }
            }
        }
    }

}