package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Window;
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

    private CanStatus canStatus = CanStatus.getInstance();



    private final SerialPortService serialPortService = new SerialPortService();

    private final CanService canService=new CanService();


    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("controller:" + Thread.currentThread().getName());
        Optional<List<Map<String, String>>> maps = serialPortService.listAllPorts();
        List<Map<String, String>> maps1 = maps.get();
        for (Map<String, String> stringMap : maps1) {
            Iterator<Entry<String, String>> iterator = stringMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, String> next = iterator.next();
                System.out.println(next.getKey() + "->" + next.getValue());
                portBox.getItems().add(next.getValue());
            }
        }

        portBox.getSelectionModel().selectFirst();

    }


    /**
     * todo
     *
     * @param event
     */
    public void onOpenSerialPort(ActionEvent event) {
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

        AgxResult agxResult = serialPortService.openSerialPort(portParam);
        if (agxResult.getCode().equals(200)) {
            textArea.appendText("打开串口成功\n");
        } else {
            textArea.appendText("打开串口失败\n");
        }

        // can
        canService.SendExcuteCMD(canStatus);

    }

    /**
     * todo
     *
     * @param event
     */
    public void onLoadFile(ActionEvent event) throws IOException {

        Node source = (Node) event.getSource();
        Window window = source.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择升级文件");
        File file = fileChooser.showOpenDialog(window);
        if (file == null) {
            System.out.println("未选择");
            return;
        }
        System.out.println("file.getAbsoluteFile() = " + file.getAbsoluteFile());
        byte[] bytes1 = FileUtils.readFileToByteArray(file.getAbsoluteFile());
        ArrayList<Byte> fileBytes = new ArrayList<>();
        fileBytes.addAll(Arrays.asList(ByteUtils.boxed(bytes1)));
        canStatus.version = getVersion(fileBytes);
        canStatus.data = fileBytes.subList(canStatus.version.size(), fileBytes.size());
        canStatus.nodeId = (byte)getNodeId(canStatus.version).intValue();
        System.out.println("字节数:" + canStatus.data.size());
        System.out.println(ByteUtils.getString(canStatus.version));
        System.out.println();

        // todo 判断读取数据是否为升级文件
        System.out.println("读取完毕");
        fileBytes.clear();
    }

    /**
     * todo
     *
     * @param event
     */
    public void onStartUpgrade(ActionEvent event) {
        System.out.println("开始升级");
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
}
