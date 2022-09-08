package code.distribution.tcc.common;

import code.distribution.tcc.exception.TccException;
import lombok.Data;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * 〈一句话功能简述〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2020/1/7
 */
@Data
public class TxMethod implements Serializable {

    private Object object;

    private Method confirmMethod;

    private Method cancelMethod;

    private Object[] args;

    public TxMethod(Object object, Method confirmMethod, Method cancelMethod, Object[] args) {
        this.object = object;
        this.confirmMethod = confirmMethod;
        this.cancelMethod = cancelMethod;
        this.args = args;
    }

    public void invoke(Method method){
        try {
            method.invoke(object, args);
        } catch (Exception e) {
            throw new TccException("two phase execute error", e);
        }
    }


}
