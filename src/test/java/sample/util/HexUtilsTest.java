package sample.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/1/18 19:04
 */
public class HexUtilsTest {

    @Test
    public void test(){
        byte[] bytes = HexUtils.hexString2bytes("a1f");
        System.out.println(bytes);
    }

    @Test
    public void test2(){
        byte[] bytes = HexUtils.hexString2bytes("4430315f56312e312d332d6735663239636331200d0a37600d0b287c0b230000");
        String s = new String(bytes);
        System.out.println("s = " + s);
    }
}
