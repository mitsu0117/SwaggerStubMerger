package SwaggerCodeMerger;

import java.sql.Struct;

/**
 * Variables
 *
 * Copyright (c) 2018 mitsu0117
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 *
 * This module offers global variables
 *
 */

public class Variables {
    static String startAnnotation = "//__swagger-anchor-start=";
    static String endAnnotation = "//__swagger-anchor-end";
    static String functionAnnotation = "function:";
    //static String mergedDirectory = "merged";
    static String[] nodejsFunctionRegexs = { "^module.exports.", "^exports."};
    static String jarName = "swagger-stub-merger";
    static String newTreeRootDir = null;
    static String oldTreeRootDir = null;
    static String mergedTreeRootDir = "./merged";
    static boolean logFlag = false;
    static void Variables() {
    }
    static String getStartAnnotation () {    return startAnnotation;    }
    static String getEndAnnotation () {    return endAnnotation;    }
    //static String getMergedDirectory () { return  mergedDirectory; }
    static String getFunctionAnnotation() {return functionAnnotation; }
    static void setNewTreeRootDir(String tmp) { newTreeRootDir = tmp; }
    static String getNewTreeRootDir() { return newTreeRootDir; }
    static void setMergedTreeRootDir(String tmp) { mergedTreeRootDir = tmp; }
    static String getMergedTreeRootDir() { return mergedTreeRootDir; }
    static void setOldTreeRootDir(String tmp) { oldTreeRootDir = tmp; }
    static String getOldTreeRootDir() { return oldTreeRootDir; }
    static void setLogFlag (boolean _flg) { logFlag = _flg; }
    static String getJarName() { return jarName; }
    static boolean getLogFlag() {return logFlag;}
    static String getNodejsFunctionRegexs (int i ) {
        if (i < 0 || i >= nodejsFunctionRegexs.length) {
            return null;
        } else {
            return nodejsFunctionRegexs[i];
        }
    }
}
