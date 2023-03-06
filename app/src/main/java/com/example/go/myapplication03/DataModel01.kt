class DataModel01 {
    data class Index(val index : Int) //  블록 체인의 체인 인덱스 번호
    data class Message(val message : String) // 블록 체인 서버에서의 전송 메세지
    data class Proof(val proof : Int)
    data class Previous_hash(val previous_hash : String)
    data class Transaction(val transaction :List<Map<String?,String?>?>?) // 트랜잭션
}