package sample.uart;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import sample.support.SerialObserver;
import sample.uart.dto.ACK_t;
import sample.uart.dto.HandShark_t;
import sample.uart.dto.UartCmd;
import sample.uart.enumeration.SystemStatus;
import sample.util.HexUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class UartObserver implements SerialObserver {
    private List<String> receiveData = new LinkedList<>();
    private UartContext uartContext=UartContext.getInstance();
    @Override
    public void handle(final List<Byte> readBuffer) {
        // 这段转换看有没有优化处理
        Byte[] bytes = readBuffer.toArray(new Byte[0]);
        byte[] bytes1 = ArrayUtils.toPrimitive(bytes);
        String[] dataHex = HexUtils.bytesToHexStrings(bytes1);
        receiveData.addAll(Arrays.asList(dataHex));
        readBuffer.clear();

        switch (uartContext.systemStatus){
        case EHANDSHAKE:
            handleHandShake();
            break;
        case ETRANSMISSION:
            handleTransmission();
            break;
        }
    }

    // 清理残存报文
    public void clearMessage(){
        receiveData.clear();
    }

    private void handleHandShake(){
        while(receiveData.size()>= HandShark_t.sizeOf) {
            if (!receiveData.get(0).equals("aa") || !receiveData.get(1).equals("55") ||
                !receiveData.get(HandShark_t.sizeOf - 1).equals("fe")) {
                receiveData.remove(0);
                continue;
            }
            List<String> temp = receiveData.subList(0, HandShark_t.sizeOf);
//            System.out.println("\n捕获指令 = " + HexUtils.hexStrings2hexString(temp.toArray(new String[0])));
            log.info("\n捕获指令 = " + HexUtils.hexStrings2hexString(temp.toArray(new String[0])));
            byte[] bytes2 = HexUtils.hexStrings2bytes(temp.toArray(new String[0]));
            HandShark_t handShark_t = HandShark_t.fromBytes(bytes2);
            uartContext.handSharkQueue.offer(handShark_t);
            // 清理报文
            temp.clear();
        }
    }

    private void handleTransmission(){
        while(receiveData.size() >= ACK_t.sizeOf) {
            if (!receiveData.get(0).equals("aa") || !receiveData.get(1).equals("55") ||
                !receiveData.get(ACK_t.sizeOf - 1).equals("fe")) {
                receiveData.remove(0);
                continue;
            }
            List<String> temp = receiveData.subList(0, ACK_t.sizeOf);
//            System.out.println("\n捕获指令 = " + HexUtils.hexStrings2hexString(temp.toArray(new String[0])));
            log.info("\n捕获指令 = " + HexUtils.hexStrings2hexString(temp.toArray(new String[0])));
            byte[] bytes2 = HexUtils.hexStrings2bytes(temp.toArray(new String[0]));
            ACK_t ack_t = ACK_t.fromBytes(bytes2);
            uartContext.ackQueue.offer(ack_t);
            // 清理报文
            temp.clear();
        }
    }
}
