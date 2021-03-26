package sample;

import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.controls.JFXDialog;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sample.component.LeftPane;
import sample.component.MainWin;
import sample.view.MainStageController;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

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
        initTestLayout();
//        initResourceLayout();
//        initMain();
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
//        Pane root = new Pane();
        StackPane root = new StackPane();
        root.setStyle("-fx-border-color: blue;-fx-border-width: 5px");
        BorderPane borderPane = new BorderPane();
        borderPane.setStyle("-fx-border-color: orange;-fx-border-width: 5px");
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setStyle("-fx-border-color: green;-fx-border-width: 5px");
        StackPane stackPane = new StackPane();
        stackPane.setStyle("-fx-border-color: yellow;-fx-border-width: 5px");
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-border-color: purple;-fx-border-width: 5px");
        VBox vBox= new VBox();
        vBox.setStyle("-fx-border-color: blanchedalmond;-fx-border-width: 5px");
        HBox hBox = new HBox();
        hBox.setStyle("-fx-border-color: red;-fx-border-width: 5px");

        VBox vBox1 = new VBox();
        vBox1.setStyle("-fx-border-color: black;-fx-border-width: 5px");


        NumberAxis xAxis = new NumberAxis(-30,0,1);
        NumberAxis yAxis = new NumberAxis(-4500,4500,750);
        LineChart<Integer, Integer> rpmLineChart = new LineChart(xAxis,yAxis);
        rpmLineChart.prefWidthProperty().bind(vBox1.widthProperty().multiply(0.8));
        rpmLineChart.maxWidthProperty().bind(vBox1.widthProperty().multiply(0.8));

        vBox1.getChildren().addAll(rpmLineChart);

        hBox.getChildren().addAll(vBox1);
        hBox.prefWidthProperty().bind(vBox.widthProperty().multiply(0.5));
        hBox.maxWidthProperty().bind(vBox.widthProperty().multiply(0.5));

        vBox.getChildren().addAll(hBox);
        VBox.setVgrow(hBox, Priority.ALWAYS);

        scrollPane.setContent(vBox);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        stackPane.getChildren().addAll(scrollPane);
//        stackPane.getChildren().addAll(vBox);

        anchorPane.getChildren().addAll(stackPane);
        AnchorPane.setBottomAnchor(stackPane, 0d);
        AnchorPane.setTopAnchor(stackPane, 0d);
        AnchorPane.setRightAnchor(stackPane, 0d);
        AnchorPane.setLeftAnchor(stackPane, 0d);

        borderPane.setCenter(anchorPane);

        root.getChildren().addAll(borderPane);

        Scene scene = new Scene(root, 1000, 750);
        primaryStage.setWidth(1000);
        primaryStage.setWidth(750);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void initResourceLayout(){
        ResourceBundle message = ResourceBundle.getBundle("language/message", Locale.CHINA);
        String cancelkey = message.getString("cancelkey");
        VBox root = new VBox();
        Button cancelBtn = new Button(cancelkey);
        StackPane stackPane = new StackPane();
        Separator separator=new Separator(Orientation.HORIZONTAL);
        Label label = new Label("数据");
        stackPane.getChildren().addAll(separator, label);

        // 图表
        NumberAxis yAxis = new NumberAxis("y laebl", -10, 10, 1);
        NumberAxis xAxis = new NumberAxis("x laebl", -5, 5, 1);

        LineChart lineChart = new LineChart(xAxis,yAxis);


        root.getChildren().addAll(cancelBtn,stackPane,lineChart);
        final Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showDialog(ActionEvent event){
        alert.show();
    }



    private void initMain(){
        MainWin mainWin=new MainWin();
//        JFXDecorator decorator = new JFXDecorator(primaryStage, mainWin);
        Scene scene = new Scene(mainWin);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}
