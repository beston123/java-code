package code.distribution.tcc.rm;

import code.distribution.tcc.common.BranchState;
import code.distribution.tcc.common.TxMethod;
import code.distribution.tcc.tc.TccTxCoordinator;
import code.distribution.tcc.tc.TxCoordinator;

/**
 * 〈一句话功能简述〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2020/1/9
 */
public class TccResManager implements ResManager{

    private TxCoordinator tc = TccTxCoordinator.getInstance();

    private static ResManager instance = new TccResManager();

    public static ResManager getInstance() {
        return instance;
    }

    @Override
    public boolean registerBranch(String xid, Long branchId, TxMethod tccMethod) {
        return tc.registerBranch(xid, branchId, tccMethod);
    }

    @Override
    public boolean onePhase(String xid, Long branchId) {
        return tc.reportBranch(xid, branchId, BranchState.ONE_PHASE_OK);
    }

    @Override
    public boolean commit(String xid, Long branchId) {
        return tc.reportBranch(xid, branchId, BranchState.TWO_COMMIT_OK);
    }

    @Override
    public boolean rollback(String xid, Long branchId) {
        return tc.reportBranch(xid, branchId, BranchState.TWO_ROLLBACK_OK);
    }

}
