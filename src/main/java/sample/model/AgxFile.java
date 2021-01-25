package sample.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/1/26 9:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgxFile {
    private String name;
    private String url;
    private String info;
}
