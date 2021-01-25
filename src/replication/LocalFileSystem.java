package replication;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class LocalFileSystem implements FileSystem {

    private FileSystem lastUpdate;
    private String root;

    public LocalFileSystem(String root){
        this(root,null);
    }

    public LocalFileSystem(String root, FileSystem lastUpdate){
        this.root=root;
        this.lastUpdate=lastUpdate;
    }

    @Override
    public String getRoot() {
        return root;
    }

    @Override
    public String getParent(String path) {
        File file = getFileAtPath(path);
        return file.getParent();
    }

    @Override
    public List<String> getChildren(String path) {
        File file = getFileAtPath(path);
        if(!file.isDirectory()) return null;
        return new LinkedList<>(Arrays.asList(Objects.requireNonNull(file.list())));
    }

    @Override
    public void createDir(String path) {
        try {
            Files.createDirectory(Paths.get(root + path));
            Synchronizer.getInstance().addAction(new Action(Action.ActionType.CREATE_DIR,new Object[]{path}));
        } catch (IOException e){
            System.err.println("Failed to create directory at "+root+path+" : ");
            e.printStackTrace();
        }
    }

    @Override
    public void createFile(String path) {
        try {
            Files.createFile(Paths.get(root + path));
            Synchronizer.getInstance().addAction(new Action(Action.ActionType.CREATE_FILE,new Object[]{path}));
        } catch (IOException e){
            System.err.println("Failed to create file at "+root+path+" : "+e.getMessage());
        }
    }

    @Override
    public boolean isDirectory(String path) {
        File file = getFileAtPath(path);
        return file.isDirectory();
    }

    @Override
    public void remove(String path) {
        File file = getFileAtPath(path);
        if(!file.exists()) return;
        Synchronizer.getInstance().addAction(new Action(Action.ActionType.DELETE_FILE,new Object[]{path}));
        if(!file.isDirectory()) {file.delete(); return;}
        try {
            for(Path p : Files.newDirectoryStream(Path.of(getRoot()+path))){
                File f = new File(String.valueOf(p));
                f.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public File getFileAtPath(String path) {
        return new File(getRoot()+path);
    }

    @Override
    public void appendToFile(String path, String text) {
        File file = getFileAtPath(path);
        if(!file.exists()) createFile(path);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.append(text);
            bw.close();
            Synchronizer.getInstance().addAction(new Action(Action.ActionType.WRITE_FILE,new Object[]{path,text}));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public FileSystem getLastUpdate() {
        return lastUpdate;
    }

    @Override
    public void update() {
        lastUpdate.replace("",this,"");
    }

    @Override
    public void rename(String source, String target) {
        copy(source, target);
        Synchronizer.getInstance().toggleSync();
        remove(source);
        Synchronizer.getInstance().toggleSync();
    }

    @Override
    public void copy(String source, String target) {
        String pathSource=getRoot()+source;
        File s = getFileAtPath(source);
        if(!s.exists()) return;
        Synchronizer.getInstance().addAction(new Action(Action.ActionType.RENAME_FILE,new Object[]{source+" -> "+target}));
        String pathTarget=root+target;
        try {
            Files.walk(Paths.get(pathSource)).forEach(src -> {
                Path dest = Paths.get(pathTarget,src.toString().substring(pathSource.length()));
                try {
                    Files.copy(src,dest, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void replace(String targetPath, FileSystem source, String sourcePath) {
        //Clear old files
        remove(targetPath);
        //Copy new ones
        String pathSource=source.getRoot()+sourcePath;
        File s = source.getFileAtPath(sourcePath);
        if(!s.exists()) return;
        String pathTarget=root+targetPath;
        try {
            Files.walk(Paths.get(pathSource)).forEach(src -> {
                Path dest = Paths.get(pathTarget,src.toString().substring(pathSource.length()));
                try {
                    Files.copy(src,dest, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
