package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sample.view.MainStageController;

import java.net.URL;

public class Main extends Application {
    private Stage primaryStage;
    private Group rootLayout;
    private MainStageController mainStageController;
    public static final String VIEW_PATH="/sample/view/";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.primaryStage=primaryStage;
        initRootLayout();
    }


    public void initRootLayout(){
        try{
            FXMLLoader fxmlLoader=new FXMLLoader();
            URL location = getClass().getResource(VIEW_PATH+"mainStage.fxml");

            fxmlLoader.setLocation(location);
            rootLayout = (Group)fxmlLoader.load();
            mainStageController =fxmlLoader.getController();
            mainStageController.setPrimaryStage(primaryStage);
            mainStageController.setMain(this);
            primaryStage.setTitle("固件升级工具V1.42");
            primaryStage.setResizable(false);
            primaryStage.setScene(new Scene(rootLayout));
            primaryStage.initStyle(StageStyle.DECORATED);
            Image image=new Image("/image/logo_title.png");
            primaryStage.getIcons().add(image);
            primaryStage.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
