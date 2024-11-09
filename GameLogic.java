import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GameLogic implements PlayableLogic {
    private static final int BOARD_SIZE = 8;
    private Disc[][] board;
    private ArrayList<Player> players;
    private boolean isFirstPlayerTurn;
    private Stack<Move> moveHistory;
    private int[][] directions = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1},   // Up, Down, Left, Right
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}  // Diagonals
    };

    public GameLogic() {
        this.board = new Disc[BOARD_SIZE][BOARD_SIZE];
        this.players = new ArrayList<>(2);
        this.isFirstPlayerTurn = true; // Set to true for first player's turn
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
        if (!isValidMove(a, disc)) {
            return false;
        }

        // Place the disc on the board
        board[a.getRow()][a.getCol()] = disc;
        flipOpponentDiscs(a, disc);

        // Record move for undo functionality
        moveHistory.push(new Move(a, disc));

        // Switch turn to the other player
        isFirstPlayerTurn = !isFirstPlayerTurn;
        return true;
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
        return isFirstPlayerTurn;
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
        if (moveHistory.isEmpty()) {
            System.out.println("Nothing to undo");
        } else if (players.get(0).isHuman() && players.get(1).isHuman())
        {
            Move lastMove = moveHistory.pop();
            Position pos = lastMove.getPosition();
            board[pos.getRow()][pos.getCol()] = null; // Remove the disc
        }

    }

    private void flipOpponentDiscs(Position startPosition, Disc disc)
    {

        for (int[] direction : directions) {
            List<Position> discsToFlip = getDiscsToFlipInDirection(startPosition, disc, direction);

            for (Position position : discsToFlip) {
                flipDisc(position);
            }
        }
    }

    //This method returns list of all the positions of discs on the board that needs to be flipped.
    private List<Position> getDiscsToFlipInDirection(Position start, Disc disc, int[] direction)
    {
        ArrayList<Position> discsToFlip = new ArrayList<>(); // Array for the discs to be flipped
        int row = start.getRow();
        int col = start.getCol();
        int deltaRow = direction[0];
        int deltaCol = direction[1];
        while (true)
        {
            row += deltaRow;
            col += deltaCol;

            // Check if we are out of bound of the board
            if(row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE)
            {
                return List.of(); // No discs to flip if out of bounds
            }
            Disc currentDisc = board[row][col];
            if(currentDisc == null)
            {
                return List.of(); // we hit an empty spot so there are no disks to be flipped
            }
            if (currentDisc.get_owner().equals(disc.get_owner())) {
                return discsToFlip; // found a matching disc, now return all the discs that needs to be flipped
            }
            discsToFlip.add(new Position(row, col)); // Add a new position to flip
        }
    }

    private Player getCurrentPlayer() {
        return isFirstPlayerTurn() ? players.get(0) : players.get(1);
    }


    private void flipDisc(Position position)
    {
        Disc disc = board[position.row()][position.col()]; // New Disc initialization
        if(disc instanceof SimpleDisc)
        {
            disc.set_owner(getCurrentPlayer()); // If simple disc --> flip
        }
        else if(disc instanceof BombDisc)
        {
            explode(position); // If bomb disc --> bomb flip
        }
        // Unflippable disc should not be flipped
    }
    private void explode(Position bombPosition)
    {
        for (int[] direction : directions)
        {
            List<Position> discsToFlip = getDiscsToFlipInDirection(bombPosition, new SimpleDisc(getCurrentPlayer()), direction);

            for (Position position : discsToFlip)
            {
                Disc disc = board[position.row()][position.col()];
                if (!(disc instanceof UnflippableDisc))
                {
                    disc.set_owner(getCurrentPlayer());
                }
            }
        }
    }













}
