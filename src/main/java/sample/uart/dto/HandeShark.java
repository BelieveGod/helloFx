package sample.uart.dto;

public class HandeShark {
    public short header;
    public short typedef;
    public short count;
    public byte[] version=new byte[32];
    public short tailer;
}
