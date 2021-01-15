package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class Controller implements Initializable {

    @FXML
    private Button myButton;

    @FXML
    private TextField myTextField;

    @FXML
    private TextArea textArea;

    public void initialize(URL url, ResourceBundle resourceBundle) {

    }


    // When user click on myButton
    // this method will be called.
    public void showDateTime(ActionEvent event) {
        System.out.println("Button Clicked!");
        System.out.println("Thread.currentThread().getName() = " + Thread.currentThread().getName());
        Thread currentThread = Thread.currentThread();
        Date now= new Date();

        DateFormat df = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss");
        String dateTimeString = df.format(now);
        // Show in VIEW
//        textArea.setText(dateTimeString);

        Timer timer=new Timer("定时器");
        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
                System.out.println("Thread.currentThread().getName() = " + Thread.currentThread().getName());
                System.out.println("currentThread.getName() = " + currentThread.getName());
                textArea.setText(dateTimeString);
            }
        };
        timer.schedule(timerTask,1000);


        new Thread(()->{
            textArea.setText("新线程");

        },"新线程").start();

    }




}
