import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GameLogic implements PlayableLogic {
    private static final int BOARD_SIZE = 8;
    private Disc[][] board;
    private ArrayList<Player> players;
    //private boolean isFirstPlayerTurn;
    private Stack<Move> moveHistory;

    public GameLogic() {
        this.board = new Disc[BOARD_SIZE][BOARD_SIZE];
        this.players = new ArrayList<>(2);
        //this.isFirstPlayerTurn = true; // Set to true for first player's turn
        moveHistory = new Stack<>();
        initializeBoard();
    }

    private void initializeBoard() {
        // Place the starting four discs in the center of the board
        int mid = BOARD_SIZE / 2;
        board[mid - 1][mid - 1] = new SimpleDisc(players.get(0)); // Black
        board[mid - 1][mid] = new SimpleDisc(players.get(1)); // White
        board[mid][mid - 1] = new SimpleDisc(players.get(1)); // White
        board[mid][mid] = new SimpleDisc(players.get(0)); // Black
    }

    @Override
    public boolean locate_disc(Position a, Disc disc) {
        return false;
    }

    @Override
    public Disc getDiscAtPosition(Position position) {
        return board[position.row()][position.col()];
    }

    @Override
    public int getBoardSize() {
        return BOARD_SIZE;
    }

    @Override
    public List<Position> ValidMoves() {
        return List.of();
    }

    @Override
    public int countFlips(Position a) {
        return 0;
    }

    @Override
    public Player getFirstPlayer() {
        return players.get(0);
    }

    @Override
    public Player getSecondPlayer() {
        return players.get(1);
    }

    @Override
    public void setPlayers(Player player1, Player player2) {
        players.clear();
        players.add(player1);
        players.add(player2);
    }

    @Override
    public boolean isFirstPlayerTurn()
    {

        return false;
    }

    @Override
    public boolean isGameFinished() {
        return false;
    }

    @Override
    public void reset() {
        board = new Disc[BOARD_SIZE][BOARD_SIZE];
        players = new ArrayList<>(2);
        moveHistory = new Stack<>();
    }

    @Override
    public void undoLastMove() {
        if (moveHistory.isEmpty())
        {
            System.out.println("Nothing to undo");
        }
            else if (players.get(0).isHuman() && players.get(1).isHuman())
            {
                Move move = moveHistory.peek();
                moveHistory.pop();

            }

    }
}
