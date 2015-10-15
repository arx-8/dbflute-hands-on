package org.docksidestage.handson.logic;

import javax.annotation.Resource;

import org.dbflute.optional.OptionalEntity;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.unit.UnitContainerTestCase;

public class HandsOn07LogicTest extends UnitContainerTestCase {
    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected HandsOn07Logic logic;

    @Resource
    protected MemberBhv memberBhv;

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    @Override
    public void setUp() throws Exception {
        super.setUp();
        logic = new HandsOn07Logic();
        inject(logic);
        log("Called setUp()");
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    // -----------------------------------------------------
    //                                          データの登録
    //                                                ------
    /**
     * 登録後の Entity から主キーの値を使って検索すること
     * とりあえず、会員名称と生年月日だけアサート
     * @throws Exception
     */
    public void test_insertMyselfMember_会員が登録されていること() throws Exception {
        // ## Arrange ##
        // ## Act ##
        Member insertedMember = logic.insertMyselfMember();

        // ## Assert ##
        log(insertedMember);
        OptionalEntity<Member> optMember = memberBhv.selectByPK(insertedMember.getMemberId());
        assertTrue(optMember.isPresent());
        optMember.ifPresent(m -> {
            assertEquals(insertedMember.getMemberName(), m.getMemberName());
            assertEquals(insertedMember.getBirthdate(), m.getBirthdate());
        });
    }

    // -----------------------------------------------------
    //                              さらに登録してみましょう
    //                                                ------
    /**
     * 登録されていることを代表的なカラムを利用してアサート
     * 関連テーブルの登録もアサート
     * 登録していない関連テーブルが登録されていないこともアサート
     */
    public void test_insertYourselfMember_会員が登録されていること() throws Exception {
        // ## Arrange ##
        // ## Act ##
        Member insertedMember = logic.insertYourselfMember();

        // ## Assert ##
        // 正式会員が必ず持つデータが存在することを確認する
        assertNotNull(insertedMember.getFormalizedDatetime());

        Member assertMember = memberBhv.selectEntityWithDeletedCheck(cb -> {
            cb.setupSelect_MemberSecurityAsOne();
            cb.setupSelect_MemberServiceAsOne();
            cb.acceptPK(insertedMember.getMemberId());
        });
        assertTrue(assertMember.getMemberSecurityAsOne().isPresent());
        assertTrue(assertMember.getMemberServiceAsOne().isPresent());
    }
}
