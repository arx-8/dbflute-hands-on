-- #df:assertListZero#
-- /------------------------------------------------
-- Member addresses should be only one at any time.
-- ------------------------------------------------/
select adr.MEMBER_ADDRESS_ID
     , adr.MEMBER_ID
     , adr.VALID_BEGIN_DATE
     , adr.VALID_END_DATE
     , adr.ADDRESS
  from MEMBER_ADDRESS adr
 where exists (
        select subadr.MEMBER_ADDRESS_ID
          from MEMBER_ADDRESS subadr
         where subadr.MEMBER_ID = adr.MEMBER_ID
           and subadr.VALID_BEGIN_DATE > adr.VALID_BEGIN_DATE
           and subadr.VALID_BEGIN_DATE <= adr.VALID_END_DATE
       )
;

-- #df:assertListZero#
-- /------------------------------------------------
-- 正式会員日時を持ってる仮会員がいないこと
-- ------------------------------------------------/
select m.MEMBER_ID
     , m.FORMALIZED_DATETIME
  from MEMBER m
 where m.MEMBER_STATUS_CODE = 'PRV'
   and m.FORMALIZED_DATETIME is not null
;

-- #df:assertListZero#
-- /------------------------------------------------
-- まだ生まれていない会員がいないこと
-- ------------------------------------------------/
select m.MEMBER_ID, m.BIRTHDATE
  from member m
 where m.BIRTHDATE >= NOW()
;

-- #df:assertListZero#
-- /------------------------------------------------
-- 退会会員が退会情報を持っていることをアサート
-- ------------------------------------------------/
select m.MEMBER_ID
  from member m
 where m.MEMBER_STATUS_CODE = 'WDL'
   and not exists (
             select mw.MEMBER_ID
               from MEMBER_WITHDRAWAL mw
              where mw.MEMBER_ID = m.MEMBER_ID
           )
;
