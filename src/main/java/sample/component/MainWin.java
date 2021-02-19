package sample.component;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.BorderPane;
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
