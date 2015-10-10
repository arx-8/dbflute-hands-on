package org.docksidestage.handson.exercise;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.dbflute.cbean.result.PagingResultBean;
import org.dbflute.exception.NonSpecifiedColumnAccessException;
import org.dbflute.helper.HandyDate;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberSecurityBhv;
import org.docksidestage.handson.dbflute.exbhv.PurchaseBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.dbflute.exentity.MemberSecurity;
import org.docksidestage.handson.dbflute.exentity.MemberStatus;
import org.docksidestage.handson.dbflute.exentity.ProductCategory;
import org.docksidestage.handson.dbflute.exentity.Purchase;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * @author swan0
 *
 */
public class HandsOn03Test extends UnitContainerTestCase {
    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    protected MemberBhv memberBhv;
    @Resource
    protected MemberSecurityBhv memberSecurityBhv;
    @Resource
    protected PurchaseBhv purchaseBhv;

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    // -----------------------------------------------------
    //                                                ConditionBeanの基本をさらに
    //                                                ------
    /**
     * [1] 会員名称がSで始まる1968年1月1日以前に生まれた会員を検索
     * 会員ステータスも取得する
     * 生年月日の昇順で並べる
     * 会員が1968/01/01以前であることをアサート
     * ※"以前" の解釈は、"その日ぴったりも含む" で。
     * ※もし、よければ HandyDate を使ってみましょう。
     * @throws Exception
     */
    public void test_1() throws Exception {
        // ## Arrange ##
        final String memberNamePrefix = "S";
        final HandyDate birthdateTo = new HandyDate("1968-1-1");

        // ## Act ##
        List<Member> memberList = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberStatus();
            cb.query().setMemberName_LikePrefix(memberNamePrefix);
            cb.query().setBirthdate_LessEqual(birthdateTo.getLocalDate());
            cb.query().addOrderBy_Birthdate_Asc();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        memberList.forEach(m -> {
            log(m.getMemberId(), m.getMemberName(), m.getBirthdate(), m.getMemberStatus());

            assertTrue(m.getBirthdate().isBefore(birthdateTo.getLocalDate()) || m.getBirthdate().isEqual(birthdateTo.getLocalDate()));
            assertTrue(birthdateTo.isGreaterEqual(m.getBirthdate()));

        });
    }

    /**
     * [2] 会員ステータスと会員セキュリティ情報も取得して会員を検索
     * 若い順で並べる。生年月日がない人は会員IDの昇順で並ぶようにする
     * 会員ステータスと会員セキュリティ情報が存在することをアサート
     * ※カージナリティを意識しましょう
     * @throws Exception
     */
    public void test_2() throws Exception {
        // ## Arrange ##
        // ## Act ##
        List<Member> memberList = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberStatus();
            cb.setupSelect_MemberSecurityAsOne();
            cb.query().addOrderBy_Birthdate_Desc().withNullsLast();
            cb.query().addOrderBy_MemberId_Asc();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        memberList.forEach(m -> {
            log(m.getMemberId(), m.getBirthdate(), m.getMemberStatus(), m.getMemberSecurityAsOne());

            assertTrue(m.getMemberStatus().isPresent());
            assertTrue(m.getMemberSecurityAsOne().isPresent());
        });
    }

    /**
     * [3] 会員セキュリティ情報のリマインダ質問で2という文字が含まれている会員を検索
     * 会員セキュリティ情報のデータ自体は要らない
     * リマインダ質問に2が含まれていることをアサート
     * アサートするために別途検索処理を入れても誰も文句は言わない
     * ※Actでの検索は本番でも実行されることを想定して、テスト都合でパフォーマンス劣化させないように
     * ※実装できたら、こんどはアサートのための検索の回数が一回になるようにしてみましょう(もし、複数回検索しているのであれば)。 また、それもできたら、会員名称とリマインダ質問を会員ごとに一行のログに出力してみましょう。
     * @throws Exception
     */
    public void test_3() throws Exception {
        // ## Arrange ##
        final String reminderQuestionContain = "2";

        // ## Act ##
        List<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().queryMemberSecurityAsOne().setReminderQuestion_LikeSearch(reminderQuestionContain, op -> op.likeContain());
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        memberList.forEach(m -> log(m.getMemberId(), m.getMemberSecurityAsOne()));

        // リマインダ質問のアサートをするために、MemberIdで会員セキュリティ情報を検索
        // ラムダさんパネーっす
        memberSecurityBhv.selectList(cb -> cb.query().setMemberId_InScope(memberBhv.extractMemberIdList(memberList)))
                .forEach(ms -> assertTrue(ms.getReminderQuestion().contains(reminderQuestionContain)));

        // [別解トライ] ラムダさんパネーっす ver.2
        // stream()でList生成（returnも省略できちゃう）
        // List<Integer> memberIdList = memberList.stream().map(m -> {
        //     return m.getMemberId();
        // }).collect(Collectors.toList());
        List<Integer> memberIdList = memberList.stream().map(m -> m.getMemberId()).collect(Collectors.toList());

        // stream()でMap生成
        Map<Integer, MemberSecurity> securityMap = memberSecurityBhv.selectList(cb -> {
            cb.query().setMemberId_InScope(memberIdList);
        }).stream().collect(Collectors.toMap(ms -> ms.getMemberId(), ms -> ms));

        // assert
        memberList.forEach(m -> {
            MemberSecurity ms = securityMap.get(m.getMemberId());
            log(m.getMemberId(), ms.getReminderQuestion());

            assertTrue(ms.getReminderQuestion().contains(reminderQuestionContain));
        });
    }

    /**
     * [4] 会員ステータスの表示順カラムで会員を並べて検索
     * 会員ステータスの "表示順" カラムの昇順で並べる
     * 会員ステータスのデータ自体は要らない
     * その次には、会員の会員IDの降順で並べる
     * 会員ステータスのデータが取れていないことをアサート
     * 会員が会員ステータスごとに固まって並んでいることをアサート
     * @throws Exception
     */
    public void test_4() throws Exception {
        // ## Arrange ##
        // ## Act ##
        List<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().queryMemberStatus().addOrderBy_DisplayOrder_Asc();
            cb.query().addOrderBy_MemberId_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        memberList.forEach(m -> assertFalse(m.getMemberStatus().isPresent()));

        // 会員が会員ステータスごとに固まって並んでいることをアサート
        // 方針：
        // 既出リストで、新規出現コード値のみを保持する。
        // 新規出現コードを検知した時に、既出でないか検査する。

        // nullpo対策に、最初のステータスコードは決め打ちでaddしておく
        LinkedList<String> foundCodeList = new LinkedList<>();
        foundCodeList.add(memberList.get(0).getMemberStatusCode());

        // fail()の動作確認用（試しに末尾に出現するコードを先頭に入れておく）
        // foundCodeList.addFirst("PRV");

        for (Member m : memberList) {
            String statusCode = m.getMemberStatusCode();
            log(m.getMemberId(), statusCode);

            if (!foundCodeList.getLast().equals(statusCode)) {
                if (foundCodeList.contains(statusCode)) {
                    fail(statusCode + "：コードが既出。固まって並んでいない。");
                } else {
                    foundCodeList.addLast(statusCode);
                }
            }
        }

        // [別解トライ] 既出コードはSetで一意にする。
        // 方針：
        // 既出コードは、Setに持たせる。
        // 直前のレコードと異なるコードが出現したら、新規出現である（既出Setにない）ことを確認する。
        Set<String> codeSet = new HashSet<>();
        String prevCode = null;

        // assertFalse()の動作確認用（試しに末尾に出現するコードを先に入れておく）
        // codeSet.add("PRV");

        for (Member m : memberList) {
            String currentCode = m.getMemberStatusCode();
            log(m.getMemberId(), "curentCode:" + currentCode, "prevCode:" + prevCode);

            if (!currentCode.equals(prevCode)) {
                assertFalse(currentCode + "：コードが既出。固まって並んでいない。", codeSet.contains(currentCode));
            }
            codeSet.add(currentCode);
            prevCode = currentCode;
        }
    }

    /**
     * [5] 生年月日が存在する会員の購入を検索
     * 会員名称と会員ステータス名称と商品名も一緒に取得(ログ出力)
     * 購入日時の降順、購入価格の降順、商品IDの昇順、会員IDの昇順で並べる
     * OrderBy がたくさん追加されていることをログで確認すること
     * 購入に紐づく会員の生年月日が存在することをアサート
     * ※ログ出力は、スーパークラスの log() メソッドが利用できる。可変長引数でカンマ区切り出力になる。
     * @throws Exception
     */
    public void test_5() throws Exception {
        // ## Arrange ##
        // ## Act ##
        List<Purchase> purchaseList = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Member().withMemberStatus();
            cb.query().queryMember().setBirthdate_IsNotNull();
            cb.query().addOrderBy_PurchaseDatetime_Desc();
            cb.query().addOrderBy_PurchasePrice_Desc();
            cb.query().addOrderBy_ProductId_Asc();
            cb.query().queryMember().addOrderBy_MemberId_Asc();
        });

        // ## Assert ##
        assertHasAnyElement(purchaseList);
        purchaseList.forEach(p -> {
            LocalDate birthdate = p.getMember().get().getBirthdate();
            log(p.getPurchaseId(), p.getPurchaseDatetime(), p.getPurchasePrice(), p.getProductId(), birthdate);

            assertNotNull(birthdate);
        });
    }

    /**
     * [6] 会員名称に "vi" を含む会員を検索
     * 会員ステータスも一緒に取得
     * ただし、会員ステータス名称だけ取得できればいい (説明や表示順カラムは不要)
     * 2005年10月の1日から3日までに正式会員になった会員を検索
     * 会員名称と正式会員日時と会員ステータス名称をログに出力
     * 会員ステータスがコードと名称だけが取得されていることをアサート
     * 会員の正式会員日時が指定された条件の範囲内であることをアサート
     * ※Java8 (DBFlute-1.1) なら、assertException(...)を使うとよいでしょう
     * ※実装できたら、こんどはスーパークラスのメソッド adjustMember_FormalizedDatetime_...() を使って、10月1日ジャスト(時分秒なし)の正式会員日時を持つ会員データを作成してテスト実行してみましょう。 もともと一件しかなかった検索結果が「二件」になるはずです。
     * @throws Exception
     */
    public void test_6() throws Exception {
        // ## Arrange ##
        final String memberNameContain = "vi";
        final LocalDateTime fromDateTime = new HandyDate("2005-10-1").moveToDayJust().getLocalDateTime();
        final LocalDateTime toDatetime = new HandyDate("2005-10-3").moveToDayTerminal().getLocalDateTime();

        // ※実装できたら、こんどはスーパークラスのメソッド adjustMember_FormalizedDatetime_...() を使って、10月1日ジャスト(時分秒なし)の正式会員日時を持つ会員データを作成してテスト実行してみましょう。 もともと一件しかなかった検索結果が「二件」になるはずです。
        adjustMember_FormalizedDatetime_FirstOnly(new HandyDate("2005-10-1").getLocalDateTime(), memberNameContain);

        // ## Act ##
        List<Member> memberList = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberStatus();
            cb.specify().specifyMemberStatus().columnMemberStatusName();
            cb.query().setMemberName_LikeContain(memberNameContain);
            cb.query().setFormalizedDatetime_FromTo(fromDateTime, toDatetime, op -> op.compareAsDate());
        });

        // ## Assert ##
        log("List.size() = " + memberList.size());
        assertEquals(2, memberList.size());

        assertHasAnyElement(memberList);
        memberList.forEach(m -> {
            MemberStatus memberStatus = m.getMemberStatus().get();
            log(m.getMemberId(), m.getFormalizedDatetime(), memberStatus.getMemberStatusName());

            // 会員ステータスがコードと名称だけが取得されていることをアサート
            assertNotNull(memberStatus.getMemberStatusCode());
            assertNotNull(memberStatus.getMemberStatusName());
            assertException(NonSpecifiedColumnAccessException.class, () -> memberStatus.getDescription());
            assertException(NonSpecifiedColumnAccessException.class, () -> memberStatus.getDisplayOrder());

            // 会員の正式会員日時が指定された条件の範囲内であることをアサート
            LocalDateTime formalizedDt = m.getFormalizedDatetime();
            assertTrue(fromDateTime.isBefore(formalizedDt) || fromDateTime.isEqual(formalizedDt));
            assertTrue(toDatetime.isAfter(formalizedDt) || toDatetime.isEqual(formalizedDt));
        });
    }

    /**
     * [7] 正式会員になってから一週間以内の購入を検索
     * 会員と会員ステータス、会員セキュリティ情報も一緒に取得
     * 商品と商品ステータス、商品カテゴリ、さらに上位の商品カテゴリも一緒に取得
     * 上位の商品カテゴリ名が取得できていることをアサート
     * 購入日時が正式会員になってから一週間以内であることをアサート
     * ※ログ出力と書いてなくても、テストの動作を確認するためにも(自由に)ログ出力すると良い。
     * ※実装できたら、こんどはスーパークラスのメソッド adjustPurchase_PurchaseDatetime_...() を呼び出し、調整されたデータによって検索結果が一件増えるかどうか確認してみましょう。 もし増えないなら、なぜ増えないのか...
     * @throws Exception
     */
    public void test_7() throws Exception {
        // ## Arrange ##
        // 時分秒を削って比較するので、1日分足しておく
        final int toDayRange = 7 + 1;

        // ※実装できたら、こんどはスーパークラスのメソッド adjustPurchase_PurchaseDatetime_...() を呼び出し、調整されたデータによって検索結果が一件増えるかどうか確認してみましょう。 もし増えないなら、なぜ増えないのか...
        adjustPurchase_PurchaseDatetime_fromFormalizedDatetimeInWeek();

        // ## Act ##
        List<Purchase> purchaseList = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Member().withMemberStatus();
            cb.setupSelect_Member().withMemberSecurityAsOne();
            cb.setupSelect_Product().withProductStatus();
            cb.setupSelect_Product().withProductCategory().withProductCategorySelf();

            cb.columnQuery(colCB -> {
                colCB.specify().columnPurchaseDatetime();
            }).greaterEqual(colCB -> {
                colCB.specify().specifyMember().columnFormalizedDatetime();
            });
            cb.columnQuery(colCB -> {
                colCB.specify().columnPurchaseDatetime();
            }).lessEqual(colCB -> {
                colCB.specify().specifyMember().columnFormalizedDatetime();
            }).convert(op -> op.truncTime().addDay(toDayRange));
        });

        // ## Assert ##
        log("List.size() = " + purchaseList.size());
        assertHasAnyElement(purchaseList);
        purchaseList.forEach(p -> {
            Member m = p.getMember().get();
            ProductCategory pc = p.getProduct().get().getProductCategory().get();
            log(p.getPurchaseId(), p.getPurchaseDatetime(), m.getFormalizedDatetime(), pc.getProductCategoryName(),
                    pc.getProductCategorySelf().get().getProductCategoryName());

            // 上位の商品カテゴリ名が取得できていることをアサート
            assertTrue(pc.getProductCategorySelf().isPresent());

            // 購入日時が正式会員になってから一週間以内であることをアサート
            assertTrue(p.getPurchaseDatetime().isAfter(m.getFormalizedDatetime()));
            log(p.getPurchaseDatetime().minusDays(Long.valueOf(toDayRange)));
            assertTrue(p.getPurchaseDatetime().minusDays(Long.valueOf(toDayRange)).isBefore(m.getFormalizedDatetime()));
        });
    }

    /**
     * [8] 1974年までに生まれた、もしくは不明の会員を検索
     * 会員ステータス名称、リマインダ質問と回答、退会理由入力テキストも取得(ログ出力)
     * 若い順だが生年月日が null のデータを最初に並べる
     * 生年月日が指定された条件に合致することをアサート
     * 1974年生まれの人が含まれていることをアサート
     * 生まれが不明の会員が先頭になっていることをアサート
     * @throws Exception
     */
    public void test_8() throws Exception {
        // ## Arrange ##
        final HandyDate expectToBirthdate = new HandyDate("1974-1-1").moveToYearTerminal();
        final String assertMarker = "found born in 1974";

        giveMeMoreTestData();

        // ## Act ##
        List<Member> memberList = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberStatus();
            cb.setupSelect_MemberSecurityAsOne();
            cb.setupSelect_MemberWithdrawalAsOne();

            // orScopeQueryは不要
            cb.query().setBirthdate_FromTo(null, expectToBirthdate.getLocalDate(), op -> op.compareAsYear().allowOneSide().orIsNull());

            cb.query().addOrderBy_Birthdate_Desc().withNullsFirst();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        memberList.forEach(m -> {
            // nullpo防止に、MemberWithdrawalが存在しない場合は空のEntityを生成（Lamdaを上手く使ったスマートな書き方が思いつかない・・・）
            // orElseGet()の方がコスト安？必要となるまで引数Objを生成しないらしい。参考：http://qiita.com/shindooo/items/815d651a72f568112910
            // MemberWithdrawal mw = m.getMemberWithdrawalAsOne().orElseGet(() -> new MemberWithdrawal());
            // Stringだけ受け取った方がよりシンプル？
            String reasonInputText = m.getMemberWithdrawalAsOne().map(mw -> mw.getWithdrawalReasonInputText()).orElse("(空っぽ)");

            MemberSecurity ms = m.getMemberSecurityAsOne().get();
            Optional<LocalDate> optBirthdate = Optional.ofNullable(m.getBirthdate());

            log(m.getMemberId(), optBirthdate.orElse(null), ms.getReminderQuestion(), ms.getReminderAnswer(), reasonInputText);

            optBirthdate.ifPresent(b -> {
                HandyDate birthdateHd = new HandyDate(b);

                // 生年月日が指定された条件に合致することをアサート
                assertTrue(birthdateHd.isLessEqual(expectToBirthdate.getLocalDate()));

                // 1974年生まれの人が含まれていることをアサート
                if (birthdateHd.isYear(1974)) {
                    markHere(assertMarker);
                }
            });
        });
        assertMarked(assertMarker);

        // 生まれが不明の会員が先頭になっていることをアサート
        // 方針：誕生日null群と誕生日存在群の、境目を見つけて、境目以降はnullが1件もないこと。
        boolean isNullGroup = true;
        for (Member m : memberList) {
            LocalDate birthdate = m.getBirthdate();
            if (isNullGroup) {
                if (birthdate != null) {
                    isNullGroup = false;
                }
            } else {
                assertNotNull(birthdate);
            }
        }
    }

    /**
     * [9] 無効な条件は無視されることを確認しつつ生年月日のない会員を検索
     * 会員名称の等値条件に null を設定
     * 会員アカウントの等値条件に空文字を設定
     * 生年月日がない、という条件を設定
     * 2005年6月に正式会員になった会員は先にして、会員IDの降順で並べる
     * 会員名称や会員アカウントの条件がないことをログで確認すること
     * 検索された会員の生年月日が存在しないことをアサート
     * 2005年6月に正式会員になった会員が先に並んでいることをアサート
     * ※Java8 (DBFlute-1.1) なら、cb.ignoreNullOrEmptyQuery()を使うとよいでしょう
     * @throws Exception
     */
    public void test_9() throws Exception {
        // ## Arrange ##
        final HandyDate formalizedOrderByFirst = new HandyDate("2005-6-1");

        // ## Act ##
        List<Member> memberList = memberBhv.selectList(cb -> {
            // 1.0.x系と同じ動作に
            cb.ignoreNullOrEmptyQuery();

            cb.query().setMemberName_Equal(null);
            cb.query().setBirthdate_IsNull();
            cb.query().addOrderBy_FormalizedDatetime_Asc().withManualOrder(mob -> {
                mob.when_FromTo(formalizedOrderByFirst.getDate(), formalizedOrderByFirst.getDate(), op -> op.compareAsMonth());
            });
            cb.query().addOrderBy_MemberId_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        memberList.forEach(m -> {
            log(m.getMemberId(), m.getMemberName(), m.getFormalizedDatetime(), m.getBirthdate());

            assertNull(m.getBirthdate());
        });

        // 2005年6月に正式会員になった会員が先に並んでいることをアサート
        // 方針：先頭群とその他群の、境目を見つけて、境目以降は先頭条件なデータが1件もないこと。
        boolean isHeadGroup = true;
        for (Member m : memberList) {
            HandyDate formalizedHd = (m.getFormalizedDatetime() == null) ? null : new HandyDate(m.getFormalizedDatetime());
            if (isHeadGroup) {
                if (formalizedHd == null || !formalizedHd.isYear(2005) || !formalizedHd.isMonth(6)) {
                    isHeadGroup = false;
                }
            } else {
                assertTrue(formalizedHd == null || !formalizedHd.isYear(2005) || !formalizedHd.isMonth(6));
            }
        }
    }

    // -----------------------------------------------------
    //                              CBでページングしてみよう
    //                                                ------
    /**
     * 全ての会員をページング検索
     * 会員ステータス名称も取得
     * 会員IDの昇順で並べる
     * ページサイズは 3、ページ番号は 1 で検索すること
     * 会員ID、会員名称、会員ステータス名称をログに出力
     * SQLのログでカウント検索時と実データ検索時の違いを確認
     * 総レコード件数が会員テーブルの全件であることをアサート
     * 総ページ数が期待通りのページ数(計算で導出)であることをアサート
     * 検索結果のページサイズ、ページ番号が指定されたものであることをアサート
     * 検索結果が指定されたページサイズ分のデータだけであることをアサート
     * PageRangeを 3 にして PageNumberList を取得し、[1, 2, 3, 4]であることをアサート
     * 前のページが存在しないことをアサート
     * 次のページが存在することをアサート
     * @throws Exception
     */
    public void test_CBでページングしてみよう() throws Exception {
        // ## Arrange ##
        final int pageSize = 3;
        final int pageNumber = 1;

        // ## Act ##
        PagingResultBean<Member> memberListPage = memberBhv.selectPage(cb -> {
            cb.setupSelect_MemberStatus();
            cb.query().addOrderBy_MemberId_Asc();
            cb.paging(pageSize, pageNumber);
        });

        // ## Assert ##
        assertHasAnyElement(memberListPage);
        memberListPage.forEach(m -> {
            log(m.getMemberId(), m.getMemberName(), m.getMemberStatus().get().getMemberStatusName());
        });

        // 総レコーwpド件数が会員テーブルの全件であることをアサート
        final int allSelectCount = memberBhv.selectCount(cb -> {});
        log("総レコード件数:" + allSelectCount);
        assertEquals(allSelectCount, memberListPage.getAllRecordCount());

        // 総ページ数が期待通りのページ数(計算で導出)であることをアサート
        final int allPageCountByCalc = (allSelectCount % pageSize == 0) ? allSelectCount / pageSize : allSelectCount / pageSize + 1;
        log("総ページ数:" + allPageCountByCalc);
        assertEquals(allPageCountByCalc, memberListPage.getAllPageCount());

        // 検索結果のページサイズ、ページ番号が指定されたものであることをアサート
        assertEquals(pageSize, memberListPage.getPageSize());
        assertEquals(pageNumber, memberListPage.getCurrentPageNumber());

        // 検索結果が指定されたページサイズ分のデータだけであることをアサート
        assertEquals(pageSize, memberListPage.size());

        // PageRangeを 3 にして PageNumberList を取得し、[1, 2, 3, 4]であることをアサート
        List<Integer> pageNumberList = memberListPage.pageRange(op -> op.rangeSize(3)).createPageNumberList();
        List<Integer> expectedList = Arrays.asList(1, 2, 3, 4);
        assertTrue(expectedList.equals(pageNumberList));

        // 前のページが存在しないことをアサート
        assertFalse(memberListPage.existsPreviousPage());

        // 次のページが存在することをアサート
        assertTrue(memberListPage.existsNextPage());
    }

    // -----------------------------------------------------
    //                            CBでカーソル検索ってみよう
    //                                                ------
    /**
     * 会員ステータスの表示順カラムで会員を並べてカーソル検索
     * 会員ステータスの "表示順" カラムの昇順で並べる
     * 会員ステータスのデータも取得
     * その次には、会員の会員IDの降順で並べる
     * 会員ステータスが取れていることをアサート
     * 会員が会員ステータスごとに固まって並んでいることをアサート
     * 検索したデータをまるごとメモリ上に持ってはいけない
     * (要は、検索結果レコード件数と同サイズのリストや配列の作成はダメ)
     * @throws Exception
     */
    public void test_CBでカーソル検索ってみよう() throws Exception {
        // ## Arrange ##
        // 会員が会員ステータスごとに固まって並んでいることをアサートする用
        LinkedList<String> foundCodeList = new LinkedList<>();

        // fail()の動作確認用
        // foundCodeList.addFirst("PRV");

        // ## Act ##
        memberBhv.selectCursor(cb -> {
            cb.setupSelect_MemberStatus();
            cb.query().queryMemberStatus().addOrderBy_DisplayOrder_Asc();
            cb.query().addOrderBy_MemberId_Desc();
        } , m -> {
            // ## Assert ##
            String statusCode = m.getMemberStatusCode();
            log(m.getMemberId(), statusCode, m.getMemberStatus().get().getDisplayOrder());

            // 会員ステータスが取れていることをアサート
            assertNotNull(m.getMemberStatus());

            // 会員が会員ステータスごとに固まって並んでいることをアサート
            // 方針：
            // 既出リストで、新規出現コード値のみを保持する。
            // 新規出現コードを検知した時に、既出でないか検査する。

            // nullpo対策に、最初のコードは必ずadd
            if (foundCodeList.size() == 0) {
                foundCodeList.addLast(statusCode);
            }

            if (!foundCodeList.getLast().equals(statusCode)) {
                if (foundCodeList.contains(statusCode)) {
                    fail(statusCode + "：コードが既出。固まって並んでいない。");
                } else {
                    foundCodeList.addLast(statusCode);
                }
            }
        });
    }

    // -----------------------------------------------------
    //                                               Utility
    //                                                ------
    /**
     * for test_8()
     * 誕生日の境界値が欲しかった。業務整合性は気にしない。
     */
    private void giveMeMoreTestData() {
        Member member = new Member();
        member.setMemberName("境界太郎");
        member.setMemberAccount("BorderTarou");
        member.setMemberStatusCode_正式会員();
        member.setFormalizedDatetime(new HandyDate("2015-09-28").getLocalDateTime());
        member.setBirthdate(new HandyDate("1974-1-1").moveToYearTerminal().getLocalDate());
        member.setRegisterDatetime(new HandyDate("2015-09-28").getLocalDateTime());
        member.setRegisterUser("anony");
        member.setUpdateDatetime(new HandyDate("2015-09-28").getLocalDateTime());
        member.setUpdateUser("anony");
        member.setVersionNo(0L);
        memberBhv.insert(member);

        MemberSecurity security = new MemberSecurity();
        security.setMemberId(member.getMemberId());
        security.setLoginPassword("pass");
        security.setReminderQuestion("☆★☆★☆");
        security.setReminderAnswer("☆★☆★☆");
        security.setRegisterDatetime(new HandyDate("2015-09-28").getLocalDateTime());
        security.setRegisterUser("anony");
        security.setUpdateDatetime(new HandyDate("2015-09-28").getLocalDateTime());
        security.setUpdateUser("anony");
        security.setVersionNo(0L);
        memberSecurityBhv.insert(security);
    }
}
