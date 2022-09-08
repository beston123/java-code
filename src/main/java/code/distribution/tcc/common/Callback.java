package code.distribution.tcc.common;

/**
 * 〈一句话功能简述〉<p>
 * 〈功能详细描述〉
 *
 * @author tianwu
 * @date 2022/09/07
 */
public interface Callback<T> {

    T execute() throws Throwable;

}