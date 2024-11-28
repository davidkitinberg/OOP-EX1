import java.util.List;


public class GreedyAI extends AIPlayer
{


    public GreedyAI(boolean isPlayerOne) {
        super(isPlayerOne);

    }

    public Move makeMove(PlayableLogic gameStatus)
    {
        if (gameStatus.isGameFinished())
        {
            return null; // Game is finished; no move to make
        }
        List<Position> moves = gameStatus.ValidMoves();
        if (moves.isEmpty())
        {
            return null; // No valid moves available, return null
        }
        Position bestMove = null;
        int maxFlips = 0;

        // Loop to find the position with the most flips
        for (Position move : moves) {
            int flips = gameStatus.countFlips(move);
            if (flips > maxFlips)
            {
                maxFlips = flips;
                bestMove = move;
            }
            else if (flips == maxFlips)
            {
                // If flips are the same, prioritize by column (most eastern)
                if (move.getCol() > bestMove.getCol() ||
                        (move.getCol() == bestMove.getCol() && move.getRow() > bestMove.getRow()))
                {
                    bestMove = move;
                }
            }
        }
        Disc aiDisc = new SimpleDisc(this);
        return new Move(bestMove, aiDisc);
    }
}
