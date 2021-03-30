package sample.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

/**
 * 实时折线图
 *
 * @author Eugen
 * @date 2021-1-29
 */
@Slf4j
public class ChartUI extends VBox {
    private Controller controller;
    private LineChart<Integer,Integer> rpmLineChart;
    private LineChart<Integer, Double> currentLineChart;
    private Series<Integer,Integer>[] rpmSeries=new Series[4];
    private Series<Integer,Double>[] currentSeries =new Series[4];
    private FlowPane legendBox;
    private static final String[] colors=new String[]{"red","orange","yellow","pink","blue","purple","green","black"};

    /**
     * 初始化
     */
    public void init() {
        log.info("init chart ui");
        initLegendBox();
        initChart();
        this.getChildren().addAll(legendBox, rpmLineChart.getParent());

        this.setAlignment(Pos.CENTER);
        this.setStyle("-fx-padding: 10,0,0,0");
        this.getStylesheets().add(getClass().getResource("/series.css").toExternalForm());
        controller = new Controller();
        // todo debug 查看大小
        this.setStyle("-fx-border-color: pink");
    }


    /**
     * 初始化折线图
     */
    private void initChart(){
        NumberAxis xAxis1 = new NumberAxis(-30, 0, 1);
        xAxis1.setMinorTickVisible(false);

        NumberAxis yAxis1 = new NumberAxis(-4500, 4500, 750);
        yAxis1.setPrefWidth(50);
        yAxis1.setPrefWidth(50);
        yAxis1.setPrefWidth(50);

        rpmLineChart = new LineChart(xAxis1, yAxis1);
        rpmLineChart.setId("rpm");
        rpmLineChart.setLegendVisible(false);
        rpmLineChart.prefHeightProperty().bind(heightProperty().multiply(0.8));
        rpmLineChart.maxHeightProperty().bind(heightProperty().multiply(0.8));
//        rpmLineChart.setMaxSize(1000, 750);
//        rpmLineChart.setMinHeight(800);
        for(int i=0;i<rpmSeries.length;i++){
            rpmSeries[i] = new Series<>();
        }
        rpmLineChart.getData().addAll(rpmSeries);


        NumberAxis xAxis2 = new NumberAxis(-30, 0, 1);
        xAxis2.setMinorTickVisible(false);
        NumberAxis yAxis2 = new NumberAxis(-5, 5, 1);
        currentLineChart = new LineChart(xAxis2, yAxis2);
        currentLineChart.setId("speed");
        currentLineChart.setLegendVisible(false);

        for(int i = 0; i < currentSeries.length; i++){
            currentSeries[i] = new Series<>();
        }
        currentLineChart.getData().addAll(currentSeries);

        yAxis2.setSide(Side.RIGHT);
        yAxis2.prefWidthProperty().bind(yAxis1.widthProperty());
        yAxis2.minWidthProperty().bind(yAxis1.widthProperty());
        yAxis2.maxWidthProperty().bind(yAxis1.widthProperty());

        currentLineChart.translateXProperty().bind(yAxis1.widthProperty());

        currentLineChart.prefWidthProperty().bind(rpmLineChart.widthProperty());
        currentLineChart.maxHeightProperty().bind(rpmLineChart.maxHeightProperty());
        currentLineChart.minWidthProperty().bind(rpmLineChart.widthProperty());
        currentLineChart.maxWidthProperty().bind(rpmLineChart.widthProperty());
        StackPane stackPane = new StackPane();
//        VBox stackPane = new VBox();
        stackPane.getChildren().addAll(rpmLineChart, currentLineChart);
        stackPane.setPadding(new Insets(0,50,0,0));

        currentLineChart.setCreateSymbols(false);
        rpmLineChart.setCreateSymbols(false);
        currentLineChart.setAnimated(false);
        rpmLineChart.setAnimated(false);

        // todo 调整折线图的大小
        rpmLineChart.setStyle("-fx-border-color: purple");

    }

    /**
     * 初始化图例
     */
    private void initLegendBox(){
        legendBox = new FlowPane();
        Label[] labels = new Label[8];
        labels[0] = new Label("rpm1");
        labels[1] = new Label("rpm2");
        labels[2] = new Label("rpm3");
        labels[3] = new Label("rpm4");
        labels[4] = new Label("current1");
        labels[5] = new Label("current2");
        labels[6] = new Label("current3");
        labels[7] = new Label("current4");

        for(int i=0;i<labels.length;i++){
            labels[i].setStyle("-fx-text-fill: "+colors[i]+";-fx-border-color: grey;-fx-border-width: 1px;-fx-padding: 2px");
        }
        legendBox.setHgap(3);
        legendBox.setVgap(3);
        legendBox.setAlignment(Pos.CENTER);
        legendBox.getChildren().addAll(labels);
        // todo debug
        legendBox.setStyle("-fx-border-color: red");
    }


    public static ChartUI getInstance() {
        return ChartUI.SingletonClassInstance.instance;
    }

    private static class SingletonClassInstance {
        private static final ChartUI instance = new ChartUI();
    }

    private ChartUI() {
    }

    /* ============== Controller ========================*/
    private class Controller{

        public Controller(){
            init();
        }
        private void init(){

        }

        /**
         * 重置UI
         */
        private void resetUI(){
            for(int i=0;i<4;i++){
                rpmSeries[i].getData().clear();
                currentSeries[i].getData().clear();
            }
        }

    }
}
