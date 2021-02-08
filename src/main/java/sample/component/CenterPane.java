package sample.component;

import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXTextArea;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/2/8 9:07
 */
public class CenterPane extends VBox {
    private JFXTabPane tabPane;
    private TextArea textArea;
    private JFXProgressBar progressBar;
    private Label label;

    private final MainWin mainWin;

    public CenterPane(MainWin mainWin) {
        super();
        this.mainWin=mainWin;
        init();
    }

    private void init(){
        tabPane=new JFXTabPane();
        tabPane.setPrefSize(415,419);
        Tab tab = new Tab("使用帮助");
        JFXTextArea helpText = new JFXTextArea("使用帮助\n使用帮助\n使用帮助\n使用帮助\n使用帮助\n使用帮助\n使用帮助\n使用帮助\n使用帮助\n使用帮助\n使用帮助\n使用帮助\n使用帮助\n使用帮助\n使用帮助\n使用帮助\n");
        helpText.setStyle("-fx-background-color: white");
        helpText.setPrefSize(415, 419);
        Tab tab2 = new Tab("操作日志");


        textArea = new TextArea();
        textArea.setPrefSize(415, 419);

        tab.setContent(helpText);
        tab2.setContent(textArea);
        tabPane.getTabs().addAll(tab, tab2);

        StackPane stackPane = new StackPane();
        progressBar = new JFXProgressBar();
        progressBar.setPrefSize(415, 30);
        progressBar.setProgress(0.2);

        label = new Label("0%");
        stackPane.getChildren().addAll(progressBar, label);
        progressBar.prefWidthProperty().bind(stackPane.widthProperty());

        this.getChildren().addAll(tabPane, stackPane);
    }

    public void updateProgress(double percentage){
        progressBar.setProgress(percentage);

    }

    public void appendLog(String s){
        textArea.appendText(s);
    }
}
