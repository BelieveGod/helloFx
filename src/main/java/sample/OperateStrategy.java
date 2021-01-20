package sample;

import sample.support.PortParam;

import java.io.File;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/1/21 11:52
 */
public interface OperateStrategy {

    boolean connect( PortParam portParam);
    boolean disconnect();
    boolean loadFile(File file) throws Exception;
    boolean loadFileOnNet(String url);
    boolean upgrade();
    void initUI(Controller controller);

}
