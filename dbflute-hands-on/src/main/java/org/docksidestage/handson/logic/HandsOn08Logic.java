package org.docksidestage.handson.logic;

import javax.annotation.Resource;

import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;

/**
 * @author swan0
 *
 */
public class HandsOn08Logic extends BsLogicLogger {
    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    protected MemberBhv memberBhv;

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    // -----------------------------------------------------
    //                                    排他制御ありの更新
    //                                                ------
    /**
     * 指定された会員を正式会員に更新する
     * 排他制御ありで更新する
     * 引数の値で null は許されない
     * @param memberId
     * @param versionNo
     * @throws IllegalAccessException
     */
    public void updateMemberChangedToFormalized(Integer memberId, Long versionNo) throws IllegalAccessException {
        if (memberId == null || versionNo == null) {
            throw new IllegalAccessException("引数の値で null は許されない");
        }

        Member member = new Member();
        member.setMemberId(memberId);
        member.setMemberStatusCode_正式会員();
        member.setVersionNo(versionNo);
        memberBhv.update(member);
    }
}
