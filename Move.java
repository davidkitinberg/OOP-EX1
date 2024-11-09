import java.util.ArrayList;
import java.util.List;

public class Move
{
    private final Position position;           // The position where the move was made
    private final Disc placedDisc;             // The disc that was placed
    //private final List<Position> flippedPositions; // List of positions that were flipped during the move

//    public Move(Position position, Disc placedDisc, List<Position> flippedPositions)
////    {
////        this.position = position;
////        this.placedDisc = placedDisc;
////        this.flippedPositions = flippedPositions;
////    }
public Move(Position position, Disc placedDisc)
{
    this.position = position;
    this.placedDisc = placedDisc;
}


    public Position getPosition()
    {
        return position;
    }
    public Disc getPlacedDisc() {
        return placedDisc;
    }

}
