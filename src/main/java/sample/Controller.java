package sample;

import cn.hutool.http.HttpUtil;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import sample.can.CanStatus;
import sample.service.CanService;
import sample.service.SerialPortService;
import sample.support.PortParam;
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
    public ComboBox<String> operateTypeBox;
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

    private CanStatus canStatus = CanStatus.getInstance();

    private String selectedPortName;



    private final SerialPortService serialPortService = new SerialPortService();

    private final CanService canService=new CanService(serialPortService,canStatus);


    public void initialize(URL url, ResourceBundle resourceBundle) {
        canStatus.controller=this;
        executorService = Executors.newCachedThreadPool(runable->{
            Thread t = new Thread(runable);
            t.setDaemon(true);
            return t;
        });
        serialPortCheckService=new SerialPortCheckService();
        serialPortCheckService.setExecutor(executorService);
        serialPortCheckService.setPeriod(Duration.seconds(1));
        serialPortCheckService.start();
        ObservableList<String> operateTypeList = FXCollections.<String>observableArrayList();
        operateTypeList.addAll("USB转RS232", "USB转CAN");
        operateTypeBox.setItems(operateTypeList);

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

        // 按钮组
        connectBtn.disableProperty().bind(canStatus.isLoadFile.not());
        disConnectBtn.visibleProperty().bind(connectBtn.visibleProperty().not());
        upgrateBtn.disableProperty().bind(connectBtn.visibleProperty());
        disConnectBtn.visibleProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                loadBtn.setDisable(newValue);
                loadBtn2.setDisable(newValue);
            }
        });
        loadBtn.setDisable(false);
        loadBtn2.setDisable(false);
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

                return canService.Connect(canStatus, portParam);
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
        task.messageProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {

                textArea.appendText(newValue);
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
                canService.disConnect();

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
                byte[] bytes1 = FileUtils.readFileToByteArray(file.getAbsoluteFile());
                ArrayList<Byte> fileBytes = new ArrayList<>();
                fileBytes.addAll(Arrays.asList(ByteUtils.boxed(bytes1)));
                canStatus.version = getVersion(fileBytes);

                canStatus.data = fileBytes.subList(canStatus.version.size(), fileBytes.size());

                try {
                    canStatus.nodeId = (byte) getNodeId(canStatus.version).intValue();
                } catch (Exception e) {
                    this.updateMessage("\n加载的不是升级文件，请重新加载");
                    this.failed();
                    return false;
                }

                StringBuilder builder = new StringBuilder();
                builder.append("\n加载的文件："+file.getAbsoluteFile())
                       .append("\n字节数:" + canStatus.data.size())
                       .append("\n升级固件版本为:"+ByteUtils.getString(canStatus.version))
                       .append("\n升级的设备节点号:"+(canStatus.nodeId &0xff))
                       .append("\n\n加载完毕，可以开始升级固件！");
                this.updateMessage(builder.toString());
                System.out.println("字节数:" + canStatus.data.size());
                System.out.println(ByteUtils.getString(canStatus.version));
                System.out.println();

                // todo 判断读取数据是否为升级文件
                System.out.println("读取完毕");
                return true;
            }
        };

        executorService.submit(task);

        task.valueProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oleValue, Boolean newValue) {
                loadBtn.setDisable(false);
                canStatus.isLoadFile.set(newValue);
            }
        });
        task.messageProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                textArea.appendText(newValue);
            }
        });
    }


    public void onLoadFileNetwork(ActionEvent event){
        Task<Boolean> task=new Task<>(){
            @Override
            protected Boolean call() throws Exception {
                byte[] bytes1=HttpUtil.downloadBytes("http://localhost/file/BFF-V2.0-3-0-g8c21336(4850驱动器).bin");
                ArrayList<Byte> fileBytes = new ArrayList<>();
                fileBytes.addAll(Arrays.asList(ByteUtils.boxed(bytes1)));
                canStatus.version = getVersion(fileBytes);
                canStatus.data = fileBytes.subList(canStatus.version.size(), fileBytes.size());
                canStatus.nodeId = (byte) getNodeId(canStatus.version).intValue();
                System.out.println("字节数:" + canStatus.data.size());
                System.out.println(ByteUtils.getString(canStatus.version));
                System.out.println();

                // todo 判断读取数据是否为升级文件
                System.out.println("读取完毕");
                textArea.appendText("读取完毕");
                progressBar.setProgress(100);
                this.succeeded();
                return null;
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

        task.messageProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                textArea.appendText(newValue);
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
                boolean flag = canService.upgrade(canStatus);
                comboxPane.setDisable(false);
                buttonPane.setDisable(false);
                return flag;
            }
        };

        task.valueProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
//                disConnectBtn.fire();
            }
        });


        executorService.submit(task);

    }


    private List<Byte> getVersion(List<Byte> fileByte) {
        for (int i = 0; i < fileByte.size(); i++) {

            if (i > 1) {
                if (fileByte.get(i - 1) == (0x0d) && fileByte.get(i) == (0x0a)) {
                    return fileByte.subList(0, i + 1);
                }
            }
        }
        return new ArrayList<>();
    }

    private Integer getNodeId(List<Byte> version) throws NumberFormatException{
        String s = ByteUtils.getString(version);
        String substring = s.substring(1, 3);

        Integer integer = null;
        integer = Integer.valueOf(substring, 16);

        return integer;


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
