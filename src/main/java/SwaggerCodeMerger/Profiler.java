package SwaggerCodeMerger;
/**
 * Profiler
 *
 * Copyright (c) 2018 mitsu0117
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 *
 * This module offers functions to get profile which is used to merge two corresponding programs.
 *
 */

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Profiler {
    public static void main(String[] args) {
    }

    static List<Delta> getDiff(File oldFile, File newFile) {
        ArrayList<String> strList1 = (ArrayList<String>) file2String(oldFile);
        ArrayList<String> strList2 = (ArrayList<String>) file2String(newFile);
        final Patch patch = DiffUtils.diff(strList1, strList2);
        MyLogging.log(new Throwable().getStackTrace()[0], newFile.getName() + ", diff = " + patch.getDeltas().toString());
        return patch.getDeltas();
    }

    static List<String> file2String (File file) {
        List<String> lines = new ArrayList<>();
        String line;
        final BufferedReader in;
        try {
            in = new BufferedReader(new FileReader(file));
            while ((line = in.readLine()) != null) {
                lines.add(line);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    static public List<FunctionProfile> getProfile(File oldFile) {
        List<String> oldFileLines = file2String(oldFile);
        boolean inFunctionFlag = false;
        List<FunctionProfile> fps = new ArrayList<>();
        FunctionProfile fp = new FunctionProfile();
        Pattern matcher = Pattern.compile("(" + Variables.getStartAnnotation() + ")(.*:)(.*)(//)");
        for (String line: oldFileLines) {
            if (inFunctionFlag) {
                fp.functionLines.add(line);

            }
            if (line.contains(Variables.getStartAnnotation())) {
                Matcher m = matcher.matcher(line);
                if (m.find()) {
                    if (m.group(2).equals(Variables.getFunctionAnnotation())) {
                        fps.add(fp);
                        fp.functionName = m.group(3);
                        MyLogging.log(new Throwable().getStackTrace()[0],  "Function: " + m.group(3) + " will be under merging process.");
                        fp.functionLines.add(line);
                        inFunctionFlag = true;
                    }
                }
            }
            if (line.contains(Variables.getEndAnnotation())) {
                if (inFunctionFlag != true ) {
                } else {
                    fp = new FunctionProfile();
                    inFunctionFlag = false;
                }

            }
        }
        return fps;
    }
}
