package sample.can;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import sample.can.dto.CmdFrame;
import sample.can.dto.CmdList;
import sample.SerialObserver;
import sample.util.HexUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/1/25 13:50
 */
@Slf4j
public class CanObserver implements SerialObserver {
    private List<String> receiveData = new LinkedList<>();
    private CanContext canContext;


    public CanObserver(CanContext canContext) {
        this.canContext = canContext;
    }

    @Override
    public void handle(List<Byte> readBuffer) {
        // 这段转换看有没有优化处理
        Byte[] bytes = readBuffer.toArray(new Byte[0]);
        byte[] bytes1 = ArrayUtils.toPrimitive(bytes);
        String[] dataHex = HexUtils.bytesToHexStrings(bytes1);
        receiveData.addAll(Arrays.asList(dataHex));
        readBuffer.clear();
        dispose();
    }

    private void dispose(){
        // 如果长度够一个帧
        while(receiveData.size() >= CmdFrame.sizeOf){
            if(!receiveData.get(0).equals("aa") || !receiveData.get(1).equals("55") || !receiveData.get(CmdFrame.sizeOf-1).equals("fe")){
                receiveData.remove(0);
                continue;
            }
            List<String> temp = receiveData.subList(0, CmdFrame.sizeOf);
            log.info("\n捕获指令 = " + HexUtils.hexStrings2hexString(temp.toArray(new String[0])));
            byte[] bytes = HexUtils.hexStrings2bytes(temp.toArray(new String[0]));
            CmdFrame cmd_ack = CmdFrame.from(bytes);
            canContext.cmd_status=(byte)(cmd_ack.canId &0x0f);
            canContext.ack_node_id= (byte)(cmd_ack.canId>>4 & 0xff);
            if(cmd_ack.dataLen!=0){
                System.arraycopy(cmd_ack.data, 0, canContext.can_rx_buf, 0, cmd_ack.dataLen);
            }
            // 清理报文
            temp.clear();
            // 注意是否会有比较错误的问题
            try {
                if(canContext.cmd_status == CmdList.CMD_SUCCESS && canContext.ack_node_id == canContext.nodeId){
                    canContext.result.put(true);
                }
                else{
                    canContext.result.put(false);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
