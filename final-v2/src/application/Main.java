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

        // 初始化界面，加载 login.fxml
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        mainScene = new Scene(root, 1250, 800);  // ⭐ 固定大小

        stage.setTitle("Coworking System");
        stage.setScene(mainScene);

        stage.setResizable(false);  // ⭐ 禁止调整窗口大小
        stage.show();
    }

    // ⭐ 统一切换页面的方法（核心！）
    public static void changeScene(String fxml) {
        try {
            Parent newRoot = FXMLLoader.load(Main.class.getResource(fxml));
            mainScene.setRoot(newRoot);  // ⭐ 只换 root，不换 Scene
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

