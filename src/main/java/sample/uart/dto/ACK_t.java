package sample.uart.dto;

import sample.support.Byteable;
import sample.util.HexUtils;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/1/20 17:00
 */
public class ACK_t implements Byteable {
    public short header;
    public short typedef;
    public short version;
    public short count;
    public short tailer;

    public final static int sizeOf=(2+2+2+2+2);

    @Override
    public byte[] getBytes() {
        byte[] bytes = new byte[sizeOf];
        int length=0;

        byte[] bytes1 = HexUtils.short2Bytes(header);
        System.arraycopy(bytes1,0,bytes,length, bytes1.length);
        length+=bytes1.length;

        byte[] bytes2 = HexUtils.short2Bytes(typedef);
        System.arraycopy(bytes2,0,bytes,length, bytes2.length);
        length+=bytes2.length;

        byte[] bytes3 = HexUtils.short2Bytes(version);
        System.arraycopy(bytes3,0,bytes,length, bytes3.length);
        length+=bytes3.length;

        byte[] bytes4 = HexUtils.short2Bytes(count);
        System.arraycopy(bytes4,0,bytes,length, bytes4.length);
        length+=bytes4.length;

        byte[] bytes5 = HexUtils.short2Bytes(tailer);
        System.arraycopy(bytes5,0,bytes,length, bytes5.length);
        return bytes;
    }

    public static ACK_t fromBytes(byte[] bytes){
        ACK_t ack_t = new ACK_t();
        ack_t.header=HexUtils.bytes2Short(bytes,0);
        ack_t.typedef=HexUtils.bytes2Short(bytes,2);
        ack_t.version=HexUtils.bytes2Short(bytes,4);
        ack_t.count=HexUtils.bytes2Short(bytes,6);
        ack_t.tailer=HexUtils.bytes2Short(bytes,8);

        return ack_t;
    }
}
