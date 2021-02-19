package sample.component;

import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXTextArea;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import sample.common.Colleague;
import sample.common.Constant;
import sample.common.Mediator;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/2/8 9:07
 */
public class CenterPane extends VBox implements Colleague {
    private JFXTabPane tabPane;
    private TextArea textArea;
    private JFXProgressBar progressBar;
    private Label label;

    private final MainWin mainWin;
    private Map<String, Callback<Object[],Void>> funMap = new HashMap<>();
    private Mediator mediator;

    public CenterPane(MainWin mainWin) {
        super();
        this.mainWin=mainWin;
        // 注册
        ((Mediator)mainWin).register(this);
        init();
        initEvent();
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

    private void initEvent(){
        on(Constant.CONNECT,objects -> {
            Platform.runLater(() -> {
                updateProgress(0);
            });
            return null;
        });

        on(Constant.LOG,objects -> {
            if(objects.length<1){
                return null;
            }
            Platform.runLater(()->{
                appendLog((String)objects[0]);
            });
            return null;
        });

        on(Constant.PROGRESS,objects -> {
            Platform.runLater(()->{
                updateProgress((Double)objects[0]);
            });
            return null;
        });
    }

    public void updateProgress(double percentage){
        progressBar.setProgress(percentage);
        int d=(int)percentage*100;
        label.setText(String.format("%d%%",d));
    }

    public void appendLog(String s){
        textArea.appendText(s);
    }

    @Override
    public void on(String event,Callback<Object[],Void> callback){
        funMap.put(event,callback);
    }

    @Override
    public void reveive(String event, Object... args) {
        Callback<Object[], Void> callback = funMap.get(event);
        if (callback != null) {
            callback.call(args);
        }
    }

    @Override
    public void send(String event, Object... args) {
        mediator.relay(event,args);
    }

    @Override
    public void setMediator(Mediator mediator) {
        this.mediator = mediator;
    }
}
