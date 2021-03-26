package sample.component;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.svg.SVGGlyph;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import javafx.stage.Window;
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
import sample.model.AgxFile;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;

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

        // todo 测试
        JFXButton button = new JFXButton("返回");
        Button button1 = new Button("嵌套");
        SVGPath svgPath=new SVGPath();
        svgPath.setFill(Color.BLUE);
        svgPath.setContent("m208.5,80.45313c-8,6 -25.13702,13.67206 -47,23c-18.1176,7.72997 -29.23738,15.73766 -37,23c-3.09818,2.8985 -4,6 -5,9c-1,3 -0.01624,11.01277 11,25c14.97795,19.0174 31.47818,38.47775 48,56c12.61253,13.37625 27.17239,26.79065 43,40c7.07832,5.90741 10.61731,9.07611 11,10c0.5412,1.30655 2.4568,-3.88531 4,-11c4.28691,-19.76418 8.22777,-44.92599 13,-60c3.15112,-9.95341 5,-15 6,-15c1,0 9.00906,0.92987 17,2c14.017,1.87714 25.98669,5.20776 40,6c23.98251,1.35585 39.01035,1.86458 53,3c11.00916,0.89351 21,2 28,2c4,0 10.05551,0.51059 21,2c11.07822,1.50761 17,2 19,2c1,0 1,-1 1,-4c0,-6 -0.36252,-12.00822 0,-20c0.50052,-11.03401 5.7847,-26.52368 11,-36c4.74869,-8.62845 6.4588,-12.69344 7,-14c0.38269,-0.92388 -3.96198,-1.15871 -12,-3c-23.89636,-5.47399 -59.95663,-11.25107 -94,-17c-30.96225,-5.22863 -47,-6 -56,-6c-1,0 -3.49829,-5.93797 -4,-12c-1.15472,-13.9523 -1.90927,-26.31513 1,-38c1.84,-7.39016 4,-11 4,-13c0,-1 0.23462,-2.15224 1,-4c0.5412,-1.30656 1,-2 1,-3c0,-1 -0.01459,-2.29561 -3,-2c-5.07422,0.50245 -17.18228,5.59136 -31,12c-16.22809,7.52657 -32,16 -44,22c-10,5 -18.37201,7.38509 -22,10c-2.29454,1.65381 -4.4588,3.69344 -5,5c-0.38269,0.92388 -0.4588,2.69344 -1,4c-1.14806,2.77164 0,6 0,7c0,1 0,2 0,3c0,1 0,3 0,5l0,1");
        Region region = new Region();
        region.setShape(svgPath);
        region.setMinSize(100,100 );
        region.setPrefSize(100, 100);
        region.setMaxSize(100, 100);
        region.setBackground(new Background(new BackgroundFill(Color.GREEN,null,null)));

        button.setTooltip(new Tooltip("返回"));
//        button.setGraphic(region);
        button.setShape(svgPath);
        button1.setShape(svgPath);
//        ((BorderPane) this.getParent()).setShape(svgPath);
        this.setShape(svgPath);
        vbox1.getChildren().addAll(stackPane, checkUpgrateBtn,button,button1);

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

        private BlockingQueue<Object> blockingQueue=new LinkedBlockingDeque<>();

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
            try {
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

                    String s = HttpUtil.get("localhost:8080/firmware/checkVersion",paramMap);
                    System.out.println("s = " + s);
                    JSONObject jsonObject = JSONUtil.parseObj(s);
                    AgxFile agxFile = new AgxFile();
                    agxFile.setName(jsonObject.getStr("name"));
                    agxFile.setUrl(jsonObject.getStr("url"));
                    agxFile.setInfo(jsonObject.getStr("info"));

                    blockingQueue.offer(agxFile);
                }).start();

                // 2. 弹窗显示信息，询问用户是否升级
                AgxFile agxFile = ((AgxFile) blockingQueue.take());
                ConfirmUpgrade dialog=new ConfirmUpgrade(agxFile);
                Window window = LeftPane.this.getScene().getWindow();
                Boolean aBoolean = dialog.showOpenDialog(window);
                System.out.println("aBoolean = " + aBoolean);


                // 2.1 如果用户取消，则停止
                if(!aBoolean){
                    return ;
                }
                // 2.2 如果用户确定，则加载联网的文件
//                byte[] bytes1= HttpUtil.downloadBytes(agxFile.getUrl());
//                System.out.println("bytes1.length = " + bytes1.length);
                // 2.4 加载完成后,解析文件
                Task<Boolean> task=new Task<>(){
                    @Override
                    protected Boolean call() throws Exception {
                        Boolean flag= operateStrategy.loadFileOnNet(agxFile.getUrl());
                        if(!flag){
                            // 加载文件失败就跳出
                            return flag;
                        }
                        // 2.5 解析完升级
                        flag=operateStrategy.upgrade();

                        return flag;
                    }
                };

                // 成功弹出窗口提示
                task.valueProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
//                        disConnectBtn.fire();
                        if(newValue){
                            Alert alert = new Alert(AlertType.INFORMATION, "升级成功");
                            alert.showAndWait().ifPresent(response->{
                            });
                        }else{
                            Alert alert = new Alert(AlertType.INFORMATION, "升级失败");
                            alert.showAndWait().ifPresent(response->{
                            });
                        }
                    }
                });
                mainWin.executorService.submit(task);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

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
