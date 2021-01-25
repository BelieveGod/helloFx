package sample.can;

import sample.AgxContext;
import sample.view.MainStageController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 *
 */
public class CanContext implements AgxContext {


    public CanContext(){
        init();
    }

    private void init(){
        Arrays.fill(writeDataBuf,(byte)0);
    }


    // MainStageController.onLoadFile加载文件获得的数据
    public List<Byte> data=new ArrayList<>();
    public List<Byte> version=new ArrayList<>();
    public byte nodeId;


    // CanStrategy.sendCheckCMD 获得的数据
    public int checkCmdVersion;
    public int fwType;


    // CanStrategy.sendGetVersionCMD获得的数据
    public byte[] getVersionBuf = new byte[32];

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


    // UI 相关
    public MainStageController mainStageController;
}
