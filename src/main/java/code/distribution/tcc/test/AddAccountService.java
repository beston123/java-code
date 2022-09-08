package code.distribution.tcc.test;

import code.distribution.tcc.annotation.TccBranch;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static code.distribution.tcc.test.AccountConst.ACCOUNT_B;
import static code.distribution.tcc.test.AccountConst.INIT_AMOUNT;

/**
 * 〈加钱服务〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2020/1/7
 */
@Service
public class AddAccountService {

    static {
        AccountDb.insert(new Account(ACCOUNT_B, INIT_AMOUNT));
    }

    /**
     * 加钱try方法，空操作
     *
     * @param orderNo
     * @param accountId
     * @param amount
     * @return
     */
    @TccBranch(actionName = "AddAccountService.add", confirm = "addConfirm", cancel = "addCancel")
    public void addTry(String orderNo, int accountId, BigDecimal amount) {
        Account account = AccountDb.selectById(accountId);
        account.checkAccount();
        System.out.println(String.format("addTry：预加金额(空操作)，账户[%d]，orderNo=%s", accountId, orderNo));
    }

    /**
     * 加钱confirm方法，执行账户加钱操作
     *
     * @param orderNo
     * @param accountId
     * @param amount
     * @return
     */
    public void addConfirm(String orderNo, int accountId, BigDecimal amount) {
        Account account = AccountDb.selectById(accountId);
        account.add(orderNo, amount);
        System.out.println(String.format("addConfirm：成功给账户[%d]加钱%s元, orderNo=%s", accountId, amount, orderNo));
    }

    /**
     * 加钱cancel方法，空操作
     *
     * @param orderNo
     * @param accountId
     * @param amount
     * @return
     */
    public void addCancel(String orderNo, int accountId, BigDecimal amount) {
        System.out.println(String.format("addCancel：回滚金额(空操作)，账户[%d]，orderNo=%s", accountId, orderNo));
    }

}
