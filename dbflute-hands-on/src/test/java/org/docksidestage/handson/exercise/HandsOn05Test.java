package org.docksidestage.handson.exercise;

import java.util.List;

import javax.annotation.Resource;

import org.dbflute.optional.OptionalEntity;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.dbflute.exentity.MemberAddress;
import org.docksidestage.handson.dbflute.exentity.MemberLogin;
import org.docksidestage.handson.dbflute.exentity.Purchase;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * @author swan0
 *
 */
public class HandsOn05Test extends UnitContainerTestCase {
    // ===================================================================================
    // Attribute
    // =========
    @Resource
    protected MemberBhv memberBhv;

    // ===================================================================================
    // Execute
    // =======
    // -----------------------------------------------------
    // 業務的one-to-oneとは？
    // ------
    /**
     * 会員住所情報を検索
     * 会員名称、有効開始日、有効終了日、住所、地域名称をログに出して確認する
     * 会員IDの昇順、有効開始日の降順で並べる
     * 会員住所情報は、会員から見て "業務的one-to-one" と言えます。
     * @throws Exception
     */
    public void test_会員住所情報を検索() throws Exception {
        // ## Arrange ##
        // ## Act ##
        List<Member> memberList = memberBhv.selectList(cb -> {});
        memberBhv.loadMemberAddress(memberList, addressCB -> {
            addressCB.query().addOrderBy_MemberId_Asc();
            addressCB.query().addOrderBy_ValidBeginDate_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        memberList.forEach(m -> {
            List<MemberAddress> addressList = m.getMemberAddressList();
            if (addressList.isEmpty()) {
                log(m.getMemberId(), "住所情報なし");
            } else {
                addressList.forEach(a -> log(m.getMemberId(), a.getValidBeginDate(), a.getValidEndDate(), a.getAddress(),
                        a.getRegionIdAsRegion().name()));
            }
        });
    }

    // -----------------------------------------------------
    // 業務的one-to-oneを利用した実装
    // ------
    /**
     * 会員と共に現在の住所を取得して検索
     * SetupSelectのJavaDocにcommentがあることを確認すること
     * 会員名称と住所をログに出して確認すること
     * 現在日付はスーパークラスのメソッドを利用 ("c" 始まりのメソッド)
     * 会員住所情報が取得できていることをアサート
     * @throws Exception
     */
    public void test_会員と共に現在の住所を取得して検索() throws Exception {
        // ## Arrange ##
        String markExistsAddress = "少なくとも1件は会員住所情報が取得できていること";

        // ## Act ##
        List<Member> memberList = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberAddressAsValid(currentLocalDate());
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        memberList.forEach(m -> {
            OptionalEntity<MemberAddress> address = m.getMemberAddressAsValid();
            if (address.isPresent()) {
                MemberAddress a = address.get();
                log(m.getMemberId(), a.getValidBeginDate(), a.getValidEndDate(), a.getAddress(), a.getRegionIdAsRegion().name());

                markHere(markExistsAddress);
            } else {
                log(m.getMemberId(), "住所情報なし");
            }
        });
        assertMarked(markExistsAddress);
    }

    /**
     * 千葉に住んでいる会員の支払済み購入を検索
     * 会員ステータス名称と住所をログに出して確認すること
     * 購入に紐づいている会員の住所の地域が千葉であることをアサート
     * @throws Exception
     */
    public void test_千葉に住んでいる会員の支払済み購入を検索() throws Exception {
        // ## Arrange ##
        // ## Act ##
        List<Member> memberList = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberAddressAsValid(currentLocalDate());
            cb.query().queryMemberAddressAsValid(currentLocalDate()).setRegionId_Equal_千葉();
            cb.query().existsPurchase(subCB -> {
                subCB.query().setPaymentCompleteFlg_Equal_True();
            });
        });
        memberBhv.loadPurchase(memberList, subCB -> {
            subCB.query().setPaymentCompleteFlg_Equal_True();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        memberList.forEach(m -> {
            MemberAddress address = m.getMemberAddressAsValid().get();
            List<Purchase> purchaseList = m.getPurchaseList();
            assertHasAnyElement(purchaseList);
            purchaseList.forEach(purchase -> {
                log(m.getMemberId(), address.getValidBeginDate(), address.getValidEndDate(), address.getAddress(),
                        address.getRegionIdAsRegion().name(), purchase.getPaymentCompleteFlgAsFlg().name());

                assertTrue(address.isRegionId千葉());
                assertTrue(purchase.isPaymentCompleteFlgTrue());
            });
        });
    }

    // -----------------------------------------------------
    // 導出的one-to-oneを利用した実装
    // ------
    /**
     * 最終ログイン時の会員ステータスを取得して会員を検索
     * SetupSelectのJavaDocに自分で設定したcommentが表示されることを目視確認
     * 会員名称と最終ログイン日時と最終ログイン時の会員ステータス名称をログに出す
     * 最終ログイン日時が取得できてることをアサート
     * @throws Exception
     */
    public void test_最終ログイン時の会員ステータスを取得して会員を検索() throws Exception {
        // ## Arrange ##
        // ## Act ##
        List<Member> memberList = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberLoginAsLatest();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        memberList.forEach(m -> {
            if (m.getMemberLoginAsLatest().isPresent()) {
                MemberLogin ml = m.getMemberLoginAsLatest().get();
                log(m.getMemberId(), m.getMemberName(), ml.getLoginDatetime(), ml.getLoginMemberStatusCodeAsMemberStatus().name());

                assertNotNull(ml.getLoginDatetime());
            } else {
                log(m.getMemberId(), m.getMemberName(), "最終ログイン情報なし");
            }
        });
    }
}
