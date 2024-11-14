import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomAI extends AIPlayer {

    private final SimpleDisc simpleDisc;
    private final BombDisc bombDisc;
    private final UnflippableDisc unflippableDisc;
    private int unflipped;
    private int bombed;

    public RandomAI(boolean isPlayerOne) {
        super(isPlayerOne);
        this.simpleDisc = new SimpleDisc(this);
        this.bombDisc = new BombDisc(this);
        this.unflippableDisc = new UnflippableDisc(this);
        this.unflipped = 2;
        this.bombed = 3;
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
            Disc aiDisc = randomDiscsType();
            if(bombed > 0)
            {
                if(aiDisc.getType().equals(bombDisc.getType()))
                {
                    bombed--;
                    aiDisc = new BombDisc(this);
                    return new Move(moves.get(randomNum), aiDisc);
                }
            }
            if(unflipped > 0)
            {
                if (aiDisc.getType().equals(unflippableDisc.getType())) {
                    unflipped--;
                    aiDisc = new UnflippableDisc(this);
                    return new Move(moves.get(randomNum), aiDisc);
                }
            }
            if(bombed == 0 || unflipped == 0)
            {
                aiDisc = new SimpleDisc(this);
            }
            Position selectedMove = moves.get(randomNum);
            System.out.println("RandomAI selected move at position: " + selectedMove + " with owner: " + aiDisc.get_owner());
        return new Move(moves.get(randomNum), aiDisc);
    }

    private Disc randomDiscsType()
    {
        Random rand = new Random();
        ArrayList<Disc> discs = new ArrayList<>();
        discs.add(simpleDisc);
        discs.add(bombDisc);
        discs.add(unflippableDisc);
        int randomNum1 = rand.nextInt(3);
        return discs.get(randomNum1);
    }
}
