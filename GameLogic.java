import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GameLogic implements PlayableLogic {
    private static final int BOARD_SIZE = 8;
    private Disc[][] board;
    private Player player1;
    private Player player2;
    private boolean isFirstPlayerTurn;
    private Stack<Move> moveHistory;
    private ArrayList<Position> flippedpositions;
    private int placedDiscsCount = 4; // Track the number of placed discs on the board
    private int[][] directions = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1},   // Up, Down, Left, Right
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}  // Diagonals
    };

    public GameLogic() {
        this.board = new Disc[BOARD_SIZE][BOARD_SIZE];
        this.isFirstPlayerTurn = true; // Set to true for first player's turn
        moveHistory = new Stack<>();
    }

    private void initializeBoard() {
        // Place the starting four discs in the center of the board
        int mid = BOARD_SIZE / 2;
        board[mid - 1][mid - 1] = new SimpleDisc(player1); // Black
        board[mid - 1][mid] = new SimpleDisc(player2); // White
        board[mid][mid - 1] = new SimpleDisc(player2); // White
        board[mid][mid] = new SimpleDisc(player1); // Black
        placedDiscsCount = 4;
    }

    @Override
    public boolean locate_disc(Position a, Disc disc) {
        if (!isValidMove(a, disc))
        {
            return false;
        }
        if(!flippedpositions.isEmpty())
        {
            flippedpositions = new ArrayList<Position>(); // Clears flipped positions list
        }
        // Place the disc on the board
        board[a.getRow()][a.getCol()] = disc;
        flipOpponentDiscs(a, disc);

        // Record move for undo functionality
        moveHistory.push(new Move(a, disc));

        // Switch turn to the other player
        isFirstPlayerTurn = !isFirstPlayerTurn;
        placedDiscsCount++;
        return true;
    }

    private boolean isValidMove(Position a, Disc disc)
    {
        if(board[a.getRow()][a.getCol()] != null) {return false;} // Check if the cell is empty
        List<Position> collectionOfDiscsToFlip = new ArrayList<>();
        for (int[] direction : directions) // Goes through all directions in for loop
        {
            // Calls for a method that for each direction collects a list of position of discs on the board to be flipped
            List<Position> discsToFlip = getDiscsToFlipInDirection(a, disc, direction);
            collectionOfDiscsToFlip.addAll(discsToFlip);
        }
        if(collectionOfDiscsToFlip.isEmpty()) {return false;}
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
        List<Position> validMoves = new ArrayList<>();
        for(int i = 0; i < BOARD_SIZE; i++)
        {
            for(int j = 0; j < BOARD_SIZE; j++)
            {
                if(board[i][j] != null)
                {
                    if(isValidMove(new Position(i, j), board[i][j]))
                    {
                        validMoves.add(new Position(i, j));
                    }
                }
            }
        }
        return validMoves;
    }

    @Override
    public int countFlips(Position a) {


        return 0;
    }

    @Override
    public Player getFirstPlayer() {
        return this.player1;
    }

    @Override
    public Player getSecondPlayer() {
        return this.player2;
    }

    @Override
    public void setPlayers(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        initializeBoard();
    }

    @Override
    public boolean isFirstPlayerTurn()
    {
        return isFirstPlayerTurn;
    }

    @Override
    public boolean isGameFinished() {
        // If the board is full, the game is finished
        if (placedDiscsCount == BOARD_SIZE * BOARD_SIZE || ValidMoves().isEmpty()) {
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        board = new Disc[BOARD_SIZE][BOARD_SIZE];
        setPlayers(player1, player2);
        placedDiscsCount = 4;
        initializeBoard();
        moveHistory.clear();
        isFirstPlayerTurn = true;
    }

    @Override
    public void undoLastMove() {
        if (moveHistory.isEmpty())
        {
            System.out.println("Nothing to undo");
        }
        if (player1.isHuman() && player2.isHuman())
        {
            Move lastMove = moveHistory.pop();
            Position pos = lastMove.getPosition();
            for (Position position : flippedpositions) // flip the positions from the last move by dics type.
            {
                flipDisc(position);
            }
            board[pos.getRow()][pos.getCol()] = null; // Remove the disc
            placedDiscsCount--;
        }
        else System.out.println("Only works when 2 humans are playing.");

    }
    //This method flips the opponent discs
    private void flipOpponentDiscs(Position startPosition, Disc disc)
    {

        for (int[] direction : directions) // Goes through all directions in for loop
        {
            // Calls for a method that for each direction collects a list of position of discs on the board to be flipped
            List<Position> discsToFlip = getDiscsToFlipInDirection(startPosition, disc, direction);
            // Another for loop to go through all positions in the list and flip them using flipDisc method
            for (Position position : discsToFlip)
            {
                flipDisc(position);
                flippedpositions.add(position); // adds the positions to be flipped
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
        return isFirstPlayerTurn() ? player1 : player2;
    }

    //This method flips the discs on the board by instance of disc
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
    //Designated method to flip bomb discs
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
