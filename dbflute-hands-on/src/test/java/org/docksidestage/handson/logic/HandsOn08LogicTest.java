package org.docksidestage.handson.logic;

import javax.annotation.Resource;

import org.dbflute.exception.EntityAlreadyUpdatedException;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * @author swan0
 *
 */
public class HandsOn08LogicTest extends UnitContainerTestCase {
    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected HandsOn08Logic logic;

    @Resource
    protected MemberBhv memberBhv;

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    @Override
    public void setUp() throws Exception {
        super.setUp();
        logic = new HandsOn08Logic();
        inject(logic);
        log("Called setUp()");
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    // -----------------------------------------------------
    //                                    排他制御ありの更新
    //                                                ------
    /**
     * 任意の仮会員の会員IDとバージョンNOを渡して更新すること
     * 更新処理後、DB上のデータが更新されていることをアサート
     * @throws Exception
     */
    public void test_updateMemberChangedToFormalized_会員が更新されていること() throws Exception {
        // ## Arrange ##
        // 更新する仮会員を適当にselect
        Member memberBeforeUpdate = memberBhv.selectEntityWithDeletedCheck(cb -> {
            cb.query().setMemberStatusCode_Equal_仮会員();
            cb.fetchFirst(1);
        });

        // ## Act ##
        logic.updateMemberChangedToFormalized(memberBeforeUpdate.getMemberId(), memberBeforeUpdate.getVersionNo());

        // ## Assert ##
        Member memberAfterUpdate = memberBhv.selectEntityWithDeletedCheck(cb -> {
            cb.query().setMemberId_Equal(memberBeforeUpdate.getMemberId());
        });
        log(memberBeforeUpdate);
        log(memberAfterUpdate);
        assertFalse(memberAfterUpdate.getVersionNo().equals(memberBeforeUpdate.getVersionNo()));
        assertTrue(memberAfterUpdate.isMemberStatusCode正式会員());
    }

    /**
     * 何かしらの方法で排他制御例外を発生させてみること
     * 排他制御例外の内容をログに出力して目視確認すること
     * @throws Exception
     */
    public void test_updateMemberChangedToFormalized_排他制御例外が発生すること() throws Exception {
        // ## Arrange ##
        String assertMarker = "passed catch";

        // 更新する仮会員を適当にselect
        Member memberBeforeUpdate = memberBhv.selectEntityWithDeletedCheck(cb -> {
            cb.query().setMemberStatusCode_Equal_仮会員();
            cb.fetchFirst(1);
        });

        // ## Act ##
        // ## Assert ##
        try {
            logic.updateMemberChangedToFormalized(memberBeforeUpdate.getMemberId(), memberBeforeUpdate.getVersionNo() + 1L);
        } catch (Exception e) {
            markHere(assertMarker);
            assertEquals(EntityAlreadyUpdatedException.class, e.getClass());
        }
        assertMarked(assertMarker);
    }
}
