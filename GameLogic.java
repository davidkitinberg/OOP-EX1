import java.util.*;

/**
 * This class represents the rule of the game and its logic.
 */
public class GameLogic implements PlayableLogic {
    private static final int BOARD_SIZE = 8;
    private Disc[][] board;
    private Player player1;
    private Player player2;
    private boolean isFirstPlayerTurn;
    private int placedDiscsCount = 4; // Track the number of placed discs on the board
    private final int[][] directions = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1},   // Up, Down, Left, Right
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}  // Diagonals
    };
    private int firstPlayerCounter;
    private int secondPlayerCounter;
    private Stack<Disc[][]> boardHistory;
    private  Stack<Move> moveHistory;
    private Stack<List<Position>> flippedPositionsHistory = new Stack<>();


    public GameLogic() {
        this.board = new Disc[BOARD_SIZE][BOARD_SIZE];
        this.isFirstPlayerTurn = true; // Set to true for first player's turn
        moveHistory = new Stack<>();
        boardHistory = new Stack<>();
        flippedPositionsHistory = new Stack<>();

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
        player1.number_of_bombs = 3;
        player2.number_of_bombs = 3;
        player1.number_of_unflippedable = 2;
        player2.number_of_unflippedable = 2;
    }

    @Override
    public boolean locate_disc(Position a, Disc disc) {
    Player currentPlayer = getCurrentPlayer();

    // Ensure correct ownership for special discs
    if (disc instanceof BombDisc)
    {
        if (currentPlayer.getNumber_of_bombs() <= 0) // Check if there are still bomb discs to current player
        {
            System.out.println("No bombs left for " + (currentPlayer.isPlayerOne() ? "Player 1" : "Player 2"));
            return false;
        }
        currentPlayer.reduce_bomb();
    }
    else if (disc instanceof UnflippableDisc) // Check if there are still unflippable discs to current player
    {
        if (currentPlayer.getNumber_of_unflippedable() <= 0)
        {
            System.out.println("No unflippables left for " + (currentPlayer.isPlayerOne() ? "Player 1" : "Player 2"));
            return false;
        }
        currentPlayer.reduce_unflippedable();
    }

    // Check move validity
    if (!isValidMove(a, disc))
    {
        System.out.println("Move at " + a.getRow() + ", " + a.getCol() + " is invalid.");
        return false;
    }
    else // If move is valid we push it the stacks
    {
        moveHistory.push(new Move(a, disc));
        boardHistory.push(copyBoard(this.board));

    }

    // Place the disc on the board
    board[a.getRow()][a.getCol()] = disc;
    System.out.printf("Player %d placed a %s in (%d, %d)\n",
            currentPlayer.isPlayerOne() ? 1 : 2,
            disc.getType(),
            a.getRow(),
            a.getCol()
    );


    List<Position> flippedPositionsForCurrentMove = new ArrayList<>();
    // Flip opponent discs and collect their positions
    for (int[] direction : directions)
    {

        // Get a list of all positions to flip
        List<Position> discsToFlip = getDiscsToFlipInDirection(a, disc, direction);

        // Iterate between all positions
        for (Position position : discsToFlip)
        {
            Disc discToFlip = board[position.getRow()][position.getCol()];
            flippedPositionsForCurrentMove.add(position); // add flipped position for undo print

            // Handling bomb discs explosion
            if (discToFlip instanceof BombDisc)
            {
                flipSurroundingDiscs(new Position(position.getRow(), position.getCol()), disc.get_owner(), flippedPositionsForCurrentMove);
                discToFlip.set_owner(disc.get_owner()); // Flip the ownership of the BombDisc itself
            }
            else if(discToFlip instanceof SimpleDisc)
            {
                // Flip the current disc
                discToFlip.set_owner(disc.get_owner());
            }
        }

        // Push flipped positions to the history stack
        flippedPositionsHistory.push(removeDuplicatesByRowAndCol(flippedPositionsForCurrentMove));
    }
    // initiate print sequence BIP BOP
    List<Position> finalPositionsToPrint = removeDuplicatesByRowAndCol(flippedPositionsForCurrentMove);

    for (Position position : finalPositionsToPrint)
    {
        Disc discToPrint = board[position.getRow()][position.getCol()];
        // Printing current player flipping
        System.out.printf("Player %d flipped the %s in (%d, %d)\n",
                currentPlayer.isPlayerOne() ? 1 : 2,
                discToPrint.getType(),
                position.getRow(),
                position.getCol()
        );
    }
    System.out.println(); // Print space between moves
    // Switch turn
    isFirstPlayerTurn = !isFirstPlayerTurn;
    placedDiscsCount++;
    return true;
}

    /**
     * This function makes a deep copy of the board and takes care
     * of assigning owners to discs and discs types
     * @param board , the current board.
     */
    private Disc[][] copyBoard(Disc[][] board) {
        Disc[][] temp = new Disc[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] != null) {
                    // Use the appropriate subclass copy constructor
                    Disc original = board[i][j];
                    if (original instanceof SimpleDisc) {
                        temp[i][j] = new SimpleDisc(original.get_owner());
                    } else if (original instanceof BombDisc) {
                        temp[i][j] = new BombDisc(original.get_owner());
                    } else if (original instanceof UnflippableDisc) {
                        temp[i][j] = new UnflippableDisc(original.get_owner());
                    }
                } else {
                    temp[i][j] = null;
                }
            }
        }
        return temp;
    }


    /**
     * This function's purpose it to implement bomb explosion and case handling
     * of multiple explosions by recourse.
     * @param bombPosition , a position which contains a bomb disc
     * @param owner , the current player
     * @param flippedPositions , list of all discs that need to explode (preserved in recourse)
     *
     */
    private void flipSurroundingDiscs(Position bombPosition, Player owner, List<Position> flippedPositions) {
        List<Position> bombPositions = new ArrayList<>();

        for (int[] direction : directions)
        {
            int row = bombPosition.getRow() + direction[0];
            int col = bombPosition.getCol() + direction[1];

            // Check bounds
            if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE)
            {
                Position adjacentPosition = new Position(row, col);
                Disc adjacentDisc = board[row][col];

                if (adjacentDisc != null && !adjacentDisc.get_owner().equals(owner))
                {
                    // Flip the disc
                    adjacentDisc.set_owner(owner);
                    flippedPositions.add(adjacentPosition); // Track this flip

                    // If the adjacent disc is another BombDisc, trigger its explosion
                    if (adjacentDisc instanceof BombDisc)
                    {
                        bombPositions.add(adjacentPosition);
                    }
                }
            }
        }
        // Process affected BombDiscs recursively after the initial pass
        for (Position bombPos : bombPositions)
        {

            flipSurroundingDiscs(bombPos, owner, flippedPositions);
            //bombFlippedPositionsHistory.push(new ArrayList<>()); // Prepare for nested flips
        }
    }

    /**
     * This function's purpose it to validate specific move by reversi's game rules.
     * @param a , a position to be validated
     * @param disc , disc object which contains its type and owner
     *
     */
    public boolean isValidMove(Position a, Disc disc)
    {
        // Ensure disc isn't null to avoid unnecessary checks
        if (disc == null) {
            //System.out.println("Skipping position due to null disc at Position: " + a.getRow() + ", " + a.getCol());
            return false;
        }

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
        Disc currentDisc = new SimpleDisc(getCurrentPlayer()); // Current player's disc
        ArrayList<Position> uniqueFlippedPositions = new ArrayList<>(); // Track unique flipped positions

        for (int[] direction : directions)
        {
            // Get discs to flip in this direction
            List<Position> discsToFlip = getDiscsToFlipInDirection(a, currentDisc, direction);

            // Check for BombDiscs in the flip sequence and process them
            for (Position position : discsToFlip)
            {
                Disc discToFlip = board[position.getRow()][position.getCol()];
                if (discToFlip instanceof BombDisc)
                {
                    // Simulate bomb-triggered flips and add to the set
                    simulateBombFlips(position, currentDisc.get_owner(), uniqueFlippedPositions);
                }
                if(!uniqueFlippedPositions.contains(position))
                {
                    uniqueFlippedPositions.add(position);
                }
            }
        }

        return removeDuplicatesByRowAndCol(uniqueFlippedPositions).size(); // Return the total number of unique flipped discs
    }

    /**
     * This is a simple helper function that removes duplicated positions.
     * @param positions , a list of positions to compare dupes
     *
     */
    public List<Position> removeDuplicatesByRowAndCol(List<Position> positions)
    {
        Set<String> uniqueKeys = new HashSet<>(); // To track unique "row,col" keys
        List<Position> uniquePositions = new ArrayList<>();

        for (Position position : positions)
        {
            // Create a unique key based on row and column values
            String key = position.getRow() + "," + position.getCol();

            // Add the position to the result if the key is not already in the set
            if (uniqueKeys.add(key))
            {
                uniquePositions.add(position);
            }
        }

        return uniquePositions;
    }

    /**
     * This function's purpose it to simulate bomb explosion (and case handling
     * of multiple explosions by recourse) for countFlips method.
     * @param bombPosition , a position which contains a bomb disc
     * @param owner , the current player
     * @param flippedPositions , list of all discs that need to explode (preserved in recourse)
     *
     */
    private void simulateBombFlips(Position bombPosition, Player owner, ArrayList<Position> flippedPositions) {
        List<Position> bombTriggeredPositions = new ArrayList<>();

        for (int[] direction : directions)
        {
            int row = bombPosition.getRow() + direction[0];
            int col = bombPosition.getCol() + direction[1];

            // Check bounds
            if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
                Position adjacentPosition = new Position(row, col);
                Disc adjacentDisc = board[row][col];

                if (adjacentDisc != null && !adjacentDisc.get_owner().equals(owner)) {
                    // Add to flipped positions only if not already added
                    if (flippedPositions.add(adjacentPosition)) {
                        // If the adjacent disc is another BombDisc, queue it for further explosions
                        if (adjacentDisc instanceof BombDisc && !flippedPositions.contains(adjacentPosition)) {

                            bombTriggeredPositions.add(adjacentPosition);
                        }
                    }
                }
            }
        }

        // Recursively process all bomb-triggered positions
        for (Position triggeredPosition : bombTriggeredPositions) {
            simulateBombFlips(triggeredPosition, owner, flippedPositions);
        }

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
        boardHistory.clear();
        isFirstPlayerTurn = true;
        player1.number_of_bombs = 3;
        player2.number_of_bombs = 3;
        player1.number_of_unflippedable = 2;
        player2.number_of_unflippedable = 2;
    }

@Override
public void undoLastMove() {
    if (boardHistory.isEmpty() || moveHistory.isEmpty() || flippedPositionsHistory.isEmpty())
    {
        System.out.println("No previous move available to undo.");
        return;
    }

    System.out.println("Undoing last move:");

    // Pop the last move from the history
    Move lastMove = moveHistory.pop();
    Position pos = lastMove.getPosition();
    Disc disc = getDiscAtPosition(pos);

    // Returning special discs number to its owner
    if (disc instanceof UnflippableDisc)
    {
        disc.get_owner().number_of_unflippedable++;
    }
    if(disc instanceof BombDisc)
    {
        disc.get_owner().number_of_bombs++;
    }

    // Printing the removal of last placed disc
    System.out.printf("\tUndo: removing %s from (%d, %d)\n", disc.getType(), pos.getRow(), pos.getCol());

    // Revert flipped discs for printing purpose
    List<Position> lastFlippedPositions = flippedPositionsHistory.pop();
    for (Position flippedPos : lastFlippedPositions)
    {
        Disc flippedDisc = board[flippedPos.getRow()][flippedPos.getCol()];
        if (flippedDisc != null)
        {
            flippedDisc.set_owner(flippedDisc.get_owner().equals(player1) ? player2 : player1);
            System.out.printf("\tUndo: flipping back %s in (%d, %d)\n",
                    flippedDisc.getType(),
                    flippedPos.getRow(),
                    flippedPos.getCol()
            );
        }
    }
    System.out.println(); // Print space between moves

    // Reverting board state from stack
    Disc[][] lastBoard = boardHistory.pop();
    for(int i=0;i<BOARD_SIZE;i++)
    {
        for(int j=0;j<BOARD_SIZE;j++)
        {
            this.board[i][j] = lastBoard[i][j];

        }
    }
    // Revert game state
    isFirstPlayerTurn = !isFirstPlayerTurn;
    placedDiscsCount--;

}

    /**
     * This function's purpose is to implement discs flipping logic in one specific direction without handling bombs explosion.
     * @param start , a position to start checking nearly positions to flip nearly discs
     * @param disc , disc object which contains its type and owner
     * @param direction , an array of all directions that are needed to be checked (only one direction is set at a call)
     *
     */
    //This method returns list of all the positions of discs on the board that needs to be flipped.
    private List<Position> getDiscsToFlipInDirection(Position start, Disc disc, int[] direction) {
        // If the initial disc is null, we can't proceed in this direction
        if (disc == null) {
            System.out.println("Starting disc is null, skipping this direction.");
            return new ArrayList<>();
        }

        List<Position> discsToFlip = new ArrayList<>();
        int row = start.getRow() + direction[0];
        int col = start.getCol() + direction[1];

        // While checks for board boundaries
        while (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE)
        {

            Disc currentDisc = board[row][col];

            // If the initial disc is null, we can't proceed in this direction
            if (currentDisc == null)
            {
                return new ArrayList<>(); // Return empty list if there's a null
            }

            // Check if current disc is an opponent's disc
            if (!currentDisc.get_owner().equals(disc.get_owner()))
            {
                // We found a matching disc after opponent discs, so the move is valid in this direction
                discsToFlip.add(new Position(row, col));
            }
            else
            {
                if (!discsToFlip.isEmpty())
                {
                    return discsToFlip;
                }
                else
                {
                    return new ArrayList<>();
                }
            }
            // Continuing in the same direction until we reach a friendly disc or boundary
            row += direction[0];
            col += direction[1];
        }
        // No valid flipping sequence found, return an empty list
        return new ArrayList<>();
    }
    /**
     * This function's purpose is to return current player.
     */
    private Player getCurrentPlayer() {
        return isFirstPlayerTurn() ? player1 : player2;
    }
}
