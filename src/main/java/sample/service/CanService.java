package sample.service;

import gnu.io.SerialPort;
import javafx.application.Platform;
import javafx.concurrent.Task;
import sample.can.CanListener;
import sample.can.CanStatus;
import sample.can.dto.CmdFrame;
import sample.can.dto.CmdList;
import sample.can.dto.DataFrame;
import sample.support.AgxResult;
import sample.support.PortParam;
import sample.util.ByteUtils;
import sample.util.CrcUtil;
import sample.util.HexUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
public class CanService {

    public static final int TIMEOUT = 3000;
    private SerialPortService serialPortService;
    private CanStatus canStatus;

    public CanService(SerialPortService serialPortService,CanStatus canStatus) {
        this.serialPortService = serialPortService;
        this.canStatus = canStatus;
    }

    private boolean SendExcuteCMD(CanStatus canStatus,final int CAN_BL_MODE){
        CmdFrame excute_fw_cmd = new CmdFrame();

        excute_fw_cmd.header=0x55aa;
        excute_fw_cmd.canId = (short) (canStatus.nodeId & 0xff << 4 | CmdList.EXCUTE);
        excute_fw_cmd.frameLen=CmdFrame.sizeOf;
        excute_fw_cmd.dataLen=4;
        Arrays.fill(excute_fw_cmd.data, (byte)0);
        excute_fw_cmd.data[0]=(byte)(CAN_BL_MODE>>24);
        excute_fw_cmd.data[1]=(byte)(CAN_BL_MODE>>16);
        excute_fw_cmd.data[2]=(byte)(CAN_BL_MODE>>8);
        excute_fw_cmd.data[3]=(byte)(CAN_BL_MODE);
        excute_fw_cmd.tailer = (short)0xfe00;

        byte[] bytes = excute_fw_cmd.getBytes();
        canStatus.cmd_status=0;
        try {
            Thread.sleep(10);
            System.out.println("\nSendExcuteCMD:");
            serialPortService.writeData(bytes,0,bytes.length);
            if(CAN_BL_MODE == CAN_BL_APP){
                Boolean poll = canStatus.result.poll(TIMEOUT, TimeUnit.MILLISECONDS);
                if(poll==null){
                    System.out.println("超时");
                    return false;
                }
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }

        boolean b = sendCheckCMD(canStatus);
        return b;


    }

    private boolean sendCheckCMD(CanStatus canStatus){
        CmdFrame excute_fw_cmd = new CmdFrame();

        excute_fw_cmd.header=0x55aa;
        excute_fw_cmd.canId = (short) (canStatus.nodeId & 0xff << 4 | CmdList.CHECK);
        excute_fw_cmd.frameLen=CmdFrame.sizeOf;
        excute_fw_cmd.dataLen=0;
        Arrays.fill(excute_fw_cmd.data, (byte)0);
        excute_fw_cmd.tailer = (short)0xfe00;

        byte[] bytes = excute_fw_cmd.getBytes();
        canStatus.cmd_status=0;
        try {
            Thread.sleep(30);
            System.out.println("\nsendCheckCMD");
            serialPortService.writeData(bytes,0,bytes.length);

            // 源码sleep(100)
//            Boolean poll = canStatus.result.take();
            Boolean poll = canStatus.result.poll(TIMEOUT, TimeUnit.MILLISECONDS);
            if(poll==null){
                System.out.println("超时");
                return false;
            }
            if(poll){
                canStatus.checkCmdVersion=(canStatus.can_rx_buf[0] &0xff )<<24 | (canStatus.can_rx_buf[1] &0xff )<<16 |(canStatus.can_rx_buf[2] &0xff )<<8 |(canStatus.can_rx_buf[3] &0xff );

                canStatus.fwType=(canStatus.can_rx_buf[4] &0xff )<<24 | (canStatus.can_rx_buf[5] &0xff )<<16 |(canStatus.can_rx_buf[6] &0xff )<<8 |(canStatus.can_rx_buf[7] &0xff );
                return true;
            }else{
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("超时");
            return false;
        }
        return false;
    }


    private boolean sendVersionCMD(CanStatus canStatus){
        CmdFrame excute_fw_cmd = new CmdFrame();

        excute_fw_cmd.header=0x55aa;
        excute_fw_cmd.canId = (short) (canStatus.nodeId & 0xff << 4 | CmdList.SEND_VERSION_INFO);
        excute_fw_cmd.frameLen=CmdFrame.sizeOf;
        excute_fw_cmd.dataLen=32;
        Arrays.fill(excute_fw_cmd.data, (byte)0);
        byte[] version_buf = ByteUtils.deBoxed(canStatus.version.toArray(new Byte[0]));
        System.arraycopy(version_buf,0,excute_fw_cmd.data,0,version_buf.length);
        excute_fw_cmd.tailer = (short)0xfe00;

        byte[] bytes = excute_fw_cmd.getBytes();
        canStatus.cmd_status=0;
        try {
            Thread.sleep(10);
            System.out.println("\nsendVersionCMD");
            serialPortService.writeData(bytes,0,bytes.length);

//            Boolean poll = canStatus.result.take();
            Boolean poll = canStatus.result.poll(TIMEOUT, TimeUnit.MILLISECONDS);
            if(poll==null){
                updateMessage("连接超时");
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
            System.out.println("超时");
            return false;
        }

        // todo
        return false;
    }

    private boolean sendGetVersionCMD(CanStatus canStatus){
        CmdFrame excute_fw_cmd = new CmdFrame();

        excute_fw_cmd.header=0x55aa;
        excute_fw_cmd.canId = (short) (canStatus.nodeId & 0xff << 4 | CmdList.GET_VERSION_INFO);
        excute_fw_cmd.frameLen=CmdFrame.sizeOf;
        excute_fw_cmd.dataLen=0;
        Arrays.fill(excute_fw_cmd.data, (byte)0);
        excute_fw_cmd.tailer = (short)0xfe00;

        byte[] bytes = excute_fw_cmd.getBytes();
        canStatus.cmd_status=0;
        try {
            Thread.sleep(10);
            System.out.println("\nsendGetVersionCMD");
            serialPortService.writeData(bytes,0,bytes.length);

            Boolean poll = canStatus.result.poll(TIMEOUT, TimeUnit.MILLISECONDS);
//            Boolean poll = canStatus.result.take();
            if(poll==null){
                updateMessage("超时");
                return false;
            }
            if(poll){
                // memcpy(version_buf,can_rx_buf,32);  不知道赋值局部变量的作用
                System.arraycopy(canStatus.can_rx_buf,0,canStatus.getVersionBuf,0,canStatus.getVersionBuf.length);
                return true;
            }else{
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("超时");
            return false;
        }
        // todo
        return false;
    }




    public boolean Connect(CanStatus canStatus, PortParam portParam){
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

        if(canStatus.nodeId==0){
            // 失败
            updateMessage("节点号错误");
            System.out.println("节点号错误");
            return false;
        }

        int handshake_send_cnt=0;
        boolean sendExcuteFlag=false;
        do{
            sendExcuteFlag=SendExcuteCMD(canStatus, CAN_BL_BOOT);
            handshake_send_cnt++;
        }while(!sendExcuteFlag && canStatus.fwType != CAN_BL_BOOT && handshake_send_cnt<3);

        if(!sendExcuteFlag){
            updateMessage("握手失败");
            System.out.println("握手失败");
            return false;
        }
        System.out.println("sendExcuteFlag成功");

        boolean sendVersionFlag = sendVersionCMD(canStatus);
        if(!sendVersionFlag){
            updateMessage("固件版本信息校验不通过,请检查固件是否匹配或重新上电!");
            System.out.println("\nsendVersionCMD 固件版本信息校验不通过,请检查固件是否匹配或重新上电!");
            return false;
        }
        System.out.println("sendVersionCMD成功");

        if(canStatus.fwType != CAN_BL_BOOT || canStatus.ack_node_id != canStatus.nodeId){
            updateMessage("固件类型错误或者返回的节点ID错误，握手失败！");
            System.out.println("固件类型错误或者返回的节点ID错误，握手失败！");
            return false;
        }

        boolean sendGetVersionFlag = sendGetVersionCMD(canStatus);
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
        int version = canStatus.checkCmdVersion;
        int a=(version>>24 & 0xff)*10 +(version>>16 & 0xff);
        int b=(version>>8 & 0xff)*10 +(version & 0xff);
        String c = new StringBuilder("当前BOOT固件版本号为：").append("v").append(a).append(".").append(b).toString();
        updateMessage(c);
        String s = new String(canStatus.getVersionBuf);
        updateMessage(s);
    }

    private boolean sendEraseFlashCmd(CanStatus canStatus){
        CmdFrame excute_fw_cmd = new CmdFrame();

        excute_fw_cmd.header=0x55aa;
        excute_fw_cmd.canId = (short) (canStatus.nodeId & 0xff << 4 | CmdList.ERASE);
        excute_fw_cmd.frameLen=CmdFrame.sizeOf;
        excute_fw_cmd.dataLen=4;
        Arrays.fill(excute_fw_cmd.data, (byte)0);
        excute_fw_cmd.data[0]=(byte)(canStatus.totalSize>>24);
        excute_fw_cmd.data[1]=(byte)(canStatus.totalSize>>16);
        excute_fw_cmd.data[2]=(byte)(canStatus.totalSize>>8);
        excute_fw_cmd.data[3]=(byte)(canStatus.totalSize);
        excute_fw_cmd.tailer = (short)0xfe00;

        byte[] bytes = excute_fw_cmd.getBytes();
        canStatus.cmd_status=0;
        try {
            Thread.sleep(10);
            System.out.println("\nsendEraseFlashCmd");
            serialPortService.writeData(bytes,0,bytes.length);
            Boolean poll = canStatus.result.poll(TIMEOUT, TimeUnit.MILLISECONDS);
//            Boolean poll = canStatus.result.take();
            if(poll==null){
                System.out.println("超时");
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


    public boolean upgrade(CanStatus canStatus){
        canStatus.totalSize=canStatus.data.size();

        System.out.println("开始擦除");
        updateMessage("开始擦除");

        boolean sendEraseFlashCmdFlag = sendEraseFlashCmd(canStatus);
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
        boolean transformFlag = transform(canStatus);
        if(transformFlag){
            updateMessage("升级成功");
            System.out.println("升级成功");
        }else{
            updateMessage("升级失败");
            System.out.println("升级失败");
        }
        return transformFlag;

    }

    private boolean transform(CanStatus canStatus){

        Arrays.fill(canStatus.writeDataBuf, (byte)0);
        canStatus.bytesToWrite=canStatus.totalSize;
        canStatus.bytesWritten=0;
        double rate=(double)canStatus.bytesWritten/canStatus.totalSize;
        updateProgress(rate);
        while(canStatus.bytesWritten<=canStatus.totalSize){
            fetchData(canStatus);

            if(canStatus.read_data_number>0){
                canStatus.writeDataBuf = CrcUtil.setParamCrcAdapter(canStatus.writeDataBuf,canStatus.read_data_number);
                System.out.println("read_data_number:"+canStatus.read_data_number);
//                String s =
//                        HexUtils.hexStrings2hexString(HexUtils.bytesToHexStrings(canStatus.writeDataBuf, 0, 1024));
//                System.out.println("s = " + s);
                // 发送数据
                int package_write_times = 0;
                boolean sendDataPackageFlag = false;
                do {
                    // 发送传送控制命令
                    boolean sendWriteInfoCmdFlag = false;
                    int cmd_send_times = 0;
                    do {
                        sendWriteInfoCmdFlag = sendWriteInfoCmd(canStatus);
                        cmd_send_times++;
                    } while (!sendWriteInfoCmdFlag && cmd_send_times < 3);

                    if (!sendWriteInfoCmdFlag) {
                        System.out.println("发送控制命令失败");
                        return false;
                    }
                    sendDataPackageFlag = sendDataPackage(canStatus);
                    package_write_times++;
                } while (!sendDataPackageFlag && package_write_times < 3);
                if(!sendDataPackageFlag){
                    System.out.println("发送数据包失败");
                    return false;
                }
                System.out.println("发送数据包成功");
                canStatus.bytesWritten+=canStatus.read_data_number;
                canStatus.bytesToWrite -= canStatus.read_data_number;
                rate=(double)canStatus.bytesWritten/canStatus.totalSize;
                updateProgress(rate);
            }
        } // end of writeData forEach

        return SendExcuteCMD(canStatus, CAN_BL_APP);
    }

    // 拿数据
    private void fetchData(CanStatus canStatus) {
        if(canStatus.bytesToWrite>=1024){
            List<Byte> dataToWrite = canStatus.data.subList(canStatus.bytesWritten, canStatus.bytesWritten+1024);
            canStatus.read_data_number=dataToWrite.size();
            for(int i=0;i<dataToWrite.size();i++){
                canStatus.writeDataBuf[i]=dataToWrite.get(i);
            }
        }else{
            List<Byte> dataToWrite = canStatus.data.subList(canStatus.bytesWritten, canStatus.bytesWritten+canStatus.bytesToWrite);
            canStatus.read_data_number=dataToWrite.size();
            for(int i=0;i<dataToWrite.size();i++){
                canStatus.writeDataBuf[i]=dataToWrite.get(i);
            }
            Arrays.fill(canStatus.writeDataBuf, dataToWrite.size(), 1024 , (byte)0xff);
            canStatus.read_data_number=1024;
        }
    }

    private boolean sendDataPackage(CanStatus canStatus) {

        DataFrame packageFrame=new DataFrame();
        packageFrame.header=0x55aa;
        packageFrame.canId=(short)(canStatus.nodeId&0xff<<4| CmdList.WRITE);
        packageFrame.frameLen=DataFrame.sizeOf;
        packageFrame.dataLen=(short)(canStatus.read_data_number+2);
        Arrays.fill(packageFrame.data, (byte) 0);
        System.arraycopy(canStatus.writeDataBuf,0,packageFrame.data,0,packageFrame.dataLen);
        packageFrame.tailer=(short)0xfe00;

        byte[] bytes = packageFrame.getBytes();
        canStatus.cmd_status=0;
        try {
            Thread.sleep(10);
            System.out.println("\nsendDataPackage");
            serialPortService.writeData(bytes,0,bytes.length);
            Boolean poll = canStatus.result.poll(TIMEOUT, TimeUnit.MILLISECONDS);
//            Boolean poll = canStatus.result.take();
            if(poll==null){
                System.out.println("超时");
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

    private boolean sendWriteInfoCmd(CanStatus canStatus) {
        CmdFrame excute_fw_cmd = new CmdFrame();

        excute_fw_cmd.header=0x55aa;
        excute_fw_cmd.canId = (short) (canStatus.nodeId & 0xff << 4 | CmdList.WRITE_INFO);
        excute_fw_cmd.frameLen=CmdFrame.sizeOf;
        excute_fw_cmd.dataLen=8;
        Arrays.fill(excute_fw_cmd.data, (byte)0);
        excute_fw_cmd.data[0]= (byte)(canStatus.bytesWritten>>24);
        excute_fw_cmd.data[1]= (byte)(canStatus.bytesWritten>>16);
        excute_fw_cmd.data[2]= (byte)(canStatus.bytesWritten>>8);
        excute_fw_cmd.data[3]= (byte)(canStatus.bytesWritten);

        excute_fw_cmd.data[4]= (byte)((canStatus.read_data_number+2)>>24);
        excute_fw_cmd.data[5]= (byte)((canStatus.read_data_number+2)>>16);
        excute_fw_cmd.data[6]= (byte)((canStatus.read_data_number+2)>>8);
        excute_fw_cmd.data[7]= (byte)((canStatus.read_data_number+2));

        excute_fw_cmd.tailer = (short)0xfe00;

        byte[] bytes = excute_fw_cmd.getBytes();
        canStatus.cmd_status=0;
        try {
            Thread.sleep(20);
            System.out.println("\nsendWriteInfoCmd");
            serialPortService.writeData(bytes,0,bytes.length);

            Boolean poll = canStatus.result.poll(TIMEOUT, TimeUnit.MILLISECONDS);
//            Boolean poll = canStatus.result.take();
            if(poll==null){
                System.out.println("超时");
                return false;
            }
            return poll;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("超时");
            return false;
        }
        return false;
    }

    private void updateMessage(String message){
        Platform.runLater(()->{
            canStatus.controller.textArea.appendText("\n"+message);
        });
    }

    private void updateProgress(Double rate){
        Platform.runLater(()->{
            canStatus.controller.progressBar.setProgress(rate);
            int d=(int)(rate*100);
            canStatus.controller.progressLabel.setText(String.format("%d%%",d));
        });
    }

    public void disConnect() {

        AgxResult agxResult = serialPortService.closeSeriaPort();
        if (agxResult.getCode() == 200) {
            updateMessage("已关闭串口");
        }else{
            updateMessage("关闭串口失败");
        }
    }
}
