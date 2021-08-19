package code.distribution.raft.rpc;

import lombok.Data;

import java.io.Serializable;

/**
 * 〈Rpc响应结果〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2019-11-05
 */
@Data
public class RaftResponse implements Serializable {

    private boolean success;

    private String errorMsg;

    private Object response;

    public RaftResponse(boolean success, String errorMsg, Object response) {
        this.success = success;
        this.errorMsg = errorMsg;
        this.response = response;
    }

    public static RaftResponse buildFail(String errorMsg) {
        return new RaftResponse(false, errorMsg, null);
    }

    public static RaftResponse buildSuccess(Object response) {
        return new RaftResponse(true, null, response);
    }
}
