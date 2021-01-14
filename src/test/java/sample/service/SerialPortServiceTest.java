package sample.service;


import gnu.io.SerialPort;
import org.junit.Test;
import org.junit.runner.RunWith;
import sample.can.CanListener;
import sample.support.AgxResult;
import sample.support.PortParam;
import sample.util.HexUtils;

import java.io.IOException;
import java.util.Scanner;
import java.util.TooManyListenersException;

import static org.junit.Assert.*;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/1/18 18:29
 */

public class SerialPortServiceTest {
    public static void main(String[] args) throws IOException, TooManyListenersException {
        SerialPortService serialPortService=new SerialPortService();
        PortParam portParam = new PortParam();
        portParam.setPortName("COM12");
        final int bauldRate = 460800;
        portParam.setBauldRate(bauldRate);
        portParam.setDataBits(PortParam.DATABITS_8);
        portParam.setStopBits(PortParam.STOPBITS_1);
        portParam.setParity(PortParam.PARITY_NONE);
        AgxResult agxResult = serialPortService.openSerialPort(portParam);
        if(agxResult.getCode().equals(200)){
            System.out.println("打开串口成功");
        }
        else{
            System.out.println("打开串口成功");
            return;
        }
        SerialPort theSerialPort = serialPortService.getTheSerialPort();
        new Thread(() -> {
            try {
                serialPortService.addEventListener(new CanListener(theSerialPort));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TooManyListenersException e) {
                e.printStackTrace();
            }
            theSerialPort.notifyOnDataAvailable(true);
        });

        new Thread(() -> {
            byte[] bytes = HexUtils.hexString2bytes("aa55f50f2804555555550000000000000000000000000000000000000000000000000000000000fe");
            try {
                serialPortService.writeData(bytes, 0, bytes.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        new Thread(() -> {
            byte[] bytes2 = HexUtils.hexString2bytes("aa55f30f2800000000000000000000000000000000000000000000000000000000000000000000fe");
            try {
                serialPortService.writeData(bytes2, 0, bytes2.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });





        System.out.println("Thread.currentThread().getName() = " + Thread.currentThread().getName());
        System.out.println("按任意键退出");

    }


}
