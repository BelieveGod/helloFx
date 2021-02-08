package sample.component;

import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/2/8 9:15
 */
public class BottomPane extends AnchorPane {
    private final MainWin mainWin;
    private Hyperlink hyperlink;
    private ImageView imageView;

    public BottomPane(MainWin mainWin) {
        super();
        this.mainWin=mainWin;
        init();
    }

    private void init(){
        hyperlink=new Hyperlink("www.agilex.ai");
        imageView = new ImageView(new Image(getClass().getResourceAsStream("/image/logo_s.png")));
        hyperlink.setLayoutX(30);
        hyperlink.setLayoutY(10);
        AnchorPane.setLeftAnchor(hyperlink, 30d);
        AnchorPane.setBottomAnchor(hyperlink, 10d);

        AnchorPane.setRightAnchor(imageView, 30d);
        AnchorPane.setBottomAnchor(imageView, 10d);

        this.getChildren().addAll(hyperlink, imageView);

    }
}
