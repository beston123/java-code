package code.distribution.raft;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 〈配置〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2019-11-05
 */
@Data
public class RaftConfig {

    /**
     * 机器节点Id
     */
    private String nodeId;

    /**
     * 集群节点配置，以英文逗号,或分号;分隔
     */
    private String clusterNodes;

    public String[] parseClusterNodes(){
        if (StringUtils.isBlank(clusterNodes)) {
            throw new IllegalArgumentException("集群节点配置不能为空");
        }
        if (clusterNodes.contains(",")) {
            return clusterNodes.split(",");
        } else if (clusterNodes.contains(";")) {
            return clusterNodes.split(";");
        } else {
            return new String[]{clusterNodes};
        }
    }

}
