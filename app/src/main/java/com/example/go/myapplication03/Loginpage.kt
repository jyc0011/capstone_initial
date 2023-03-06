package com.example.go.myapplication03


import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.go.myapplication03.databinding.ActivityLoginpageBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_loginpage.*
import kotlinx.android.synthetic.main.activity_mypetregister.*
import kotlinx.android.synthetic.main.mypetregisterdialog.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Loginpage : AppCompatActivity() {
    val DATABASE_VERSION = 1
    val DATABASE_NAME = "LocalDB.db"
    //권한 플래그값 정의
    val FLAG_PERM_CAMERA = 98
    val FLAG_PERM_STORAGE = 99

    //카메라와 갤러리를 호출하는 플래그
    val FLAG_REQ_CAMERA = 101
    val FLAG_REA_STORAGE = 102
    val TAG = "TAG_MyPetRegisterActivity"
    var imagePath : String? = null
    var id : String? = null
    lateinit var mRetrofit : Retrofit
    lateinit var mRetrofitAPI: RetrofitAPI
    lateinit var mCallChain : retrofit2.Call<JsonObject> // Json형식의 데이터를 요청하는 객체입니다.
    private lateinit var binding:ActivityLoginpageBinding
    private lateinit var localDB: LocalDB
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loginpage)
        // 뷰바인딩 사용
        setContentView(R.layout.activity_loginpage)
        // SQLite 모듈 생성
        localDB= LocalDB(this, "REGISTERED",null, DATABASE_VERSION)

        setContentView(R.layout.activity_loginpage)
        // 로그인 확인 버튼을 누를시
        check_button.setOnClickListener {
            id = join_email.text.toString()
            val passwd = join_password.text.toString()
            val exist = localDB.logIn(id!!,passwd) // 로그인 실행
            if(exist){ // 로그인 성공
                val builder02 = AlertDialog.Builder(this)
                builder02.setTitle("로그인 확인"
                )
                    .setMessage("로그인 정보가 확인되었습니다.")
                    .setPositiveButton("확인",
                        DialogInterface.OnClickListener { dialog_message01, Okay_button ->
                            // 확인버튼으로 메인 서비스 페이지로 넘어갈때, 해당 사용자의 id 정보는 다음 페이지에도 그 값을 넘겨준다.
                            val intent001 = Intent(this,Mainservicepage::class.java)
                            intent001.putExtra("email_id",id)
                            // 상태값, 전달할려는 intent 설정
                            setResult(Activity.RESULT_OK,intent001)
                            // 메인 서비스 페이지 시작.
                            startActivity(intent001)
                        }
                   )
                    .setNegativeButton("취소",
                        DialogInterface.OnClickListener { dialog, id ->
                            dialog_message01.text = "확인되었습니다."
                        })
                // 다이얼로그를 띄워주기
                builder02.show()
                //val intent01 = Intent(this, Loginpage::class.java)
                //startActivity(intent01)
            }else{ // 실패
                Toast.makeText(this@Loginpage, "아이디나 비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show()
            }
            
        }
    }
}