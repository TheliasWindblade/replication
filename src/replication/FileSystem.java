package replication;

import java.io.File;
import java.util.List;

public interface FileSystem {
    //Navigation
    public String getRoot();
    public String getParent(String path);
    public List<String> getChildren(String path);
    public boolean isDirectory(String path);

    //File Manipulation
    public void createDir(String path);
    public void createFile(String path);
    public void remove(String path);
    public void appendToFile(String path, String text);
    public File getFileAtPath(String path);
    public void rename(String source, String target);
    public void copy(String source, String target);

    //Synchronizer
    public FileSystem getLastUpdate();
    public void update();
    public void replace(String targetPath, FileSystem source, String sourcePath);
}
