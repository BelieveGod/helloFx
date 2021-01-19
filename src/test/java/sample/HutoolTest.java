package sample;

import cn.hutool.Hutool;
import cn.hutool.http.HttpUtil;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/1/19 14:49
 */
public class HutoolTest {

    @Test
    public void test(){
        String url = "localhost:8080/file";
        String url2 = "http://localhost/file/BFF-V2.0-3-0-g8c21336(4850驱动器).bin";
        String content = HttpUtil.get(url2);
        System.out.println(content);
        content.getBytes();

    }

    @Test
    public void test2() throws IOException {
        String file = "D:/agxDocument/BFF-V2.0-3-0-g8c21336(4850驱动器).bin";
        String string = FileUtils.readFileToString(new File(file), "utf-8");
        byte[] bytes = FileUtils.readFileToByteArray(new File(file));

        // 网络
        String url2 = "http://localhost/file/BFF-V2.0-3-0-g8c21336(4850驱动器).bin";
        String content = HttpUtil.get(url2);

        Assert.assertEquals(content, string);

    }

    @Test
    public void test3() throws IOException {
        String file = "D:/agxDocument/BFF-V2.0-3-0-g8c21336(4850驱动器).bin";
        byte[] bytes = FileUtils.readFileToByteArray(new File(file));
        String string = FileUtils.readFileToString(new File(file), "ISO-8859-1");
        String string2 = FileUtils.readFileToString(new File(file), "utf-8");

        byte[] bytes1 = string.getBytes();
        byte[] bytes2 = string2.getBytes();
        String string3 = new String(bytes, "ISO-8859-1");
        Assert.assertEquals(string,string3);
        Assert.assertArrayEquals(bytes,bytes1);
        Assert.assertEquals(string,string2);
    }

    @Test
    public void  test4() throws IOException {
        // 网络
        String url2 = "http://localhost/file/BFF-V2.0-3-0-g8c21336(4850驱动器).bin";
        byte[] bytes = HttpUtil.downloadBytes(url2);

        String file = "D:/agxDocument/BFF-V2.0-3-0-g8c21336(4850驱动器).bin";
        byte[] bytes2 = FileUtils.readFileToByteArray(new File(file));

        Assert.assertArrayEquals(bytes,bytes2);
    }
}
