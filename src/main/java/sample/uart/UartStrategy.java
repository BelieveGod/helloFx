package sample.uart;

import gnu.io.SerialPort;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import sample.AbstractStrategy;
import sample.view.MainStageController;
import sample.service.SerialPortService;
import sample.support.AgxResult;
import sample.support.PortParam;
import sample.uart.dto.ACK_t;
import sample.uart.dto.HandShark_t;
import sample.uart.dto.Header_t;
import sample.uart.dto.UartCmd;
import sample.uart.enumeration.SystemStatus;
import sample.util.ByteUtils;
import sample.util.HexUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/1/20 17:25
 */
@Slf4j
public class UartStrategy extends AbstractStrategy {

    private static final int FAIL_TIME = 3;
    private final int TIMEOUT=1000;
    private SerialPortService serialPortService;
    private UartContext uartContext ;
    private UartObserver observer;

    public UartStrategy(SerialPortService serialPortService, UartContext uartContext,
                        MainStageController mainStageController) {
        this.serialPortService = serialPortService;
        this.uartContext = uartContext;
        this.uartContext.mainStageController = mainStageController;
        observer=new UartObserver(uartContext);
    }

    @Override
    public boolean connect(PortParam portParam) {
        SerialPort theSerialPort = serialPortService.getTheSerialPort();
        if(theSerialPort!=null){
            serialPortService.closeSeriaPort();
        }
        AgxResult agxResult = serialPortService.openSerialPort(portParam);
        if (agxResult.getCode().equals(200)) {
            theSerialPort = serialPortService.getTheSerialPort();
//            updateMessage("打开串口成功");
            log.info("打开串口成功\n");
        } else {
            updateMessage("打开串口失败");
            log.error("打开串口失败\n");
            return false;
        }

        serialPortService.addObserver(observer);
        boolean b =false;
        uartContext.systemStatus = SystemStatus.EHANDSHAKE;
        int failTime=0;
        do{
            b = sendAppToAckHandShake();
            failTime++;
        }while(!b && failTime<15 );

        if(!b){
            updateMessage("握手失败");
            log.error("握手失败");
            return b;
        }
        uartContext.systemStatus = SystemStatus.ENOMORE;
        sendHandShakeChassisToApp();
        updateMessage("握手成功");
        log.info("握手成功");

        final String s = new String(ByteUtils.deBoxed(uartContext.get_Version));
        log.debug("系统序列号：{}",s);
        updateMessage("系统序列号："+s);
        return b;
    }

    protected boolean paserFile(byte[] file,String url){
        ArrayList<Byte> fileBytes = new ArrayList<>();
        fileBytes.addAll(Arrays.asList(ByteUtils.boxed(file)));
        uartContext.version.addAll( getVersion(fileBytes));
        uartContext.data.addAll(fileBytes.subList(uartContext.version.size(), fileBytes.size()));
        StringBuilder builder = new StringBuilder();
        builder.append("\n加载的文件："+url)
               .append("\n字节数:" + uartContext.data.size())
               .append("\n升级固件版本为:"+ByteUtils.getString(uartContext.version))
               .append("\n\n加载完毕，可以开始升级固件！");
        log.info(builder.toString());
        this.updateMessage(builder.toString());
        return true;
    }

    @Override
    public boolean upgrade() {
       uartContext.systemStatus=SystemStatus.ETRANSMISSION;
       uartContext.totalPackageSize = uartContext.data.size() / UartCmd.Transmitted_DataSize;
       if(uartContext.data.size() % UartCmd.Transmitted_DataSize != 0){
           uartContext.totalPackageSize++;
       }
        uartContext.writteenSize=0;

       updateProgress((double)uartContext.writteenSize/uartContext.totalPackageSize);
        boolean sendResultFlag = false;
        int failTime=0;
       do{
           sendResultFlag = sendFirstFrame();
           failTime++;
       }while(!sendResultFlag&&failTime<FAIL_TIME);
       if(!sendResultFlag){
           log.error("发送第一帧失败");
           updateMessage("升级过程发送失败");
           return false;
       }
        uartContext.writteenSize++;
        updateProgress((double)uartContext.writteenSize/uartContext.totalPackageSize);

        sendResultFlag=false;
        failTime=0;
        // 循环发送到最后一帧之前
       while(uartContext.writteenSize<uartContext.totalPackageSize-1 ){
           sendResultFlag=false;
           failTime=0;
           do{
               sendResultFlag= sendFrame();
               failTime++;
           }while(!sendResultFlag&&failTime<FAIL_TIME);
           // 发送成功就更改进度
           if(!sendResultFlag){
             break;
           }
           uartContext.writteenSize++;
           updateProgress((double)uartContext.writteenSize/uartContext.totalPackageSize);
       }
        if(!sendResultFlag){
            log.error("发送帧失败");
            updateMessage("升级过程发送失败");
            return false;
        }

        sendResultFlag=false;
        failTime=0;
        do{
            try{
                sendResultFlag= sendLastFrame();
            }catch (Exception e){
                e.printStackTrace();
            }
            failTime++;
        }while(!sendResultFlag&&failTime<FAIL_TIME);
        if(!sendResultFlag){
            log.error("最后帧失败");
            updateMessage("升级过程发送失败");
            return false;
        }
        uartContext.writteenSize++;
        updateProgress((double)uartContext.writteenSize/uartContext.totalPackageSize);

        log.info("升级完成");
        updateMessage("升级完成");
        return sendResultFlag;

    }

    private boolean sendFirstFrame(){
        return sendFrame();
    }

    private boolean sendLastFrame(){
        int writtenSize=uartContext.writteenSize;
        int totalPackageSize=uartContext.totalPackageSize;
        int dataSize = UartCmd.Transmitted_DataSize;
        int count=writtenSize+1;

        Header_t header = new Header_t();
        header.header=0x55aa;
        header.count = (short) count;
        header.len=Header_t.sizeOf;
        header.flag=1;
        header.datalen = (short) (uartContext.data.size() - writtenSize * dataSize);
        for(int i=0;i<uartContext.version.size();i++){
            header.version[i]=uartContext.version.get(i);
        }

        // 添加帧头
        List<Byte> list = ByteUtils.boxedAsList(header.getBytes());

        // 添加数据
        int startPos = writtenSize * dataSize;
        list.addAll(uartContext.data.subList(startPos, uartContext.data.size()));
        // 待填充长度
        int fillLength = dataSize - header.datalen;
        byte[] fillArray = new byte[fillLength];
        Arrays.fill(fillArray, (byte) 0);
        list.addAll(ByteUtils.boxedAsList(fillArray));

        // 重新计算长度
        int size = list.size();
        Byte b1 = (byte) size;
        Byte b2=(byte)(size>>8);
        list.set(4,b1);
        list.set(5,b2);

        // 添加校验和
        int sum = getSum(list);
        list.add(Byte.valueOf((byte)sum));
        list.add(Byte.valueOf((byte)(sum>>8)));
        list.add(Byte.valueOf((byte)(sum>>16)));
        list.add(Byte.valueOf((byte)(sum>>24)));

        // 添加帧尾
        list.add(Byte.valueOf((byte)(0xfe)));

        byte[] bytes = ByteUtils.deBoxed(list);
        try {
            Thread.sleep(10);
            serialPortService.writeData(bytes, 0, bytes.length);
            ACK_t poll = uartContext.ackQueue.poll(TIMEOUT, TimeUnit.MILLISECONDS);
            if(poll==null){
                log.warn("\n超时");
                return false;
            }
            if(poll.typedef==UartCmd.ChassisToACK_Final){
                log.debug(String.format("\n接收完成 实际接收到%d帧", poll.count));
                uartContext.systemStatus = SystemStatus.ENOMORE;
                uartContext.get_Version.clear();
                uartContext.data.clear();
                uartContext.version.clear();
                return true;
            }else if(poll.typedef==UartCmd.ChassisToACK_Count){
                log.debug(String.format("\n接收到第%d帧", poll.count));
                return true;
            }else if(poll.typedef==UartCmd.ChassisToACK_LOST){
                log.error(String.format("\n丢失第%d帧", poll.count));
                return false;
            }else{
                log.warn("\n未知状态");
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean sendFrame(){
        int writtenSize=uartContext.writteenSize;
        int totalPackageSize=uartContext.totalPackageSize;
        int dataSize = UartCmd.Transmitted_DataSize;
        int count=writtenSize+1;

        Header_t header = new Header_t();
        header.header=0x55aa;
        header.count = (short) count;
        header.len=Header_t.sizeOf;
        header.flag=0;
        header.datalen = (short) dataSize;
        header.size_h = (short)(uartContext.data.size() >> 16);
        header.size_l = (short) uartContext.data.size();
        for(int i=0;i<uartContext.version.size();i++){
            header.version[i]=uartContext.version.get(i);
        }

        // 添加帧头
        List<Byte> list = ByteUtils.boxedAsList(header.getBytes());

        // 添加数据
        int startPos = writtenSize * dataSize;
        int endPos=startPos+dataSize;
        list.addAll(uartContext.data.subList(startPos, endPos));
        // 重新计算长度
        int size = list.size();
        Byte b1 = (byte) size;
        Byte b2=(byte)(size>>8);
        list.set(4,b1);
        list.set(5,b2);

        // 添加校验和
        int sum = getSum(list);
        list.add(Byte.valueOf((byte)sum));
        list.add(Byte.valueOf((byte)(sum>>8)));
        list.add(Byte.valueOf((byte)(sum>>16)));
        list.add(Byte.valueOf((byte)(sum>>24)));
        // 添加帧尾
        list.add(Byte.valueOf((byte)(0xfe)));
        byte[] bytes = ByteUtils.deBoxed(list);
        try {
            Thread.sleep(10);
            serialPortService.writeData(bytes, 0, bytes.length);
            ACK_t poll = uartContext.ackQueue.poll(TIMEOUT, TimeUnit.MILLISECONDS);
            if(poll==null){
               log.warn("\n超时");
                return false;
            }
            if(poll.typedef==UartCmd.ChassisToACK_Clear){
                log.debug("\n正在擦除 " + poll.count);
                return true;
            }else if(poll.typedef==UartCmd.ChassisToACK_Count){
                log.debug(String.format("\n接收到第%d帧", poll.count));
                return true;
            }else if(poll.typedef==UartCmd.ChassisToACK_LOST){
                log.error(String.format("\n丢失第%d帧", poll.count));
                return false;
            }else{
                log.warn("\n未知状态");
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private int getSum(List<Byte> list) {
        int sum=0;
        for (Byte aByte : list) {
            // 这里有个坑，不转换的话是负数，源C++代码是无符号的数
            int temp=(int)aByte & 0xff;
            sum+=temp;
        }
        return sum;

    }

    @Override
    public void initUI(MainStageController mainStageController) {
        // 清除上一个按钮绑定的影响,目前这样写确实耦合度太高
        mainStageController.connectBtn.disableProperty().unbind();
        mainStageController.disConnectBtn.visibleProperty().unbind();
        mainStageController.upgrateBtn.disableProperty().unbind();
        mainStageController.portBox.disableProperty().unbind();
        mainStageController.operateTypeBox.disableProperty().unbind();
        if (mainStageController.disConnectBtnListener != null) {
            mainStageController.disConnectBtn.visibleProperty().removeListener(mainStageController.disConnectBtnListener);
        }

        ChangeListener<Boolean> changeListener = mainStageController.connectBtnListener;

        if(changeListener==null) {
            changeListener = new ChangeListener<>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue,
                                    Boolean newValue) {
                    mainStageController.loadBtn.setDisable(newValue);
                    mainStageController.loadBtn2.setDisable(newValue);
                }
            };
            mainStageController.connectBtnListener=changeListener;
        }
        // 按钮组. 属性绑定以connectBtn 为基准
        // 1. 先设置好监听的
        mainStageController.connectBtn.visibleProperty().addListener(changeListener);
        // 2. 再设置属性绑定的
        mainStageController.disConnectBtn.visibleProperty().bind(mainStageController.connectBtn.visibleProperty().not());
        mainStageController.upgrateBtn.disableProperty().bind(
                mainStageController.isLoadFile.not().or(mainStageController.connectBtn.visibleProperty()));
        mainStageController.portBox.disableProperty().bind(mainStageController.connectBtn.visibleProperty().not());
        mainStageController.operateTypeBox.disableProperty().bind(mainStageController.connectBtn.visibleProperty().not());
        mainStageController.connectBtn.disableProperty().bind(mainStageController.isConnecting);
        // 3. 最后设置赋值的
        mainStageController.loadBtn.setDisable(true);
        mainStageController.loadBtn2.setDisable(true);
        }

    /**
     * todo
     * @param typedef
     */
    private void sendMsgForChassis(short typedef){
        HandShark_t handShark_t=new HandShark_t();
        handShark_t.header = (short) 0x55aa;
        handShark_t.typedef=typedef;
        handShark_t.count=0;
        Arrays.fill(handShark_t.version, (byte) 0);
        handShark_t.tailer=(short)0xfe00;

        byte[] bytes = handShark_t.getBytes();
        try {
            Thread.sleep(10);
            serialPortService.writeData(bytes,0,bytes.length);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    private boolean sendAppToAckHandShake(){
        try {
            log.debug("\nAPPToACK_HandShake:");
            sendMsgForChassis(UartCmd.APPToACK_HandShake);
            HandShark_t handShark_t = uartContext.handSharkQueue.poll(TIMEOUT, TimeUnit.MILLISECONDS);
            if(handShark_t==null){
                log.warn("\n超时");
                return false;
            }
            if (handShark_t.typedef == UartCmd.ChassisToACK_HandShake) {
                Byte[] bytes3 = ArrayUtils.toObject(handShark_t.version);
                uartContext.get_Version.addAll(Arrays.asList(bytes3));
                // 为了代码简写一点
                final List<Byte> get_version = uartContext.get_Version;
                log.debug("get version=" + HexUtils.bytesToHexString(get_version));
                for (int i = 0; i < get_version.size(); i++) {
                    if (i > 1) {
                        if (get_version.get(i - 1) == (byte) 0x0d && get_version.get(i) == (byte) 0x0a) {
                            final List<Byte> temp = new ArrayList<>();
                            temp.addAll(uartContext.get_Version.subList(0, i + 1));
                            uartContext.get_Version.clear();
                            uartContext.get_Version.addAll(temp);
                            log.debug("get version=" + HexUtils.bytesToHexString(uartContext.get_Version));
                        }
                    }
                }
                uartContext.systemStatus = SystemStatus.ENOMORE;
                observer.clearMessage();
                return true;
            }else{
                return false;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void sendHandShakeChassisToApp(){
        log.debug("\nsendHandShakeChassisToApp:");
        sendMsgForChassis(UartCmd.HandShake_ChassisToApp);
    }




    protected void updateMessage(String message){
        Platform.runLater(()->{
            uartContext.mainStageController.textArea.appendText("\n" + message);
        });
    }

    private void updateProgress(Double rate){
        Platform.runLater(()->{
            uartContext.mainStageController.progressBar.setProgress(rate);
            int d=(int)(rate*100);
            uartContext.mainStageController.progressLabel.setText(String.format("%d%%", d));
        });
    }


}
