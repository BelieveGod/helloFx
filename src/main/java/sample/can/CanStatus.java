package sample.can;

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

    public static CanStatus getInstance(){
        return CanStatusHoler.INSTANCE;
    }

    public List<Byte> data;
    public List<Byte> version;
    public byte nodeId;

    public int checkCmdVersion;
    public int fwType;
    public byte[] getVersionBuf = new byte[32];
    public boolean serial_is_open=false;

    public byte ack_node_id;
    public byte cmd_status;
    public byte[] can_rx_buf = new byte[32];

    public BlockingDeque<Boolean> result = new LinkedBlockingDeque<>();

}
