package code.distribution.tcc.annotation;

import java.lang.annotation.*;

/**
 * 〈TCC事务〉<p>
 * 〈功能详细描述〉
 *
 * @author tianwu
 * @date 2022/09/08
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TccTransaction {

}
