package org.example.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DotReader {

    private final String DOT_FILE_PATH;

    private final List<File> dotFiles = new ArrayList<>();

    private final Logger logger = LogManager.getLogger(DotReader.class);

    public DotReader(String path) {
        this.DOT_FILE_PATH = path;
    }

    public List<File> getDotFiles(){
        File folder = new File(DOT_FILE_PATH);
        File[] listFileOfDots = folder.listFiles((dir, name) -> name.endsWith(".dot"));
        if (listFileOfDots != null){
            dotFiles.addAll(Arrays.asList(listFileOfDots));
        }
        return dotFiles;
    }

    public String readDotFile(File file) throws IOException {
        return new String(Files.readAllBytes(Paths.get(file.getPath())));
    }


}
