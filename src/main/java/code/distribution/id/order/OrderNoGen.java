package code.distribution.id.order;


import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;

import java.util.Date;

/**
 * 〈订单号生成器〉<p>
 * 〈功能详细描述〉
 *
 * @author tianwu
 * @date 2022/09/08
 */
public class OrderNoGen {

    /**
     * 订单号规则
     * yyMMdd 6位+ 秒数5位+ 订单场景2位 + 随机数3位 + 买家Id后3位
     * @param bizType 业务类型
     * @param buyerId 买家Id
     * @return
     */
    public long generateNo(int bizType, int buyerId) {
        Date nowTime = new Date();
        StringBuffer stringBuffer = new StringBuffer();
        //一天内的秒数
        long secondsInDay = DateUtil.between(DateUtil.beginOfDay(nowTime).toJdkDate(), nowTime, DateUnit.SECOND);
        stringBuffer.append(DateUtil.format(nowTime, "yyMMdd"))
                .append(String.format("%05d", secondsInDay))
                .append(String.format("%02d", bizType))
                .append(String.format("%03d", RandomUtil.randomInt(0, 999)))
                .append(String.format("%03d", buyerId % 1000));
        return Long.valueOf(stringBuffer.toString());
    }

    public static void main(String[] args) {
        System.out.println(new OrderNoGen().generateNo(10, RandomUtil.randomInt(100, 999)));
    }

}
