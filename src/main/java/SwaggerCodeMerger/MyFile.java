package SwaggerCodeMerger; /**
 * MyFile
 *
 * Copyright (c) 2018 mitsu0117
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 *
 * This module offers file manipulation functions such as file copy and make intermediate directories.
 *
 */

import java.io.*;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Stack;

public class MyFile  {
    public static void MyFile() {

    }

    static public void copy(Path sourcePath, Path targetPath, CopyOption opt) {
       makeIntermediateDirectory(targetPath);
        try {
            Files.copy(sourcePath, targetPath,opt);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public void makeIntermediateDirectory(Path path) {
        if (Files.notExists(path, LinkOption.NOFOLLOW_LINKS)) {
            Path tmp = path.getParent();
            Stack<Path> dirStack = new Stack<>();
            while (Files.isDirectory(tmp) == false) {
                dirStack.push(tmp);
                tmp = tmp.getParent();
            }
            while (dirStack.isEmpty() == false) {
                File tmpFile = new File(String.valueOf(dirStack.pop()));
                tmpFile.mkdir();
            }
        }
    }
    static public void generateFromStringLines(Path path, List<String > content) {
        makeIntermediateDirectory(path);
        try (PrintWriter pw = new PrintWriter(path.toString(), "UTF-8")) {
            for ( String line: content) {
                pw.println(line);
            }
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
