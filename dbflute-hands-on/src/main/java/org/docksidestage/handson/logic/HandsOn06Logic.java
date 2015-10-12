package org.docksidestage.handson.logic;

import java.util.List;

import javax.annotation.Resource;

import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;

/**
 * @author swan0
 *
 */
public class HandsOn06Logic extends BsLogicLogger {
    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    protected MemberBhv memberBhv;

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    // -----------------------------------------------------
    //                                      別名(和名)の利用
    //                                                ------
    /**
     * 指定された suffix で会員名称を後方一致検索
     * 会員名称の昇順で並べる
     * suffixが無効な値なら例外: IllegalArgumentException
     * 会員名称、生年月日、正式会員日時をログに出す (Commons Logging)
     * @param suffix
     * @return
     */
    public List<Member> selectSuffixMemberList(String suffix) {
        if (suffix == null || suffix.trim().isEmpty()) {
            throw new IllegalArgumentException("suffixにnullと空文字は無効");
        }
        List<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().setMemberName_LikeSearch(suffix, op -> op.likeSuffix());
            cb.query().addOrderBy_MemberName_Asc();
        });
        memberList.forEach(m -> log(m.getMemberId(), m.getMemberName(), m.getBirthdate(), m.getFormalizedDatetime()));
        return memberList;
    }
}
