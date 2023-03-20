package com.example.go.myapplication03

import java.io.File

class DataModel02 {
    // transaction/new/dog POST 요청시 보내는 데이터 모델
    data class Registerdog(
        var ownerid: String? = null, //소유자 이메일 id(블록체인 DB의 기본키 및 범용 DB의 기본키와 연결되는 기본키)
        var owner: String? = null, // 소유자 성함
        var name: String? = null, // 펫 이름
        var sex: String? = null, // 펫 성별
        var species: String? = null, // 펫 종류
        var url: String? = null, // 펫 이미지 url
        var img_hash: String? = null // 펫 비문 이미지 해쉬 정보
    )
    // POST 요청후 응답받는 데이터 모델
    data class PostResult02(
        var message: String? = null // PostModel02에 대한 결과 출력 메세지(해당 메세지는 Retrofit2에 의해 app을 사용하는 사용자한테로 받게 됨)
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
    // 서비스에 아이디를 가입시 등록시키는 객체 구조
    data class Registerid(
        var idcode : String? = null,
        var idname : String? = null,
        var emailid : String? = null,
        var idpw : String? = null
    )
    // 가입시, 아이디 중복을 확인하기 위해 요청으로 보내는 객체 구조
    data class Searchid(
        var emailid : String? = null
    )
    // 로그인시, 아이디와 비밀번호를 전송하는 객체 구조
    data class Loginid(
        var emailid : String? = null,
        var idpw : String? = null
    )
    data class sendimage(
     var petid : String? = null,
     var imagefile : File? = null
    )
}
