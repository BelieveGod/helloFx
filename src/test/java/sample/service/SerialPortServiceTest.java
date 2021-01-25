package sample.service;


import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import sample.support.PortParam;
import sample.util.HexUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.TooManyListenersException;

/**
 * 找到一个困扰很久的bug， 这固件不能连续接收指令，需要程序停顿一定间隔。
 * @author LTJ
 * @version 1.0
 * @date 2021/1/18 18:29
 */

public class SerialPortServiceTest {
    public static void main(String[] args)
            throws IOException, TooManyListenersException, NoSuchPortException, UnsupportedCommOperationException,
                   PortInUseException {
        SerialPortService serialPortService=new SerialPortService();
        PortParam portParam = new PortParam();
        portParam.setPortName("COM12");
        final int bauldRate = 460800;
        portParam.setBauldRate(bauldRate);
        portParam.setDataBits(PortParam.DATABITS_8);
        portParam.setStopBits(PortParam.STOPBITS_1);
        portParam.setParity(PortParam.PARITY_NONE);
        CommPortIdentifier theCommPortIdentifier = CommPortIdentifier.getPortIdentifier(portParam.getPortName());
        SerialPort theSerialPort = theCommPortIdentifier.open("轨迹测量", 3000);
        theSerialPort.setSerialPortParams(portParam.getBauldRate(),portParam.getDataBits(),portParam.getStopBits(),portParam.getParity());



//        try {
//            serialPortService.addEventListener(new CanListener(theSerialPort));
//            theSerialPort.notifyOnDataAvailable(true);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (TooManyListenersException e) {
//            e.printStackTrace();
//        }


        new Thread(() -> {
            try {
                OutputStream outputStream = theSerialPort.getOutputStream();
                byte[] bytes = HexUtils.hexString2bytes("aa55f50f2804555555550000000000000000000000000000000000000000000000000000000000fe");
                System.out.print("\n输入指令:");
                Arrays.stream(HexUtils.bytesToHexStrings(bytes, 0, bytes.length)).forEach(System.out::print);
                outputStream.write(bytes,0,bytes.length);
//                outputStream.flush();
                Thread.sleep(10);
            byte[] bytes2 = HexUtils.hexString2bytes("aa55f30f2800000000000000000000000000000000000000000000000000000000000000000000fe");
                System.out.print("\n输入指令:");
                Arrays.stream(HexUtils.bytesToHexStrings(bytes2, 0, bytes2.length)).forEach(System.out::print);
                outputStream.write(bytes2,0,bytes2.length);
            } catch (Exception  e) {
                e.printStackTrace();
            }
        },"写线程").start();








        System.out.println("\nThread.currentThread().getName() = " + Thread.currentThread().getName());
        System.out.println("按任意键退出");
        while (true);

    }
}
