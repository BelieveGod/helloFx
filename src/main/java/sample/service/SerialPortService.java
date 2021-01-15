package sample.service;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import lombok.extern.slf4j.Slf4j;
import sample.exception.OpendPortException;
import sample.support.AgxResult;
import sample.support.PortParam;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/1/15 10:38
 */

@Slf4j
public class SerialPortService {
    // 应用程序唯一的串口对象
    private CommPortIdentifier theCommPortIdentifier;
    private SerialPort theSerialPort;

    /**
     * 列出所有可用串口
     */
    public Optional<List<Map<String,String>>> listAllPorts(){

        List<Map<String,String>> serialPorts = new ArrayList();

        // 遍历获取所有串口
        Enumeration portIdentifiers = CommPortIdentifier.getPortIdentifiers();
        while (portIdentifiers.hasMoreElements()) {
            CommPortIdentifier commPortIdentifier = (CommPortIdentifier) portIdentifiers.nextElement();
            if(CommPortIdentifier.PORT_SERIAL==commPortIdentifier.getPortType()){
                Map map=new HashMap();
                map.put("portName",commPortIdentifier.getName());
                serialPorts.add(map);
            }
        }

        return Optional.of(serialPorts);
    }


    /**
     * 根据串口参数打开串口
     *
     */

    public AgxResult openSerialPort(PortParam portParam){
        //  这里应该为了最稳妥的起见，不是null的话就当作打开了串口，逻辑后续再理。
        if(theCommPortIdentifier !=null && theSerialPort!=null){
            throw new OpendPortException("串口已经打开");
        }

        try {
            theCommPortIdentifier =CommPortIdentifier.getPortIdentifier(portParam.getPortName());
            theSerialPort = theCommPortIdentifier.open("轨迹测量", 3000);
            theSerialPort.setSerialPortParams(portParam.getBauldRate(),portParam.getDataBits(),portParam.getStopBits(),portParam.getParity());

        } catch (NoSuchPortException e) {
            log.error("不存在串口:"+portParam.getPortName(),e);

            return AgxResult.fail("不存在串口",null);
        } catch (PortInUseException e) {
            log.error("端口已占用:"+portParam.getPortName(),e);
            return AgxResult.fail("端口已占用",null);
        } catch (UnsupportedCommOperationException e) {
            log.error("不支持的串口操作:"+portParam.getPortName(),e);
            return AgxResult.fail("不支持的串口操作",null);
        }

        return AgxResult.ok("打开串口成功",null);

    }


    /**
     * 关闭串口
     *
     */

    public AgxResult closeSeriaPort(){
        theSerialPort.close();
        theSerialPort =null;
        theCommPortIdentifier =null;
        return AgxResult.ok("关闭串口成功",null);
    }

    /**
     * 写数据
     */
    public void writeData(byte[] data,int off,int n) throws IOException {
        OutputStream outputStream = theSerialPort.getOutputStream();
        outputStream.write(data,off,n);
        outputStream.close();
    }

    /**
     * 读数据
     */
    public void addEventListener(SerialPortEventListener listener) throws IOException {
        InputStream inputStream = theSerialPort.getInputStream();

    }

}
