package sample.service;

import sample.can.CanStatus;
import sample.can.dto.CmdFrame;
import sample.can.dto.CmdList;
import sample.util.ByteUtils;
import sample.util.HexUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static sample.can.dto.CmdList.CAN_BL_BOOT;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/1/15 11:13
 */
public class CanService {

    private SerialPortService serialPortService;

    public boolean SendExcuteCMD(CanStatus canStatus){
        CmdFrame excute_fw_cmd = new CmdFrame();

        excute_fw_cmd.header=0x55aa;
        excute_fw_cmd.canId = (short) (canStatus.nodeId & 0xff << 4 | 0x05);
        excute_fw_cmd.frameLen=CmdFrame.sizeOf;
        excute_fw_cmd.dataLen=4;
        Arrays.fill(excute_fw_cmd.data, (byte)0);
        excute_fw_cmd.data[0]=CAN_BL_BOOT>>24;
        excute_fw_cmd.data[1]=(byte)(CAN_BL_BOOT>>16);
        excute_fw_cmd.data[2]=(byte)(CAN_BL_BOOT>>8);
        excute_fw_cmd.data[3]=(byte)(CAN_BL_BOOT);
        excute_fw_cmd.tailer = (short)0xfe00;

        byte[] bytes = excute_fw_cmd.getBytes();
        String s = HexUtils.hexStrings2hexString(HexUtils.bytesToHexStrings(bytes));
        System.out.println("s = " + s);

        try {
            serialPortService.writeData(bytes,0,bytes.length);

            // 源码sleep(100)

        } catch (IOException e) {
            e.printStackTrace();
        }

        return sendCheckCMD(canStatus);



    }

    private boolean sendCheckCMD(CanStatus canStatus){
        CmdFrame excute_fw_cmd = new CmdFrame();

        excute_fw_cmd.header=0x55aa;
        excute_fw_cmd.canId = (short) (canStatus.nodeId & 0xff << 4 | 0x05);
        excute_fw_cmd.frameLen=CmdFrame.sizeOf;
        excute_fw_cmd.dataLen=0;
        Arrays.fill(excute_fw_cmd.data, (byte)0);
        excute_fw_cmd.tailer = (short)0xfe00;

        byte[] bytes = excute_fw_cmd.getBytes();
        canStatus.cmd_status=0;
        try {
            serialPortService.writeData(bytes,0,bytes.length);

            // 源码sleep(100)
            Boolean poll = canStatus.result.poll(10, TimeUnit.MILLISECONDS);
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
        excute_fw_cmd.canId = (short) (canStatus.nodeId & 0xff << 4 | 0x05);
        excute_fw_cmd.frameLen=CmdFrame.sizeOf;
        excute_fw_cmd.dataLen=32;
        Arrays.fill(excute_fw_cmd.data, (byte)0);
        excute_fw_cmd.data=ByteUtils.deBoxed(canStatus.version.toArray(new Byte[0]));
        excute_fw_cmd.tailer = (short)0xfe00;

        byte[] bytes = excute_fw_cmd.getBytes();

        try {
            serialPortService.writeData(bytes,0,bytes.length);

            // 源码sleep(100)
            Boolean poll = canStatus.result.poll(10, TimeUnit.MILLISECONDS);
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
        // todo
        return false;
    }




    public void Connect(CanStatus canStatus){
        if(canStatus.nodeId==0){
            // 失败
            System.out.println("节点号错误");
            return ;
        }

        int handshake_send_cnt=0;
        boolean sendExcuteFlag=false;
        do{
            sendExcuteFlag=SendExcuteCMD(canStatus);
            handshake_send_cnt++;
        }while(!sendExcuteFlag && canStatus.fwType != CAN_BL_BOOT && handshake_send_cnt<3);

        if(!sendExcuteFlag){
            System.out.println("握手失败");
            return;
        }

        boolean sendVersionFlag = sendVersionCMD(canStatus);
        if(!sendVersionFlag){
            System.out.println("\"固件版本信息校验不通过,请检查固件是否匹配或重新上电!");
            return;
        }

        if(canStatus.fwType != CAN_BL_BOOT || canStatus.ack_node_id != canStatus.nodeId){
            System.out.println("固件类型错误或者返回的节点ID错误，握手失败！");
            return ;
        }
        boolean sendGetVersionFlag = sendGetVersionCMD(canStatus);
        if(!sendGetVersionFlag){
            System.out.println("sendGetVersionFlag 失败");
            return;
        }

        System.out.println("连接成功");


    }

}
