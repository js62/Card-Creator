package com.mycompany.cardcreator.Modules;

import java.io.File;
import java.nio.file.Files;
import com.google.gson.Gson;
import java.io.IOException;
//import java.util.Map;
//import com.google.gson.reflect.TypeToken;
//import java.lang.reflect.Type;

/**
 * Handles saving and loading data from files
 */
public class FileIO {

    public static void saveModel(Model model) {

        File dataFile = new File(model.getFolder(), "data.json");

        SavableModel m=new SavableModel(model);

        String json = new Gson().toJson(m);

        try {
            Files.writeString(dataFile.toPath(), json);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    public static void createProject(File ProjectFolder) {

        Model model = new Model();//create empty model with default settings to save as new project
        model.setFolder(ProjectFolder);

        saveModel(model);
    }
}
class SavableModel{
    private String folderStr;
    public SavableModel(Model m) {
        folderStr=m.getFolder().getAbsolutePath();
    }
    
}