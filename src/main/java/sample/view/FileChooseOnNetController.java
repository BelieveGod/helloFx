package sample.view;

import cn.hutool.Hutool;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import sample.Main;
import sample.model.AgxFile;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/1/26 9:00
 */
@Slf4j
public class FileChooseOnNetController implements Initializable {

    @FXML
    private TableView<AgxFile> tableView;
    @FXML
    private TableColumn<AgxFile,String> nameColumn;
    @FXML
    private TableColumn<AgxFile, String> infoColumn;
    @FXML
    private Button chooseBtn;
    @FXML
    private Button cancelBtn;

    private Main main;

    private Stage dialogStage;

    private ObservableList<AgxFile> fileList = FXCollections.observableArrayList();
    private String choosenUrl;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
//        AgxFile a1 = new AgxFile("a1", "d:", "升级");
//        AgxFile a2 = new AgxFile("a2", "d:", "升级");
//        AgxFile a3 = new AgxFile("a3", "d:", "升级");
//        fileList.addAll(a1,a2,a3);

        // 获取文件列表
        Thread thread = new Thread(()->{

            try {
                String content = HttpUtil.get("localhost:8080/firmware/files");
                JSONArray jsonList = JSONUtil.parseArray(content);
                for(int i=0;i<jsonList.size();i++){
                    JSONObject jsonObject = jsonList.getJSONObject(i);
                    AgxFile agxFile = new AgxFile();
                    agxFile.setName(jsonObject.getStr("name"));
                    agxFile.setUrl(jsonObject.getStr("url"));
                    agxFile.setInfo(jsonObject.getStr("info"));
                    fileList.add(agxFile);
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        },"Thread-getFileList");
        thread.start();

        nameColumn.setCellValueFactory(cellData  -> new SimpleStringProperty(cellData.getValue().getName()) );
        infoColumn.setCellValueFactory(cellData  -> new SimpleStringProperty(cellData.getValue().getInfo()) );
        tableView.setItems(fileList);

    }

    @FXML
    private void onCancel(ActionEvent event){
        choosenUrl=null;
        dialogStage.close();
    }

    @FXML
    private void onChoose(ActionEvent event){
        AgxFile selectedItem = tableView.getSelectionModel().getSelectedItem();
        if(selectedItem==null){
            log.info("什么都没选中");
            return;
        }
        choosenUrl = selectedItem.getUrl();
        dialogStage.close();
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public String getChoosenUrl() {
        return choosenUrl;
    }
}
