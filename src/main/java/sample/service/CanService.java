package sample.service;

import gnu.io.SerialPort;
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

import static sample.can.dto.CmdList.CAN_BL_BOOT;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/1/15 11:13
 */
public class CanService {

    private SerialPortService serialPortService;

    public CanService(SerialPortService serialPortService) {
        this.serialPortService = serialPortService;
    }

    public boolean SendExcuteCMD(CanStatus canStatus,final int CAN_BL_MODE){
        CmdFrame excute_fw_cmd = new CmdFrame();

        excute_fw_cmd.header=0x55aa;
        excute_fw_cmd.canId = (short) (canStatus.nodeId & 0xff << 4 | CmdList.EXCUTE);
        excute_fw_cmd.frameLen=CmdFrame.sizeOf;
        excute_fw_cmd.dataLen=4;
        Arrays.fill(excute_fw_cmd.data, (byte)0);
        excute_fw_cmd.data[0]=(byte)(CAN_BL_MODE>>24);
        excute_fw_cmd.data[1]=(byte)(CAN_BL_MODE>>16);
        excute_fw_cmd.data[2]=(byte)(CAN_BL_MODE>>8);
        excute_fw_cmd.data[3]=(byte)(CAN_BL_BOOT);
        excute_fw_cmd.tailer = (short)0xfe00;

        byte[] bytes = excute_fw_cmd.getBytes();
        canStatus.cmd_status=0;
        try {
            System.out.println("\nSendExcuteCMD");
            serialPortService.writeData(bytes,0,bytes.length);
        } catch (IOException e) {
            e.printStackTrace();
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
            System.out.println("\nsendCheckCMD");
            serialPortService.writeData(bytes,0,bytes.length);

            // 源码sleep(100)
            Boolean poll = canStatus.result.take();
//            Boolean poll = canStatus.result.poll(2,TimeUnit.SECONDS);
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
            System.out.println("\nsendVersionCMD");
            serialPortService.writeData(bytes,0,bytes.length);

            // 源码sleep(100)
            Boolean poll = canStatus.result.take();
//            Boolean poll = canStatus.result.poll(2,TimeUnit.SECONDS);
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
        byte[] version_buf = ByteUtils.deBoxed(canStatus.version.toArray(new Byte[0]));
        System.arraycopy(version_buf,0,excute_fw_cmd.data,0,version_buf.length);
        excute_fw_cmd.tailer = (short)0xfe00;

        byte[] bytes = excute_fw_cmd.getBytes();
        canStatus.cmd_status=0;
        try {
            System.out.println("\nsendGetVersionCMD");
            serialPortService.writeData(bytes,0,bytes.length);

            // 源码sleep(100)
//            Boolean poll = canStatus.result.poll(2,TimeUnit.SECONDS);
            Boolean poll = canStatus.result.take();
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
            System.out.println("打开串口成功\n");
        } else {
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
            System.out.println("握手失败");
            return false;
        }
        System.out.println("sendExcuteFlag成功");

        boolean sendVersionFlag = sendVersionCMD(canStatus);
        if(!sendVersionFlag){
            System.out.println("\nsendVersionCMD 固件版本信息校验不通过,请检查固件是否匹配或重新上电!");
            return false;
        }
        System.out.println("sendVersionCMD成功");

        if(canStatus.fwType != CAN_BL_BOOT || canStatus.ack_node_id != canStatus.nodeId){
            System.out.println("固件类型错误或者返回的节点ID错误，握手失败！");
            return false;
        }

        boolean sendGetVersionFlag = sendGetVersionCMD(canStatus);
        if(!sendGetVersionFlag){
            System.out.println("sendGetVersionFlag 失败");
            return false;
        }

        System.out.println("连接成功");
        return true;

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
            System.out.println("\nsendEraseFlashCmd");
            serialPortService.writeData(bytes,0,bytes.length);
//            Boolean poll = canStatus.result.poll(2,TimeUnit.SECONDS);
            Boolean poll = canStatus.result.take();
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
        // todo
        return false;
    }


    public boolean upgrade(CanStatus canStatus){


        System.out.println("开始擦除");

        boolean sendEraseFlashCmdFlag = sendEraseFlashCmd(canStatus);
        if(!sendEraseFlashCmdFlag){
            System.out.println("擦除失败");
            return false;
        }
        System.out.println("擦除成功");
        System.out.println("传输开始");
        return transform(canStatus);



    }

    private boolean transform(CanStatus canStatus){

        Arrays.fill(canStatus.writeDataBuf, (byte)0);
        canStatus.totalSize=canStatus.data.size();
        canStatus.bytesToWrite=canStatus.totalSize;

        while(canStatus.bytesWritten<=canStatus.totalSize){
            fetchData(canStatus);

            if(canStatus.read_data_number>0){
                canStatus.writeDataBuf = CrcUtil.setParamCRC(canStatus.writeDataBuf);
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
            }
        } // end of writeData forEach
        System.out.println("传输成功");
        SendExcuteCMD(canStatus,CmdList.CAN_BL_APP);
        return true;
    }

    // 拿数据
    private void fetchData(CanStatus canStatus) {
        if(canStatus.bytesToWrite>=1024){
            List<Byte> dataToWrite = canStatus.data.subList(canStatus.bytesWritten, 1024);
            canStatus.read_data_number=dataToWrite.size();
            for(int i=0;i<dataToWrite.size();i++){
                canStatus.writeDataBuf[i]=dataToWrite.get(i);
            }
        }else{
            List<Byte> dataToWrite = canStatus.data.subList(canStatus.bytesWritten, canStatus.bytesToWrite);
            canStatus.read_data_number=dataToWrite.size();
            for(int i=0;i<dataToWrite.size();i++){
                canStatus.writeDataBuf[i]=dataToWrite.get(i);
            }
            Arrays.fill(canStatus.writeDataBuf, dataToWrite.size(), 1024 - dataToWrite.size(), (byte)0xff);
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
            System.out.println("\nsendDataPackage");
            serialPortService.writeData(bytes,0,bytes.length);
//            Boolean poll = canStatus.result.poll(2,TimeUnit.SECONDS);
            Boolean poll = canStatus.result.take();
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

        excute_fw_cmd.data[4]= (byte)(canStatus.bytesWritten>>24);
        excute_fw_cmd.data[5]= (byte)(canStatus.bytesWritten>>16);
        excute_fw_cmd.data[6]= (byte)(canStatus.bytesWritten>>8);
        excute_fw_cmd.data[7]= (byte)(canStatus.bytesWritten);

        excute_fw_cmd.tailer = (short)0xfe00;

        byte[] bytes = excute_fw_cmd.getBytes();
        canStatus.cmd_status=0;
        try {
            System.out.println("\nsendWriteInfoCmd");
            serialPortService.writeData(bytes,0,bytes.length);

//            Boolean poll = canStatus.result.poll(2,TimeUnit.SECONDS);
            Boolean poll = canStatus.result.take();
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

}
