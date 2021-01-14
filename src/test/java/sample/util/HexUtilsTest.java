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
}
