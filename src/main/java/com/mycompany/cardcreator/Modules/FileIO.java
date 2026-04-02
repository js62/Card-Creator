package com.mycompany.cardcreator.Modules;

import java.io.File;
import java.nio.file.Files;
import java.io.IOException;
import com.google.gson.Gson;

public class FileIO {

    public static void saveModel(Model model) {
        File dataFile = new File(model.getFolder(), "data.json");
        SavableModel m = new SavableModel(model);
        String json = new Gson().toJson(m);
        try {
            Files.writeString(dataFile.toPath(), json);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    public static Model loadModel(File projectFolder) {
        File dataFile = new File(projectFolder, "data.json");
        if (!dataFile.exists()) return new Model(); // create new empty model if none

        try {
            String json = Files.readString(dataFile.toPath());
            SavableModel sm = new Gson().fromJson(json, SavableModel.class);
            Model model = new Model();
            model.setFolder(projectFolder);
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

    public static void createProject(File ProjectFolder) {
        Model model = new Model();
        model.setFolder(ProjectFolder);
        saveModel(model);
    }
}

class SavableModel {

    String folder;
    int pageWidth;
    int pageHeight;
    int cardWidth;
    int cardHeight;
    String backgroundImagePath;
    int imgX;
    int imgY;
    int imgW;
    int imgH;

    public SavableModel() {}
    public SavableModel(Model m) {
        folder = m.getFolder().getAbsolutePath();
        pageWidth = m.getPageWidth();
        pageHeight = m.getPageHeight();
        cardWidth = m.getCardWidth();
        cardHeight = m.getCardHeight();
        backgroundImagePath = m.getBackgroundImagePath();
        imgX = m.getImgX();
        imgY = m.getImgY();
        imgW = m.getImgW();
        imgH = m.getImgH();
    }
}