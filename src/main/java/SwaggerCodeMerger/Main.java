package SwaggerCodeMerger;
/**
 * Main
 *
 * Copyright (c) 2018 mitsu0117
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 *
 *
 */
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        for (String arg: args ) {
            switch (arg) {
                case "-l":
                case "--log":
                    Variables.setLogFlag(true);
                    //arg.equals("--log"))) {
                    break;
                case "-n":
                case "--new-stub-root-dir":
                    if (nextArgExist(args, arg)) {
                        //Variables.setNewTreeRootDir(getNextArg(args, arg));
                        Variables.setNewTreeRootDir(new File(getNextArg(args, arg)).getCanonicalPath());
                    }
                    break;
                case "-c":
                case "--current-stub-root-dir":
                    if (nextArgExist(args, arg)) {
                        //File tmp = new File(getNextArg(args, arg));
                        //Variables.setOldTreeRootDir(tmp.getAbsolutePath());
                        Variables.setOldTreeRootDir(new File(getNextArg(args, arg)).getCanonicalPath());
                    }
                    break;
                case "-o":
                case "--output-dir":
                    if (nextArgExist(args, arg)) {
                        //File tmp = new File(getNextArg(args, arg));
                        Variables.setMergedTreeRootDir(new File(getNextArg(args, arg)).getCanonicalPath());
                    }
                    break;
                case "-h":
                case "--help":
                    printHelp();
                    break;
                case "-f":
                case "--framework":

                    break;

            }
        }
        Variables.setMergedTreeRootDir(new File(Variables.getMergedTreeRootDir()).getCanonicalPath());
        if (Variables.getNewTreeRootDir() == null) {
            System.out.println("Program stopped! Use option -n | --new-stub-root-dir");
            System.exit(0);
        }
        if (Variables.getOldTreeRootDir() == null) {
            System.out.println("Program stopped! Use option -c | --current-stub-root-dir");
            System.exit(0);
        }

        System.out.println("New stub root dir: " + Variables.getNewTreeRootDir());
        System.out.println("Current stub root dir: " + Variables.getOldTreeRootDir());
        System.out.println("Merged stub root dir: " + Variables.getMergedTreeRootDir());


        MergedTreeGenerator mtg = new MergedTreeGenerator();
        mtg.setNewTree(Variables.getNewTreeRootDir());
        mtg.setOldTree(Variables.getOldTreeRootDir());
        mtg.findNewlyAddedFiles();
        String targetPath = Variables.getMergedTreeRootDir();
        mtg.genMergedDirectoryWithNewlyAddedFiles(targetPath);
        mtg.copyIdenticalFilesToMergedDirectory(targetPath);
        mtg.mergeCorrespondingFilesInBothDirectory(targetPath);
    }
    static private void printHelp () {
        String space = "   ";
        String[] messages = {
                "Uasge: java -jar " + Variables.getJarName() + " [options]",
                "where options include:",
                space + "-l | --log",
                space + space + space + "Output execution logs",
                space + "-n | --new-stub-root-dir PATH_TO_DIR",
                space + space + space + "Required: Input new stub root dir to be merged with the",
                space + space + space + "currently-developing stub programs.",
                space + "-c | --current-stub-root-dir PATH_TO_DIR",
                space + space + space + "Required: Input currently developing stub root dir to be merged.",
                space + "-o | --output-dir PATH_TO_DIR",
                space + space + space + "Output directory for the merged stub. Default value is \"./merged\".",
                ""};
        for (String line: messages) { System.out.println(line);}
        System.exit(0);
    }

    static private boolean nextArgExist(String[] args, String arg) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(arg)) {
                if (args.length - 1 >= i + 1) {
                    return true;
                }
            }
        }
        return false;
    }
    static private String getNextArg(String[] args, String arg) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(arg)) {
                return args[i + 1 ];
            }
        }
        return null;
    }

}
