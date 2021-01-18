package sample.util;

import java.util.List;

public class ByteUtils {

    public static Byte[] boxed(byte[] bytes){
        Byte[] result = new Byte[bytes.length];
        for(int i=0;i<bytes.length;i++){
            result[i] = bytes[i];
        }
        return result;
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
}
