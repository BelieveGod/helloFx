package sample.uart;

import sample.AgxContext;
import sample.Controller;
import sample.service.SerialPortService;
import sample.uart.dto.ACK_t;
import sample.uart.dto.HandShark_t;
import sample.uart.enumeration.SystemStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/1/20 17:25
 */
public class UartContext implements AgxContext {




    private  static class InstanceHolder{
        private static final UartContext instance = new UartContext();
    }

    public static UartContext getInstance(){
        return InstanceHolder.instance;
    }

    // 控制程序行为的状态数据
    public SystemStatus systemStatus = SystemStatus.ENOMORE;
    public boolean lostFrame=false;
    public boolean lastFrame=false;
    public boolean clearFlash=false;

    // 传输数据包过程中的状态量
    public int totalPackageSize;
    public int writteenSize;
    public int ackCount=0;

    // 串口返回的数据
    public BlockingQueue<HandShark_t> handSharkQueue = new LinkedBlockingDeque();
    public BlockingQueue<ACK_t> ackQueue = new LinkedBlockingDeque<>();

    // 通过命令获得设备的数据
    public List<Byte> get_Version = new ArrayList<>();

    // 从加载文件中得到的数据
    public List<Byte> version=new ArrayList<>();
    public List<Byte> data=new ArrayList<>();;

    // UI
    public Controller controller;
}
