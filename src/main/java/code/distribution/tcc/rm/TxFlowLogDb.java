package code.distribution.tcc.rm;

import code.distribution.tcc.common.TxFlowLogDO;
import code.distribution.tcc.common.TxState;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 〈一句话功能简述〉<p>
 *
 * @author zixiao
 * @date 2020/1/8
 */
public class TxFlowLogDb {

    /**
     * xid+branchId => TxFlowLogDO
     */
    private static ConcurrentHashMap<String, TxFlowLogDO> txFlowTable = new ConcurrentHashMap<>();

    public static TxFlowLogDO select(String xid, Long branchId){
        return txFlowTable.get(TxFlowLogDO.buildKey(xid, branchId));
    }

    public static TxFlowLogDO selectForUpdate(String xid, Long branchId){
        return txFlowTable.get(TxFlowLogDO.buildKey(xid, branchId));
    }

    public static boolean insert(TxFlowLogDO txFlowLogDO){
        TxFlowLogDO old = txFlowTable.putIfAbsent(txFlowLogDO.getUniqueKey(), txFlowLogDO);
        return old == null;
    }

    public static boolean updateStatus(String xid, Long branchId, TxState status, TxState oldStatus) {
        TxFlowLogDO txFlowLogDO = selectForUpdate(xid, branchId);
        if (txFlowLogDO != null && oldStatus.equals(txFlowLogDO.getState())) {
            txFlowLogDO.setState(status);
            return true;
        }
        return false;
    }

    public static boolean delete(String xid, Long branchId){
        txFlowTable.remove(TxFlowLogDO.buildKey(xid, branchId));
        return true;
    }

}
