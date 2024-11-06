public class SimpleDisc implements Disc
{
    private Player _owner;

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
}
