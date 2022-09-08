package code.distribution.tcc.test;

import code.distribution.tcc.annotation.TccBranch;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static code.distribution.tcc.test.AccountConst.*;

/**
 * 〈减钱服务〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2020/1/7
 */
@Service
public class ReduceAccountService {

    static {
        AccountDb.insert(new Account(ACCOUNT_A, INIT_AMOUNT));
    }

    /**
     * 减钱Try方法， 预留资源，冻结部分可用余额，即减少可用余额，增加冻结金额
     *
     * @param orderNo
     * @param accountId
     * @param amount
     * @return
     */
    @TccBranch(actionName = "ReduceAccountService.reduce", confirm = "reduceConfirm", cancel = "reduceCancel")
    public void reduceTry(String orderNo, int accountId, BigDecimal amount) {
        Account account = AccountDb.selectById(accountId);
        account.preReduce(orderNo, amount);
        System.out.println(String.format("reduceTry：成功预扣账户[%d]%s元，orderNo=%s", accountId, amount, orderNo));
    }

    /**
     * 减钱confirm方法，直接将冻结金额扣除
     *
     * @param orderNo
     * @param accountId
     * @param amount
     * @return
     */
    public void reduceConfirm(String orderNo, int accountId, BigDecimal amount) {
        Account account = AccountDb.selectById(accountId);
        account.reduce(orderNo, amount);
        System.out.println(String.format("reduceConfirm：成功扣除账户[%d]%s元，orderNo=%s", accountId, amount, orderNo));
    }

    /**
     * 减钱cancel方法，冻结金额解冻到可用余额
     *
     * @param orderNo
     * @param accountId
     * @param amount
     * @return
     */
    public void reduceCancel(String orderNo, int accountId, BigDecimal amount) {
        Account account = AccountDb.selectById(accountId);
        account.unFreeze(orderNo, amount);
        System.out.println(String.format("reduceCancel：成功回滚账户[%d]%s元，orderNo=%s", accountId, amount, orderNo));
    }

}
