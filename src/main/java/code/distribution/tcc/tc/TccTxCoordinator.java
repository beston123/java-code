package code.distribution.tcc.tc;

import code.distribution.tcc.common.*;
import code.distribution.tcc.rm.TccFlowHandler;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 〈TCC事务协调器〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2020/1/7
 */
public class TccTxCoordinator implements TxCoordinator {

    private static TxCoordinator instance = new TccTxCoordinator();

    private static ConcurrentHashMap<String, GlobalTx> globalSessionMap = new ConcurrentHashMap<>();

    public static TxCoordinator getInstance() {
        return instance;
    }

    @Override
    public String beginGlobal() {
        GlobalTx globalTx = new GlobalTx();
        globalSessionMap.put(globalTx.getXid(), globalTx);
        return globalTx.getXid();
    }

    @Override
    public boolean registerBranch(String xid, Long branchId, TxMethod tccMethod) {
        GlobalTx globalTx = globalSessionMap.get(xid);
        if (globalTx == null || globalTx.getState() != GlobalState.PROCESSING) {
            return false;
        }
        for (BranchTx branch : globalTx.getBranches()) {
            //已注册
            if (branch.getId().equals(branchId)) {
                return true;
            }
        }
        //不存在，注册上去
        globalTx.getBranches().add(new BranchTx(branchId, BranchState.INITIAL, tccMethod));
        return true;
    }

    @Override
    public boolean commitGlobal(String xid) {
        GlobalTx globalTx = globalSessionMap.get(xid);
        if (globalTx == null) {
            return false;
        }
        //如果已经提交或提交中，返回成功
        if (globalTx.getState() == GlobalState.TWO_COMMITTED || globalTx.getState() == GlobalState.TWO_COMMITTING) {
            return true;
            //否则不是处理中，则返回失败
        } else if (globalTx.getState() != GlobalState.PROCESSING) {
            return false;
        }

        //状态置为提交中
        globalTx.setState(GlobalState.TWO_COMMITTING);

        //逐个提交分支事务
        boolean flag = true;
        System.out.println("-----------二阶段提交-----------");
        for (BranchTx branch : globalTx.getBranches()) {
            //必须一阶段成功，才能提交
            if (branch.getState() == BranchState.ONE_PHASE_OK) {
                //设置为提交中
                branch.setState(BranchState.TWO_COMMITTING);
                if (TccFlowHandler.getInstance().beforeConfirm(xid, branch.getId(), branch.getMethod())) {
                    branch.setState(BranchState.TWO_COMMIT_OK);
                } else {
                    flag = false;
                }
            }
        }
        //全部成功，则事务提交成功
        if (flag) {
            globalTx.setState(GlobalState.TWO_COMMITTED);
        }
        return flag;
    }

    @Override
    public boolean rollbackGlobal(String xid) {
        GlobalTx globalTx = globalSessionMap.get(xid);
        if (globalTx == null) {
            return false;
        }
        //如果已经回滚或回滚中，返回成功
        if (globalTx.getState() == GlobalState.TWO_ROLLED_BACK || globalTx.getState() == GlobalState.TWO_ROLLING_BACK) {
            return true;
            //否则不是处理中，则返回失败
        } else if (globalTx.getState() != GlobalState.PROCESSING) {
            return false;
        }

        //状态置为提交中
        globalTx.setState(GlobalState.TWO_ROLLING_BACK);

        //逐个回滚分支事务
        boolean flag = true;
        System.out.println("-----------二阶段回滚-----------");
        for (BranchTx branch : globalTx.getBranches()) {
            //一阶段成功或执行中
            if (branch.getState() == BranchState.ONE_PHASE_OK || branch.getState() == BranchState.INITIAL) {
                //设置为回滚中
                branch.setState(BranchState.TWO_ROLLING_BACK);
                if (TccFlowHandler.getInstance().beforeCancel(xid, branch.getId(), branch.getMethod())) {
                    branch.setState(BranchState.TWO_ROLLBACK_OK);
                } else {
                    flag = false;
                }
            }
        }
        //全部成功，则事务回滚成功
        if (flag) {
            globalTx.setState(GlobalState.TWO_ROLLED_BACK);
        }
        return flag;
    }

    public boolean reportBranch(String xid, Long branchId, BranchState state) {
        GlobalTx globalTx = globalSessionMap.get(xid);
        if (globalTx == null || globalTx.getState() != GlobalState.PROCESSING) {
            return false;
        }

        // 分支事务状态必须严格按状态机变更
        for (BranchTx branch : globalTx.getBranches()) {
            if (!branchId.equals(branch.getId())) {
                continue;
            }
            if (branch.getState() == BranchState.TWO_COMMIT_OK || branch.getState() == BranchState.TWO_ROLLBACK_OK) {
                return false;
            }
            branch.setState(state);
            return true;
        }
        return false;
    }

}
