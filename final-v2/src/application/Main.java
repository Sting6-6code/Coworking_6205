package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        Scene scene = new Scene(loader.load(), 600, 400);
        stage.setTitle("Login System");
        stage.setScene(scene);
        stage.show();
    }

    public static void changeScene(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxml));
        Scene scene = new Scene(loader.load(), 600, 400);
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}


