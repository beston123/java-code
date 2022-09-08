package code.distribution.tcc.common;

import code.distribution.id.snowflake.IdWorker;

import java.util.HashMap;
import java.util.Map;

/**
 * 〈事务容器〉<p>
 * 〈功能详细描述〉
 *
 * @author tianwu
 * @date 2022/09/07
 */
public class TxContainer {

    private static final ThreadLocal<String> XID = new ThreadLocal<>();

    private static final ThreadLocal<Map<String, Long>> BRANCH_ID_MAP = ThreadLocal.withInitial(HashMap::new);

    public static String getXid(){
        return XID.get();
    }

    public static void setXid(String xid) {
        XID.set(xid);
    }

    public static Long getBranchId(String actionName){
        return BRANCH_ID_MAP.get().get(actionName);
    }

    public static Long setBranchId(String actionName){
        Long branchId = IdWorker.getInstance().nextId();
        BRANCH_ID_MAP.get().put(actionName, branchId);
        return branchId;
    }

    public static void remove(){
        XID.remove();
        BRANCH_ID_MAP.get().clear();
    }

}
