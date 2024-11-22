import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GameLogic implements PlayableLogic {
    private static final int BOARD_SIZE = 8;
    private Disc[][] board;
    private Player player1;
    private Player player2;
    private boolean isFirstPlayerTurn;
    private ArrayList<Position> flippedpositions;
    private  Stack<Move> moveHistory;
    private int placedDiscsCount = 4; // Track the number of placed discs on the board
    private final int[][] directions = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1},   // Up, Down, Left, Right
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}  // Diagonals
    };
    private int firstPlayerCounter;
    private int secondPlayerCounter;
    private Stack<List<Position>> flippedPositionsHistory = new Stack<>();



    public GameLogic() {
        this.board = new Disc[BOARD_SIZE][BOARD_SIZE];
        this.isFirstPlayerTurn = true; // Set to true for first player's turn
        moveHistory = new Stack<>();
    }

    private void initializeBoard() {
        if (player1 == null || player2 == null) {
            throw new IllegalStateException("Players must be set before initializing the board.");
        }
        // Place the starting four discs in the center of the board
        int mid = BOARD_SIZE / 2;
        board[mid - 1][mid - 1] = new SimpleDisc(player1); // Black
        board[mid - 1][mid] = new SimpleDisc(player2); // White
        board[mid][mid - 1] = new SimpleDisc(player2); // White
        board[mid][mid] = new SimpleDisc(player1); // Black
        placedDiscsCount = 4;
        firstPlayerCounter = 0;
        secondPlayerCounter = 0;
    }

//    @Override
//    public boolean locate_disc(Position a, Disc disc) {
//        // Initialize flippedpositions if it is null
//        if (flippedpositions == null) {
//            flippedpositions = new ArrayList<>();
//        } else {
//            // Clear flippedpositions to ensure it only contains positions for the current move
//            flippedpositions.clear();
//        }
//        if(!disc.get_owner().equals(getCurrentPlayer())) { disc.set_owner(getCurrentPlayer()); }
//        if (!isValidMove(a, disc)) {
//            return false;
//        }
//
//        // Place the disc on the board
//        board[a.getRow()][a.getCol()] = disc;
//        flipOpponentDiscs(a, disc);
//
//        // Record move for undo functionality
//        moveHistory.push(new Move(a, disc));
//
//        // Switch turn to the other player
//        isFirstPlayerTurn = !isFirstPlayerTurn;
//        placedDiscsCount++;
//        System.out.println(placedDiscsCount);
//        return true;
//    }
@Override
public boolean locate_disc(Position a, Disc disc) {
    if (flippedpositions == null) {
        flippedpositions = new ArrayList<>();
    } else {
        flippedpositions.clear();
    }

    Player currentPlayer = getCurrentPlayer();

    // Bomb placement logic
    if (disc instanceof BombDisc) {
        if (currentPlayer.getNumber_of_bombs() <= 0) {
            System.out.println("No bombs left for " + (currentPlayer.isPlayerOne() ? "Player 1" : "Player 2"));
            return false;
        }
        currentPlayer.reduce_bomb();
        disc.set_owner(currentPlayer); // Set ownership of the bomb
    } else {
        disc.set_owner(currentPlayer); // Normal disc logic
    }

    if (!isValidMove(a, disc)) {
        return false;
    }

    // Place the disc on the board
    board[a.getRow()][a.getCol()] = disc;

    // Trigger explosion for bombs
    if (disc instanceof BombDisc) {
        explode(a, currentPlayer);
    }

    // Handle flipping of opponent discs
    for (int[] direction : directions) {
        List<Position> discsToFlip = getDiscsToFlipInDirection(a, disc, direction);
        for (Position position : discsToFlip) {
            Disc discToFlip = board[position.getRow()][position.getCol()];
            if (!(discToFlip instanceof UnflippableDisc)) {
                discToFlip.set_owner(disc.get_owner());
            }
            flippedpositions.add(position);
        }
    }

    flippedPositionsHistory.push(new ArrayList<>(flippedpositions));
    moveHistory.push(new Move(a, disc));
    isFirstPlayerTurn = !isFirstPlayerTurn;
    placedDiscsCount++;
    return true;
}


    public boolean isValidMove(Position a, Disc disc)
    {
        // Ensure disc isn't null to avoid unnecessary checks
        if (disc == null) {
            //System.out.println("Skipping position due to null disc at Position: " + a.getRow() + ", " + a.getCol());
            return false;
        }

        //System.out.println("Checking move at Position: " + a.getRow() + ", " + a.getCol());

        // Check if the cell is empty
        if (board[a.getRow()][a.getCol()] != null) {
            //System.out.println("Position already occupied");
            return false;
        }

        List<Position> collectionOfDiscsToFlip = new ArrayList<>();

        // Check each direction
        for (int[] direction : directions) {
            List<Position> discsToFlip = getDiscsToFlipInDirection(a, disc, direction);

            // Only add to flip list if there are discs to flip in this direction
            if (!discsToFlip.isEmpty()) {
                collectionOfDiscsToFlip.addAll(discsToFlip);
            }
        }

        // Check if we have any discs to flip in total
        if (collectionOfDiscsToFlip.isEmpty()) {
            //System.out.println("No discs to flip, move invalid");
            return false;
        }

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
        Disc currentDisc = new SimpleDisc(getCurrentPlayer()); // Use the current player's disc consistently

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Position potentialPosition = new Position(row, col);
                if (isValidMove(potentialPosition, currentDisc)) {
                    validMoves.add(potentialPosition);
                }
            }
        }
        return validMoves;
    }


    @Override
    public int countFlips(Position a) {
        Disc disc = new SimpleDisc(getCurrentPlayer());// Current player's disc
        int totalFlips = 0;

        // Check each direction and count flips
        for (int[] direction : directions) {
            List<Position> discsToFlipInDirection = getDiscsToFlipInDirection(a, disc, direction);
            totalFlips += discsToFlipInDirection.size();
        }

        return totalFlips;
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
        if (placedDiscsCount == BOARD_SIZE * BOARD_SIZE || ValidMoves().isEmpty())
        {
            for (int row = 0; row < BOARD_SIZE; row++)
            {
                for (int col = 0; col < BOARD_SIZE; col++)
                {
                    Position potentialPosition = new Position(row, col);
                    Disc disc = getDiscAtPosition(potentialPosition);
                    if(getDiscAtPosition(potentialPosition) == null)
                    {
                        break;
                    }
                    else if(disc.get_owner().equals(getCurrentPlayer()))
                        firstPlayerCounter++;

                    else {secondPlayerCounter++;}
                }
            }
            if(firstPlayerCounter > secondPlayerCounter)
            {
                getFirstPlayer().addWin();
                return true;
            }
            else
            {
                getSecondPlayer().addWin();
                return true;
            }
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
    if (moveHistory.isEmpty() || flippedPositionsHistory.isEmpty()) {
        System.out.println("Nothing to undo, already at initial board state");
        return;
    }

    // Pop the last move from the history
    Move lastMove = moveHistory.pop();
    Position pos = lastMove.getPosition();

    // Clear the disc at the last move's position
    board[pos.getRow()][pos.getCol()] = null;

    // Revert flipped positions for this move
    List<Position> lastFlippedPositions = flippedPositionsHistory.pop();
    for (Position flippedPos : lastFlippedPositions) {
        Disc flippedDisc = board[flippedPos.getRow()][flippedPos.getCol()];
        if (flippedDisc != null) {
            flippedDisc.set_owner(
                    flippedDisc.get_owner().equals(player1) ? player2 : player1
            );
        }
    }

    // Revert game state
    isFirstPlayerTurn = !isFirstPlayerTurn;
    placedDiscsCount--;

    // Reset to initial state if no moves remain
    if (moveHistory.isEmpty()) {
        System.out.println("Reached the initial board state");
        initializeBoard();
        isFirstPlayerTurn = true;
        placedDiscsCount = 4; // Adjust according to initial setup
    }
}

    //This method flips the opponent discs
//    private void flipOpponentDiscs(Position startPosition, Disc disc)
//    {
//
//        for (int[] direction : directions) // Goes through all directions in for loop
//        {
//            // Calls for a method that for each direction collects a list of position of discs on the board to be flipped
//            List<Position> discsToFlip = getDiscsToFlipInDirection(startPosition, disc, direction);
//            // Another for loop to go through all positions in the list and flip them using flipDisc method
//            for (Position position : discsToFlip)
//            {
//                flipDisc(position);
//                flippedpositions.add(position); // adds the positions to be flipped
//
//            }
//        }
//    }

    private List<Position> getDiscsToFlipInDirection(Position start, Disc disc, int[] direction) {
        if (disc == null) {
            System.out.println("Starting disc is null, skipping this direction.");
            return new ArrayList<>();
        }

        List<Position> discsToFlip = new ArrayList<>();
        int row = start.getRow() + direction[0];
        int col = start.getCol() + direction[1];

        while (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
            Disc currentDisc = board[row][col];

            if (currentDisc == null) {
                // Empty spot ends the direction
                return new ArrayList<>();
            }

            // Only process simple discs and bombs
            if (currentDisc instanceof SimpleDisc || currentDisc instanceof BombDisc) {
                if (!currentDisc.get_owner().equals(disc.get_owner())) {
                    discsToFlip.add(new Position(row, col));

                    // Trigger bomb explosion if it's an opponent's bomb
                    if (currentDisc instanceof BombDisc) {
                        explode(new Position(row, col), disc.get_owner());
                    }
                } else {
                    // Found a disc owned by the same player, end search in this direction
                    return discsToFlip;
                }
            }

            row += direction[0];
            col += direction[1];
        }

        return discsToFlip;
    }



    private Player getCurrentPlayer() {
        return isFirstPlayerTurn() ? player1 : player2;
    }

    //This method flips the discs on the board by instance of disc
//    private void flipDisc(Position position)
//    {
//        Disc disc = board[position.row()][position.col()]; // New Disc initialization
//        if(disc instanceof SimpleDisc)
//        {
//            disc.set_owner(getCurrentPlayer()); // If simple disc --> flip
//        }
//        else if(disc instanceof BombDisc)
//        {
//            //if(disc.get_owner() != getCurrentPlayer()) {disc.set_owner(getCurrentPlayer());}
//            explode(position); // If bomb disc --> bomb flip
//            disc.set_owner(getCurrentPlayer());
//        }
//        // Unflippable disc should not be flipped
//    }
//    private void explode(Position bombPosition) {
//        // Keep track of bomb positions that have already exploded during this chain reaction
//        List<Position> explodedBombs = new ArrayList<>();
//        explodedBombs.add(bombPosition);
//        for (int[] direction : directions)
//        {
//            getBombDiscsToFlipInDirection(bombPosition,  direction);
//        }
//        //getBombDiscsToFlipInDirection(bombPosition, explodedBombs);
//        //explodedBombs.clear();
//    }

    private List<Position> getBombDiscsToFlipInDirection(Disc disc ,Position bombPosition , int[] direction, List<Position> discsToFlipForBombs) {
            int row = bombPosition.getRow() + direction[0];
            int col = bombPosition.getCol() + direction[1];

            // Check bounds
            if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE)
            {
                Disc currentDisc = board[row][col];

                // If there's an opponent disc, flip it
                if (currentDisc != null && !currentDisc.get_owner().equals(disc.get_owner()))
                {
                    discsToFlipForBombs.add(new Position(row, col));

                    // If the flipped disc is a bomb, trigger its explosion (if not already exploded)
                    if (currentDisc instanceof BombDisc)
                    {
                        getBombDiscsToFlipInDirection(disc,new Position(row, col),direction, discsToFlipForBombs);
                    }
                }
            }
        return discsToFlipForBombs;
    }


    private void explode(Position bombPosition, Player bombOwner) {
        for (int[] direction : directions) {
            int row = bombPosition.getRow() + direction[0];
            int col = bombPosition.getCol() + direction[1];

            // Check bounds
            if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
                Disc nearbyDisc = board[row][col];

                if (nearbyDisc instanceof SimpleDisc || nearbyDisc instanceof BombDisc) {
                    // Change ownership if owned by an opponent
                    if (!nearbyDisc.get_owner().equals(bombOwner)) {
                        nearbyDisc.set_owner(bombOwner);

                        // Trigger chain reaction if it's an opponent's bomb
                        if (nearbyDisc instanceof BombDisc) {
                            explode(new Position(row, col), bombOwner);
                        }
                    }
                }
            }
        }
    }

}
