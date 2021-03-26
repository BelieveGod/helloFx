package sample.component;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import sample.common.Colleague;
import sample.common.Mediator;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/2/8 9:04
 */
public class MainWin extends BorderPane implements Mediator {
    private LeftPane leftPane;
    private CenterPane centerPane;
    private BottomPane bottomPane;

    // 容器
    private List<Colleague> list = new CopyOnWriteArrayList();
    private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor(runable ->{
        Thread t = new Thread(runable);
        t.setDaemon(true);
        return t;
    } );

    public ExecutorService executorService=Executors.newCachedThreadPool(runable->{
        Thread t = new Thread(runable);
        t.setDaemon(true);
        return t;
    });

    // 状态
    public SimpleBooleanProperty isLoadFile=new SimpleBooleanProperty(false);
    public SimpleBooleanProperty isConnecting=new SimpleBooleanProperty(false);


    public MainWin() {
        super();
        init();
        SVGPath svgPath=new SVGPath();
        svgPath.setFill(Color.BLUE);
        svgPath.setContent("m208.5,80.45313c-8,6 -25.13702,13.67206 -47,23c-18.1176,7.72997 -29.23738,15.73766 -37,23c-3.09818,2.8985 -4,6 -5,9c-1,3 -0.01624,11.01277 11,25c14.97795,19.0174 31.47818,38.47775 48,56c12.61253,13.37625 27.17239,26.79065 43,40c7.07832,5.90741 10.61731,9.07611 11,10c0.5412,1.30655 2.4568,-3.88531 4,-11c4.28691,-19.76418 8.22777,-44.92599 13,-60c3.15112,-9.95341 5,-15 6,-15c1,0 9.00906,0.92987 17,2c14.017,1.87714 25.98669,5.20776 40,6c23.98251,1.35585 39.01035,1.86458 53,3c11.00916,0.89351 21,2 28,2c4,0 10.05551,0.51059 21,2c11.07822,1.50761 17,2 19,2c1,0 1,-1 1,-4c0,-6 -0.36252,-12.00822 0,-20c0.50052,-11.03401 5.7847,-26.52368 11,-36c4.74869,-8.62845 6.4588,-12.69344 7,-14c0.38269,-0.92388 -3.96198,-1.15871 -12,-3c-23.89636,-5.47399 -59.95663,-11.25107 -94,-17c-30.96225,-5.22863 -47,-6 -56,-6c-1,0 -3.49829,-5.93797 -4,-12c-1.15472,-13.9523 -1.90927,-26.31513 1,-38c1.84,-7.39016 4,-11 4,-13c0,-1 0.23462,-2.15224 1,-4c0.5412,-1.30656 1,-2 1,-3c0,-1 -0.01459,-2.29561 -3,-2c-5.07422,0.50245 -17.18228,5.59136 -31,12c-16.22809,7.52657 -32,16 -44,22c-10,5 -18.37201,7.38509 -22,10c-2.29454,1.65381 -4.4588,3.69344 -5,5c-0.38269,0.92388 -0.4588,2.69344 -1,4c-1.14806,2.77164 0,6 0,7c0,1 0,2 0,3c0,1 0,3 0,5l0,1");
        this.setShape(svgPath);
    }

    private void init(){
        leftPane = new LeftPane(this);
        centerPane = new CenterPane(this);
        bottomPane=new BottomPane(this);
        this.setLeft(leftPane);
        this.setCenter(centerPane);
        this.setBottom(bottomPane);

    }

    public void updateProgress(double percentage){
        centerPane.updateProgress(percentage);

    }

    public void appendLog(String s){
        centerPane.appendLog(s);
    }

    @Override
    public void register(Colleague colleague) {
        if(!list.contains(colleague)){
            list.add(colleague);
            colleague.setMediator(this);
        }
    }

    @Override
    public void remove(Colleague colleague) {
        list.remove(colleague);
        colleague.setMediator(null);

    }

    @Override
    public void relay(String event,Object... args) {
        // 单线程池，串行化任务执行
        singleThreadExecutor.submit(()-> {
            // 这里可以改进成多线程通知。
            for (Colleague colleague1 : list) {
                colleague1.reveive(event, args);
            }
        });


    }
}
