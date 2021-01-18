package sample.can;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 单例CAN 状态，因为原本的C++代码用指针传值，java 没办法，改用统一状态管理
 */
public class CanStatus {

    private static class CanStatusHoler{
        private static final CanStatus INSTANCE = new CanStatus();
    }

    private CanStatus(){
        init();
    }

    private void init(){
        Arrays.fill(writeDataBuf,(byte)0);
    }

    public static CanStatus getInstance(){
        return CanStatusHoler.INSTANCE;
    }

    // Controller.onLoadFile加载文件获得的数据
    public List<Byte> data;
    public List<Byte> version;
    public byte nodeId;

    // CanService.sendCheckCMD 获得的数据
    public int checkCmdVersion;
    public int fwType;


    // CanService.sendGetVersionCMD获得的数据
    public byte[] getVersionBuf = new byte[32];
    public boolean serial_is_open=false;

    // CanListener 串口监听获取的数据
    public byte ack_node_id;
    public byte cmd_status;
    public byte[] can_rx_buf = new byte[32];
    public BlockingDeque<Boolean> result = new LinkedBlockingDeque<>();


    // 传输数据产生的数据
    public int totalSize;
    public int bytesWritten;
    public int bytesToWrite;
    public int read_data_number;
    public byte[] writeDataBuf = new byte[1026];


}
