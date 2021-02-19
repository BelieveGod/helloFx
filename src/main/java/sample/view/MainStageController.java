package sample.view;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.util.StringConverter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import sample.Main;
import sample.OperateStrategy;
import sample.can.CanContext;
import sample.can.CanStrategy;
import sample.component.FileChooseOnNet;
import sample.enumeration.OperationType;
import sample.service.SerialPortService;
import sample.support.PortParam;
import sample.uart.UartContext;
import sample.uart.UartStrategy;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class MainStageController implements Initializable {
    @FXML
    public GridPane comboxPane;
    @FXML
    public VBox buttonPane;
    @FXML
    public TextArea textArea;
    @FXML
    public ComboBox<String> portBox;
    @FXML
    public ComboBox<OperationType> operateTypeBox;
    @FXML
    public Button loadBtn;
    @FXML
    public Button loadBtn2;
    @FXML
    public Button connectBtn;
    @FXML
    public Button disConnectBtn;
    @FXML
    public Button upgrateBtn;
    @FXML
    public ProgressBar progressBar;
    @FXML
    public Label progressLabel;
    @FXML
    public ImageView imageView;

    public ChangeListener<Boolean> disConnectBtnListener=null;
    public ChangeListener<Boolean> connectBtnListener=null;
    private ExecutorService executorService;
    private SerialPortCheckService serialPortCheckService;
    private CanContext canContext;
    private UartContext uartContext;
    private String selectedPortName;
    public SimpleBooleanProperty isLoadFile=new SimpleBooleanProperty(false);
    public SimpleBooleanProperty isConnecting=new SimpleBooleanProperty(false);
    private final SerialPortService serialPortService = new SerialPortService();
    private OperateStrategy operateStrategy;;
    // 主窗口引用
    private Stage primaryStage;
    // App 引用
    private Main main;


/*================================= 方法区 ===========================================================================================*/
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    /**
     * 控制器初始化
     * @param url 绑定的fxml url
     * @param resourceBundle 国际化资源的绑定
     */
    public void initialize(URL url, ResourceBundle resourceBundle) {
        imageView.setImage(new Image("/image/logo_s.png"));
        executorService = Executors.newCachedThreadPool(runable->{
            Thread t = new Thread(runable);
            t.setDaemon(true);
            return t;
        });

        // 定时扫描更新串口的变化情况
        serialPortCheckService=new SerialPortCheckService();
        serialPortCheckService.setExecutor(executorService);
        serialPortCheckService.setPeriod(Duration.seconds(1));
        serialPortCheckService.start();
        serialPortCheckService.valueProperty().addListener(new ChangeListener<List<Map<String, String>>>() {
            @SneakyThrows
            @Override
            public void changed(ObservableValue<? extends List<Map<String, String>>> observableValue, List<Map<String, String>> oldValue, List<Map<String, String>> newValue) {
                List<Map<String, String>> portList = oldValue != null ? oldValue : newValue;
                portBox.getItems().clear();
                for (Map<String, String> stringMap : portList) {
                    Iterator<Entry<String, String>> iterator = stringMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Entry<String, String> next = iterator.next();
                        String value = next.getValue();
                        portBox.getItems().add(value);
                        if(value.equals(selectedPortName)){
                            portBox.getSelectionModel().selectLast();
                        }
                    }
                }
            }
        });


        // 记住选中的串口
        portBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                if(newValue!=null){
                    selectedPortName=newValue;
                }
            }
        });

        portBox.getSelectionModel().selectFirst();
        selectedPortName=portBox.getValue();


        // 升级工具选择控件的类型转换器
        operateTypeBox.setConverter(new StringConverter<OperationType>() {
            @Override
            public String toString(OperationType operationType) {
                return operationType.getChinese();
            }

            @Override
            public OperationType fromString(String s) {
                return OperationType.get(s);
            }
        });

        ObservableList<OperationType> operateTypeList = FXCollections.observableArrayList();
        operateTypeList.addAll(OperationType.CAN, OperationType.RS232);
        operateTypeBox.setItems(operateTypeList);
        operateTypeBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<OperationType>() {
            @Override
            public void changed(ObservableValue<? extends OperationType> observableValue, OperationType oldValue, OperationType newValue) {
                if(newValue==null){
                    return;
                }
                // 切换之前，清理之前的痕迹
                serialPortService.closeSeriaPort();
                isLoadFile.set(false);

                switch (newValue){
                case RS232:
                    uartContext=new UartContext();
//                    operateStrategy= new UartStrategy(serialPortService, uartContext, MainStageController.this);
                    break;
                case CAN:
                    canContext=new CanContext();
//                    operateStrategy= new CanStrategy(serialPortService, canContext, MainStageController.this);
                    break;
                }

                operateStrategy.initUI(MainStageController.this);
            }
        });
        operateTypeBox.getSelectionModel().selectFirst();

    }


    /**
     * 连接设备
     *
     * @param event
     */
    public void onConnectDevice(ActionEvent event) {
        isConnecting.set(true);
        progressBar.setProgress(0);
        progressLabel.setText("0%");
        String value = portBox.getValue();
        log.info("value={}", value);
        PortParam portParam = new PortParam();
        portParam.setPortName(value);
        final int bauldRate = 460800;
        portParam.setBauldRate(bauldRate);
        portParam.setDataBits(PortParam.DATABITS_8);
        portParam.setStopBits(PortParam.STOPBITS_1);
        portParam.setParity(PortParam.PARITY_NONE);
        textArea.appendText("\n正在连接，请稍后.....");

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
                isConnecting.set(false);
            }
        });
        executorService.submit(task);
    }

    /**
     * 断开连接
     */
    public void onDisconnectDevice(ActionEvent event){
        toogleConnectBtn();
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                operateStrategy.disconnect();
                return null;
            }
        };
        executorService.submit(task);
    }

    /**
     * 加载本地文件
     *
     * @param event
     */
    public void onLoadFile(ActionEvent event) throws IOException {
        loadBtn.setDisable(true);
        loadBtn2.setDisable(true);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择升级文件");
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file == null) {
            log.info("未选择");
            loadBtn.setDisable(false);
            loadBtn2.setDisable(false);
            return ;
        }

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return operateStrategy.loadFile(file);
            }
        };
        executorService.submit(task);
        task.valueProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oleValue, Boolean newValue) {
                loadBtn.setDisable(false);
                loadBtn2.setDisable(false);
                isLoadFile.set(newValue);
            }
        });
    }


    /**
     * 加载网络文件
     * @param event
     */
    public void onLoadFileNetwork(ActionEvent event){

        FileChooseOnNet fileChooseOnNet = new FileChooseOnNet();
        fileChooseOnNet.setTitle("选择升级文件");
        String url = fileChooseOnNet.showOpenDialog(primaryStage);
        if (url == null) {
            log.info("未选择");
            loadBtn.setDisable(false);
            loadBtn2.setDisable(false);
            return ;
        }
        Task<Boolean> task=new Task<>(){
            @Override
            protected Boolean call() throws Exception {
                return operateStrategy.loadFileOnNet(url);
            }
        };
        executorService.submit(task);

        task.valueProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oleValue, Boolean newValue) {
                loadBtn.setDisable(false);
                loadBtn2.setDisable(false);
                isLoadFile.set(newValue);
            }
        });


    }

    /**
     * 升级设备
     *
     * @param event
     */
    public void onStartUpgrade(ActionEvent event) {
        comboxPane.setDisable(true);
        buttonPane.setDisable(true);
        log.info("开始升级");
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                boolean flag = operateStrategy.upgrade();
                comboxPane.setDisable(false);
                buttonPane.setDisable(false);
                return flag;
            }
        };
        // 成功弹出窗口提示
        task.valueProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                disConnectBtn.fire();
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
        executorService.submit(task);
    }

    private void toogleConnectBtn(){
        connectBtn.visibleProperty().setValue(!connectBtn.isVisible());
    }

    private class SerialPortCheckService extends ScheduledService<List<Map<String, String>>>{
        @Override
        protected Task<List<Map<String, String>> >createTask() {
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

    @FXML

    private void onHyberLink(ActionEvent event){
        final Hyperlink link = (Hyperlink)event.getSource();
        final String url = link.getText();
        main.getHostServices().showDocument(url);
    }
}
