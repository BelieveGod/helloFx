package sample.common;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/2/19 13:43
 */
public interface Mediator {
    void register(Colleague colleague);
    void remove(Colleague colleague);
    void relay(String event,Object... args);
}
