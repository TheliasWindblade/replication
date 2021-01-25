package replication;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;

public class Synchronizer {

    private static Synchronizer instance=new Synchronizer();
    private SortedSet<Action> timeline;
    private boolean syncing=false;

    private Synchronizer(){
        timeline=new TreeSet<>();
    }

    public static Synchronizer getInstance() {
        return instance;
    }

    public void synchronize(FileSystem fA, FileSystem fB){
        System.out.println(timeline);
        Set<String> dA = computeDirty(fA,"");
        Set<String> dB = computeDirty(fB,"");
        System.out.println("Dirty paths (A) : "+dA);
        System.out.println("Dirty paths (B) : "+dB);
        reconcile(fA,dA,fB,dB,"");
        Scanner sc = new Scanner(System.in);
        sc.nextLine();
        fA.update();
        fB.update();
        timeline.clear();
}

    public void addAction(Action action){
        if(syncing) return;
        timeline.add(action);
    }

    public void toggleSync() {
        syncing=!syncing;
    }

    public void reconcile(FileSystem fA, Set<String> dPathsA, FileSystem fB, Set<String> dPathsB, String currentPath){
        if(!dPathsA.contains(currentPath) && !dPathsB.contains(currentPath)){
            System.out.println(currentPath+" : both not dirty");
        }
        else{
            if(fA.isDirectory(currentPath) && fB.isDirectory(currentPath)){
                Set<String> children = new HashSet<>();
                children.addAll(fA.getChildren(currentPath));
                children.addAll(fB.getChildren(currentPath));
                for(String s : children){
                    reconcile(fA,dPathsA,fB,dPathsB,currentPath+"\\"+s);
                }
            }
            else {
                if(!dPathsA.contains(currentPath)){
                    fA.replace(currentPath,fB,currentPath);
                    System.out.println(currentPath+" : A not dirty, replaced by B");
                }
                else {
                    if (!dPathsB.contains(currentPath)) {
                        fB.replace(currentPath, fA, currentPath);
                        System.out.println(currentPath+" : B not dirty, replaced by A");
                    }
                    else {
                        System.out.println(currentPath+" : both dirty ; conflict cannot be resolved");
                    }
                }
            }
        }
    }

    public Set<String> computeDirty(FileSystem fs, String currentPath){
        Set<String> dirtyPaths = new HashSet<>();
        File file = fs.getFileAtPath(currentPath);
        File fileSync = fs.getLastUpdate().getFileAtPath(currentPath);

        if(fs.isDirectory(currentPath)){
            boolean dirty = false;
            //Find children nodes
            Set<String> children = new HashSet<>();
            List<String> fsC = fs.getChildren(currentPath);
            if(fsC!=null) children.addAll(fsC);
            fsC = fs.getLastUpdate().getChildren(currentPath);
            if(fsC!=null) children.addAll(fsC);
            //Walk said nodes for dirty paths
            for(String child : children){
                Set<String> cDirty = computeDirty(fs,currentPath+"\\"+child);
                dirtyPaths.addAll(cDirty);
                dirty=dirty||cDirty.size()>0;
            }
            if(dirty) dirtyPaths.add(currentPath);
        }
        else{
            //Check file freshness
            if(fileSync.exists() && file.exists()) {
                try {
                    if (getFileChecksum(file).compareTo(getFileChecksum(fileSync)) == 0) {
                        return dirtyPaths;
                    }
                } catch (Exception e) {
                    System.err.println("Something messed up during checksum creation : " + e.getMessage());
                    System.exit(1);
                }
            }
            //File doesn't exist in the reference / copy or checksum doesn't check out -> is dirty
            dirtyPaths.add(currentPath);
        }

        return dirtyPaths;
    }

    private String getFileChecksum(File file) throws Exception {
        InputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        int read=0;
        while((read=fis.read(buffer)) != -1){
            digest.update(buffer,0,read);
        }
        byte[] db = digest.digest();
        StringBuilder checksum= new StringBuilder();
        for(int i=0;i<digest.digest().length;i++){
            checksum.append(Integer.toString((db[i] & 0xff) + 0x100, 16).substring(1));
        }
        fis.close();
        return checksum.toString();
    }
}
