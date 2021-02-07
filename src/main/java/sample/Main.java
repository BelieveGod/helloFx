package sample;

import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sample.component.LeftPane;
import sample.view.MainStageController;

import java.net.URL;

public class Main extends Application {
    private Stage primaryStage;
    private Group rootLayout;
    private MainStageController mainStageController;
    public static final String VIEW_PATH="/sample/view/";

    JFXDialog dialog;
    JFXAlert<String> alert;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.primaryStage=primaryStage;
//        initRootLayout();
//        initTestLayout();
        initLeftPane();
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

    public void initTestLayout(){
        StackPane stackPane = new StackPane();
//        JFXButton btn = new JFXButton("对话框");
        Button btn = new Button("对话框");
        stackPane.getChildren().add(btn);
        stackPane.setPrefHeight(400);
        stackPane.setPrefWidth(400);

        final Scene scene = new Scene(stackPane);

         dialog=new JFXDialog();
        dialog.setDialogContainer(stackPane);
        dialog.setContent(new Label("content"));


        primaryStage.setScene(scene);
        dialog.setPrefWidth(200);
        dialog.setPrefHeight(200);

        primaryStage.show();
        alert = new JFXAlert(primaryStage);
        alert.setContent(new Label("content"));
        alert.setSize(200, 200);


      btn.setOnAction(this::showDialog);


    }

    private void showDialog(ActionEvent event){
        alert.show();
    }

    private void initLeftPane(){
        LeftPane leftPane = new LeftPane();
        StackPane stackPane = new StackPane();
        stackPane.setPrefWidth(200);
        stackPane.setStyle("-fx-background-color: yellow");
        Scene scene = new Scene(leftPane);
        primaryStage.setWidth(500);
        primaryStage.setHeight(500);

        primaryStage.setScene(scene);
        primaryStage.show();

    }

}
