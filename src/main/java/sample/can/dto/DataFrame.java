package sample.can.dto;

import sample.util.HexUtils;

public class DataFrame {
    public short header;
    public short canId;
    public short frameLen;
    public short dataLen;
    public byte[] data=new byte[1026];
    public short tailer;

    // todo 不知道有没有截断的问题
    public static final short sizeOf = (short)(2+2+2+2+1026+2);

    public byte[] getBytes(){
        byte[] bytes = new byte[sizeOf];
        int length=0;

        byte[] bytes1 = HexUtils.short2Bytes(header);
        System.arraycopy(bytes1,0,bytes,length, bytes1.length);
        length+=bytes1.length;

        byte[] bytes2 = HexUtils.short2Bytes(canId);
        System.arraycopy(bytes2,0,bytes,length, bytes2.length);
        length+=bytes2.length;

        byte[] bytes3 = HexUtils.short2Bytes(frameLen);
        System.arraycopy(bytes3,0,bytes,length, bytes3.length);
        length+=bytes3.length;

        byte[] bytes4 = HexUtils.short2Bytes(dataLen);
        System.arraycopy(bytes4,0,bytes,length, bytes4.length);
        length+=bytes4.length;

        System.arraycopy(data,0,bytes,length,data.length);
        length+=data.length;

        byte[] bytes6 = HexUtils.short2Bytes(tailer);
        System.arraycopy(bytes6,0,bytes,length, bytes6.length);
        return bytes;
    }



}
