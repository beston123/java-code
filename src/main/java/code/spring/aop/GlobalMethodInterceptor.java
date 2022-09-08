package code.spring.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 〈GlobalMethodInterceptor〉<p>
 * 〈功能详细描述〉
 *
 * @author tianwu
 * @date 2022/09/07
 */
@Component("globalMethodInterceptor")
public class GlobalMethodInterceptor implements MethodInterceptor {

    /**
     * 拦截索引方法
     *
     * @param invocation
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = getMethod(invocation);
        if (method != null) {
            System.out.println("globalMethodInterceptor:" + method.getDeclaringClass() + "#" + method.getName());
        }
        return invocation.proceed();
    }

    private Method getMethod(MethodInvocation invocation) throws NoSuchMethodException {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(invocation.getThis());
        return targetClass.getMethod(invocation.getMethod().getName(), invocation.getMethod().getParameterTypes());
    }

}
