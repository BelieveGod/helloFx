package sample.support;

import lombok.Data;

/**
 * @author LTJ
 * @version 1.0
 * @date 2020/11/19 15:19
 */
@Data
public class AgxResult {

    private Integer code;
    private String msg;
    private Object data;

    public AgxResult(Integer code, String msg, Object data){
        this.code=code;
        this.msg=msg;
        this.data=data;
    }

    public static AgxResult ok(String msg,Object data){
        return new AgxResult(200,msg,data);
    }

    public static AgxResult ok(Object data){
        return  ok("请求成功",data);
    }

    public static AgxResult ok(){
        return ok(null);
    }

    public static AgxResult fail(String msg,Object data){
        return new AgxResult(400, msg, data);
    }

    public static AgxResult fail(Object data){
        return fail( "请求失败", data);
    }

    public static AgxResult fail(){
        return fail(null);
    }


}
