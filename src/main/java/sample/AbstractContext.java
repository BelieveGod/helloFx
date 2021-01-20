package sample;

import java.util.List;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/1/22 11:04
 */
public class AbstractContext implements AgxContext{
    public List<Byte> data;
    public List<Byte> version;
    // UI 相关
    public Controller controller;
}
