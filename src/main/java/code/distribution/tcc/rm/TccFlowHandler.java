package code.distribution.tcc.rm;

import code.distribution.tcc.common.Callback;
import code.distribution.tcc.common.TxFlowLogDO;
import code.distribution.tcc.common.TxMethod;
import code.distribution.tcc.common.TxState;
import code.distribution.tcc.exception.TccException;

import java.lang.reflect.Method;

/**
 * 〈TCC异常流程处理器器〉<p>
 * 〈解决幂等，防悬挂，空回滚〉
 *
 * @author tianwu
 * @date 2022/09/07
 */
public class TccFlowHandler {

    private static TccFlowHandler instance = new TccFlowHandler();

    public static TccFlowHandler getInstance() {
        return instance;
    }

    /**
     * 防悬挂，防止cancel执行后，有执行try 导致预留资源无法释放
     * 先插入事务控制表记录，
     * 如果插入成功，说明第二阶段还没有执行，可以继续执行第一阶段。
     * 如果插入失败，则说明第二阶段已经执行或正在执行，则抛出异常，终止即可。
     *
     * @param xid
     * @param branchId
     * @return
     * @see io.seata.rm.tcc.TCCFenceHandler#prepareFence
     */
    public <R> R beforeTry(String xid, Long branchId, Callback<R> callback) throws Throwable {
        boolean insertSuccess = TxFlowLogDb.insert(TxFlowLogDO.buildTry(xid, branchId));
        if (!insertSuccess) {
            throw new TccException("Two phase executed or executing");
        }
        try {
            return callback.execute();
        } catch (Throwable e) {
            TxFlowLogDb.delete(xid, branchId);
            throw e;
        }
    }

    /**
     * 先锁定事务记录，
     * 如果事务记录为空，则说明是一个空提交
     */
    public boolean beforeConfirm(String xid, Long branchId, TxMethod txMethod) {
        TxFlowLogDO txFlowLogDO = TxFlowLogDb.selectForUpdate(xid, branchId);

        //如果事务记录为空，则说明是一个空提交，不允许，终止执行
        if (txFlowLogDO == null) {
            throw new TccException("Blank commit, one phase not executed.");
        }

        //幂等控制
        if (TxState.COMMITTED == txFlowLogDO.getState()) {
            //已提交，直接返回成功
            return true;
        } else if (TxState.ROLLBACKED == txFlowLogDO.getState()) {
            //已回滚，异常报警
            return false;
        }
        //状态为初始化，说明一阶段正确执行，可以执行二阶段
        return updateStatusAndInvokeMethod(txMethod, txMethod.getConfirmMethod(), xid, branchId, TxState.COMMITTED);
    }

    private boolean updateStatusAndInvokeMethod(TxMethod txMethod, Method method, String xid, Long branchId, TxState txState) {
        if (TxFlowLogDb.updateStatus(xid, branchId, txState, TxState.TRIED)) {
            txMethod.invoke(method);
            return true;
        }
        return false;
    }

    /**
     * 先锁定事务记录，
     * 如果事务记录为空，则说明是一个空回滚
     */
    public boolean beforeCancel(String xid, Long branchId, TxMethod txMethod) {
        TxFlowLogDO txFlowLogDO = TxFlowLogDb.selectForUpdate(xid, branchId);

        //如果事务记录为空，则说明是一个空回滚
        if (txFlowLogDO == null) {
            //先插入一条事务记录，确保后续的 Try 方法不会再执行
            boolean insertSuccess = TxFlowLogDb.insert(TxFlowLogDO.buildRollback(xid, branchId));
            if (insertSuccess) {
                //如果插入成功，则说明 Try 方法还没有执行，空回滚继续执行
                return true;
            } else {
                //try正在执行，回滚失败 等待下次重试
                throw new TccException("Roll back fail.");
            }
        } else {
            //幂等控制
            if (TxState.ROLLBACKED == txFlowLogDO.getState()) {
                //已回滚，直接返回成功
                return true;
            } else if (TxState.COMMITTED == txFlowLogDO.getState()) {
                //已提交，异常报警
                return false;
            }
            //状态为初始化，说明一阶段正确执行，可以执行二阶段
            return updateStatusAndInvokeMethod(txMethod, txMethod.getCancelMethod(), xid, branchId, TxState.ROLLBACKED);
        }
    }

}
