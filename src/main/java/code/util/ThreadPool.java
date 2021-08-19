package code.util;

import java.util.concurrent.*;

/**
 * 〈线程池工具类〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 18/9/26
 */
public class ThreadPool {

    private static int keepAliveMin = 5;

    private static RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();

    public static ExecutorService createSingle(int queueSize){
        return create(1, 1, queueSize);
    }

    public static ExecutorService createSingle(int queueSize, String threadPrefix){
        return create(1, 1, queueSize, threadPrefix);
    }

    public static ExecutorService create(int coreSize, int maxSize, int queueSize){
        return new ThreadPoolExecutor(
                coreSize,
                maxSize,
                keepAliveMin,
                TimeUnit.MINUTES,
                new ArrayBlockingQueue<Runnable>(queueSize),
                rejectedExecutionHandler);
    }

    public static ExecutorService create(int coreSize, int maxSize, int queueSize, String threadPrefix){
        return new ThreadPoolExecutor(
                coreSize,
                maxSize,
                keepAliveMin,
                TimeUnit.MINUTES,
                new ArrayBlockingQueue<Runnable>(queueSize),
                new NamedThreadFactory(threadPrefix),
                rejectedExecutionHandler);
    }

}
