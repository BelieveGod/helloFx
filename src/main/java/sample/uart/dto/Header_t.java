package sample.uart.dto;

import sample.support.Byteable;
import sample.util.HexUtils;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/1/20 17:06
 */
public class Header_t implements Byteable {

    public short header;
    public short count;
    public short len;
    public short flag;
    public short datalen;
    public short size_l;
    public short size_h;
    public byte[] version = new byte[32];

    public static final short sizeOf=(2+2+2+2+2+2+2+32);

    @Override
    public byte[] getBytes() {
        byte[] bytes = new byte[sizeOf];
        int length=0;

        byte[] bytes1 = HexUtils.short2Bytes(header);
        System.arraycopy(bytes1,0,bytes,length, bytes1.length);
        length+=bytes1.length;

        byte[] bytes2 = HexUtils.short2Bytes(count);
        System.arraycopy(bytes2,0,bytes,length, bytes2.length);
        length+=bytes2.length;

        byte[] bytes3 = HexUtils.short2Bytes(len);
        System.arraycopy(bytes3,0,bytes,length, bytes3.length);
        length+=bytes3.length;


        byte[] bytes4 = HexUtils.short2Bytes(flag);
        System.arraycopy(bytes4,0,bytes,length, bytes4.length);
        length+=bytes4.length;

        byte[] bytes5 = HexUtils.short2Bytes(datalen);
        System.arraycopy(bytes5,0,bytes,length, bytes5.length);
        length+=bytes5.length;


        byte[] bytes6 = HexUtils.short2Bytes(size_l);
        System.arraycopy(bytes6,0,bytes,length, bytes6.length);
        length+=bytes6.length;

        byte[] bytes7 = HexUtils.short2Bytes(size_h);
        System.arraycopy(bytes7,0,bytes,length, bytes7.length);
        length+=bytes7.length;

        System.arraycopy(version,0,bytes,length,version.length);
        length+=version.length;

        return bytes;
    }
}
