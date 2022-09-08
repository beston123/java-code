package code.distribution.tcc.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;

import static code.distribution.tcc.test.AccountConst.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 〈一句话功能简述〉<p>
 * 〈功能详细描述〉
 *
 * @author tianwu
 * @date 2022/09/08
 */
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class TransferMoneyServiceTest {

    @Resource
    private TransferMoneyService transferMoneyService;

    public void initDb(){
        AccountDb.insertOrUpdate(new Account(ACCOUNT_A, INIT_AMOUNT));
        AccountDb.insertOrUpdate(new Account(ACCOUNT_B, INIT_AMOUNT));
    }

    @Test
    public void testConfirm() {
        this.initDb();

        BigDecimal addAmount = new BigDecimal("50.00");
        assertTrue(transferMoneyService.transfer(ACCOUNT_A, ACCOUNT_B, addAmount));

        Account accountA = AccountDb.selectById(ACCOUNT_A);
        Account accountB = AccountDb.selectById(ACCOUNT_B);
        assertTrue(accountA.getBalanceAmount().compareTo(INIT_AMOUNT.subtract(addAmount)) == 0);
        assertTrue(accountA.getFreezeAmount().compareTo(BigDecimal.ZERO) == 0);
        assertTrue(accountB.getBalanceAmount().compareTo(INIT_AMOUNT.add(addAmount)) == 0);
    }

    @Test
    public void testCancel() {
        this.initDb();

        //减钱失败
        BigDecimal transferAmount = INIT_AMOUNT.add(BigDecimal.TEN);
        assertFalse(transferMoneyService.transfer(ACCOUNT_A, ACCOUNT_B, transferAmount));

        Account accountA = AccountDb.selectById(ACCOUNT_A);
        Account accountB = AccountDb.selectById(ACCOUNT_B);
        assertTrue(accountA.getBalanceAmount().compareTo(INIT_AMOUNT) == 0);
        assertTrue(accountB.getBalanceAmount().compareTo(INIT_AMOUNT) == 0);
    }

    @Test
    public void testCancel2() {
        this.initDb();

        //加钱账户状态被禁用
        AccountDb.selectById(ACCOUNT_B).disable();
        assertFalse(transferMoneyService.transfer(ACCOUNT_A, ACCOUNT_B, new BigDecimal("50.00")));

        Account accountA = AccountDb.selectById(ACCOUNT_A);
        Account accountB = AccountDb.selectById(ACCOUNT_B);
        assertTrue(accountA.getBalanceAmount().compareTo(INIT_AMOUNT) == 0);
        assertTrue(accountB.getBalanceAmount().compareTo(INIT_AMOUNT) == 0);
    }

}