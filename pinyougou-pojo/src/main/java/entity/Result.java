package entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package entity *
 * @since 1.0
 */
public class Result implements Serializable {
    private Boolean success;//定义是否成功
    private String message;//定义相关的信息

    //用于存储 错误信息的格式为：[{field:,msg:},{}]
    List<Error> errorsList = new ArrayList<>();

    public List<Error> getErrorsList() {
        return errorsList;
    }

    public void setErrorsList(List<Error> errorsList) {
        this.errorsList = errorsList;
    }

    public Result() {
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Result(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
