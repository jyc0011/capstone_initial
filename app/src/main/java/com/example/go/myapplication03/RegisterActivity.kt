package com.example.go.myapplication03

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_register.*
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class RegisterActivity : AppCompatActivity() {
    val DATABASE_VERSION = 1
    var code_exist : String? = "00"
    val DATABASE_NAME = "LocalDB.db"
    lateinit var registerresultcode :String
    private lateinit var localDB: LocalDB
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        register_code.visibility = View.INVISIBLE
        register_code.setClickable(false);
        register_code.setFocusable(false);
        // 관리자 코드 입력란 비활성화
        input_code.visibility = View.INVISIBLE
        //관리자 코드 및 등록 번호 안내란 숨기기
        input_code.setClickable(false);
        input_code.setFocusable(false);
        //관리자 코드 및 등록 번호 안내란 비활성화
        spinner.adapter = ArrayAdapter.createFromResource(this,R.array.register_type,android.R.layout.simple_spinner_item)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            //가입유형 콤보박스 생성(스피너)

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    //일반사용자
                    0 -> {
                        register_code.setText("0000000000")
                    }
                    //펫샵
                    1 -> {
                        input_code.visibility = View.VISIBLE
                        input_code.text = "사업자 등록 번호를 입력하십시오(10자리)"

                        register_code.setFocusableInTouchMode(true);
                        register_code.setFocusable(true);
                        register_code.visibility = View.VISIBLE

                    }
                    //관리자
                    2 -> {
                        input_code.visibility = View.VISIBLE
                        input_code.text = "관리자 등록 번호를 입력하십시오(10자리)"

                        register_code.setFocusableInTouchMode(true);
                        register_code.setFocusable(true);
                        register_code.visibility = View.VISIBLE
                    }
                    else -> {
                    }
                }
            }
        }
        if (spinner.selectedItemPosition == 1){
            code_exist = "01"
        } // 선택된 가입 유형이 펫샵인 경우
        else if (spinner.selectedItemPosition == 2){
            code_exist = "02"
        } // 선택된 가입 유형이 관리자인 경우
        else {
            code_exist = "00"
        } // 선택된 가입 유형이 일반 사용자인 경우
        registerresultcode = code_exist + register_code.text.toString()
        setContentView(R.layout.activity_register)
        //레이아웃과 연결하여 화면 출력
        localDB= LocalDB(this, DATABASE_NAME,null, DATABASE_VERSION) // SQLite 모듈 생성
        join_button.setOnClickListener { view->
            if(register_code.text.isEmpty()||join_name.text.isEmpty()||join_email.text.isEmpty()||join_password.text.isEmpty()||join_pwck.text.isEmpty()){// 값이 전부 입력되지 않은경우
                Toast.makeText(this,"값을 전부 입력해주세요..",Toast.LENGTH_LONG).show()
            }else{
                if(join_pwck.text.toString().equals(join_password.text.toString())){//패스워드/패스워드 확인이 일치
                    if(localDB.checkIdExist(join_email.text.toString())){// 아이디 중복 확인
                        Toast.makeText(this,"아이디가 이미 존재합니다.",Toast.LENGTH_LONG).show()
                    }else{// 존재하는 아이디
                        localDB.registerUser(join_email.text.toString(),join_password.text.toString(),registerresultcode,join_name.text.toString())
                    }
                }else{ // 패스워드/패스워드 확인이 일치하지 않음
                    Toast.makeText(this,"패스워드가 틀렸습니다.",Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    //메일전송메소드
    @SuppressLint("QueryPermissionsNeeded")
    private fun sendEmail(content: String,emailadd: String) {
        val title = "강아지 비문서비스에 대한 인증번호"
        val packageManager = packageManager
        var rand : String = listOf(('0'..'9'), ('a'..'z'), ('A'..'Z')).flatten().random().toString()
        // 이메일 인증을 하도록 구현한다. 랜덤한 문자열 7자리를 생성하고 이를 사용자가 입력한 이메일로 전송하여 사용자가 해당 문자열에 대한 검증으로
        // 인증을 구현하였다.
        while (rand.length > 6){
            rand += listOf(('0'..'9'), ('a'..'z'), ('A'..'Z')).flatten().random().toString()
            // 랜덤한 7자리의 문자열을 생성할 때까지 반복한다.
        }
        val intent = Intent(Intent.ACTION_SENDTO) // 메일 전송 설정
            .apply {
                type = "text/plain" // 데이터 타입 설정
                data = Uri.parse("mailto:") // 이메일 앱에서만 인텐트 처리되도록 설정

                putExtra(Intent.EXTRA_EMAIL, emailadd) // 메일 수신 주소 목록
                putExtra(Intent.EXTRA_SUBJECT, title) // 메일 제목 설정
                putExtra(Intent.EXTRA_TEXT, "$content$rand ==> 인증확인 문자열입니다.") // 메일 본문 설정
            }

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(Intent.createChooser(intent, "메일 전송하기"))
            return
        } else {
            Toast.makeText(this, "메일을 전송할 수 없습니다", Toast.LENGTH_LONG).show()
        }
    }
}


