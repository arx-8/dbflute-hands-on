package org.docksidestage.handson.logic;

import java.util.ArrayList;
import java.util.List;

import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.unit.UnitContainerTestCase;

public class HandsOn06LogicTest extends UnitContainerTestCase {
    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected HandsOn06Logic logic;

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    @Override
    public void setUp() throws Exception {
        super.setUp();
        logic = new HandsOn06Logic();
        inject(logic);
        log("Called setUp()");
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    // -----------------------------------------------------
    //                                      別名(和名)の利用
    //                                                ------
    /**
     * suffix は "vic" で
     * テストメソッド名通りのアサート
     * テストが成り立っていることも(できる範囲で)アサート (今後ずっとそう)
     * @throws Exception
     */
    public void test_selectSuffixMemberList_指定したsuffixで検索されること()  throws Exception {
        // ## Arrange ##
        String memberNameSuffix = "vic";

        // ## Act ##
        List<Member> memberList = logic.selectSuffixMemberList(memberNameSuffix);

        // ## Assert ##
        assertHasAnyElement(memberList);
        memberList.forEach(m -> assertTrue(m.getMemberName().endsWith(memberNameSuffix)));
    }

    /**
     * 無効な値とは、nullと空文字とトリムして空文字になる値
     * @throws Exception
     */
    public void test_selectSuffixMemberList_suffixが無効な値なら例外が発生すること() throws Exception {
        // ## Arrange ##
        int assertPassedCount = 0;
        List<String> invalidSuffixList = new ArrayList<>();
        invalidSuffixList.add(null);
        invalidSuffixList.add("");
        invalidSuffixList.add(" ");

        // ## Act ##
        for (String invalidSuffix : invalidSuffixList) {
            try {
                logic.selectSuffixMemberList(invalidSuffix);
            } catch (Exception e) {
                // ## Assert ##
                assertTrue(e.getClass().equals(IllegalArgumentException.class));
                assertPassedCount++;
            }
        }
        assertEquals(invalidSuffixList.size(), assertPassedCount);
    }
}
