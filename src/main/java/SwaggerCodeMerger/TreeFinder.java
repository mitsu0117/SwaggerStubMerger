package SwaggerCodeMerger;
 /**
  * TreeFinder
  *
  * Copyright (c) 2018 mitsu0117
  *
  * This software is released under the MIT License.
  * http://opensource.org/licenses/mit-license.php
  *
  *
  *
  */
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class TreeFinder {
    List<File> listFiles;
    HashMap<String, File> fileHashMap;
    String treeRootPath;
    public void main(String[] args) {
        //System.out.println("aaa");
        //List<File> fileList = getTree(".");
        //for (File tmp: fileList) System.out.println(tmp.getAbsolutePath());
    }

    public void scanTree(String path) {
        treeRootPath = path;
        List<File> files = new ArrayList<>();
        Stack<File> stack = new Stack<>();
        stack.add(new File(path));
        while (!stack.isEmpty()) {
            File tmp = stack.pop();
            if (tmp.isFile()) {
                //System.out.println(tmp.getAbsolutePath());
                files.add(tmp);
            }
            if (tmp.isDirectory()) {
                for (File childFile: tmp.listFiles()) {
                    stack.push(childFile);
                }
            }
        }
         listFiles = files;

    }
    public List<String> getRelativePaths() {
        List<String> tmp = new ArrayList<>();
        fileHashMap = new HashMap<>();
        for (File file: listFiles) {
            String relativePath = file.getPath().replace(treeRootPath + "/", "");
            tmp.add(relativePath);
            fileHashMap.put(relativePath, file);
        }
        return  tmp;
    }
    public File getFileByRelativePath (String relativePath) {
        return fileHashMap.get(relativePath);
    }
    public  HashMap<String, File> getFileHashMap() {
        return fileHashMap;
    }
}
