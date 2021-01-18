package sample.uart.dto;

public class Header_T {
    public short header;
    public short count;
    public short len;
    public short flag;
    public short dataLen;
    public short size_L;
    public short size_H;
    public byte[] vesion=new byte[32];
}
