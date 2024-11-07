public class BombDisc implements Disc
{
    private Player _owner;

    public BombDisc(Player owner)
    {
        this._owner = owner;
    }
    @Override
    public Player get_owner()
    {
        return _owner;
    }

    @Override
    public void set_owner(Player player)
    {
        this._owner = player;
    }

    @Override
    public String getType()
    {
        return "💣";
    }
}
