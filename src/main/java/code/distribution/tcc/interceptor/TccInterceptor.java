package code.distribution.tcc.interceptor;

import code.distribution.tcc.annotation.TccTransaction;
import code.distribution.tcc.tm.TccTxManager;
import code.distribution.tcc.tm.TxManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import code.distribution.tcc.annotation.TccBranch;
import code.distribution.tcc.common.TxContainer;
import code.distribution.tcc.common.TxMethod;
import code.distribution.tcc.exception.TccException;
import code.distribution.tcc.rm.ResManager;
import code.distribution.tcc.rm.TccFlowHandler;
import code.distribution.tcc.rm.TccResManager;
import code.distribution.tcc.utils.TccUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 〈TccInterceptor〉<p>
 * 〈功能详细描述〉
 *
 * @author tianwu
 * @date 2022/09/07
 */
@Aspect
@Component
public class TccInterceptor {

    private ResManager rm = TccResManager.getInstance();

    private TxManager tm = TccTxManager.getInstance();

    private TccFlowHandler tccFlowHandler = TccFlowHandler.getInstance();

    /**
     * 拦截 @TccTransaction 方法
     */
    @Around("@annotation(tccTransaction)")
    public Object tccTransactionAround(ProceedingJoinPoint pjp, TccTransaction tccTransaction) throws Throwable {
        if (TxContainer.getXid() != null) {
            return pjp.proceed();
        }
        //0 开启全局事务
        String xid = tm.begin();
        try {
            Object ret = pjp.proceed();
            tm.commit(xid);
            return ret;
        } catch (Throwable e) {
            tm.rollback(xid);
            throw e;
        }
    }

    /**
     * 拦截 @TccBranch 方法
     */
    @Around("@annotation(tccBranch)")
    public Object tccBranchAround(ProceedingJoinPoint pjp, TccBranch tccBranch) throws Throwable {
        if (TxContainer.getXid() == null) {
            return pjp.proceed();
        }
        TxMethod txMethod = getTwoPhaseMethod(pjp, tccBranch);
        //注册分支事务
        Long branchId = TxContainer.setBranchId(tccBranch.actionName());
        rm.registerBranch(TxContainer.getXid(), branchId, txMethod);

        return doTry(pjp, tccBranch);
    }

    private TxMethod getTwoPhaseMethod(ProceedingJoinPoint pjp, TccBranch tccBranch){
        Class<?> clazz = pjp.getTarget().getClass();
        Method confirmMethod = TccUtils.getMethod(clazz, tccBranch.confirm());
        if (confirmMethod == null) {
            throw new TccException(String.format("confirm方法'%s'不存在， class=%s", tccBranch.confirm(), clazz));
        }
        Method cancelMethod = TccUtils.getMethod(clazz, tccBranch.cancel());
        if (cancelMethod == null) {
            throw new TccException(String.format("cancel方法'%s'不存在， class=%s", tccBranch.cancel(), clazz));
        }
        return new TxMethod(pjp.getTarget(), confirmMethod, cancelMethod, pjp.getArgs());
    }

    private Object doTry(ProceedingJoinPoint pjp, TccBranch tccBranch) throws Throwable {
        Long branchId = TxContainer.getBranchId(tccBranch.actionName());
        //try防悬挂处理
        Object ret = tccFlowHandler.beforeTry(TxContainer.getXid(), branchId, () -> pjp.proceed());
        //上报状态
        rm.onePhase(TxContainer.getXid(), branchId);
        return ret;
    }

}
