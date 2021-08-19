package code.distribution.raft.client;

import code.distribution.raft.model.Command;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 〈客户端请求〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2019-11-04
 */
@Data
@AllArgsConstructor
public class ClientReq implements Serializable {

    private boolean read;

    private Command command;

}
