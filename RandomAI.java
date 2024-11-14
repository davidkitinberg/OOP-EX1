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
        while (!gameStatus.isGameFinished())
        {
            Random rand = new Random();
            List<Position> moves = gameStatus.ValidMoves();
            if (moves.isEmpty())
            {
                moves = gameStatus.ValidMoves();
            }
            int numMoves = moves.size();
            int randomNum = rand.nextInt(numMoves);
            gameStatus.locate_disc(moves.get(randomNum), disc);
            return new Move(moves.get(randomNum), disc);
        }
        return null;
    }
}
