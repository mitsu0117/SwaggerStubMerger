package SwaggerCodeMerger;

/**
 * MyLogging
 *
 * Copyright (c) 2018 mitsu0117
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 *
 * This module offers standardized log output.
 *
 */
public class MyLogging {
    static public void log(StackTraceElement ste, String msg) {
        if (Variables.getLogFlag() == true ) {
            System.out.println("Log: " + msg + ": " + ste.getMethodName() + " @ " + ste.getFileName());
        }
    }
}
