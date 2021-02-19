package sample.common;

import javafx.util.Callback;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/2/19 11:11
 */
public interface Colleague {


    void reveive(String event, Object... args);
    void send(String event, Object... args);

    void setMediator(Mediator mediator);

    void on(String event, Callback<Object[], Void> callback);
}
