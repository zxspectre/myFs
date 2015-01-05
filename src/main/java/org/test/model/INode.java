package org.test.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nay on 1/3/2015.
 */
public class INode {
    //TODO sync on RWLock using soft links for locks?
    private Map<String, INode> childs;
    private boolean isFile = false;

    public INode(boolean isFile){
        this.isFile = isFile;
        if(isFile){

        }else{
            childs = new HashMap<>();
        }
    }

    public boolean isFile() {
        return isFile;
    }

    public Map<String, INode> getChilds() {
        if(isFile){
            throw new RuntimeException("Programmer error. Should not request children of a file.");
        }
        return childs;
    }
}
