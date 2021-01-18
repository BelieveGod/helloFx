package sample.can;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import lombok.extern.slf4j.Slf4j;
import sample.can.dto.CmdFrame;
import sample.can.dto.CmdList;
import sample.util.HexUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class CanListener implements SerialPortEventListener {


    // 接收的数据
    private List<String> reveivedData = new LinkedList<>();
    private SerialPort serialPort;
    private InputStream in;
    private AtomicInteger count = new AtomicInteger(0);
    private AtomicInteger count2 = new AtomicInteger(0);

    public CanListener(SerialPort serialPort) throws IOException {
        this.serialPort = serialPort;
        this.in = serialPort.getInputStream();
    }

    @Override
    public void serialEvent(SerialPortEvent ev) {
        switch (ev.getEventType()) {
            case SerialPortEvent.BI:
            case SerialPortEvent.OE:
            case SerialPortEvent.FE:
            case SerialPortEvent.PE:
            case SerialPortEvent.CD:
            case SerialPortEvent.CTS:
            case SerialPortEvent.DSR:
            case SerialPortEvent.RI:
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                break;
            case SerialPortEvent.DATA_AVAILABLE:
                // 数据接收处理函数
                // 1. 读取数据

                readComm();
                System.out.println(Thread.currentThread().getName());
                // 父类的模板方法
                handle();
                break;
            default:
                break;
        }
    }

    private void handle() {
        // 如果长度够一个帧
        while(reveivedData.size()>= CmdFrame.sizeOf){
            if(!reveivedData.get(0).equals("aa") || !reveivedData.get(1).equals("55") || !reveivedData.get(CmdFrame.sizeOf-1).equals("fe")){
                reveivedData.remove(0);
                continue;
            }
            List<String> temp = reveivedData.subList(0, CmdFrame.sizeOf);
            System.out.println("捕获指令 = " + HexUtils.hexStrings2hexString(temp.toArray(new String[0])));
            byte[] bytes = HexUtils.hexStrings2bytes(temp.toArray(new String[0]));
            CmdFrame cmd_ack = CmdFrame.from(bytes);
            CanStatus canStatus = CanStatus.getInstance();
            canStatus.cmd_status=(byte)(cmd_ack.canId &0x0f);
            canStatus.ack_node_id= (byte)(cmd_ack.canId>>4 & 0xff);
            if(cmd_ack.dataLen!=0){
                System.arraycopy(cmd_ack.data, 0, canStatus.can_rx_buf, 0, cmd_ack.dataLen);
            }
            // 清理报文
            temp.clear();
            // 注意是否会有比较错误的问题
            try {
                if(canStatus.cmd_status == CmdList.CMD_SUCCESS && canStatus.ack_node_id == canStatus.nodeId){
                    canStatus.result.put(true);
                }
                else{
                    canStatus.result.put(false);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void readComm() {

        try {
            int available = in.available();
            byte[] readBuffer = new byte[in.available()];
            // k用来记录实际读取的字节数
            int k = 0;
            while ((k = in.read(readBuffer)) != -1) {
                String[] dataHex = HexUtils.bytesToHexStrings(readBuffer, 0, k);
                reveivedData.addAll(Arrays.asList(dataHex));
                // 读到结束符或者没有读入1个字符串就推出循环
                if (1 > k) {
                    break;
                }
//                log.info("读取的数据：{}", HexUtils.hexStrings2hexString(dataHex));
//                System.out.println("读取的数据:"+ HexUtils.hexStrings2hexString(dataHex));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
