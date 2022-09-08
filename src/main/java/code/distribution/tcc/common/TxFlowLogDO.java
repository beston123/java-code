package code.distribution.tcc.common;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 〈事务控制表〉<p>
 `xid`           VARCHAR(128)  NOT NULL COMMENT 'global id',
 `branch_id`     BIGINT        NOT NULL COMMENT 'branch id',
 `action_name`   VARCHAR(64)   NOT NULL COMMENT 'action name',
 `status`        TINYINT       NOT NULL COMMENT 'status(tried:1;committed:2;rollbacked:3;suspended:4)',
 `gmt_create`    DATETIME(3)   NOT NULL COMMENT 'create time',
 `gmt_modified`  DATETIME(3)   NOT NULL COMMENT 'update time',
 PRIMARY KEY (`xid`, `branch_id`)
 *
 * @author zixiao
 * @date 2020/1/7
 */
@Data
public class TxFlowLogDO implements Serializable {

    /**
     * 全局事务id
     */
    private String xid;

    /**
     * 分支事务id
     */
    private Long branchId;

    /**
     * 已Try，已提交，已回滚
     */
    private TxState state;

    private Date gmtCreate;

    private Date gmtModified;

    public TxFlowLogDO(String xid, Long branchId, TxState state) {
        this.xid = xid;
        this.branchId = branchId;
        this.state = state;
        this.gmtCreate = new Date();
        this.gmtModified = new Date();
    }

    public static TxFlowLogDO buildTry(String xid, Long branchId){
        return new TxFlowLogDO(xid, branchId, TxState.TRIED);
    }

    public static TxFlowLogDO buildRollback(String xid, Long branchId){
        return new TxFlowLogDO(xid, branchId, TxState.ROLLBACKED);
    }

    public String getUniqueKey() {
        return buildKey(xid, branchId);
    }

    public static String buildKey(String xid, Long branchId) {
        return xid + "@" + branchId;
    }

}
