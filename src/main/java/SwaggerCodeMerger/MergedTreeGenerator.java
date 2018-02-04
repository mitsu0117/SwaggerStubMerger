package SwaggerCodeMerger; /**
 * MergedTreeGenerator
 *
 * Copyright (c) 2018 mitsu0117
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 *
 * This module
 */

import difflib.Delta;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MergedTreeGenerator {
    TreeFinder newTree;
    TreeFinder oldTree;
    List<String> newRelativeTree;
    List<String> oldRelativeTree;
    List<String> newlyAddedFileList;
    List<String> relativePathOfIdenticalFiles;

    public void MergedTreeGenerater () {
    }

    void setNewTree(String path) {
        newTree = new TreeFinder();
        newTree.scanTree(path);
        newRelativeTree = newTree.getRelativePaths();
    }

    void setOldTree(String path) {
        oldTree = new TreeFinder();
        oldTree.scanTree(path);
        oldRelativeTree = oldTree.getRelativePaths();
    }


    public void findNewlyAddedFiles() {
        /*
         * This function finds newly-added files those do not exist in the
         * corresponding directory in the old tree.
         */
        newlyAddedFileList = new ArrayList<>();
        for (String after: newRelativeTree) {
            boolean exist = false;
            for ( String before: oldRelativeTree) {
                if ( after.equals(before)) { exist = true; }
            }
            if (exist == false) {
                newlyAddedFileList.add(after);
            }
        }
        for (String tmp: newlyAddedFileList) {
            MyLogging.log(new Throwable().getStackTrace()[0],  tmp + " is copied to the " + Variables.getMergedTreeRootDir() + ".");
        }
    }

    public void genMergedDirectoryWithNewlyAddedFiles(String targetPath) {
        File targetPathDir = new File(targetPath);
        targetPathDir.mkdir();
        for (String relativePath: newlyAddedFileList) {
            File file = newTree.getFileByRelativePath(relativePath);
            String tmp = targetPath + "/" + relativePath;
            Path targetFilePath = Paths.get(targetPath + "/" + relativePath);
            File targetFile = new File(targetPath + "/" + relativePath);
            MyFile.copy(Paths.get(file.getAbsolutePath()), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public void copyIdenticalFilesToMergedDirectory(String targetPath) {
        Set<String> tmp = oldTree.fileHashMap.keySet();
        relativePathOfIdenticalFiles = new ArrayList<>();
        for (String tmpKey:tmp) {
            if (newTree.fileHashMap.containsKey(tmpKey)) {
                // this file exists in the both old and new directory
                List<Delta> diff = Profiler.getDiff(newTree.getFileHashMap().get(tmpKey), oldTree.getFileHashMap().get(tmpKey));
                if (diff.size() == 0) {
                    Path targetFilePath = Paths.get(targetPath + "/" + tmpKey);
                    MyFile.copy(newTree.getFileHashMap().get(tmpKey).toPath(), targetFilePath, StandardCopyOption.REPLACE_EXISTING);
                    relativePathOfIdenticalFiles.add(tmpKey);
                }
            }
        }
    }

    public void mergeCorrespondingFilesInBothDirectory(String targetPath) {
        Set<String> tmpKeys = oldTree.getFileHashMap().keySet();
        for( String tmpKey: tmpKeys) {
            if (relativePathOfIdenticalFiles.contains(tmpKey) == false) {
                // the file, which exists in the both old and new directory, with update
                MyLogging.log(new Throwable().getStackTrace()[0], tmpKey + "is under merging process.");
                //Profiler.getProfile(newTree.getFileHashMap().get(tmpKey), oldTree.getFileHashMap().get(tmpKey));
                List<FunctionProfile> tmpFps = Profiler.getProfile(oldTree.getFileHashMap().get(tmpKey));
                List<String> stringLines = genMergedFileContentWithProfile(newTree.getFileHashMap().get(tmpKey), tmpFps);
                MyFile.generateFromStringLines(Paths.get(targetPath + "/" + tmpKey), stringLines);
            }
        }
    }

    private List<String> genMergedFileContentWithProfile(File newFile, List<FunctionProfile> oldFunctionProfiles) {
        List<String> newFileLines = Profiler.file2String(newFile);
        List<String> newStringList = new ArrayList<>();
        List<FunctionProfile> newFps = new ArrayList<>();
        if (oldFunctionProfiles.size() == 0) {
            MyLogging.log(new Throwable().getStackTrace()[0], newFile.getName() +" : No inherited content exists..");
            newStringList = null;
            newStringList = newFileLines;
            //System.out.println(newStringList.get(0));
        } else {
            for (FunctionProfile fp : oldFunctionProfiles) {
                Pattern functionDefinitionPattern1 = Pattern.compile(Variables.getNodejsFunctionRegexs(0) + fp.functionName);
                Pattern functionDefinitionPattern2 = Pattern.compile(Variables.getNodejsFunctionRegexs(1) + fp.functionName);
                boolean inFunction = false;
                int countBrace1 = 0;
                int countBrace2 = 0;
                FunctionProfile tmpFp = new FunctionProfile();
                tmpFp.functionName = fp.functionName;
                tmpFp.lineFrom = 0;
                tmpFp.lineTo = 0;
                int lineCount = 0;
                for (String line : newFileLines) {
                    Matcher m1 = functionDefinitionPattern1.matcher(line);
                    Matcher m2 = functionDefinitionPattern2.matcher(line);
                    if (m1.find() || m2.find()) {
                        inFunction = true;
                        tmpFp.lineFrom = lineCount;
                        MyLogging.log(new Throwable().getStackTrace()[0], newFile.getName() +":Function=" + fp.functionName + " is inherited..");
                    }
                    for (char x : line.toCharArray()) {
                        if (x == '{') {
                            countBrace1++;
                        } else if (x == '}') {
                            countBrace2++;
                        }
                    }
                    if (inFunction == true) {
                        if (countBrace1 == countBrace2) {
                            // end of function: nodejs-server
                            tmpFp.lineTo = lineCount;
                            inFunction = false;
                            newFps.add(tmpFp);
                            break;
                        }
                    }
                    lineCount++;
                }
            }
            // NOT YET: need to sort the new fp by  lineTo
            for ( int i = newFps.size() - 1; i >= 0; i--) {
                int currentNewFileLineLength = newFileLines.size();
                for (int j = 0; j < newFps.get(i).lineFrom; j++) {
                    newStringList.add(newFileLines.get(j));
                }
                for (String line : oldFunctionProfiles.get(i).functionLines) {
                    newStringList.add(line);
                }
                for (int j = newFps.get(i).lineTo + 1; j < currentNewFileLineLength; j++) {
                    newStringList.add(newFileLines.get(j));
                }
                if (i != 0) {
                    newFileLines = null;
                    newFileLines = new ArrayList<String>(newStringList);
                    newStringList = null;
                    newStringList = new ArrayList<>();
                }
            }
        }
        return  newStringList;
    }
}
