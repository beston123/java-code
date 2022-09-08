package code.distribution.tcc.test;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 〈账户〉<p>
 * 〈功能详细描述〉
 *
 * @author tianwu
 * @date 2022/09/08
 */
@Data
public class Account {

    private int id;

    private boolean enabled;

    /**
     * 余额
     */
    private BigDecimal balanceAmount;

    /**
     * 冻结
     */
    private BigDecimal freezeAmount;


    /**
     * 预扣减（冻结）流水号
     */
    private List<String> freezeFlowNos;

    /**
     * 扣减流水号
     */
    private List<String> reduceFlowNos;

    /**
     * 加钱流水号
     */
    private List<String> addFlowNos;

    public Account(int id, BigDecimal balanceAmount) {
        this.id = id;
        this.enabled = true;
        this.balanceAmount = balanceAmount;
        this.freezeAmount = new BigDecimal("0.00");
        this.freezeFlowNos = new ArrayList<>(8);
        this.reduceFlowNos = new ArrayList<>(8);
        this.addFlowNos = new ArrayList<>(8);
    }

    /**
     * 预扣
     *
     * @param amount
     * @return
     */
    public boolean preReduce(String orderNo, BigDecimal amount) {
        checkAccount();
        if (balanceAmount.compareTo(amount) < 0) {
            throw new RuntimeException(String.format("账户[%d]余额不足，可用余额：%s", id, balanceAmount));
        }
        this.balanceAmount = balanceAmount.subtract(amount);
        this.freezeAmount = freezeAmount.add(amount);
        this.freezeFlowNos.add(orderNo);
        return true;
    }

    /**
     * 扣除
     *
     * @param amount
     * @return
     */
    public boolean reduce(String orderNo, BigDecimal amount) {
        if (reduceFlowNos.contains(orderNo)) {
            throw new RuntimeException(String.format("账户[%d]该笔扣减流水(orderNo=%s)已存在", id, orderNo));
        }
        checkAccount();
        this.freezeAmount = freezeAmount.subtract(amount);
        this.reduceFlowNos.add(orderNo);
        return true;
    }

    /**
     * 释放预扣金额
     *
     * @param amount
     * @return
     */
    public boolean unFreeze(String orderNo, BigDecimal amount) {
        if (!freezeFlowNos.contains(orderNo)) {
            //未冻结，空回滚
            throw new RuntimeException(String.format("账户[%d]未冻结该笔流水(orderNo=%s)，不能释放", id, orderNo));
        }
        checkAccount();
        if (freezeAmount.compareTo(amount) < 0) {
            throw new RuntimeException(String.format("账户[%d]已冻结金额不足，已冻结金额：%s", id, freezeAmount));
        }
        this.freezeAmount = freezeAmount.subtract(amount);
        this.balanceAmount = balanceAmount.add(amount);
        return true;
    }

    /**
     * 增加余额
     *
     * @param amount
     * @return
     */
    public boolean add(String orderNo, BigDecimal amount) {
        if (addFlowNos.contains(orderNo)) {
            throw new RuntimeException(String.format("账户[%d]该笔加钱流水(orderNo=%s)已存在", id, orderNo));
        }
        checkAccount();
        this.balanceAmount = balanceAmount.add(amount);
        this.addFlowNos.add(orderNo);
        return true;
    }

    public void checkAccount() {
        if (!enabled) {
            throw new RuntimeException(String.format("账户[%d]当前不可用", id));
        }
    }

    public BigDecimal getTotalAmount() {
        return balanceAmount.add(freezeAmount);
    }

    public void disable() {
        this.enabled = false;
    }

}

