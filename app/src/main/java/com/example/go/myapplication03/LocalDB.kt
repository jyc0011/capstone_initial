package com.example.go.myapplication03
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
class LocalDB (
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version){
    //코드 참조
//https://developer.android.com/training/data-storage/sqlite?hl=ko
    // 생성된 DB가 없을시 최초로 DB생성.
    override fun onCreate(db: SQLiteDatabase?) {
        // DB 생성시 실행
        if (db != null) {
            createDatabase(db)
        }
    }
    // SQ DB사용을 위한 해당 버전 업그레이드
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // DB 버전 변경시 실행됨
        val sql : String = "DROP TABLE if exists ${LocalDatas.userData.TABLE_NAME}"
        if (db != null) {
            db.execSQL(sql)
            onCreate(db)
        } //  버전이 변경되면 기존 Table을 삭제후 재생성함.
    }
    // Local DB에 테이블 생성시 사용되는 함수
    fun createDatabase(db: SQLiteDatabase) {
        // 테이블이 존재하지 않는경우 생성
        var sql: String = "CREATE TABLE if not exists ${LocalDatas.userData.TABLE_NAME} (" +
                "${BaseColumns._ID} integer primary key autoincrement," +
                "${LocalDatas.userData.COLUMN_NAME_ID} varchar(30)," +
                "${LocalDatas.userData.COLUMN_NAME_PASSWORD} varchar(20),"+
                "${LocalDatas.userData.COLUMN_REGISTER_CODE} varchar(12),"+
                "${LocalDatas.userData.COLUMN_USER_NAME} varchar(12)"+
                ");"
        db.execSQL(sql)
    }
    // 유저 가입시 사용되는 함수
    fun registerUser(email_id: String, password:String,register_type:String,user_name:String){
        val db =this.writableDatabase
        val values = ContentValues().apply {// insert될 데이터값
            put(LocalDatas.userData.COLUMN_NAME_ID, email_id)
            put(LocalDatas.userData.COLUMN_NAME_PASSWORD, password)
            put(LocalDatas.userData.COLUMN_REGISTER_CODE,register_type)
            put(LocalDatas.userData.COLUMN_REGISTER_CODE,user_name)
        }
        val newRowId = db?.insert(LocalDatas.userData.TABLE_NAME, null, values)
        // 인서트후 인서트된 primary key column의 값(_id) 반환.
    }
    fun checkIdExist(email_id: String): Boolean {
        val db = this.readableDatabase

        // 리턴받고자 하는 컬럼 값의 array
        val projection = arrayOf(BaseColumns._ID)
        //,LocalDatas.userData.COLUMN_NAME_ID, LocalDatas.userData.COLUMN_NAME_PASSWORD)


        //  WHERE "id" = id AND "password"=password 구문 적용하는 부분
        val selection = "${LocalDatas.userData.COLUMN_NAME_ID} = ?"
        val selectionArgs = arrayOf(email_id)

//         정렬조건 지정
//        val sortOrder = "${FeedEntry.COLUMN_NAME_SUBTITLE} DESC"

        val cursor = db.query(
            LocalDatas.userData.TABLE_NAME,   // 테이블
            projection,             // 리턴 받고자 하는 컬럼
            selection,              // where 조건
            selectionArgs,          // where 조건에 해당하는 값의 배열
            null,                   // 그룹 조건
            null,                   // having 조건
            null               // orderby 조건 지정
        )
        if(cursor.count>0){//  반환된 cursor 값이 존재
            return true;
        }else{//반환된 cursor 값이 없음
            return false;
        }

    }
    fun logIn(email_id: String, password:String,): Boolean {
        val db = this.readableDatabase

        // 리턴받고자 하는 컬럼 값의 array
        val projection = arrayOf(BaseColumns._ID)
        //,LocalDatas.userData.COLUMN_NAME_ID, LocalDatas.userData.COLUMN_NAME_PASSWORD)


        //  WHERE "id" = id AND "password"=password 구문 적용하는 부분
        val selection = "${LocalDatas.userData.COLUMN_NAME_ID} = ? AND ${LocalDatas.userData.COLUMN_NAME_PASSWORD} = ?"
        val selectionArgs = arrayOf(email_id,password)

//         정렬조건 지정
//        val sortOrder = "${FeedEntry.COLUMN_NAME_SUBTITLE} DESC"

        val cursor = db.query(
            LocalDatas.userData.TABLE_NAME,   // 테이블
            projection,             // 리턴 받고자 하는 컬럼
            selection,              // where 조건
            selectionArgs,          // where 조건에 해당하는 값의 배열
            null,                   // 그룹 조건
            null,                   // having 조건
            null               // orderby 조건 지정
        )
        if(cursor.count>0){//  반환된 cursor의 0번째 값이 null이면
            return true;
        }else{
            return false;
        }

    }
}