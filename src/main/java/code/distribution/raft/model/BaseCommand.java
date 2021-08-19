package code.distribution.raft.model;

import lombok.Data;

/**
 * 〈基础命令对象〉<p>
 * 〈功能详细描述〉
 *
 * @author beston
 * @date 2020/12/07
 */
@Data
public abstract class BaseCommand implements Command{

    protected String key;

}
