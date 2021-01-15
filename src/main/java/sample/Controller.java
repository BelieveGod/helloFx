package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import sample.service.SerialPortService;
import sample.support.AgxResult;
import sample.support.PortParam;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

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

    private final SerialPortService serialPortService=new SerialPortService();


    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("controller:"+Thread.currentThread().getName());
        Optional<List<Map<String, String>>> maps = serialPortService.listAllPorts();
        List<Map<String, String>> maps1 = maps.get();
        for (Map<String, String> stringMap : maps1) {
            Iterator<Entry<String, String>> iterator = stringMap.entrySet().iterator();
            while (iterator.hasNext()){
                Entry<String, String> next = iterator.next();
                System.out.println(next.getKey()+"->"+next.getValue());
                portBox.getItems().add(next.getValue());
            }
        }

        portBox.getSelectionModel().selectFirst();

    }


    // When user click on myButton
    // this method will be called.
    public void showDateTime(ActionEvent event) {
        System.out.println("Button Clicked!");
        System.out.println("Thread.currentThread().getName() = " + Thread.currentThread().getName());
        Thread currentThread = Thread.currentThread();
        Date now= new Date();

        DateFormat df = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss");
        String dateTimeString = df.format(now);
        // Show in VIEW
//        textArea.setText(dateTimeString);

        Timer timer=new Timer("定时器");
        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
                System.out.println("Thread.currentThread().getName() = " + Thread.currentThread().getName());
                System.out.println("currentThread.getName() = " + currentThread.getName());
                textArea.setText(dateTimeString);
            }
        };
        timer.schedule(timerTask,1000);


        new Thread(()->{
            textArea.setText("新线程");

        },"新线程").start();


    }

    /**
     * todo
     * @param event
     */
    public void onOpenSerialPort(ActionEvent event){
        String value = portBox.getValue();
        System.out.println("value = " + value);
        log.info("value={}",value);
        PortParam portParam=new PortParam();
        portParam.setPortName(value);
        final int bauldRate=460800;
        portParam.setBauldRate(bauldRate);
        portParam.setDataBits(PortParam.DATABITS_8);
        portParam.setStopBits(PortParam.STOPBITS_1);
        portParam.setParity(PortParam.PARITY_NONE);

        AgxResult agxResult = serialPortService.openSerialPort(portParam);
        if(agxResult.getCode().equals(200)){
            textArea.appendText("打开串口成功\n");
        }else{
            textArea.appendText("打开串口失败\n");
        }

        Timer timer=new Timer("定时器");
        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
                System.out.println("Thread.currentThread().getName() = " + Thread.currentThread().getName());
                AgxResult agxResult1 = serialPortService.closeSeriaPort();
                if (agxResult.getCode().equals(200)) {
                    textArea.appendText("\n关闭串口成功");

                }else{
                    textArea.appendText("\n关闭串口失败");
                }
            }
        };
        timer.schedule(timerTask,1000);

    }

    /**
     * todo
     * @param event
     */
    public void onLoadFile(ActionEvent event){
        System.out.println("加载文件");
    }

    /**
     * todo
     * @param event
     */
    public void onStartUpgrade(ActionEvent event){
        System.out.println("开始升级");
    }


}
