package sample.uart.dto;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/1/20 17:04
 */
public class Receive_Status_t {
    public short count;
    public boolean lostFrame;
    public boolean lastFrame;
    public boolean clearFlash;

}
