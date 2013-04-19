package net.jlekstrand.wheatley;

public interface Region
{
    public interface Factory
    {
        public Region createRegion();
    }

    public abstract boolean contains(int x, int y);
	public abstract void add(int x, int y, int width, int height);
	public abstract void subtract(int x, int y, int width, int height);
	public abstract void add(Region r);
    public abstract Region clone();
}

