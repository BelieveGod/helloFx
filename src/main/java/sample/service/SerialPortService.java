package sample.service;

import gnu.io.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import sample.exception.OpendPortException;
import sample.support.AgxResult;
import sample.support.PortParam;
import sample.SerialObserver;
import sample.util.HexUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
    private AtomicInteger count=new AtomicInteger(0);
    private List<Byte> readBuffer = new LinkedList<>();

    // 存在多线程增删问题
    private List<SerialObserver> observerList = new ArrayList<>();
//    public SerialListener serialListener=new SerialListener();

    /**
     * 列出所有可用串口
     */
    public static Optional<List<Map<String,String>>> listAllPorts(){

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
            theSerialPort.close();
            throw new OpendPortException("串口已经打开");
        }

        try {
            theCommPortIdentifier =CommPortIdentifier.getPortIdentifier(portParam.getPortName());
            theSerialPort = theCommPortIdentifier.open("固件升级", 3000);
            theSerialPort.setSerialPortParams(portParam.getBauldRate(),portParam.getDataBits(),portParam.getStopBits(),portParam.getParity());

            theSerialPort.addEventListener(new SerialListener());
            theSerialPort.notifyOnDataAvailable(true);

        } catch (NoSuchPortException e) {
            log.error("不存在串口:"+portParam.getPortName(),e);

            return AgxResult.fail("不存在串口",null);
        } catch (PortInUseException e) {
            log.error("端口已占用:"+portParam.getPortName(),e);
            return AgxResult.fail("端口已占用",null);
        } catch (UnsupportedCommOperationException e) {
            log.error("不支持的串口操作:"+portParam.getPortName(),e);
            return AgxResult.fail("不支持的串口操作",null);
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        }

        return AgxResult.ok("打开串口成功",null);

    }


    /**
     * 关闭串口
     *
     */

    public AgxResult closeSeriaPort(){
        if(theSerialPort!=null){
            theSerialPort.close();
        }
        theSerialPort =null;
        theCommPortIdentifier =null;
        observerList.clear();
        return AgxResult.ok("关闭串口成功",null);
    }

    /**
     * 写数据
     */
    public void writeData(byte[] data,int off,int n) throws IOException {
        if(theSerialPort==null){
            log.warn("串口没有打开，不能写入始数据");
            return ;
        }
        OutputStream outputStream = theSerialPort.getOutputStream();
        final String string = HexUtils.bytesToHexString(data);
        log.info("输入指令:{}",string);
        outputStream.write(data,off,n);
        outputStream.flush();
        outputStream.close();
    }

    public void addObserver(SerialObserver observer){
        observerList.add(observer);
    }

    public void removeObserver(SerialObserver observer){
        observerList.remove(observer);
    }

    public void clearObservers(SerialObserver observer){
        observerList.clear();
    }

    /**
     * 读数据
     */
    public void addEventListener(SerialPortEventListener listener) throws IOException, TooManyListenersException {
        theSerialPort.addEventListener(listener);

    }

    public SerialPort getTheSerialPort() {
        return theSerialPort;
    }

    public class SerialListener implements SerialPortEventListener {
        @Override
        public void serialEvent(SerialPortEvent ev) {
            switch (ev.getEventType()) {
                case SerialPortEvent.BI:
                case SerialPortEvent.OE:
                case SerialPortEvent.FE:
                case SerialPortEvent.PE:
                case SerialPortEvent.CD:
                case SerialPortEvent.CTS:
                case SerialPortEvent.DSR:
                case SerialPortEvent.RI:
                case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                    break;
                case SerialPortEvent.DATA_AVAILABLE:
                    // 数据接收处理函数
                    // 1. 读取数据

                    readComm();
                    // 父类的模板方法
                    inform();
                    break;
                default:
                    break;
            }
        }

        private void readComm(){
            try (InputStream in=theSerialPort.getInputStream()){
                int available = in.available();
                byte[] tempBuffer = new byte[in.available()];
                // k用来记录实际读取的字节数
                int k = 0;
                while ((k = in.read(tempBuffer)) != -1) {
                    if (1 > k) {
                        break;
                    }
                    String[] dataHex = HexUtils.bytesToHexStrings(tempBuffer, 0, k);
                    String s = HexUtils.bytesToHexString(tempBuffer, 0, k);
                    Byte[] bytes = ArrayUtils.toObject(tempBuffer);
                    readBuffer.addAll(Arrays.asList(bytes).subList(0, k));
                    // 读到结束符或者没有读入1个字符串就推出循环
                    log.info("\n读取的数据： " + s);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void inform(){
            for (SerialObserver serialObserver : observerList) {
                // 这里同步调用，应该不会和读数据冲突
                serialObserver.handle(readBuffer);
            }
        }
    }
}
