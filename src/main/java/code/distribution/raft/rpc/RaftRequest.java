package code.distribution.raft.rpc;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 〈Rpc请求〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2019-11-05
 */
@Data
@AllArgsConstructor
public class RaftRequest implements Serializable {

    /**
     * 请求类型
     *
     * @see code.distribution.raft.enums.RequestType
     */
    private int requestType;

    private Object request;

}
