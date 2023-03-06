package com.example.go.myapplication03

import com.google.gson.JsonObject //JSON형태로 파일을 읽어들이기 위한 라이브러리
import retrofit2.Call   //RETROFIT2의 Call라이브러리(요청에 대한 응답을 불러옴)
import retrofit2.http.* //RETROFIT2로 HTTP통신을 하기 위한 라이브러리
// retrofit API로 Block Chain 서버와 사용자의 어플리케이션을 연결해주는 인터페이스.
// 사용자는 Retrofit에 있는 함수를 사용해서 블록체인 서버에 요청을 보내고
// 각 함수의 형식에 따라 응답받을 수 있다.
interface RetrofitAPI {
    //서버에 요청할 주소를 입력하고 요청한 주소에 대한 함수를 등록.
    @GET("/mine/transaction")
    fun getMine() : Call<JsonObject> //MainActivity에서 불러와서 이 함수에 큐를 만들고 대기열에 콜백을 넣어주면 그거갖고 요청하는거임.
    @POST("/transactions/new/dog") // 블록체인 서버에 나의 강아지 정보를 등록시에 POST요청으로 실행됨.
    fun savetransaction(
        //해당 함수 요청시 Body에 나의 강아지 정보를 파라미터로 전송함.
        @Body jsonparams: DataModel02.PostModel02 // 나의 강아지 정보 전송시에 DataModel02.PostModel02 데이터 형식의
    ) : Call<DataModel02.PostResult02> // POST요청후 DatamODEL02.PostResult02의 데이터 형식으로 응답받음
    @GET("/chain")
    // 블록체인의 체인을 조회. 정보를 조회할때 사용된다.
    fun getchaininfo(): Call<DataModel02.ChainResult01>
}