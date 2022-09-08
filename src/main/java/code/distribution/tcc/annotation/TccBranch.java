package code.distribution.tcc.annotation;

import java.lang.annotation.*;

/**
 * 〈TCC分支事务〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2020/1/7
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TccBranch {

    /**
     * 分支名称
     * @return
     */
    String actionName();

    /**
     * 确认方法
     */
    String confirm();

    /**
     * 回滚方法
     */
    String cancel();
}
