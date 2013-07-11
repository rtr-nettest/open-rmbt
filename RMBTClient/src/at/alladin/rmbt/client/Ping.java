package at.alladin.rmbt.client;

public class Ping
{
    public Ping(long client, long server)
    {
        this.client = client;
        this.server = server;
    }
    
    final public long client;
    final public long server;
}
