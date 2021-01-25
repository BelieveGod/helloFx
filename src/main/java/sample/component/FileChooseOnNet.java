package sample.component;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import sample.view.FileChooseOnNetController;

import java.net.URL;

import static sample.Main.*;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/1/26 9:48
 */
public class FileChooseOnNet extends Stage {

    public String showOpenDialog(Window owner){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL location = getClass().getResource(VIEW_PATH+"fileChooseOnNet.fxml");
            fxmlLoader.setLocation(location);
            BorderPane root = (BorderPane) fxmlLoader.load();
            FileChooseOnNetController controller = (FileChooseOnNetController) fxmlLoader.getController();
            this.setScene(new Scene(root));
            this.initOwner(owner);
            this.initModality(Modality.WINDOW_MODAL);
            controller.setDialogStage(this);
            this.setResizable(false);
            this.showAndWait();
            String url=controller.getChoosenUrl();
            return url;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
