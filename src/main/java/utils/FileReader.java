package utils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author ashan on 2020-05-09
 */
public class FileReader {

    public static List<String> readLines(String path) throws Exception{
        return Files.readAllLines(Paths.get(path));
    }
}
