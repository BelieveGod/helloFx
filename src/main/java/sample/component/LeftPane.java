package sample.component;

import cn.hutool.http.HttpUtil;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Duration;
import lombok.SneakyThrows;
import sample.OperateStrategy;
import sample.can.CanContext;
import sample.can.CanStrategy;
import sample.common.Colleague;
import sample.common.Constant;
import sample.common.Mediator;
import sample.enumeration.CanNode;
import sample.enumeration.OperationType;
import sample.service.SerialPortService;
import sample.support.PortParam;
import sample.uart.UartContext;
import sample.uart.UartStrategy;
import sample.util.ByteUtils;
import sample.util.HexUtils;
import sample.view.MainStageController;

import java.util.HashMap;
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
public class LeftPane extends VBox implements Colleague {

    private HBox hbox1;
    private ToggleGroup upgradeWayGroup;
    private RadioButton usb2Can;
    private RadioButton usb2RS232;
    private JFXComboBox<String> portCombo;
    private HBox canNodeHbox;
    private JFXComboBox<String> canNodeCombo;
    private JFXComboBox chassisCombo;
    // 按钮组
    private VBox vbox1;
    private StackPane stackPane;
    private JFXButton connectBtn;
    private JFXButton disConnectBtn;
    private JFXButton checkUpgrateBtn;

    private Controller controller;
    private final MainWin mainWin;
    private Map<String, Callback<Object[],Void>> funMap = new HashMap<>();
    private Mediator mediator;

    public LeftPane(MainWin mainWin) {
        super();
        this.mainWin=mainWin;
        // 注册
        ((Mediator)mainWin).register(this);
        init();
        this.setSpacing(5);
    }

    public void init(){
        initUpgradeWay();
        initPort();
        initCanNode();
        initChassis();
        initButtons();
        this.getChildren().addAll(hbox1, portCombo.getParent(), canNodeCombo.getParent(), chassisCombo.getParent(),vbox1);
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
        canNodeCombo.getItems().addAll(CanNode.MASTER.getNodeName(), CanNode.MOTOR.getNodeName());
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
        usb2Can = new RadioButton(OperationType.CAN.getChinese());

        usb2Can.setToggleGroup(upgradeWayGroup);
        usb2Can.setUserData("can");
        vbox.getChildren().add(usb2Can);
        usb2RS232 = new RadioButton(OperationType.RS232.getChinese());
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
        private SerialPortService serialPortService= new SerialPortService();;
        private SerialPortCheckService serialPortCheckService;
        private String selectedPortName;
        private OperateStrategy operateStrategy;
        private CanContext canContext;
        private UartContext uartContext;

        public void init(){
          initPort();
          initEvent();
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

        // todo 初始化控件的事件
        private void initEvent(){
            initUpgradeWayToogle();
            initConnectBtn();
            initCanNodeCombo();
            initCheckUpgrateBtn();
        }

        private void initCanNodeCombo() {
            canNodeCombo.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                    canContext.nodeId=(byte)CanNode.getCanNode(newValue).getNodeId();
                }
            });

        }
        private void toogleConnectBtn(){
            connectBtn.visibleProperty().setValue(!connectBtn.isVisible());
        }

        // todo 初始化升级方式切换的逻辑
        private void initUpgradeWayToogle(){
            usb2Can.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                    if (usb2Can.isSelected()) {
                        System.out.println("选中USB2CAN");
                        // 切换之前，清理之前的痕迹
                        serialPortService.closeSeriaPort();
                        mainWin.isLoadFile.set(false);
                        canNodeCombo.getParent().setVisible(true);
                        chassisCombo.getParent().setVisible(true);
                        canContext=new CanContext();
                        operateStrategy= new CanStrategy(serialPortService, canContext,LeftPane.this);


                        // todo 切换后的按钮逻辑

                    }
                }
            });

            usb2RS232.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                    if (usb2RS232.isSelected()) {
                        System.out.println("选中usb2RS232");
                        serialPortService.closeSeriaPort();
                        mainWin.isLoadFile.set(false);
                        canNodeCombo.getParent().setVisible(false);
                        chassisCombo.getParent().setVisible(false);
                        uartContext=new UartContext();
                        operateStrategy= new UartStrategy(serialPortService, uartContext,LeftPane.this);

                        // todo 切换后的按钮逻辑
                    }
                }
            });

            usb2Can.setSelected(true); // 默认选中usb转can方式
        }

        private void initConnectBtn() {
            connectBtn.setOnAction(this::onConnect);
        }
        public void onConnect(ActionEvent event) {

            mainWin.isConnecting.set(true);
            // todo 改造成中介者模式
            send(Constant.CONNECT);
//                    progressBar.setProgress(0);
//                    progressLabel.setText("0%");
//                    String value = portBox.getValue();
            String value = portCombo.getValue();
            PortParam portParam = new PortParam();
            portParam.setPortName(value);
            final int bauldRate = 460800;
            portParam.setBauldRate(bauldRate);
            portParam.setDataBits(PortParam.DATABITS_8);
            portParam.setStopBits(PortParam.STOPBITS_1);
            portParam.setParity(PortParam.PARITY_NONE);
//                    textArea.appendText("\n正在连接，请稍后.....");
            send(Constant.LOG,"\n正在连接，请稍后.....");
            Task<Boolean> task = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return operateStrategy.connect(portParam);
                }
            };

            task.valueProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                    if(newValue){
                        toogleConnectBtn();
                    }
                    mainWin.isConnecting.set(false);
                }
            });
            mainWin.executorService.submit(task);
        }

        // todo
        private void initCheckUpgrateBtn(){
            checkUpgrateBtn.setOnAction(this::onCheckUpgrateBtn);
        }

        // todo
        public void onCheckUpgrateBtn(ActionEvent event){
            // 1. 发送当前版本信息、升级方式、can节点、车型等到服务器获取最新版本信息
            String text = ((RadioButton) upgradeWayGroup.getSelectedToggle()).getText();
            OperationType operationType = OperationType.get(text);
            HashMap<String, Object> paramMap = new HashMap<>();
            paramMap.put("upgradeWay", text);
            switch (operationType){
                case CAN:
                    paramMap.put("version", new String(canContext.getVersionBuf));
                    break;
                case RS232:
                    paramMap.put("version", new String(ByteUtils.deBoxed(uartContext.get_Version)));
                    break;
            }
//            String s = HttpUtil.get("localhost:8080/firmware/checkVersion", paramMap);
            new Thread(()->{

                String s = HttpUtil.get("www.baidu.com");
            }).start();

            // 2. 弹窗显示信息，询问用户是否升级

            // 2.1 如果用户取消，则停止

            // 2.2 如果用户确定，则加载联网的文件

            // 2.4 加载完成后，进行升级
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
