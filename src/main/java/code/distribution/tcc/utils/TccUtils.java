package code.distribution.tcc.utils;

import java.lang.reflect.Method;

/**
 * 〈Tcc工具类〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2020/1/7
 */
public class TccUtils {

    public static Method getMethod(Class clazz, String methodName){
        for(Method method : clazz.getDeclaredMethods()){
            if(method.getName().equals(methodName)){
                return method;
            }
        }
        return null;
    }
}
