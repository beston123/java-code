package code.distribution.tcc.tm;

import code.distribution.at.common.RoleType;
import code.distribution.at.utils.Log;
import code.distribution.tcc.common.TxContainer;
import code.distribution.tcc.tc.TccTxCoordinator;
import code.distribution.tcc.tc.TxCoordinator;

/**
 * 〈一句话功能简述〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2020/1/7
 */
public class TccTxManager implements TxManager {

    private static Log log = Log.getLog(RoleType.TM);

    private static TccTxManager instance = new TccTxManager();

    private TxCoordinator tc = TccTxCoordinator.getInstance();

    public static TccTxManager getInstance() {
        return instance;
    }

    @Override
    public String begin() {
        String xid = tc.beginGlobal();
        TxContainer.setXid(xid);
        log.info("Global tx begin, xid=" + xid);
        return xid;
    }

    @Override
    public boolean commit(String xid) {
        if (tc.commitGlobal(xid)) {
            log.info("Global tx commit success, xid=" + xid);
            return true;
        } else {
            log.info("Global tx commit false, xid=" + xid);
            return true;
        }
    }

    @Override
    public boolean rollback(String xid) {
        log.info("Global tx rollback, xid=" + xid);
        return tc.rollbackGlobal(xid);
    }
}
