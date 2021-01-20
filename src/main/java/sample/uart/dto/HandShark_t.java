package sample.uart.dto;

import sample.can.dto.CmdFrame;
import sample.support.Byteable;
import sample.util.HexUtils;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/1/20 17:02
 */
public class HandShark_t implements Byteable {

    public short header;
    public short typedef;
    public short count;
    public byte[] version = new byte[32];
    public short tailer;

    public final static int sizeOf = (2 + 2 + 2 + 32 + 2);

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

        byte[] bytes3 = HexUtils.short2Bytes(count);
        System.arraycopy(bytes3,0,bytes,length, bytes3.length);
        length+=bytes3.length;

        System.arraycopy(version,0,bytes,length,version.length);
        length+=version.length;

        byte[] bytes5 = HexUtils.short2Bytes(tailer);
        System.arraycopy(bytes5,0,bytes,length, bytes5.length);
        return bytes;
    }

    public static HandShark_t fromBytes(byte[] bytes){
        HandShark_t handShark_t = new HandShark_t();

        handShark_t.header=HexUtils.bytes2Short(bytes,0);
        handShark_t.typedef=HexUtils.bytes2Short(bytes,2);
        handShark_t.count=HexUtils.bytes2Short(bytes,4);
        System.arraycopy(bytes,6,handShark_t.version,0,32);
        handShark_t.tailer=HexUtils.bytes2Short(bytes,38);
        return handShark_t;
    }

}
