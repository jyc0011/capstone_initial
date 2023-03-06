package com.example.go.myapplication03
import android.provider.BaseColumns
object LocalDatas { //  로컬 데이터 베이스의 자료형태 정의된 object
    object userData : BaseColumns {  //  users 라는 DB 테이블의 데이터 컬럼 내용 정리
        const val TABLE_NAME = "users" //테이블 이름
        const val COLUMN_NAME_ID = "email_id" // 이메일 아이디(기본키)
        const val COLUMN_NAME_PASSWORD = "password" // 패스워드
        const val COLUMN_REGISTER_CODE = "register_code"//	임의의 컬럼명 작성
        const val COLUMN_USER_NAME = "user_name" // 사용자 이름
    }
    object Groups :BaseColumns{ // 만약 그룹에 관련한 DB 형식을 지정하고 싶다면 동일한 방식으로 추가합니다.

    }
}