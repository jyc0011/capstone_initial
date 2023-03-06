package com.example.go.myapplication03

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Region
import android.icu.util.TimeZone.*
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region.getRegion
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_mypetregister.*
import kotlinx.android.synthetic.main.activity_mypetregister.spinner01
import kotlinx.android.synthetic.main.activity_mypetregister.textView
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.mypetregisterdialog.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.security.DigestException
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.jar.*


class MyPetRegisterActivity : AppCompatActivity() {
    //Manifest 에서 설정한 권한을 가지고 온다.
    val CAMERA_PERMISSION = arrayOf(Manifest.permission.CAMERA)
    val STORAGE_PERMISSION = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)

    //권한 플래그값 정의
    val FLAG_PERM_CAMERA = 98
    val FLAG_PERM_STORAGE = 99

    //카메라와 갤러리를 호출하는 플래그
    val FLAG_REQ_CAMERA = 101
    val FLAG_REA_STORAGE = 102
    val TAG = "TAG_MyPetRegisterActivity"
    var imagePath : String? = null

    // 촬영한 사진을 경로에 저장하고 
    lateinit var indogimage : File
    // flask - Retrofit통신을 위한 API객체 설정
    lateinit var mRetrofit : Retrofit
    lateinit var mRetrofitAPI: RetrofitAPI
    // s3 이미지 버킷 서버의 접근에 필요한 객체들
    private val accesskey01 : String = "AKIA3B3FW4YSGYUEGL5N"
    // 엑세스 키
    private val secretkey01 : String = "Rmof3Y3HNKNHH84AM+aqw0lanD0BET6TdyY/06q+"
    // 시크릿 키
    private val clientRegion: String? = "ap-northeast-2"
    // 해당 버킷 서버 지역
    private val bucketName: String? = "tester1informationserver"
    // 버킷 이름

    override fun onCreate(savedInstanceState: Bundle?) {
        setRetrofit()
        // 레트로핏 설정
        val emailid : String? = intent.getStringExtra("email_id")
        val name : String? = intent.getStringExtra("name")
        // 사용자의 이메일 아이디와 성함을 이전 페이지에서 넘겨받음.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypetregister)
        // 화면 출력

        spinner01.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.pet_type,
            android.R.layout.simple_spinner_item
        )
        spinner01.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            //품종 유형 콤보박스 생성(스피너)
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
            }
        }
        // 왼쪽의 텍스트를 입력하면 오른쪽의 스피너의 목록에서 입력된 텍스트와 근접한 목록이 선택되도록 구현.
        // pet_type의 배열이 스피너의 목록이므로 해당 배열을 가져옴.
        val array_type: Array<String> = arrayOf(R.array.pet_type.toString())
        var textlength: Int = 0 // 텍스트와 비슷한 입력의 길이 초기값 0 설정

        // 스피너의 첫번째 인덱스 목록부터 순차대로 반복문을 통해 0부터 해당 스피너 길이만큼 진행됨.
        for (i in array_type.indices) {
            var check_spinner: String = spinner01.getItemAtPosition(i).toString()
            if (check_spinner.indexOf(Pettype.text.toString()) >= textlength) {
                spinner01.setSelection(i) //입력된 텍스트와 가장 가까운 목록을 발견시 해당 항목 선택
                textlength =
                    check_spinner.indexOf(Pettype.text.toString()) // 현재 가장 텍스트와 비슷한 항목의 동일 길이로 리셋
            }
        }
        spinner01.setOnClickListener{
            val builder01 = AlertDialog.Builder(this)
            builder01.setTitle("품종을 선택해주십시오.")
                .setItems(array_type,
                    DialogInterface.OnClickListener { dialog, which ->
                        // 여기서 인자 'which'는 배열의 position을 나타냅니다.
                        spinner01.setSelection(which)
                    })
            // 다이얼로그를 띄워주기
            builder01.show()
        }
        // 화면이 만들어 지면서 저장소 권한을 체크 합니다.
        // 권한이 승인되어 있으면 카메라를 호출하는 메소드를 실행합니다.
        if (checkPermission(STORAGE_PERMISSION, FLAG_PERM_STORAGE)) {
            setViews()
        }
        // 정보 입력 확인 버튼
        insert_info.setOnClickListener {
            val builder02 = AlertDialog.Builder(this)
            // 다이얼 로그 메세지의 타이틀로 해당 메세지가 나옴.
            builder02.setTitle("정보 저장 확인"                               )
                .setMessage(
                        "\n강아지 이름: " + Petid.text.toString()
                        +"\n강아지 성별: " + malegroup01.isSelected.toString()
                        +"\n강아지 품종: " + spinner01.selectedItemPosition.toString()
                        +"해당 펫 정보를 저장하시겠습니까?")
                .setPositiveButton("확인",
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog_message01.text = "확인 클릭"
                        // 업로드한 이미지를 s3버킷 서버로 전송후 생성된 이미지 url을 가져옴.
                        val stringimageUrl : String? = uploadWithTransferUtility(Petid.text.toString() + newJpgFileName(), indogimage)
                        // 입력한 펫 이름,성별,품종,입력받은 이미지 url,이미지 해싱 데이터를 BlockChain서버에 전송.
                        val sendPostdata01 = DataModel02.PostModel02(
                            emailid.toString(), // 넘겨받은 이메일 id
                            name.toString(), // 넘겨받은 사용자 성함
                            Petid.text.toString(), // 입력한 펫 이름
                            malegroup01.isSelected.toString(), // 입력한 펫 성별
                            spinner01.selectedItemPosition.toString(), // 입력한 펫 종
                            stringimageUrl, // s3서버에서 입력받은 이미지 url
                            hashSHA256(stringimageUrl) // 입력받은 이미지 url을 sha256해쉬함수로 해시값 출력
                        )
                        // 위에서 입력을 정리한 모델 sendPostdata01을 API의 POST함수의 큐에 담아 전송.
                        mRetrofitAPI.savetransaction(sendPostdata01).enqueue(object : Callback<DataModel02.PostResult02> {
                            override fun onResponse(call: Call<DataModel02.PostResult02>, response: Response<DataModel02.PostResult02>) {
                                Log.d("log",response.toString())
                                Log.d("log", response.body().toString())
                            }
                            override fun onFailure(call: Call<DataModel02.PostResult02>, t: Throwable) {
                                // 실패
                                Log.d("log",t.message.toString())
                                Log.d("log","블록 체인 서버 정보 입력에 실패하였습니다.")
                            }
                        })
                    })
                .setNegativeButton("취소",
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog_message01.text = "취소 클릭"
                    })
            // 다이얼로그를 띄워주기
            builder02.show()
            // 링크가 없는 경우 기본 이미지를 보여주기 위함
            // 링크가 있는 경우 링크에서 이미지를 가져와서 보여준다.
        }
        // 비밀번호 변경하기를 누를시
        pwchange.setOnClickListener{
        }
    }

    private fun setViews() {
        //카메라 버튼 클릭
        camerabutton.setOnClickListener {
            //카메라 호출 메소드
            openCamera()
        }
    }


    private fun openCamera() {
        //카메라 권한이 있는지 확인
        if(checkPermission(CAMERA_PERMISSION,FLAG_PERM_CAMERA)){
            //권한이 있으면 카메라를 실행시킵니다.
            val intent:Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent,FLAG_REQ_CAMERA)
        }
    }

    //권한이 있는지 체크하는 메소드
    fun checkPermission(permissions:Array<out String>,flag:Int):Boolean{
        //안드로이드 버전이 마쉬멜로우 이상일때
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            for(permission in permissions){
                //만약 권한이 승인되어 있지 않다면 권한승인 요청을 사용에 화면에 호출합니다.
                if(ContextCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,permissions,flag)
                    return false
                }
            }
        }
        return true
    }

    //checkPermission() 에서 ActivityCompat.requestPermissions 을 호출한 다음 사용자가 권한 허용여부를 선택하면 해당 메소드로 값이 전달 됩니다.
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            FLAG_PERM_STORAGE ->{
                for(grant in grantResults){
                    if(grant != PackageManager.PERMISSION_GRANTED){
                        //권한이 승인되지 않았다면 return 을 사용하여 메소드를 종료시켜 줍니다
                        Toast.makeText(this,"저장소 권한을 승인해야지만 앱을 사용할 수 있습니다..",Toast.LENGTH_SHORT).show()
                        finish()
                        return
                    }
                }
                //카메라 호출 메소드
                setViews()
            }
            FLAG_PERM_CAMERA ->{
                for(grant in grantResults){
                    if(grant != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this,"카메라 권한을 승인해야지만 카메라를 사용할 수 있습니다.",Toast.LENGTH_SHORT).show()
                        return
                    }
                }
                openCamera()
            }
        }
    }


    //startActivityForResult 을 사용한 다음 돌아오는 결과값을 해당 메소드로 호출합니다.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            when(requestCode){
                FLAG_REQ_CAMERA ->{
                    if(data?.extras?.get("data") != null){
                        //카메라로 방금 촬영한 이미지를 미리 만들어 놓은 이미지뷰로 전달 합니다.
                        val bitmap = data.extras?.get("data") as Bitmap
                        indogimage = bitmapToFile(bitmap,File(filesDir,"image").toString())
                        // 촬영한 비트맵 이미지를 일반 파일 형식의 변수인 indogimage에 할당
                        iv_pre.setImageBitmap(bitmap)
                        // 촬영한 이미지를 이미지 뷰에 보여줌
                    }
                }
            }
        }
    }
    
    // 시간에 따른 랜덤 JPG파일 이름 생성 함수
    private fun newJpgFileName() : String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        val filename = sdf.format(System.currentTimeMillis())
        return "${filename}.jpg"
    }
    
    // 비트맵 형식을 일반 파일 형식으로 변환하는 함수
    fun bitmapToFile(bitmap: Bitmap, path: String): File{
        var file = File(path)
        var out: OutputStream? = null
        try{
            file.createNewFile()
            out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        }finally{
            out?.close()
        }
        return file
    }

    // 파일이름과 파일(파일 경로)을 입력하여 해당 파일을 등록된 AWS S3 서버에 업로드하는 함수
    fun uploadWithTransferUtility(fileName: String, file: File): String {
        // IAM 생성하며 받은 것 입력. 접근할려는 S3서버의 전근및 암호키로 해당 버킷서버의 이름과 지역으로 접근권한을 받아냄
        val awsCredentials: AWSCredentials =
            BasicAWSCredentials(
                accesskey01,
                secretkey01
            ) 
        // 접근할려는 S3서버의 설정
        val s3Client : AmazonS3 = AmazonS3ClientBuilder.standard()
            .withCredentials(AWSStaticCredentialsProvider(awsCredentials))
            .withRegion(clientRegion)
            .build()

        // 전송하는 인터페이스 함수 설정.
        val transferUtility = TransferUtility.builder().s3Client(s3Client)
            .context(applicationContext).build()
        TransferNetworkLossHandler.getInstance(applicationContext)

        // 전송 인터페이스에 해당 전송보낼려는 버킷이름과 파일이름, 파일을 입력하여 보냄.
        val uploadObserver = transferUtility.upload(
            bucketName,
            fileName,
            file
        ) // (bucket api, file이름, file객체)

        // 해당 파일을 전송보내고 전송 보낸 상태에 따른 함수 설정.
        uploadObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (state == TransferState.COMPLETED) {
                    // Handle a completed upload
                }
            }

            override fun onProgressChanged(id: Int, current: Long, total: Long) {
                val done = (current.toDouble() / total * 100.0).toInt()
                Log.d("MYTAG", "UPLOAD - - ID: \$id, percent done = \$done")
            }

            override fun onError(id: Int, ex: java.lang.Exception) {
                Log.d("MYTAG", "UPLOAD ERROR - - ID: \$id - - EX:$ex")
            }
        })
        // 결과로 해당 버킷서버에 업로드한 이미지의 S3 URL을 받아옴.
    return s3Client.getUrl(bucketName,fileName).toString()
    }
    // sha256 해시함수의 출력을 bytes가 아닌 hex로 출력하기 위한 bytes를 hex로 바꾸는 함수
    fun bytesToHex(byteArray: ByteArray): String {
        val digits = "0123456789ABCDEF"
        val hexChars = CharArray(byteArray.size * 2)
        for (i in byteArray.indices) {
            val v = byteArray[i].toInt() and 0xff
            hexChars[i * 2] = digits[v shr 4]
            hexChars[i * 2 + 1] = digits[v and 0xf]
        }
        return String(hexChars)
    }
    // sha256 해시함수. 해당 함수를 이용해 이미지url을 해싱한다.
    fun hashSHA256(msg: String?) : String{
        val hash: ByteArray
        try {
            val md = MessageDigest.getInstance("SHA-256")
            if (msg != null) {
                md.update(msg.toByteArray())
            }
            hash = md.digest()
        } catch (e: CloneNotSupportedException) {
            throw DigestException("couldn't make digest of partial content");
        }

        return bytesToHex(hash)
    }

    //http요청을 보냈고 이건 응답을 받을 콜벡메서드
    private fun setRetrofit(){
        mRetrofit = Retrofit.Builder() // Retrofit2 인터페이스 빌더 생성
            .baseUrl(getString(R.string.baseUrl)) // 인터페이스와 연결될 서버 주소입력
            .addConverterFactory(GsonConverterFactory.create()) 
            .build() // 인터페이스 생성

        mRetrofitAPI = mRetrofit.create(RetrofitAPI::class.java)
        // 위의 설정을 기반으로 Retrofit 인터페이스 생성
    }
    private val mRetrofitCallback  = (object : retrofit2.Callback<JsonObject>{//Json객체를 응답받는 콜백 객체

        //응답을 가져오는데 실패
        @SuppressLint("LongLogTag")
        override fun onFailure(call: Call<JsonObject>, t: Throwable) {
            t.printStackTrace()
            Log.d(TAG, "에러입니다. => ${t.message.toString()}")
            textView.text = "에러\n" + t.message.toString()
        }
        //응답을 가져오는데 성공 -> 성공한 반응 처리
        @SuppressLint("LongLogTag")
        override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
            val result = response.body()
            Log.d(TAG, "결과는 => $result")

            var mGson = Gson()
            val dataParsed1 = mGson.fromJson(result, DataModel02.ChainResult01::class.java)
        }
    })
}