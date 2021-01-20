package sample.can;

import cn.hutool.http.HttpUtil;
import gnu.io.SerialPort;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.apache.commons.io.FileUtils;
import sample.Controller;
import sample.OperateStrategy;
import sample.can.dto.CmdFrame;
import sample.can.dto.CmdList;
import sample.can.dto.DataFrame;
import sample.service.SerialPortService;
import sample.support.AgxResult;
import sample.support.PortParam;
import sample.util.ByteUtils;
import sample.util.CrcUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.concurrent.TimeUnit;

import static sample.can.dto.CmdList.*;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/1/15 11:13
 */
public class CanStrategy implements OperateStrategy {

    public static final int TIMEOUT = 3000;
    private SerialPortService serialPortService;
    private CanContext canContext;

    public CanStrategy(SerialPortService serialPortService, CanContext canContext,Controller controller) {
        this.serialPortService = serialPortService;
        this.canContext = canContext;
        this.canContext.controller=controller;
    }
    @Override
    public boolean disconnect() {

        AgxResult agxResult = serialPortService.closeSeriaPort();
        if (agxResult.getCode() == 200) {
            updateMessage("已关闭串口");
            return true;
        }else{
            updateMessage("关闭串口失败");
            return false;
        }

    }

    @Override
    public boolean loadFile(File file) throws IOException {
        byte[] bytes1 = FileUtils.readFileToByteArray(file.getAbsoluteFile());
        return paserFile(bytes1,file.getAbsolutePath());
    }

    @Override
    public boolean loadFileOnNet(String url) {
        byte[] bytes1= HttpUtil.downloadBytes(url);
        return paserFile(bytes1,url);
    }

    private boolean paserFile(byte[] file,String url){
        ArrayList<Byte> fileBytes = new ArrayList<>();
        fileBytes.addAll(Arrays.asList(ByteUtils.boxed(file)));
        canContext.version = getVersion(fileBytes);

        canContext.data = fileBytes.subList(canContext.version.size(), fileBytes.size());

        try {
            canContext.nodeId = (byte) getNodeId(canContext.version).intValue();
        } catch (Exception e) {
            this.updateMessage("\n加载的不是升级文件，请重新加载");
            return false;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("\n加载的文件："+url)
               .append("\n字节数:" + canContext.data.size())
               .append("\n升级固件版本为:"+ByteUtils.getString(canContext.version))
               .append("\n升级的设备节点号:"+(canContext.nodeId &0xff))
               .append("\n\n加载完毕，可以开始升级固件！");
        this.updateMessage(builder.toString());
        System.out.println("字节数:" + canContext.data.size());
        System.out.println(ByteUtils.getString(canContext.version));
        System.out.println();
        System.out.println("读取完毕");
        return true;
    }

    @Override
    public void initUI(Controller controller) {
        controller.connectBtn.disableProperty().unbind();
        controller.disConnectBtn.disableProperty().unbind();
        controller.loadBtn.disableProperty().unbind();
        controller.loadBtn2.disableProperty().unbind();
        controller.upgrateBtn.disableProperty().unbind();
        controller.connectBtn.visibleProperty().unbind();
        controller.disConnectBtn.visibleProperty().unbind();
        controller.loadBtn.visibleProperty().unbind();
        controller.loadBtn2.visibleProperty().unbind();
        controller.upgrateBtn.visibleProperty().unbind();


        // 按钮组
        controller.connectBtn.disableProperty().bind(controller.isLoadFile.not());
        controller.disConnectBtn.visibleProperty().bind(controller.connectBtn.visibleProperty().not());
        controller.upgrateBtn.disableProperty().bind(controller.connectBtn.visibleProperty());
        controller.disConnectBtn.visibleProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                controller.loadBtn.setDisable(newValue);
                controller.loadBtn2.setDisable(newValue);
            }
        });
        controller.loadBtn.setDisable(false);
        controller.loadBtn2.setDisable(false);
    }


    private boolean SendExcuteCMD(final int CAN_BL_MODE){
        CmdFrame excute_fw_cmd = new CmdFrame();

        excute_fw_cmd.header=0x55aa;
        excute_fw_cmd.canId = (short) (canContext.nodeId & 0xff << 4 | CmdList.EXCUTE);
        excute_fw_cmd.frameLen=CmdFrame.sizeOf;
        excute_fw_cmd.dataLen=4;
        Arrays.fill(excute_fw_cmd.data, (byte)0);
        excute_fw_cmd.data[0]=(byte)(CAN_BL_MODE>>24);
        excute_fw_cmd.data[1]=(byte)(CAN_BL_MODE>>16);
        excute_fw_cmd.data[2]=(byte)(CAN_BL_MODE>>8);
        excute_fw_cmd.data[3]=(byte)(CAN_BL_MODE);
        excute_fw_cmd.tailer = (short)0xfe00;

        byte[] bytes = excute_fw_cmd.getBytes();
        canContext.cmd_status=0;
        try {
            Thread.sleep(10);
            System.out.println("\nSendExcuteCMD:");
            serialPortService.writeData(bytes,0,bytes.length);
            if(CAN_BL_MODE == CAN_BL_APP){
                Boolean poll = canContext.result.poll(TIMEOUT, TimeUnit.MILLISECONDS);
                if(poll==null){
                    System.out.println("\n超时");
                    return false;
                }
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }

        boolean b = sendCheckCMD();
        return b;


    }

    private boolean sendCheckCMD(){
        CmdFrame excute_fw_cmd = new CmdFrame();

        excute_fw_cmd.header=0x55aa;
        excute_fw_cmd.canId = (short) (canContext.nodeId & 0xff << 4 | CmdList.CHECK);
        excute_fw_cmd.frameLen=CmdFrame.sizeOf;
        excute_fw_cmd.dataLen=0;
        Arrays.fill(excute_fw_cmd.data, (byte)0);
        excute_fw_cmd.tailer = (short)0xfe00;

        byte[] bytes = excute_fw_cmd.getBytes();
        canContext.cmd_status=0;
        try {
            Thread.sleep(30);
            System.out.println("\nsendCheckCMD");
            serialPortService.writeData(bytes,0,bytes.length);

            // 源码sleep(100)
//            Boolean poll = canContext.result.take();
            Boolean poll = canContext.result.poll(TIMEOUT, TimeUnit.MILLISECONDS);
            if(poll==null){
                System.out.println("\n超时");
                return false;
            }
            if(poll){
                canContext.checkCmdVersion=(canContext.can_rx_buf[0] &0xff )<<24 | (canContext.can_rx_buf[1] &0xff )<<16 |(canContext.can_rx_buf[2] &0xff )<<8 |(canContext.can_rx_buf[3] &0xff );

                canContext.fwType=(canContext.can_rx_buf[4] &0xff )<<24 | (canContext.can_rx_buf[5] &0xff )<<16 |(canContext.can_rx_buf[6] &0xff )<<8 |(canContext.can_rx_buf[7] &0xff );
                return true;
            }else{
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("\n超时");
            return false;
        }
        return false;
    }


    private boolean sendVersionCMD(){
        CmdFrame excute_fw_cmd = new CmdFrame();

        excute_fw_cmd.header=0x55aa;
        excute_fw_cmd.canId = (short) (canContext.nodeId & 0xff << 4 | CmdList.SEND_VERSION_INFO);
        excute_fw_cmd.frameLen=CmdFrame.sizeOf;
        excute_fw_cmd.dataLen=32;
        Arrays.fill(excute_fw_cmd.data, (byte)0);
        byte[] version_buf = ByteUtils.deBoxed(canContext.version.toArray(new Byte[0]));
        System.arraycopy(version_buf,0,excute_fw_cmd.data,0,version_buf.length);
        excute_fw_cmd.tailer = (short)0xfe00;

        byte[] bytes = excute_fw_cmd.getBytes();
        canContext.cmd_status=0;
        try {
            Thread.sleep(10);
            System.out.println("\nsendVersionCMD");
            serialPortService.writeData(bytes,0,bytes.length);

//            Boolean poll = canContext.result.take();
            Boolean poll = canContext.result.poll(TIMEOUT, TimeUnit.MILLISECONDS);
            if(poll==null){
                updateMessage("连接\n超时");
                return false;
            }
            if(poll){
                // memcpy(version_buf,can_rx_buf,32);  不知道赋值局部变量的作用
                return true;
            }else{
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("\n超时");
            return false;
        }

        // todo
        return false;
    }

    private boolean sendGetVersionCMD(){
        CmdFrame excute_fw_cmd = new CmdFrame();

        excute_fw_cmd.header=0x55aa;
        excute_fw_cmd.canId = (short) (canContext.nodeId & 0xff << 4 | CmdList.GET_VERSION_INFO);
        excute_fw_cmd.frameLen=CmdFrame.sizeOf;
        excute_fw_cmd.dataLen=0;
        Arrays.fill(excute_fw_cmd.data, (byte)0);
        excute_fw_cmd.tailer = (short)0xfe00;

        byte[] bytes = excute_fw_cmd.getBytes();
        canContext.cmd_status=0;
        try {
            Thread.sleep(10);
            System.out.println("\nsendGetVersionCMD");
            serialPortService.writeData(bytes,0,bytes.length);

            Boolean poll = canContext.result.poll(TIMEOUT, TimeUnit.MILLISECONDS);
//            Boolean poll = canContext.result.take();
            if(poll==null){
                updateMessage("\n超时");
                return false;
            }
            if(poll){
                // memcpy(version_buf,can_rx_buf,32);  不知道赋值局部变量的作用
                System.arraycopy(canContext.can_rx_buf,0,canContext.getVersionBuf,0,canContext.getVersionBuf.length);
                return true;
            }else{
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("\n超时");
            return false;
        }
        // todo
        return false;
    }




    public boolean connect(PortParam portParam){
        SerialPort theSerialPort = serialPortService.getTheSerialPort();
        if(theSerialPort!=null){
            serialPortService.closeSeriaPort();
        }
        AgxResult agxResult = serialPortService.openSerialPort(portParam);
        if (agxResult.getCode().equals(200)) {
            updateMessage("打开串口成功");
            System.out.println("打开串口成功\n");
        } else {
            updateMessage("打开串口失败");
            System.out.println("打开串口失败\n");
            return false;
        }
        try {
            theSerialPort = serialPortService.getTheSerialPort();
            serialPortService.addEventListener(new CanListener(theSerialPort));
            theSerialPort.notifyOnDataAvailable(true);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        }

        if(canContext.nodeId==0){
            // 失败
            updateMessage("节点号错误");
            System.out.println("节点号错误");
            return false;
        }

        int handshake_send_cnt=0;
        boolean sendExcuteFlag=false;
        do{
            sendExcuteFlag=SendExcuteCMD(CAN_BL_BOOT);
            handshake_send_cnt++;
        }while(!sendExcuteFlag && canContext.fwType != CAN_BL_BOOT && handshake_send_cnt<3);

        if(!sendExcuteFlag){
            updateMessage("握手失败");
            System.out.println("握手失败");
            return false;
        }
        System.out.println("sendExcuteFlag成功");

        boolean sendVersionFlag = sendVersionCMD();
        if(!sendVersionFlag){
            updateMessage("固件版本信息校验不通过,请检查固件是否匹配或重新上电!");
            System.out.println("\nsendVersionCMD 固件版本信息校验不通过,请检查固件是否匹配或重新上电!");
            return false;
        }
        System.out.println("sendVersionCMD成功");

        if(canContext.fwType != CAN_BL_BOOT || canContext.ack_node_id != canContext.nodeId){
            updateMessage("固件类型错误或者返回的节点ID错误，握手失败！");
            System.out.println("固件类型错误或者返回的节点ID错误，握手失败！");
            return false;
        }

        boolean sendGetVersionFlag = sendGetVersionCMD();
        if(!sendGetVersionFlag){
            updateMessage("获取固件版本失败");
            System.out.println("sendGetVersionFlag 失败");
            return false;
        }

        updateMessage("连接成功");
        System.out.println("连接成功");
        showConnectMessage();
        return true;

    }

    private void showConnectMessage() {
        int version = canContext.checkCmdVersion;
        int a=(version>>24 & 0xff)*10 +(version>>16 & 0xff);
        int b=(version>>8 & 0xff)*10 +(version & 0xff);
        String c = new StringBuilder("当前BOOT固件版本号为：").append("v").append(a).append(".").append(b).toString();
        updateMessage(c);
        String s = new String(canContext.getVersionBuf);
        updateMessage(s);
    }

    private boolean sendEraseFlashCmd(){
        CmdFrame excute_fw_cmd = new CmdFrame();

        excute_fw_cmd.header=0x55aa;
        excute_fw_cmd.canId = (short) (canContext.nodeId & 0xff << 4 | CmdList.ERASE);
        excute_fw_cmd.frameLen=CmdFrame.sizeOf;
        excute_fw_cmd.dataLen=4;
        Arrays.fill(excute_fw_cmd.data, (byte)0);
        excute_fw_cmd.data[0]=(byte)(canContext.totalSize>>24);
        excute_fw_cmd.data[1]=(byte)(canContext.totalSize>>16);
        excute_fw_cmd.data[2]=(byte)(canContext.totalSize>>8);
        excute_fw_cmd.data[3]=(byte)(canContext.totalSize);
        excute_fw_cmd.tailer = (short)0xfe00;

        byte[] bytes = excute_fw_cmd.getBytes();
        canContext.cmd_status=0;
        try {
            Thread.sleep(10);
            System.out.println("\nsendEraseFlashCmd");
            serialPortService.writeData(bytes,0,bytes.length);
            Boolean poll = canContext.result.poll(TIMEOUT, TimeUnit.MILLISECONDS);
//            Boolean poll = canContext.result.take();
            if(poll==null){
                System.out.println("\n超时");
                return false;
            }
            return poll;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            return false;
        }
        // todo
        return false;
    }


    public boolean upgrade(){
        canContext.totalSize=canContext.data.size();

        System.out.println("开始擦除");
        updateMessage("开始擦除");

        boolean sendEraseFlashCmdFlag = sendEraseFlashCmd();
        if(!sendEraseFlashCmdFlag){
            System.out.println("擦除失败");
            updateMessage("擦除失败");
            return false;
        }
        updateMessage("擦除成功");
        System.out.println("擦除成功");
        System.out.println("传输开始");
        updateMessage("传输开始");
        updateMessage("传输开始");
        boolean transformFlag = transform();
        if(transformFlag){
            updateMessage("升级成功");
            System.out.println("升级成功");
        }else{
            updateMessage("升级失败");
            System.out.println("升级失败");
        }
        return transformFlag;

    }

    private boolean transform(){

        Arrays.fill(canContext.writeDataBuf, (byte)0);
        canContext.bytesToWrite=canContext.totalSize;
        canContext.bytesWritten=0;
        double rate=(double)canContext.bytesWritten/canContext.totalSize;
        updateProgress(rate);
        while(canContext.bytesWritten<=canContext.totalSize){
            fetchData(canContext);

            if(canContext.read_data_number>0){
                canContext.writeDataBuf = CrcUtil.setParamCrcAdapter(canContext.writeDataBuf,canContext.read_data_number);
                System.out.println("read_data_number:"+canContext.read_data_number);
//                String s =
//                        HexUtils.hexStrings2hexString(HexUtils.bytesToHexStrings(canContext.writeDataBuf, 0, 1024));
//                System.out.println("s = " + s);
                // 发送数据
                int package_write_times = 0;
                boolean sendDataPackageFlag = false;
                do {
                    // 发送传送控制命令
                    boolean sendWriteInfoCmdFlag = false;
                    int cmd_send_times = 0;
                    do {
                        sendWriteInfoCmdFlag = sendWriteInfoCmd();
                        cmd_send_times++;
                    } while (!sendWriteInfoCmdFlag && cmd_send_times < 3);

                    if (!sendWriteInfoCmdFlag) {
                        System.out.println("发送控制命令失败");
                        return false;
                    }
                    sendDataPackageFlag = sendDataPackage();
                    package_write_times++;
                } while (!sendDataPackageFlag && package_write_times < 3);
                if(!sendDataPackageFlag){
                    System.out.println("发送数据包失败");
                    return false;
                }
                System.out.println("发送数据包成功");
                canContext.bytesWritten+=canContext.read_data_number;
                canContext.bytesToWrite -= canContext.read_data_number;
                rate=(double)canContext.bytesWritten/canContext.totalSize;
                updateProgress(rate);
            }
        } // end of writeData forEach

        return SendExcuteCMD( CAN_BL_APP);
    }

    // 拿数据
    private void fetchData(CanContext canContext) {
        if(canContext.bytesToWrite>=1024){
            List<Byte> dataToWrite = canContext.data.subList(canContext.bytesWritten, canContext.bytesWritten+1024);
            canContext.read_data_number=dataToWrite.size();
            for(int i=0;i<dataToWrite.size();i++){
                canContext.writeDataBuf[i]=dataToWrite.get(i);
            }
        }else{
            List<Byte> dataToWrite = canContext.data.subList(canContext.bytesWritten, canContext.bytesWritten+canContext.bytesToWrite);
            canContext.read_data_number=dataToWrite.size();
            for(int i=0;i<dataToWrite.size();i++){
                canContext.writeDataBuf[i]=dataToWrite.get(i);
            }
            Arrays.fill(canContext.writeDataBuf, dataToWrite.size(), 1024 , (byte)0xff);
            canContext.read_data_number=1024;
        }
    }

    private boolean sendDataPackage() {

        DataFrame packageFrame=new DataFrame();
        packageFrame.header=0x55aa;
        packageFrame.canId=(short)(canContext.nodeId&0xff<<4| CmdList.WRITE);
        packageFrame.frameLen=DataFrame.sizeOf;
        packageFrame.dataLen=(short)(canContext.read_data_number+2);
        Arrays.fill(packageFrame.data, (byte) 0);
        System.arraycopy(canContext.writeDataBuf,0,packageFrame.data,0,packageFrame.dataLen);
        packageFrame.tailer=(short)0xfe00;

        byte[] bytes = packageFrame.getBytes();
        canContext.cmd_status=0;
        try {
            Thread.sleep(10);
            System.out.println("\nsendDataPackage");
            serialPortService.writeData(bytes,0,bytes.length);
            Boolean poll = canContext.result.poll(TIMEOUT, TimeUnit.MILLISECONDS);
//            Boolean poll = canContext.result.take();
            if(poll==null){
                System.out.println("\n超时");
                return false;
            }
            return poll;


        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // todo
        return false;
    }

    private boolean sendWriteInfoCmd() {
        CmdFrame excute_fw_cmd = new CmdFrame();

        excute_fw_cmd.header=0x55aa;
        excute_fw_cmd.canId = (short) (canContext.nodeId & 0xff << 4 | CmdList.WRITE_INFO);
        excute_fw_cmd.frameLen=CmdFrame.sizeOf;
        excute_fw_cmd.dataLen=8;
        Arrays.fill(excute_fw_cmd.data, (byte)0);
        excute_fw_cmd.data[0]= (byte)(canContext.bytesWritten>>24);
        excute_fw_cmd.data[1]= (byte)(canContext.bytesWritten>>16);
        excute_fw_cmd.data[2]= (byte)(canContext.bytesWritten>>8);
        excute_fw_cmd.data[3]= (byte)(canContext.bytesWritten);

        excute_fw_cmd.data[4]= (byte)((canContext.read_data_number+2)>>24);
        excute_fw_cmd.data[5]= (byte)((canContext.read_data_number+2)>>16);
        excute_fw_cmd.data[6]= (byte)((canContext.read_data_number+2)>>8);
        excute_fw_cmd.data[7]= (byte)((canContext.read_data_number+2));

        excute_fw_cmd.tailer = (short)0xfe00;

        byte[] bytes = excute_fw_cmd.getBytes();
        canContext.cmd_status=0;
        try {
            Thread.sleep(20);
            System.out.println("\nsendWriteInfoCmd");
            serialPortService.writeData(bytes,0,bytes.length);

            Boolean poll = canContext.result.poll(TIMEOUT, TimeUnit.MILLISECONDS);
//            Boolean poll = canContext.result.take();
            if(poll==null){
                System.out.println("\n超时");
                return false;
            }
            return poll;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("\n超时");
            return false;
        }
        return false;
    }

    private void updateMessage(String message){
        Platform.runLater(()->{
            canContext.controller.textArea.appendText("\n" + message);
        });
    }

    private void updateProgress(Double rate){
        Platform.runLater(()->{
            canContext.controller.progressBar.setProgress(rate);
            int d=(int)(rate*100);
            canContext.controller.progressLabel.setText(String.format("%d%%", d));
        });
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

}
