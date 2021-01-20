package sample;

import cn.hutool.http.HttpUtil;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.util.StringConverter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import sample.can.CanContext;
import sample.can.CanStrategy;
import sample.enumeration.OperationType;
import sample.service.SerialPortService;
import sample.support.PortParam;
import sample.uart.UartContext;
import sample.uart.UartStrategy;
import sample.util.ByteUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class Controller implements Initializable {

    @FXML
    public GridPane comboxPane;
    @FXML
    public VBox buttonPane;
    @FXML
    public StackPane connectPane;
    @FXML
    public Button myButton;
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

    private ExecutorService executorService;

    private SerialPortCheckService serialPortCheckService;

    private CanContext canContext = CanContext.getInstance();
    private UartContext uartContext = UartContext.getInstance();

    private String selectedPortName;
    public SimpleBooleanProperty isLoadFile=new SimpleBooleanProperty(false);



    private final SerialPortService serialPortService = new SerialPortService();

//    private final CanStrategy canService=new CanStrategy(serialPortService, canContext);
    private  OperateStrategy operateStrategy;;


    public void initialize(URL url, ResourceBundle resourceBundle) {
        canContext.controller=this;
        executorService = Executors.newCachedThreadPool(runable->{
            Thread t = new Thread(runable);
            t.setDaemon(true);
            return t;
        });
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
                switch (newValue){
                case RS232:
// todo 切换逻辑
                    break;
                case CAN:
                    break;
                }
            }
        });
        operateTypeBox.getSelectionModel().selectFirst();
//        operateStrategy = new CanStrategy(serialPortService,canContext,this);
        operateStrategy = new UartStrategy(serialPortService, uartContext, this);

        operateStrategy.initUI(this);

    }


    /**
     * todo
     *
     * @param event
     */
    public void onConnectDevice(ActionEvent event) {
        toogleConnectBtn();
//        connectBtn.setDisable(true);
        String value = portBox.getValue();
        System.out.println("value = " + value);
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
                if(!newValue){
                    disConnectBtn.fire();
                }
            }
        });
        executorService.submit(task);
    }

    /**
     * todo
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
     * todo
     *
     * @param event
     */
    public void onLoadFile(ActionEvent event) throws IOException {

        loadBtn.setDisable(true);
        Node source = (Node) event.getSource();
        Window window = source.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择升级文件");
        File file = fileChooser.showOpenDialog(window);
        if (file == null) {
            System.out.println("未选择");
            loadBtn.setDisable(false);
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
                isLoadFile.set(newValue);
            }
        });
    }


    public void onLoadFileNetwork(ActionEvent event){
        Task<Boolean> task=new Task<>(){
            @Override
            protected Boolean call() throws Exception {
                String url="http://localhost/file/BFF-V2.0-3-0-g8c21336(4850驱动器).bin";
                return operateStrategy.loadFileOnNet(url);
            }
        };
        executorService.submit(task);

        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                loadBtn.setDisable(false);
                progressBar.setProgress(0);
                connectBtn.setDisable(false);

            }
        });
        task.valueProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oleValue, Boolean newValue) {
                loadBtn.setDisable(false);
                isLoadFile.set(newValue);
            }
        });
    }

    /**
     * todo
     *
     * @param event
     */
    public void onStartUpgrade(ActionEvent event) {
        comboxPane.setDisable(true);
        buttonPane.setDisable(true);
        System.out.println("开始升级");
        // 弹出模态窗口 todo
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                boolean flag = operateStrategy.upgrade();
                comboxPane.setDisable(false);
                buttonPane.setDisable(false);
                return flag;
            }
        };
        task.valueProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
//                disConnectBtn.fire();
                Alert alert = new Alert(AlertType.INFORMATION, "升级成功");
                alert.showAndWait().ifPresent(response->{

                });
            }
        });
        executorService.submit(task);
    }

    private class SerialPortCheckService extends ScheduledService<List<Map<String, String>>>{
        @Override
        protected Task<List<Map<String, String>> >createTask() {
            return new Task<List<Map<String, String>>>() {
                @Override
                protected List<Map<String, String>> call() throws Exception {
//                    System.out.println("SerialPortCheckService = " + Thread.currentThread().getName());

                    Optional<List<Map<String, String>>> maps = serialPortService.listAllPorts();
                    List<Map<String, String>> maps1 = maps.get();

                    return maps1;
                }
            };
        }
    }

    private void toogleConnectBtn(){
        connectBtn.visibleProperty().setValue(!connectBtn.isVisible());
    }
}
