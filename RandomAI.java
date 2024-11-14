import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomAI extends AIPlayer {

    private final Disc disc;

    public RandomAI(boolean isPlayerOne) {
        super(isPlayerOne);
        this.disc = new SimpleDisc(this);
    }

    @Override
    public Move makeMove(PlayableLogic gameStatus)
    {
        if (gameStatus.isGameFinished())
        {
            return null; // Game is finished; no move to make
        }
            Random rand = new Random();
            List<Position> moves = gameStatus.ValidMoves();
            if (moves.isEmpty())
            {
                return null; // No valid moves available, return null
            }
            int randomNum = rand.nextInt(moves.size());
            //gameStatus.locate_disc(moves.get(randomNum), disc);
            Disc aiDisc = new SimpleDisc(this);
            Position selectedMove = moves.get(randomNum);
            System.out.println("RandomAI selected move at position: " + selectedMove + " with owner: " + aiDisc.get_owner());

        return new Move(moves.get(randomNum), aiDisc);

    }
}
