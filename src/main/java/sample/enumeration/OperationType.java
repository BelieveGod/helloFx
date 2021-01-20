package sample.enumeration;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/1/21 11:25
 */
public enum OperationType {
    RS232("USB转RS232"),CAN("USB转CAN");
    private String Chinese;

    OperationType(String chinese) {
        Chinese = chinese;
    }

    public  String getChinese() {
        return Chinese;
    }

    public static OperationType get(String chinese){
        OperationType[] values = values();
        for (OperationType value : values) {
            if (value.Chinese.equals(chinese)) {
                return value;
            }
        }
        return null;
    }


}
