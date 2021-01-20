package sample.util;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ByteUtils {

    public static Byte[] boxed(byte[] bytes){
        Byte[] result = new Byte[bytes.length];
        for(int i=0;i<bytes.length;i++){
            result[i] = bytes[i];
        }
        return result;
    }

    public static List<Byte> boxedAsList(byte[] bytes){
        Byte[] bytes1 = ArrayUtils.toObject(bytes);
        List<Byte> list = new LinkedList<>();
        list.addAll(Arrays.asList(bytes1));
        return list;

    }

    public static byte[] deBoxed(Byte[] bytes){
        byte[] result = new byte[bytes.length];
        for(int i=0;i<bytes.length;i++){
            result[i] = bytes[i];
        }
        return result;
    }

    public static String getString(List<Byte> list){
        byte[] bytes = deBoxed(list.toArray(new Byte[0]));
        return new String(bytes);
    }

    public static byte[] deBoxed(List<Byte> list){
        return deBoxed(list.toArray(new Byte[0]));
    }
}
