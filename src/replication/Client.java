package replication;

public class Client {
    public static void main(String[] args) {
        //--Create references
        FileSystem refA = new LocalFileSystem("C:\\Users\\mercu\\replica\\sys\\refA",null);
        FileSystem refB = new LocalFileSystem("C:\\Users\\mercu\\replica\\sys\\refB",null);
        //--Create copies
        FileSystem copyA = new LocalFileSystem("C:\\Users\\mercu\\replica\\sys\\copyA",refA);
        FileSystem copyB = new LocalFileSystem("C:\\Users\\mercu\\replica\\sys\\copyB",refB);
        //--Synchronize
        Synchronizer.getInstance().synchronize(copyA,copyB);
    }
}
