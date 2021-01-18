package sample.can.dto;

public class DataFrame {
    public short header;
    public short canId;
    public short frameLen;
    public short dataLen;
    public byte[] data=new byte[1026];
    public short tailer;

    // todo 不知道有没有截断的问题
    public static final short sizeOf = (short)(2+2+2+2+1026+2);
}
