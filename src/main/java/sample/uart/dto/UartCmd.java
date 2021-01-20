package sample.uart.dto;

public class UartCmd {
    public static final short ChassisToACK_Final=(short)0x8f10;
    public static final short ChassisToACK_LOST=(short)0x13f5;
    public static final short ChassisToACK_Count=(short)0x5623;
    public static final short APPToACK_HandShake=(short)0x2345;
    public static final short ChassisToACK_HandShake=(short)0x5321;
    public static final short HandShake_ChassisToApp =(short)0x2913;
    public static final short ChassisToACK_Clear=(short)0x7364;
    public static final int Transmitted_DataSize=1024;

}
