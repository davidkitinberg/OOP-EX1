public class Position
{
    private int row;
    private int col;

    public Position(int x, int y)
    {
        this.row = x;
        this.col = y;
    }
    public int row()
    {
        return row;
    }
    public int col()
    {
        return col;
    }
    public int getCol()
    {
        return col;
    }

    public int getRow() {
        return row;
    }
    public Position getPosition(Position pos)
    {
        return new Position(pos.row(), pos.col());
    }
}
