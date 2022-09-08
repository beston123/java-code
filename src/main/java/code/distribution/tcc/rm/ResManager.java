package code.distribution.tcc.rm;

import code.distribution.tcc.common.TxMethod;

/**
 * 〈资源管理器〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2020/1/9
 */
public interface ResManager {

    boolean registerBranch(String xid, Long branchId, TxMethod tccMethod);

    boolean onePhase(String xid, Long branchId);

    boolean commit(String xid, Long branchId);

    boolean rollback(String xid, Long branchId);

}
