import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

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
    private Stack<Disc[][]> boardHistory = new Stack<>();






    public GameLogic() {
        this.board = new Disc[BOARD_SIZE][BOARD_SIZE];
        this.isFirstPlayerTurn = true; // Set to true for first player's turn
        //moveHistory = new Stack<>();
        boardHistory = new Stack<>();
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

@Override
public boolean locate_disc(Position a, Disc disc) {
    Player currentPlayer = getCurrentPlayer();
    boardHistory.push(copyBoard(this.board));

    // Ensure correct ownership for special discs
    if (disc instanceof BombDisc)
    {
        if (currentPlayer.getNumber_of_bombs() <= 0)
        {
            System.out.println("No bombs left for " + (currentPlayer.isPlayerOne() ? "Player 1" : "Player 2"));
            return false;
        }
        currentPlayer.reduce_bomb();
    }
    else if (disc instanceof UnflippableDisc)
    {
        if (currentPlayer.getNumber_of_unflippedable() <= 0)
        {
            System.out.println("No unflippables left for " + (currentPlayer.isPlayerOne() ? "Player 1" : "Player 2"));
            return false;
        }
        currentPlayer.reduce_unflippedable();
    }

    // Debug: Check move validity
    if (!isValidMove(a, disc))
    {
        System.out.println("Move at " + a.getRow() + ", " + a.getCol() + " is invalid.");
        return false;
    }
    System.out.println("Move at " + a.getRow() + ", " + a.getCol() + " is valid.");

    // Place the disc on the board
    board[a.getRow()][a.getCol()] = disc;
    System.out.println("Placed disc of type " + disc.getType() + " at position: " + a.getRow() + ", " + a.getCol());

    // Flip opponent discs and collect their positions
    for (int[] direction : directions)
    {
        System.out.println("Processing direction: " + direction[0] + ", " + direction[1]);
        // Get a list of all positions to flip
        List<Position> discsToFlip = getDiscsToFlipInDirection(a, disc, direction);

        for (Position position : discsToFlip)
        {
            Disc discToFlip = board[position.getRow()][position.getCol()];

            System.out.println("Flipping disc at position: " + position.getRow() + ", " + position.getCol());
            // Handling bomb discs explosion
            if (discToFlip instanceof BombDisc)
            {
                System.out.println("BombDisc flipped at position: " + position.getRow() + ", " + position.getCol());

                flipSurroundingDiscs(new Position(position.getRow(), position.getCol()), disc.get_owner());

                // Flip the ownership of the BombDisc itself
                discToFlip.set_owner(disc.get_owner());
                System.out.println("BombDisc at " + position.getRow() + ", " + position.getCol() + " flipped to owner " + disc.get_owner());
            }
            else if(discToFlip instanceof SimpleDisc)
            {
                // Flip the current disc
                discToFlip.set_owner(disc.get_owner());
            }
        }
    }


    // Save flipped positions for undo functionality
    //flippedPositionsHistory.push(new ArrayList<>(flippedpositions));
    //moveHistory.push(new Move(a, disc));

    // Debug: Show which player flipped the disc
    String player = isFirstPlayerTurn ? "Player1" : "Player2";
    System.out.println(player + " flipped the " + disc.getType());

    // Switch turn
    isFirstPlayerTurn = !isFirstPlayerTurn;
    placedDiscsCount++;
    return true;
}

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




    private void flipSurroundingDiscs(Position bombPosition, Player owner) {
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

                    // If the adjacent disc is another BombDisc, trigger its explosion
                    if (adjacentDisc instanceof BombDisc)
                    {
                        bombPositions.add(adjacentPosition);
                    }
                }
            }
        }
        // Add flipped positions due to this bomb to the global flipped positions list
        //bombFlippedPositionsHistory.peek().addAll(bombPositions);

        // Process affected BombDiscs recursively after the initial pass
        for (Position bombPos : bombPositions)
        {

            flipSurroundingDiscs(bombPos, owner);
            //bombFlippedPositionsHistory.push(new ArrayList<>()); // Prepare for nested flips
        }
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
        //moveHistory.clear();
        boardHistory.clear();
        isFirstPlayerTurn = true;
    }

@Override
public void undoLastMove() {
    if (boardHistory.isEmpty()) {
        System.out.println("Nothing to undo, already at initial board state");
        return;
    }

    // Pop the last move from the history
    //Move lastMove = moveHistory.pop();
    Disc[][] lastBoard = boardHistory.pop();
    for(int i=0;i<BOARD_SIZE;i++)
    {
        for(int j=0;j<BOARD_SIZE;j++)
        {
            this.board[i][j] = lastBoard[i][j];

        }
    }
    //Position pos = lastMove.getPosition();
//    Disc theLastPlacedDisc = board[pos.getRow()][pos.getCol()];
//    if (theLastPlacedDisc instanceof BombDisc)
//    {
//        theLastPlacedDisc.get_owner().number_of_bombs++;
//    }
//    if (theLastPlacedDisc instanceof UnflippableDisc)
//    {
//        theLastPlacedDisc.get_owner().number_of_unflippedable++;
//    }
    // Clear the disc at the last move's position
    //board[pos.getRow()][pos.getCol()] = null;

//    // Revert flipped positions for this move
//    List<Position> lastFlippedPositions = flippedPositionsHistory.pop();
//    for (Position flippedPos : lastFlippedPositions)
//    {
//        Disc flippedDisc = board[flippedPos.getRow()][flippedPos.getCol()];
//        if (flippedDisc != null)
//        {
//            flippedDisc.set_owner(flippedDisc.get_owner().equals(player1) ? player2 : player1);
//
//        }
//    }
//    while (!bombFlippedPositionsHistory.isEmpty())
//    {
//        // Revert bomb-related flipped positions
//        List<Position> bombFlippedPositions = bombFlippedPositionsHistory.pop();
//        for (Position flippedPos : bombFlippedPositions)
//        {
//            Disc flippedBombDisc = board[flippedPos.getRow()][flippedPos.getCol()];
//            if (flippedBombDisc != null)
//            {
//                flippedBombDisc.set_owner(flippedBombDisc.get_owner().equals(player1) ? player2 : player1);
//
//            }
//            for (int[] direction : directions) // Goes through all directions in for loop
//            {
//
//                int row = flippedPos.getRow() + direction[0];
//                int col = flippedPos.getCol() + direction[1];
//                // Check bounds
//                if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE)
//                {
//                    Position adjacentPosition = new Position(row, col);
//                    if(lastFlippedPositions.contains(adjacentPosition))
//                    {
//                        System.out.println("it does contain already");
//                        break;
//                    }
//                    Disc adjacentDisc = board[row][col];
//
//                    if (adjacentDisc != null && !adjacentDisc.get_owner().equals(flippedBombDisc.get_owner()) && !bombFlippedPositions.contains(adjacentPosition))
//                    {
//                        // Flip the disc
//                        adjacentDisc.set_owner(flippedBombDisc.get_owner().equals(player1) ? player2 : player1);
//                        flippedpositions.add(adjacentPosition);
//
//
//                    }
//                }
//            }
//        }
//    }

    // Revert game state
    isFirstPlayerTurn = !isFirstPlayerTurn;
    placedDiscsCount--;

    // Reset to initial state if no moves remain
//    if (moveHistory.isEmpty()) {
//        System.out.println("Reached the initial board state");
//        initializeBoard();
//        isFirstPlayerTurn = true;
//        placedDiscsCount = 4; // Adjust according to initial setup
//    }
}

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

        while (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE)
        {

            Disc currentDisc = board[row][col];

            // Check for null to prevent NullPointerException
            if (currentDisc == null)
            {
                //System.out.println("Hit an empty spot at: " + row + ", " + col + ", stopping search in this direction");
                return new ArrayList<>(); // Return empty list if there's a null
            }

            // Check if current disc is an opponent's disc
            if (!currentDisc.get_owner().equals(disc.get_owner()))
            {
                discsToFlip.add(new Position(row, col));
                //System.out.println("Adding disc to flip at: " + row + ", " + col);
            } else
            {
                // We found a matching disc after opponent discs, so the move is valid in this direction
                if (!discsToFlip.isEmpty())
                {
                    //System.out.println("Found a sequence to flip ending at " + row + ", " + col);
                    return discsToFlip;
                } else
                {
                    //System.out.println("Found matching color disc at " + row + ", " + col + " but no discs to flip before it.");
                    return new ArrayList<>();
                }
            }

            row += direction[0];
            col += direction[1];
        }

        // No valid flipping sequence found, return an empty list
        return new ArrayList<>();
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


    //Designated method to flip bomb discs
    private void explode1(Position bombPosition)
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
