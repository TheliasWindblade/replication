package replication;

public class TestClient {
    private static int GLOBAL_CLOCK=0;

    public static int getNextStep(){
        return GLOBAL_CLOCK++;
    }

    public static void main(String[] args) {
        //Initialize filesystems
        //--Create references
        FileSystem refA = new LocalFileSystem("C:\\Users\\mercu\\replica\\sys\\refA",null);
        FileSystem refB = new LocalFileSystem("C:\\Users\\mercu\\replica\\sys\\refB",null);
        //--Create copies
        FileSystem copyA = new LocalFileSystem("C:\\Users\\mercu\\replica\\sys\\copyA",refA);
        FileSystem copyB = new LocalFileSystem("C:\\Users\\mercu\\replica\\sys\\copyB",refB);

        //Modify copies
        //--Initial system
        copyA.createFile("\\f.txt");
        copyB.createFile("\\g.txt");
        copyA.createFile("\\a.txt");
        copyB.createFile("\\b.txt");
        Synchronizer.getInstance().synchronize(copyA,copyB);
        //--File modification > split
        copyA.appendToFile("\\f.txt","banana kong");
        copyB.appendToFile("\\g.txt","hello its me jeremy");
        Synchronizer.getInstance().synchronize(copyA,copyB);
        //--File modification > conflict
        copyA.appendToFile("\\g.txt","1 bajillion degrees");
        copyB.appendToFile("\\g.txt","9 quadrillion degrees");
        Synchronizer.getInstance().synchronize(copyA,copyB);
        //--Create and remove
        copyA.createFile("\\h.txt");
        copyA.appendToFile("\\h.txt","this is a proof of concept");
        copyB.remove("\\f.txt");
        Synchronizer.getInstance().synchronize(copyA,copyB);
        //--Rename
        copyA.rename("\\h.txt","\\e.txt");
        Synchronizer.getInstance().synchronize(copyA,copyB);
        //--Modify then remove > divergence
        copyA.appendToFile("\\a.txt","stop right there criminal scum");
        copyB.remove("\\a.txt");
        copyB.appendToFile("\\b.txt","but i took an arrow in the knee");
        Synchronizer.getInstance().synchronize(copyA,copyB);
        //--Deep directory copy
        copyA.createDir("\\dir\\");
        copyA.createFile("\\dir\\k.txt");
        copyA.createDir("\\dir\\dir\\");
        copyA.createFile("\\dir\\dir\\j.txt");
        Synchronizer.getInstance().synchronize(copyA,copyB);
    }
}
