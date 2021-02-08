package sample.component;

import javafx.scene.layout.BorderPane;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/2/8 9:04
 */
public class MainWin extends BorderPane {
    private LeftPane leftPane;
    private CenterPane centerPane;
    private BottomPane bottomPane;

    public MainWin() {
        super();
        init();
    }

    private void init(){
        leftPane = new LeftPane(this);
        centerPane = new CenterPane(this);
        bottomPane=new BottomPane(this);
        this.setLeft(leftPane);
        this.setCenter(centerPane);
        this.setBottom(bottomPane);

    }

    public void updateProgress(double percentage){
        centerPane.updateProgress(percentage);

    }

    public void appendLog(String s){
        centerPane.appendLog(s);
    }
}
