package sample.can.dto;

public class CmdList{
    public static final short ERASE=0X00;
    public static final short WRITE_INFO=0X01;
    public static final short WRITE=0X02;
    public static final short CHECK=0X03;
    public static final short SET_BAUD_RATE=0X04;
    public static final short EXCUTE=0X05;
    public static final short GET_VERSION_INFO=0X06;
    public static final short SEND_VERSION_INFO=0X07;
    public static final short CMD_SUCCESS=0X08;
    public static final short CMD_FAILD=0X09;


    // fw_type
    public static final int CAN_BL_APP=0xAAAAAAAA;
    public static final int CAN_BL_BOOT=0x55555555;

}
