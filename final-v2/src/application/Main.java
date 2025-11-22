package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static Stage primaryStage;
    public static Scene mainScene;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        mainScene = new Scene(root, 1250, 800);  

        stage.setTitle("Coworking System");
        stage.setScene(mainScene);

        stage.setResizable(false);  
        stage.show();
    }

    
    public static void changeScene(String fxml) {
        try {
            Parent newRoot = FXMLLoader.load(Main.class.getResource(fxml));
            mainScene.setRoot(newRoot);  
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

