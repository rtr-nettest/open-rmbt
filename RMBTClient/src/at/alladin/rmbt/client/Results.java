package at.alladin.rmbt.client;

public class Results
{
    public final long[] bytes;
    public final long[] nsec;
    
    public Results(int numResults)
    {
        bytes = new long[numResults];
        nsec = new long[numResults];
    }
    
    public Results(long[] bytes, long[] nsec)
    {
        if (bytes.length != nsec.length)
            throw new IllegalArgumentException("length of bytes and nsec not identical");
        this.bytes = bytes;
        this.nsec = nsec;
    }
}
