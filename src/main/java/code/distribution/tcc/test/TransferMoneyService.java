package code.distribution.tcc.test;

import code.distribution.tcc.annotation.TccTransaction;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * 〈转账服务〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2020/1/7
 */
@Service
public class TransferMoneyService {

    @Resource
    private ReduceAccountService reduceAccountService;

    @Resource
    private AddAccountService addAccountService;

    @TccTransaction
    public boolean transfer(int fromId, int toId, BigDecimal amount) {
        try {
            String orderNo = getOrderNo();
            //1 调用减钱服务
            reduceAccountService.reduceTry(orderNo, fromId, amount);
            //2 调用加钱服务
            addAccountService.addTry(orderNo, toId, amount);
            System.out.println(String.format(">>>账户[%d]->账户[%s]转账 %s 元成功", fromId, toId, amount));
            return true;
        } catch (Exception e) {
            System.out.println(String.format(">>>账户[%d]->账户[%s]转账 %s 元失败", fromId, toId, amount));
            e.printStackTrace();
            return false;
        }
    }

    private String getOrderNo() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

}
