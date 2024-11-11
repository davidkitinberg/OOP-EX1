public class SimpleDisc implements Disc
{
    private Player _owner;
    private Disc[][] _board;

    public SimpleDisc(Player owner)
    {
        this._owner = owner;
    }
    @Override
    public Player get_owner()
    {
        return _owner;
    }

    @Override
    public void set_owner(Player owner)
    {
        this._owner = owner;
    }

    @Override
    public String getType() {
        return "⬤";
    }
//    public Disc[][] get_board()
//    {
//        return _board;
//    }
}
