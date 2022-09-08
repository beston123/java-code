package code.spring.aop;

import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;

/**
 * 〈AopBeanPostProcessor〉<p>
 * 〈功能详细描述〉
 *
 * @author tianwu
 * @date 2022/09/07
 */
@Component
public class AopBeanPostProcessor implements BeanPostProcessor, PriorityOrdered {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof AnnotationAwareAspectJAutoProxyCreator) {
            AnnotationAwareAspectJAutoProxyCreator creator = (AnnotationAwareAspectJAutoProxyCreator) bean;
            creator.setInterceptorNames("globalMethodInterceptor");
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return 1;
    }

}