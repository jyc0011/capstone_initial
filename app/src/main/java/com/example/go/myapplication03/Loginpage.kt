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
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Loginpage : AppCompatActivity() {
    var connectnode : String? = null
    var id : String? = null
    var idcheckanswer : String? = null
    lateinit var mRetrofit : Retrofit
    lateinit var mRetrofitAPI: RetrofitAPI
    lateinit var mCallChain : retrofit2.Call<JsonObject> // Json형식의 데이터를 요청하는 객체입니다.
    private lateinit var binding:ActivityLoginpageBinding
    private lateinit var localDB: LocalDB
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRetrofit()
        setContentView(R.layout.activity_loginpage)
        // 로그인 확인 버튼을 누를시
        check_button.setOnClickListener {
            val id = join_email.text.toString()
            // 입력한 이메일 아이디
            val passwd = join_password.text.toString()
            // 입력한 패스워드
            val sendPostdataloginid = DataModel02.Loginid(
                id,
                passwd
            )
            mRetrofitAPI.loginid(sendPostdataloginid).enqueue(object :
                Callback<DataModel02.PostResult02> {
                override fun onResponse(
                    call: Call<DataModel02.PostResult02>,
                    response: Response<DataModel02.PostResult02>
                ) {
                    Log.d("log", response.toString())
                    Log.d("log", response.body().toString())
                    idcheckanswer = response.body().toString()
                }

                override fun onFailure(
                    call: Call<DataModel02.PostResult02>,
                    t: Throwable
                ) {
                    // 실패
                    Log.d("log", t.message.toString())
                    Log.d("log", "블록 체인 서버 정보 입력에 실패하였습니다.")
                }
            })
            if(idcheckanswer == "LoginOK"){
                // 로그인 성공
                val builder02 = AlertDialog.Builder(this)
                builder02.setTitle("로그인 확인")
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
    private fun setRetrofit(){
        mRetrofit = Retrofit.Builder() // Retrofit2 인터페이스 빌더 생성
            .baseUrl(connectnode) // 인터페이스와 연결될 서버 주소입력
            .addConverterFactory(GsonConverterFactory.create())
            .build() // 인터페이스 생성

        mRetrofitAPI = mRetrofit.create(RetrofitAPI::class.java)
        // 위의 설정을 기반으로 Retrofit 인터페이스 생성
    }
}