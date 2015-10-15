package org.docksidestage.handson.logic;

import java.time.LocalDateTime;

import javax.annotation.Resource;

import org.dbflute.helper.HandyDate;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberSecurityBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberServiceBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.dbflute.exentity.MemberSecurity;
import org.docksidestage.handson.dbflute.exentity.MemberService;

/**
 * @author swan0
 *
 */
public class HandsOn07Logic extends BsLogicLogger{
    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    protected MemberBhv memberBhv;
    @Resource
    protected MemberSecurityBhv memberSecurityBhv;
    @Resource
    protected MemberServiceBhv memberServiceBhv;

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    // -----------------------------------------------------
    //                                          データの登録
    //                                                ------
    /**
     * 自分自身の会員を登録
     * 正式会員で登録
     * 現在日時を取得する Logic を作成して、正式会員日時を入れる
     * @return
     */
    public Member insertMyselfMember(){
        Member member = new Member();
        member.setMemberName("自分自身");
        member.setMemberAccount("Myself");
        member.setMemberStatusCode_正式会員();
        member.setFormalizedDatetime(currentLocalDateTime());
        member.setBirthdate(new HandyDate("1999-9-9").getLocalDate());
        // commonColumnMapのおかげで不要に。
        // member.setRegisterDatetime(getCurrentLocalDateTime());
        // member.setRegisterUser("anony");
        // member.setUpdateDatetime(getCurrentLocalDateTime());
        // member.setUpdateUser("anony");
        // バージョン番号は自動で初期値に
        // member.setVersionNo(0L);
        memberBhv.insert(member);
        return member;
    }

    // -----------------------------------------------------
    //                              さらに登録してみましょう
    //                                                ------
    /**
     * 誰かを正式会員として登録
     * 業務的に必須の関連テーブルも登録
     * @return
     */
    public Member insertYourselfMember() {
        Member member = new Member();
        member.setMemberName("誰か");
        member.setMemberAccount("anonymous");
        member.setMemberStatusCode_正式会員();
        member.setFormalizedDatetime(currentLocalDateTime());
        memberBhv.insert(member);

        // 業務的に必須の関連テーブルも登録
        MemberSecurity security = new MemberSecurity();
        security.setMemberId(member.getMemberId());
        security.setLoginPassword("pass");
        security.setReminderQuestion("好きなORMは？");
        security.setReminderAnswer("DBFlute");
        memberSecurityBhv.insert(security);

        MemberService service = new MemberService();
        service.setMemberId(member.getMemberId());
        service.setAkirakaniOkashiiKaramuMei(100);
        service.setServiceRankCode_Bronze();
        memberServiceBhv.insert(service);

        log(member);
        log(security);
        log(service);

        return member;
    }

    // -----------------------------------------------------
    //                                               Utility
    //                                                ------
    /**
     * 現在日時の LocalDateTime を返す
     * @return
     */
    private LocalDateTime currentLocalDateTime() {
        return LocalDateTime.now();
    }
}
