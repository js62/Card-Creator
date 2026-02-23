package com.mycompany.cardcreator.Modules;

import java.io.File;

/**
 * Model contains all data for project and handles all operations on this data.
 */
public class Model {

    public Model() {
    }
    
    private File projectFolder=null;
    
    public void setFolder(File folder){
        projectFolder=folder;
    }
    public File getFolder(){
        return projectFolder;
    }
    
}
