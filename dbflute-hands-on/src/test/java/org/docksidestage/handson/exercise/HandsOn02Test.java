package org.docksidestage.handson.exercise;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * @author swan0
 *
 */
public class HandsOn02Test extends UnitContainerTestCase {
    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    protected MemberBhv memberBhv;

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    // -----------------------------------------------------
    //                                    テストデータの登録
    //                                                ------
    /**
     * テストデータがあることをアサートするテスト
     * @throws Exception
     */
    public void test_existAnyData() throws Exception {
        // ## Arrange ##
        // ## Act ##
        // ## Assert ##
        assertTrue(0 < memberBhv.selectList(cb -> {}).size());
    }

    // -----------------------------------------------------
    //                                    テストデータの閲覧
    //                                                ------
    /**
     * 会員名称の昇順で並べる (これは実装要件、Arrange でこの通りに実装すること)
     * (検索結果の)会員名称がSで始まっていることをアサート (これはアサート要件、Assert でこの通りに実装すること)
     * @throws Exception
     */
    public void test_会員名称がSで始まる会員を検索() throws Exception {
        // ## Arrange ##
        final String memberNamePrefix = "S";

        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().setMemberName_LikeSearch(memberNamePrefix, op -> op.likePrefix());
            cb.query().addOrderBy_MemberName_Asc();
        });

        // ## Assert ##
        // assert の素通り防止
        assertHasAnyElement(memberList);

        // log は assert より前（落ちたら確認できなくなるので）
        memberList.forEach(m -> {
            log(m.getMemberId(), m.getMemberName());

            assertTrue(m.getMemberName().startsWith(memberNamePrefix));
        });
    }

    /**
     * 一件検索として検索すること
     * 会員IDが 1 であることをアサート
     * @throws Exception
     */
    public void test_会員IDが1の会員を検索() throws Exception {
        // ## Arrange ##
        final Integer memberIdExpect = 1;

        // ## Act ##
        // ## Assert ##
        memberBhv.selectEntity(cb -> cb.acceptPK(memberIdExpect)).alwaysPresent(m -> {
            log(m.getMemberId(), m.getMemberName());

            assertEquals(memberIdExpect, m.getMemberId());
        });
    }

    /**
     * 更新日時の降順で並べる
     * 生年月日がないことをアサート
     * @throws Exception
     */
    public void test_生年月日がない会員を検索() throws Exception {
        // ## Arrange ##
        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().setBirthdate_IsNull();
            cb.query().addOrderBy_UpdateDatetime_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        memberList.forEach(m -> {
            log(m.getMemberId(), m.getBirthdate());

            assertNull(m.getBirthdate());
        });
    }
}
