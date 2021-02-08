package sample.component;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.SneakyThrows;
import sample.service.SerialPortService;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/2/7 15:01
 */
public class LeftPane extends VBox {

    private HBox hbox1;
    private ToggleGroup upgradeWayGroup;
    private RadioButton usb2Can;
    private RadioButton usb2RS232;
    private JFXComboBox<String> portCombo;
    private HBox canNodeHbox;
    private JFXComboBox canNodeCombo;
    private JFXComboBox chassisCombo;
    // 按钮组
    private VBox vbox1;
    private StackPane stackPane;
    private JFXButton connectBtn;
    private JFXButton disConnectBtn;
    private JFXButton checkUpgrateBtn;

    private Controller controller;
    private final MainWin mainWin;

    public LeftPane(MainWin mainWin) {
        super();
        this.mainWin=mainWin;
        init();
        this.setSpacing(5);
    }

    public void init(){
        initUpgradeWay();
        initPort();
        initCanNode();
        initChassis();
        initButtons();
        StackPane stackPane1 = new StackPane(new Label("statckpanel"));
        stackPane1.setStyle("-fx-background-color: red;-fx-pref-width: 300px;");
        Pane pane = new Pane();
        pane.setStyle("-fx-background-color: green;-fx-pref-width: 300px;");
        pane.getChildren().add(stackPane1);
        this.getChildren().addAll(hbox1, portCombo.getParent(), canNodeCombo.getParent(), chassisCombo.getParent(),vbox1,pane);
        this.setFillWidth(false);
        this.setPrefSize(200,457);

        controller=new Controller();
        controller.init();

    }

    private void initButtons() {
        vbox1 = new VBox();
        vbox1.setSpacing(5);

        stackPane = new StackPane();
        stackPane.setPrefWidth(180);
        stackPane.setStyle("-fx-background-color: blue;-fx-pref-width: 180px");
        stackPane.widthProperty().addListener((observableValue, oldValue, newValue) -> {
            System.out.println("stackPane oldValue = " + oldValue);
            System.out.println("stackPane newValue = " + newValue);
            System.out.println("stackPane.getPrefWidth() = " + stackPane.getPrefWidth());
        });
        connectBtn=createButton("连接设备");
        disConnectBtn=createButton("断开设备");
        disConnectBtn.visibleProperty().bind(connectBtn.visibleProperty().not());
        stackPane.getChildren().addAll(connectBtn, disConnectBtn);

        checkUpgrateBtn=createButton("检查升级");

        vbox1.getChildren().addAll(stackPane, checkUpgrateBtn);

    }

    private void initChassis() {
        HBox hbox = createHbox();
        Label label = createLabel("车型");
        chassisCombo = createComboBox();
        // todo 要改成动态数据
        chassisCombo.getItems().addAll("scout","hunter");
        hbox.getChildren().addAll(label, chassisCombo);
    }

    private void initCanNode() {
        HBox hbox = createHbox();
        Label label = createLabel("升级节点");
        canNodeCombo = createComboBox();
        // todo 要改成动态数据
        canNodeCombo.getItems().addAll("主控","驱动1");
        hbox.getChildren().addAll(label, canNodeCombo);
    }

    private void initPort() {
        HBox hbox = createHbox();
        Label label = createLabel("串口");
        portCombo = createComboBox();
        // todo 要改成动态数据
//        portCombo.getItems().addAll("com1","com2");
        hbox.getChildren().addAll(label, portCombo);

    }

    private void initUpgradeWay() {
        hbox1 = createHbox();
        Label text = this.createLabel("升级方式");
        hbox1.getChildren().add(text);
        VBox vbox = new VBox();
        vbox.setSpacing(5.0);
        upgradeWayGroup = new ToggleGroup();
        usb2Can = new RadioButton("USB转CAN");
        usb2Can.setSelected(true); // 默认选中usb转can方式
        usb2Can.setToggleGroup(upgradeWayGroup);
        usb2Can.setUserData("can");
        vbox.getChildren().add(usb2Can);
        usb2RS232 = new RadioButton("USB转RS232");
        usb2RS232.setToggleGroup(upgradeWayGroup);
        usb2RS232.setUserData("rs232");
        vbox.getChildren().add(usb2RS232);
        hbox1.getChildren().add(vbox);
    }

    private HBox createHbox(){
        HBox hbox = new HBox();
        hbox.setSpacing(5.0);
        return hbox;
    }

    private JFXComboBox createComboBox() {
        JFXComboBox combo = new JFXComboBox();
        combo.setPrefWidth(120.0);
        return combo;
    }

    private JFXButton createButton(String text) {
        JFXButton btn = new JFXButton(text);
        btn.setPrefWidth(180.0);
        return btn;
    }

    /**
     * 显示文本
     *
     * @param text
     * @return
     */
    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setPrefWidth(60.0);
        label.setAlignment(Pos.CENTER_RIGHT);
        return label;
    }

    private class Controller{
        private SerialPortService serialPortService;
        private SerialPortCheckService serialPortCheckService;
        private String selectedPortName;
        public void init(){
          initPort();

        }

        private void initPort(){
            serialPortCheckService = new SerialPortCheckService();
            serialPortCheckService.setPeriod(Duration.seconds(1));
            serialPortCheckService.start();
            serialPortCheckService.valueProperty().addListener(new ChangeListener<List<Map<String, String>>>() {
                @SneakyThrows
                @Override
                public void changed(ObservableValue<? extends List<Map<String, String>>> observableValue, List<Map<String, String>> oldValue, List<Map<String, String>> newValue) {
                    List<Map<String, String>> portList = oldValue != null ? oldValue : newValue;
                    portCombo.getItems().clear();
                    for (Map<String, String> stringMap : portList) {
                        Iterator<Entry<String, String>> iterator = stringMap.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Entry<String, String> next = iterator.next();
                            String value = next.getValue();
                            portCombo.getItems().add(value);
                            if(value.equals(selectedPortName)){
                                portCombo.getSelectionModel().selectLast();
                            }
                        }
                    }
                }
            });
            // 记住选中的串口
            portCombo.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                    if(newValue!=null){
                        selectedPortName=newValue;
                    }
                }
            });

            portCombo.getSelectionModel().selectFirst();
            selectedPortName=portCombo.getValue();
        }

        private class SerialPortCheckService extends ScheduledService<List<Map<String, String>>> {
            @Override
            protected Task<List<Map<String, String>> > createTask() {
                return new Task<List<Map<String, String>>>() {
                    @Override
                    protected List<Map<String, String>> call() throws Exception {
                        Optional<List<Map<String, String>>> maps = serialPortService.listAllPorts();
                        List<Map<String, String>> maps1 = maps.get();
                        return maps1;
                    }
                };
            }

        }
    }
}
