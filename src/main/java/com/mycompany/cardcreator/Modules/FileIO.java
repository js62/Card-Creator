package com.mycompany.cardcreator.Modules;

import java.io.File;
import java.nio.file.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
    
    
    /**
     * Loads a Model from the data.json file in the given project folder.
     * Returns null if the file doesn't exist or can't be read.
     */
    public static Model loadModel(File projectFolder) {
        File dataFile = new File(projectFolder, "data.json");
        if (!dataFile.exists()) {
            System.out.println("No data.json found in " + projectFolder);
            return null;
        }
        try {
            String json = Files.readString(dataFile.toPath());
            SavableModel sm = new Gson().fromJson(json, SavableModel.class);
            Model model = new Model();
            model.setFolder(projectFolder);
            model.setCanvasWidth(sm.canvasWidth);
            model.setCanvasHeight(sm.canvasHeight);
            model.setBackgroundImagePath(sm.backgroundImagePath);
            model.setImgX(sm.imgX);
            model.setImgY(sm.imgY);
            model.setImgW(sm.imgW);
            model.setImgH(sm.imgH);
            return model;
        } catch (IOException ex) {
            System.out.println("Error loading project: " + ex);
            return null;
        }
    }
    

    /** Creates a new empty project in the given folder and saves data.json */
    public static void createProject(File ProjectFolder) {

        Model model = new Model();//create empty model with default settings to save as new project
        model.setFolder(ProjectFolder);

        saveModel(model);
    }
}
/**
 * Wrapper class for serializing/deserializing Model data to JSON via GSON.
 */
class SavableModel{
    
    String folder;
    int canvasWidth;
    int canvasHeight;
    String backgroundImagePath;
    // Image position and size on the canvas
    int imgX;
    int imgY;
    int imgW;
    int imgH;

    
    public SavableModel() {}

    public SavableModel(Model m) {
        folder=m.getFolder().getAbsolutePath();
        canvasWidth = m.getCanvasWidth();
        canvasHeight= m.getCanvasHeight();
        backgroundImagePath = m.getBackgroundImagePath();
        imgX = m.getImgX();
        imgY = m.getImgY();
        imgW = m.getImgW();
        imgH = m.getImgH();
    }
    
}