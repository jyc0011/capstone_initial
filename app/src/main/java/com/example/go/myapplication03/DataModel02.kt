package com.example.go.myapplication03
class DataModel02 {
    // transaction/new/dog POST 요청시 보내는 데이터 모델
    data class PostModel02(
        var ownerid: String? = null, //소유자 이메일 id(블록체인 DB의 기본키 및 범용 DB의 기본키와 연결되는 기본키)
        var owner: String? = null, // 소유자 성함
        var name: String? = null, // 펫 이름
        var sex: String? = null, // 펫 성별
        var species: String? = null, // 펫 종류
        var url: String? = null, // 펫 이미지 url
        var img_hash: String? = null // 펫 비문 이미지 해쉬 정보
    )
    // /transaction/new/dog POST 요청후 응답받는 데이터 모델
    data class PostResult02(
        var result02: String? = null // PostModel02에 대한 결과 출력 메세지(해당 메세지는 Retrofit2에 의해 app을 사용하는 사용자한테로 받게 됨)
    )
    // 체인 조회시 객체 구조
    data class ChainResult01(
    var chain: MutableList<Block?>? , // 체인을 조회할 시 블록 체인의 체인
    var length: Int? = null // 체인의 길이
    )
    // 체인의 블록 객체 구조
    data class Block(
        var index:Int? = null, // 해당 체인의 인덱스
        var timestamp:Float? = null,
        var transaction:MutableMap<String,String?>? = null,
        var proof: Int? = null,
        var previous_hash: String? = null
    )
}
