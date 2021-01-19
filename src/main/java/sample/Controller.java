package sample;

import cn.hutool.http.HttpUtil;
import javafx.beans.property.SimpleIntegerProperty;
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
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import sample.can.CanStatus;
import sample.service.CanService;
import sample.service.SerialPortService;
import sample.support.AgxResult;
import sample.support.PortParam;
import sample.util.ByteUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class Controller implements Initializable {

    @FXML
    private Button myButton;

    @FXML
    private TextField myTextField;

    @FXML
    private TextArea textArea;

    @FXML
    private ComboBox<String> portBox;

    @FXML
    private ComboBox<String> operateTypeBox;



    @FXML
    private Button loadBtn;

    @FXML
    private Button loadBtn2;

    @FXML
    private Button openBtn;

    @FXML
    private Button upgrateBtn;

    @FXML
    private ProgressBar progressBar;

    private ExecutorService executorService;

    private SerialPortCheckService serialPortCheckService;

    private CanStatus canStatus = CanStatus.getInstance();

    private String selectedPortName;



    private final SerialPortService serialPortService = new SerialPortService();

    private final CanService canService=new CanService(serialPortService);


    public void initialize(URL url, ResourceBundle resourceBundle) {
        executorService = Executors.newCachedThreadPool();
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
        openBtn.setDisable(true);
        loadBtn.setDisable(false);
        upgrateBtn.setDisable(true);
        loadBtn2.setDisable(false);
    }


    /**
     * todo
     *
     * @param event
     */
    public void onOpenSerialPort(ActionEvent event) {


        openBtn.setDisable(true);
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

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {

                return canService.Connect(canStatus, portParam);
            }
        };

        task.valueProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean neWValue) {
//                System.out.println("oldValue = " + oldValue);
//                System.out.println("neWValue = " + neWValue);
                openBtn.setDisable(neWValue);
                upgrateBtn.setDisable(!neWValue);
            }
        });
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

        Task<Void> task = new Task<>() {
            @Override

            protected Void call() throws Exception {
                System.out.println("onLoadFile = " + Thread.currentThread().getName());
                try {
                    openBtn.setDisable(true);
                    progressBar.setProgress(50.0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                byte[] bytes1 = FileUtils.readFileToByteArray(file.getAbsoluteFile());
                ArrayList<Byte> fileBytes = new ArrayList<>();
                fileBytes.addAll(Arrays.asList(ByteUtils.boxed(bytes1)));
                canStatus.version = getVersion(fileBytes);
                canStatus.data = fileBytes.subList(canStatus.version.size(), fileBytes.size());
                canStatus.nodeId = (byte) getNodeId(canStatus.version).intValue();

                this.updateMessage("\n加载的文件："+file.getAbsoluteFile());
                this.updateMessage("\n字节数:" + canStatus.data.size());
                this.updateMessage("\n升级固件版本为:"+ByteUtils.getString(canStatus.version));
                this.updateMessage("\n升级的设备节点号:"+(canStatus.nodeId &0xff) );
                this.updateMessage("\n\n加载完毕，可以开始升级固件！");
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
                openBtn.setDisable(false);

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
                openBtn.setDisable(false);

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

        System.out.println("开始升级");
        // 弹出模态窗口 todo
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return canService.upgrade(canStatus);
            }
        };

        task.valueProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {

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

    private Integer getNodeId(List<Byte> version) {
        String s = ByteUtils.getString(version);
        String substring = s.substring(1, 3);
        return Integer.valueOf(substring, 16);


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
}
