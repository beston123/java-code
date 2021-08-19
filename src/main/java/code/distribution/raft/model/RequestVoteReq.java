package code.distribution.raft.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 〈请求投票 RPC 请求参数〉<p>
 由候选人负责调用用来征集选票（5.2 节）

 参数	解释
 term	候选人的任期号
 candidateId	请求选票的候选人的 Id
 lastLogIndex	候选人的最后日志条目的索引值
 lastLogTerm	候选人最后日志条目的任期号
 *
 * @author zixiao
 * @date 2019/3/11
 */
@Data
@AllArgsConstructor
public class RequestVoteReq implements Serializable{

    /**
     * 候选人的任期号
     */
    private int term;

    /**
     * 请求选票的候选人的 Id
     */
    private String candidateId;

    /**
     * 候选人的最后日志条目的索引值
     */
    private int lastLogIndex;

    /**
     * 候选人最后日志条目的任期号
     */
    private int lastLogTerm;

    /**
     * 是否预投票
     */
    private boolean preVote;

    public static RequestVoteReq build(int term, String candidateId, int lastLogIndex, int lastLogTerm){
        return new RequestVoteReq(term, candidateId, lastLogIndex, lastLogTerm, false);
    }

    public static RequestVoteReq build(int term, String candidateId, int lastLogIndex, int lastLogTerm, boolean preVote){
        return new RequestVoteReq(term, candidateId, lastLogIndex, lastLogTerm, preVote);
    }
}
