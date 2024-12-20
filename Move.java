import java.util.ArrayList;
import java.util.List;

public class Move
{
    private final Position position;           // The position where the move was made
    private final Disc placedDisc;             // The disc that was placed
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

    public Position position() {
        return position;
    }

    public Disc disc() {
    return placedDisc;
    }
}
