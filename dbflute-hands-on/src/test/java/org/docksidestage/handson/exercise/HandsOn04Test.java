package org.docksidestage.handson.exercise;

import java.util.HashSet;
import java.util.List;

import javax.annotation.Resource;

import org.docksidestage.handson.dbflute.allcommon.CDef.MemberStatus;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.PurchaseBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.dbflute.exentity.Product;
import org.docksidestage.handson.dbflute.exentity.Purchase;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * @author swan0
 *
 */
public class HandsOn04Test extends UnitContainerTestCase {
    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    protected MemberBhv memberBhv;
    @Resource
    protected PurchaseBhv purchaseBhv;

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    // -----------------------------------------------------
    //                                      ベタベタのやり方
    //                                                ------
    /**
     * 退会会員のステータスコードは "WDL"。ひとまずベタで
     * 支払完了フラグは "0" で未払い。ひとまずベタで
     * 購入日時の降順で並べる
     * 会員名称と商品名と一緒にログに出力
     * 購入が未払いであることをアサート
     * @throws Exception
     */
    // public void test_退会会員の未払い購入を検索() throws Exception {
    //     // ## Arrange ##
    //     String memberStatusCode_Withdrawal = "WDL";
    //     final int paymentCompleteFlg_Unpaid = 0;
    //
    //     // ## Act ##
    //     List<Purchase> purchaseList = purchaseBhv.selectList(cb -> {
    //         cb.setupSelect_Member();
    //         cb.setupSelect_Product();
    //         cb.query().queryMember().setMemberStatusCode_Equal(memberStatusCode_Withdrawal);
    //         cb.query().setPaymentCompleteFlg_Equal(paymentCompleteFlg_Unpaid);
    //         cb.query().addOrderBy_PurchaseDatetime_Desc();
    //     });
    //
    //     // ## Assert ##
    //     assertHasAnyElement(purchaseList);
    //     purchaseList.forEach(pur -> {
    //         Member m = pur.getMember().get();
    //         Product pro = pur.getProduct().get();
    //         log(m.getMemberId(), m.getMemberName(), pur.getPaymentCompleteFlg(), pur.getPurchaseDatetime(), pro.getProductName());
    //
    //         assertTrue(pur.getPaymentCompleteFlg() == 0);
    //     });
    // }

    /**
     * 退会会員でない会員は、会員退会情報を持っていないことをアサート
     * 退会会員のステータスコードは "WDL"。ひとまずベタで
     * 不意のバグや不意のデータ不備でもテストが(できるだけ)成り立つこと
     * @throws Exception
     */
    public void test_会員退会情報も取得して会員を検索() throws Exception {
        // ## Arrange ##
        String memberStatusCode_Withdrawal = "WDL";

        // ## Act ##
        List<Member> memberList = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberWithdrawalAsOne();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        memberList.forEach(m -> {
            log(m.getMemberId(), m.getMemberStatusCode(), m.getMemberWithdrawalAsOne());

            if (!memberStatusCode_Withdrawal.equals(m.getMemberStatusCode())) {
                assertFalse(m.getMemberWithdrawalAsOne().isPresent());
            }
        });
    }

    // -----------------------------------------------------
    //                            区分値メソッドの生成と活用
    //                                                ------
    /**
     * fix test_退会会員の未払い購入を検索()
     * @throws Exception
     */
    public void test_退会会員の未払い購入を検索_fix() throws Exception {
        // ## Arrange ##
        // ## Act ##
        List<Purchase> purchaseList = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Member();
            cb.setupSelect_Product();
            cb.query().queryMember().setMemberStatusCode_Equal_退会会員();
            cb.query().setPaymentCompleteFlg_Equal_Flase();
            cb.query().addOrderBy_PurchaseDatetime_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(purchaseList);
        purchaseList.forEach(pur -> {
            Member m = pur.getMember().get();
            Product pro = pur.getProduct().get();
            log(m.getMemberId(), m.getMemberName(), pur.getPaymentCompleteFlg(), pur.getPurchaseDatetime(), pro.getProductName());

            assertTrue(pur.getPaymentCompleteFlg() == 0);
        });
    }

    /**
     * fix test_会員退会情報も取得して会員を検索()
     * @throws Exception
     */
    public void test_会員退会情報も取得して会員を検索_fix() throws Exception {
        // ## Arrange ##
        // ## Act ##
        List<Member> memberList = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberWithdrawalAsOne();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        memberList.forEach(m -> {
            log(m.getMemberId(), m.getMemberStatusCode(), m.getMemberWithdrawalAsOne());

            if (!m.isMemberStatusCode退会会員()) {
                assertFalse(m.getMemberWithdrawalAsOne().isPresent());
            }
        });
    }

    // -----------------------------------------------------
    //                            区分値メソッドを使って実装
    //                                                ------
    /**
     * 一番若い仮会員の会員を検索
     * 区分値メソッドの JavaDoc コメントを確認する
     * 会員ステータス名称も取得する(ログに出力)
     * 会員が仮会員であることをアサート
     * ※できれば、テストメソッド内の検索回数は一回で...
     * @throws Exception
     */
    public void test_一番若い仮会員の会員を検索() throws Exception {
        // ## Arrange ##
        // ## Act ##
        List<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().scalar_Equal().max(subCB -> {
                subCB.query().setMemberStatusCode_Equal_仮会員();
                subCB.specify().columnBirthdate();
            });
        });

        // ## Assert ##
        memberList.forEach(m -> {
            MemberStatus status = m.getMemberStatusCodeAsMemberStatus();
            log(m.getMemberId(), status.alias(), status.code(), status.name());

            assertTrue(m.isMemberStatusCode仮会員());
        });
    }

    /**
     * 支払済みの購入の中で一番若い正式会員のものだけ検索
     * 会員ステータス名称も取得する(ログに出力)
     * 購入日時の降順で並べる
     * 購入の紐づいている会員が正式会員であることをアサート
     * ※これ難しい...かも!? (解釈に "曖昧さ" あり、実際にデータが存在している方を優先)
     * @throws Exception
     */
    public void test_支払済みの購入の中で一番若い正式会員のものだけ検索() throws Exception {
        // ## Arrange ##
        // ## Act ##
        List<Purchase> purchaseList = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Member();
            cb.query().setPaymentCompleteFlg_Equal_True();
            cb.query().queryMember().setMemberStatusCode_Equal_正式会員();
            cb.query().queryMember().scalar_Equal().max(memberCB -> {
                memberCB.specify().columnBirthdate();
                memberCB.query().setMemberStatusCode_Equal_正式会員();
                memberCB.query().existsPurchase(purchaseCB -> {
                    purchaseCB.query().setPaymentCompleteFlg_Equal_True();
                });
            });
            cb.query().addOrderBy_PurchaseDatetime_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(purchaseList);
        purchaseList.forEach(p -> {
            Member m = p.getMember().get();
            log(p.getPurchaseId(), p.getPurchaseDatetime(), m.getMemberId(), m.getMemberStatusCodeAsMemberStatus().name(),
                    m.getBirthdate());

            assertTrue(m.isMemberStatusCode正式会員());
        });
    }

    /**
     * 生産販売可能な商品の購入を検索
     * 商品ステータス名称も取得する(ログに出力)
     * 購入価格の降順で並べる
     * 購入の紐づいている商品が生産販売可能であることをアサート
     * @throws Exception
     */
    public void test_生産販売可能な商品の購入を検索() throws Exception {
        // ## Arrange ##
        // ## Act ##
        List<Purchase> purchaseList = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Product();
            cb.query().queryProduct().setProductStatusCode_Equal_生産販売可能();
            cb.query().addOrderBy_PurchasePrice_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(purchaseList);
        purchaseList.forEach(p -> {
            Product product = p.getProduct().get();
            log(p.getPurchaseId(), p.getPurchasePrice(), product.getProductStatusCodeAsProductStatus().name());

            assertTrue(product.isProductStatusCode生産販売可能());
        });
    }

    /**
     * 正式会員と退会会員の会員を検索
     * 会員ステータスの表示順で並べる
     * 会員が正式会員と退会会員であることをアサート
     * 両方とも存在していることをアサート
     * Entity上だけで正式会員を退会会員に変更する
     * 変更した後、退会会員に変更されていることをアサート
     * @throws Exception
     */
    public void test_正式会員と退会会員の会員を検索() throws Exception {
        // ## Arrange ##
        // ## Act ##
        List<Member> memberList = memberBhv.selectList(cb -> {
            cb.orScopeQuery(orCB -> {
                orCB.query().setMemberStatusCode_Equal_正式会員();
                orCB.query().setMemberStatusCode_Equal_退会会員();
            });
            cb.query().queryMemberStatus().addOrderBy_DisplayOrder_Asc();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        memberList.forEach(this::log);

        // 会員が正式会員と退会会員であることをアサート
        // 両方とも存在していることをアサート
        // 方針:一意なHashSet作って、それが正式会員と退会会員のみの2件になればOK
        HashSet<String> statusHash = new HashSet<>();
        memberList.forEach(m -> statusHash.add(m.getMemberStatusCode()));
        assertEquals(2, statusHash.size());
        statusHash.forEach(
                statusCode -> assertTrue(MemberStatus.正式会員.code().equals(statusCode) || MemberStatus.退会会員.code().equals(statusCode)));

        // Entity上だけで正式会員を退会会員に変更する
        memberList.forEach(m -> {
            if (m.isMemberStatusCode正式会員()) {
                m.setMemberStatusCode_退会会員();
            }
        });

        // 変更した後、退会会員に変更されていることをアサート
        // 今回の条件だと、全員が退会会員になっているはず
        memberList.forEach(m -> assertTrue(m.isMemberStatusCode退会会員()));
    }

    /**
     * 銀行振込で購入を支払ったことのある、会員ステータスごとに一番若い会員を検索
     * 正式会員で一番若い、仮会員で一番若い、という風にそれぞれのステータスで若い会員を検索
     * 一回の ConditionBean による検索で会員たちを検索すること (PartitionBy...)
     * ログのSQLを見て、検索が妥当であることを目視で確認すること
     * 検索結果がステータスの件数以上であることをアサート
     * ※"where句の再利用"のドキュメントをよく読んで、MemberCQクラスに arrangeExistsBankTransferPayment() というメソッドを 作ってみたいと思ったら 作ってみましょう。
     * @throws Exception
     */
    public void test_銀行振込で購入を支払ったことのある() throws Exception {
        // ## Act ##
        List<Member> memberList1 = memberBhv.selectList(cb -> {
            cb.query().setMemberStatusCode_Equal_正式会員();
            cb.query().scalar_Equal().max(subCB -> {
                subCB.specify().columnBirthdate();
                subCB.query().setMemberStatusCode_Equal_正式会員();
                subCB.query().arrangeExistsBankTransferPayment();
            });
        });
        // ## Assert ##
        assertHasAnyElement(memberList1);
        memberList1.forEach(m -> log(m.getMemberId(), m.getBirthdate(), m.getMemberStatusCodeAsMemberStatus().name()));

        // ## Act ##
        List<Member> memberList2 = memberBhv.selectList(cb -> {
            cb.query().setMemberStatusCode_Equal_仮会員();
            cb.query().scalar_Equal().max(subCB -> {
                subCB.specify().columnBirthdate();
                subCB.query().setMemberStatusCode_Equal_仮会員();
                subCB.query().arrangeExistsBankTransferPayment();
            });
        });
        // ## Assert ##
        assertHasAnyElement(memberList2);
        memberList2.forEach(m -> log(m.getMemberId(), m.getBirthdate(), m.getMemberStatusCodeAsMemberStatus().name()));

        // ## Act ##
        List<Member> memberList3 = memberBhv.selectList(cb -> {
            cb.query().setMemberStatusCode_Equal_退会会員();
            cb.query().scalar_Equal().max(subCB -> {
                subCB.specify().columnBirthdate();
                subCB.query().setMemberStatusCode_Equal_退会会員();
                subCB.query().arrangeExistsBankTransferPayment();
            });
        });
        // ## Assert ##
        assertHasAnyElement(memberList3);
        memberList3.forEach(m -> log(m.getMemberId(), m.getBirthdate(), m.getMemberStatusCodeAsMemberStatus().name()));
    }

    // -----------------------------------------------------
    //                                      姉妹コードの利用
    //                                                ------
    /**
     * 未払い購入のある会員を検索
     * 姉妹コードの設定によって生成されたメソッドを利用
     * 正式会員日時の降順(nullを後に並べる)、会員IDの昇順で並べる
     * 会員が未払いの購入を持っていることをアサート
     * @throws Exception
     */
    public void test_未払い購入のある会員を検索() throws Exception {
        // ## Arrange ##
        // ## Act ##
        List<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().setMemberStatusCode_InScope_ServiceAvailable();
            cb.query().addOrderBy_FormalizedDatetime_Desc().withNullsLast();
            cb.query().addOrderBy_MemberId_Asc();
            cb.query().existsPurchase(subCB -> {
                subCB.query().setPaymentCompleteFlg_Equal_AsBoolean(false);
            });
        });
        memberBhv.loadPurchase(memberList, purchaseCB -> {
            purchaseCB.query().setPaymentCompleteFlg_Equal_Flase();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        memberList.forEach(m -> {
            List<Purchase> purchaseList = m.getPurchaseList();
            purchaseList.forEach(p -> {
                log(m.getMemberId(), m.getMemberStatusCodeAsMemberStatus().name(), p.getPaymentCompleteFlgAlias());

                assertTrue(p.isPaymentCompleteFlgFlase());
            });
        });
    }

    // -----------------------------------------------------
    //                                      独自の属性を追加
    //                                                ------
    /**
     * 会員ステータスの表示順カラムで会員を並べて検索
     * 会員ステータスの "表示順" カラムの昇順で並べる
     * 会員ステータスのデータ自体は要らない
     * その次には、会員の会員IDの降順で並べる
     * 会員ステータスのデータが取れていないことをアサート
     * 会員が会員ステータスの表示順ごとに並んでいることをアサート
     * @throws Exception
     */
    public void test_会員ステータスの表示順カラムで会員を並べて検索() throws Exception {
        // ## Arrange ##
        // ## Act ##
        List<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().queryMemberStatus().addOrderBy_DisplayOrder_Asc();
            cb.query().addOrderBy_MemberId_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);

        // 会員が会員ステータスの表示順ごとに並んでいることをアサートする用。初期値は最小のdisplayOrderとする。
        Integer prevDisplayOrderAsInteger = 0;

        for (Member m : memberList) {
            Integer displayOrderAsInteger = Integer.valueOf(m.getMemberStatusCodeAsMemberStatus().subItemMap().get("displayOrder").toString());
            log(m.getMemberId(), displayOrderAsInteger);

            // 会員ステータスのデータが取れていないことをアサート
            assertFalse(m.getMemberStatus().isPresent());

            // 会員が会員ステータスの表示順ごとに並んでいることをアサート
            log("前表示順：" + prevDisplayOrderAsInteger, "現在表示順：" + displayOrderAsInteger);
            assertTrue(prevDisplayOrderAsInteger <= displayOrderAsInteger);
            prevDisplayOrderAsInteger = displayOrderAsInteger;
        }
    }
}
