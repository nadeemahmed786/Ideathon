package main.java;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

public class RenameFiles {
    String baseDir = System.getProperty("user.dir");
    static String[] directories;
    String parentDirectory = baseDir + "\\results";

    public void listAllDirectories() {
        File file = new File(parentDirectory);
        directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        System.out.println("Folders: "+Arrays.toString(directories));

        for (String dirname: directories) {
            System.out.println(parentDirectory+"\\"+dirname);
            renameFilesUnderDirectory(parentDirectory+"\\"+dirname);
        }
    }

    public void renameFilesUnderDirectory(String folderPath) {
        String dirPath = folderPath;
        File folder = new File(dirPath);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if(listOfFiles[i].isFile()) {
                listOfFiles[i].renameTo(new File(dirPath +
                        "\\" + (i+1) + ".jpg"));
            }
        }
    }

}
