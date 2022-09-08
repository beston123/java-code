package code.distribution.tcc.common;

import lombok.Data;

/**
 * 〈一句话功能简述〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2020/1/7
 */
@Data
public class BranchTx {

    private Long id;

    private BranchState state;

    private TxMethod method;

    public BranchTx(Long id, BranchState state, TxMethod methods) {
        this.id = id;
        this.state = state;
        this.method = methods;
    }
}
