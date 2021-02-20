package sample.component;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import sample.model.AgxFile;
import sample.view.FileChooseOnNetController;

import java.net.URL;

import static sample.Main.*;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/2/20 10:50
 */
public class ConfirmUpgrade extends Stage {
    private Boolean confirm=false;
    private AgxFile agxFile;


    public ConfirmUpgrade(AgxFile agxFile) {
        super();
        this.agxFile = agxFile;
    }

    public Boolean showOpenDialog(Window owner){
        try {
            FlowPane root = new FlowPane();
            BorderPane borderPane=new BorderPane();
            VBox vBox = new VBox();
            Label label=new Label(agxFile.getName());
            TextArea textArea = new TextArea(agxFile.getInfo());
            vBox.getChildren().addAll(label, textArea);
            borderPane.setCenter(vBox);

            borderPane.setBottom(root);
            Button confirmBtn = new Button("确定");
            Button cancelBtn = new Button("取消");
            confirmBtn.setOnAction(this::onConfirm);
            cancelBtn.setOnAction(this::onCancel);
            root.getChildren().addAll(confirmBtn,cancelBtn);
            this.setScene(new Scene(borderPane));
            this.initOwner(owner);
            this.initModality(Modality.WINDOW_MODAL);
            this.setResizable(false);
            this.showAndWait();

            return confirm;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private void onConfirm(ActionEvent event){
        confirm=true;
        this.close();
    }

    private void onCancel(ActionEvent event){
        confirm=false;
        this.close();
    }


}
