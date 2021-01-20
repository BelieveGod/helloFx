package sample;

import cn.hutool.http.HttpUtil;
import javafx.application.Platform;
import org.apache.commons.io.FileUtils;
import sample.service.SerialPortService;
import sample.support.AgxResult;
import sample.support.PortParam;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/1/22 11:00
 */
public abstract class  AbstractStrategy implements OperateStrategy{

    protected SerialPortService serialPortService;



    @Override
    public boolean disconnect() {
        AgxResult agxResult = serialPortService.closeSeriaPort();
        if (agxResult.getCode() == 200) {
            updateMessage("已关闭串口");
            return true;
        }else{
            updateMessage("关闭串口失败");
            return false;
        }
    }

    abstract protected void updateMessage(String message);

    @Override
    public boolean loadFile(File file) throws IOException {
        byte[] bytes1 = FileUtils.readFileToByteArray(file.getAbsoluteFile());
        return paserFile(bytes1,file.getAbsolutePath());
    }

    @Override
    public boolean loadFileOnNet(String url) {
        byte[] bytes1= HttpUtil.downloadBytes(url);
        return paserFile(bytes1,url);
    }

    abstract protected boolean paserFile(byte[] file,String url);






    protected List<Byte> getVersion(List<Byte> fileByte) {
        for (int i = 0; i < fileByte.size(); i++) {

            if (i > 1) {
                if (fileByte.get(i - 1) == (0x0d) && fileByte.get(i) == (0x0a)) {
                    return fileByte.subList(0, i + 1);
                }
            }
        }
        return new ArrayList<>();
    }
}
