package org.test.impl;

/**
 * Created by nay on 1/5/2015.
 */
public class Util {
    static String[] getParentFromPath(String[] path){
        if(path.length == 0){
            throw new RuntimeException("Programmer error. Path should not be empty when this method is called.");
        }
        String[] res = new String[path.length - 1];
        System.arraycopy(path, 0, res, 0, path.length - 1);
        return res;
    }
}
