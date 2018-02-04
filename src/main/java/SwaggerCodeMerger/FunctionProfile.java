package SwaggerCodeMerger;
/**
 * FunctionProfile
 *
 * Copyright (c) 2018 mitsu0117
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 *
 * This class is used as profile about functions in the processed programs.
 *
 */
import java.util.ArrayList;
import java.util.List;

public class FunctionProfile {
    public String functionName;
    public String filePath;
    public Integer order;
    public Integer lineFrom;
    public Integer lineTo;
    public boolean isHeritageFunction;
    public List<String> functionLines;
    FunctionProfile(){
        functionName = new String();
        filePath = new String();
        lineFrom = -1;
        lineTo = -1;
        isHeritageFunction = false;
        functionLines = new ArrayList<>();
    }
    public void printFunctionProfile() {
        System.out.println("==============================");
        System.out.println(this.filePath);
        System.out.println(this.functionName);
        System.out.println(this.order);
        System.out.println(this.lineFrom.toString() + " -> " + this.lineTo.toString());
        System.out.println("isHeritageFunc.: " + this.isHeritageFunction);
        System.out.println();
    }
}
