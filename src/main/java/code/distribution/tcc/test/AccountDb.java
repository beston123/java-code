package code.distribution.tcc.test;

import org.springframework.beans.BeanUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 〈一句话功能简述〉<p>
 * 〈功能详细描述〉
 *
 * @author tianwu
 * @date 2022/09/08
 */
public class AccountDb {

    private static Map<Integer, Account> accountMap = new HashMap<>();

    public static boolean insert(Account account) {
        Account exist = accountMap.putIfAbsent(account.getId(), account);
        return exist == null;
    }

    public static boolean insertOrUpdate(Account account) {
        Account exist = accountMap.putIfAbsent(account.getId(), account);
        if (exist != null) {
            BeanUtils.copyProperties(account, exist);
        }
        return true;
    }


    public static Account selectById(int accountId) {
        return accountMap.get(accountId);
    }

}
