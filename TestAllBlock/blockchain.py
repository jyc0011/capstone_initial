#!/usr/bin/env python
# coding: utf-8

# In[3]:


import hashlib
import json
from time import time
from urllib.parse import urlparse
import requests

####### block generation & its principle
# 블록체인 클래스 생성
# 해당 클래스는 server.ipynb에서 실제 블록체인 프레임(블록체인 함수및 구조)로 사용됨.
class Blockchain(object):
    # initialize the blockchain info
    def __init__(self):
        self.chain = [] # 블록체인의 빈 체인 생성(리스트)
        self.current_transaction = [] # 블록체인의 빈 현재 트랜잭션 생성(리스트)
        self.nodes = set() # 가입된 노드 생성(set = 동적 리스트)
        # genesis block
        self.new_block(previous_hash=1, proof=100) 
        # 블록체인 생성시 반드시 맨 처음 블록도 같이 생성되어야 하기 때문에 맨 처음 블록 생성
        
    # 마이닝의 결과로 새로운 블록 생성시 만들어지는 블록 
    def new_block(self,proof,previous_hash=None):
        block = {
            'index': len(self.chain)+1, # 생성된 블록의 인덱스 = 블록의 마지막 인덱스 + 1의 값
            'timestamp': time(), # timestamp from 1970
            'transaction': self.current_transaction, # 생성된 블록에 저장된 트랜잭션
            'proof': proof, # 마이닝을 하기 위해서 다른 블록 값들과 넣어지는 nonce(논스)값
            'previous_hash': previous_hash or self.hash(self.chain[-1]) # 이전 블록의 해쉬값.혹은 처음 체인에서의 블록 해쉬값
        }
        self.current_transaction = [] # 현재 트랜잭션 - 딕셔너리
        self.chain.append(block) # 체인에 위에서 입력한 블록정보를 추가함.
        return block
    # 트랜잭션 키와 값을 받아서 해당 트랜잭션을 출력하는 함수(키는 두개를 받을수 있고, 두개를 받을 경우 각각의 입력값이 
    # 등록된 값과 동일해야 트랜잭션을 반환. 아님 None을 반환한다.)
    def search_transaction(self,insertkey,insertvalues,insertkey2=None,insertvalues2=None):
        for i in range(len(self.chain)):
            # block들의 transaction을 조회
            block=self.chain[i]
            transaction=block.get('transaction')
            for n in range(len(transaction)):
                value01=transaction[n].get(insertkey)
                if value01 == insertvalues:
                    if insertkey2 == None:
                        return transaction
                    else:
                        if transaction[n].get(insertkey2) == insertvalues2:
                            return transaction
                        else :
                            continue
        return None
    # 해당 key에 대한 values가 존재하면 해당 트랜잭션을 출력. 
    
    # 사용자가 해당 서비스를 이용한 분양시, 그 거래에 대한 트랜잭션
    def new_transaction_transaction(self, buyer, seller, dog_info, price,transactioncode): 
        self.current_transaction.append(
            {
            'buyer': buyer, # 구매자 email id - 스트링
            'seller': seller,  # 판매자 email id - 스트링
            'dog_info': dog_info, # 강아지 정보 - 딕셔너리
            'price': price, # 판매 가격 - 스트링
            'transactioncode' : transactioncode # 거래 코드 
            }
        )
        self.current_transaction.append(
            {
            'owner': buyer, # 구매됨으로써 현재의
            'dog_info': dog_info # 강아지 정보 - 딕셔너리
            }
        )
        return self.last_block['index']+1
    # 사용자가 서비스 가입시 사용자의 id와 비밀번호를 네트워크에 등록하는 함수
    def new_transaction_registerid(self, idcode, idname,emailid , idpw): 
        self.current_transaction.append(
            {
            'idcode': idcode, # 사용자 관련 칼럼
            'idname': idname,  # 사용자 이름
            'emailid': emailid, # 사용자 이메일 아이디
            'idpw': idpw, # 사용자 암호
            }
        )
        return self.last_block['index']+1
    
    # 개의 정보로 저장하기 위한 함수 
    def get_dog_information(self,email_id, owner,name, sex, species,url):
        dog_info = {
        'ownerid':email_id,#이메일 아이디(로그인 정보를 담고있는 범용DB와 연결되는 칼럼)
        'owner':owner, # 소유자 이름
        'name':name, # 강아지 이름
        'sex' : sex, # 강아지 성별
        'species': species, # 강아지 종
        'url': url, # s3서버 이미지 url
        }
        # GET으로 넘겨주는 정보 출력
        print('%s' %email_id) 
        print('%s' %owner)
        print('%s' %sex)
        print('%s' %species)
        print('%s' %url)
        return dog_info

    # 개 정보 등록 함수
    def new_registration_dog (self, owner, dog_info,img_hash):
        global dog_info_exist
        dog_info_exist = 1 
        if dog_info_exist == 1:
            self.current_transaction.append(
                {
                'owner': owner, 
                'dog_info': dog_info,
                'img_hash' : img_hash
                }
            )
            return self.last_block['index'] + 1
        else:
            return self.last_block['index']
    
    # 해당 노드를 블록 체인 서버에 등록(풀노드)
    def register_node(self, address):
        parsed_url = urlparse(address)
        # 가입 노드에 대한 
        self.nodes.add(parsed_url.netloc) # netloc attribute! network lockation
    # 유효한 체인인지 검사하는 함수.
    def valid_chain(self,chain):
        # 큐로 생각하여 가장 처음에 넣어진 체인의 블록은 체인의 맨 처음에 위치함.
        # 현재 블록(last_block)의 해쉬값과 다음 블록의 이전 해쉬값(previous_hash)값을 비교하여 해당 체인이 유효한지
        # 검사.
        last_block = chain[0]
        # 맨 처음에 제네시스 블록의 해시값과 이전 블록에서의 해시값을 비교하는 작업으로 시작됨으로 체인의 제네시스 블록을 
        # 해시값을 비교할 마지막 블록으로 설정
        current_index = 1
        # 해당 체인의 길이만큼 순차대로 검사.
        while current_index < len(chain):
            # 순차대로 체인의 블록
            block = chain[current_index]
            print('%s' % last_block)
            print('%s' % block)
            print("\n---------\n")
            # check that the hash of the block is correct(해당 블록의 이전 해쉬값과 실제 업데이트되있는 마지막 블록의 
            # 해쉬값을 비교) 만약 맞지 않을 경우, 해당 체인은 유효하지 않음.
            if block['previous_hash'] != self.hash(last_block):
                return False
            # 현재 블록을 마지막 블록으로 바꾸고 다음 블록의 이전 해쉬값과 비교하며 검사
            last_block = block
            # 현재 체인의 인덱스를 1 높임.
            current_index += 1
        return True

    def request_update_chain(self):
        # 마이닝 이후 반드시 실행되는 함수. 마이닝을 하여 블록을 블록체인에 넣어둔 노드를 기준으로 모든 노드들을 
        # 자신이 추가한 블록까지 업데이트하는 함수
        neighbours = self.nodes
        # 해당 블록체인 네트워크에 등록된 다른 노드들
        for node in neighbours:
            tmp_url = 'http://' + str(node) + '/nodes/resolved'
            # 다른 노드들을 업데이트하도록 설정합니다.
            response = requests.get(tmp_url)
            if response.status_code == 200:
                # 다른 노드들이 자신의 체인으로 업데이트되었는지에 대한 응답을 받습니다.
                print("response : "+response.json()['message'])
                # 각 노드들에 대한 메세지를 응답받아 그것을 출력하는 명령어
        print("All node update my chain")
        # 모든 노드들이 업데이트되었다는 것을 출력하는 명령어
        return True
        
    
    def resolve_conflicts(self):
        # 블록 생성후 체인에 블록을 넣고나서 해당 노드에서의 체인이 유효한지를 검사하고 
        # 각 노드들의 체인을 검사하여 해당 노드의 체인의 길이가 더 길고, 유효한 체인이 검증되면
        neighbours = self.nodes
        # 해당 블록체인 네트워크에 등록된 다른 노드들
        new_chain = None
        # 업데이트될 체인
        # 처음에는 나의 체인이 제일 최신 체인으로 생각하여 None으로 초기화

        max_length = len(self.chain) 
        # Our chain length 
        for node in neighbours:
            # 각 다른 노드들의 체인을 비교해가며 다른 노드의 체인의 길이가 더 길고,
            # 그 노드의 체인이 유효하다면 해당 노드의 체인으로 업데이트한뒤, 응답으로 True를 return
            tmp_url = 'http://' + str(node) + '/chain'
            # 다른 노드들을 순차적으로 server파일에 있는 함수를 호출하여 해당 노드의 체인을 검사 것이며, 
            # 체인을 응답받는 url
            response = requests.get(tmp_url)
            # 해당 노드의 체인의 길이를 응답받음.
            if response.status_code == 200:
                # 응답이 정상적으로 수행되었을 시, 조건문 진입
                length = response.json()['length']
                # 응답받은 json형식의 출력에서 해당 노드의 체인 길이를 length 지역 변수에 할당.
                chain = response.json()['chain']
                # 응답받은 json형식의 출력에서 해당 노드의 체인을 지역 변수에 할당
                if length > max_length and self.valid_chain(chain):
                    # 만약 검사하는 노드의 체인 길이가 가장 최신의 체인이여서 해당 체인의 길이가 함수를 수행하는 노드의 
                    # 체인 길이보다 길어진 경우, 그리고 해당 노드의 체인이 유효한 경우
                    max_length = length
                    # 가장 긴 길이를 해당 길이로 업데이트함.
                    new_chain = chain
                    # 해당 체인으로 업데이트할 체인에 할당.
                    continue
                    # new_chain이 바뀌었다면 다시 반복문으로 돌아감.
            if new_chain:
                # 최종적으로 나의 체인의 길이가 가장 긴 최신 체인을 new_chain에 할당한 경우
                self.chain = new_chain
                # new_chain의 체인을 나의 체인으로 업데이트함.
                return True
                # 해당 체인으로 대체되었으므로 True를 반환.
            return False
            # 만약 나의 체인이 가장 최신이였어서 new_chain이 None으로 남게된 경우
            # 나의 체인은 가장 최신의 체인으로 인증된 것이므로 False를 반환.

    # directly access from class, share! not individual instance use it
    @staticmethod
    # 위의 staticmethod는 blockchain이라는 클래스 밖의 전역에서도 해당 함수를 사용할 수 있도록 정의하기위해서 
    # 사용한 것이다.
    def hash(block):
        # 블록을 입력받아 sha256해시함수를 이용한 해시값을 반환하는 함수.
        block_string = json.dumps(block, sort_keys=True).encode()
        # json.dumps로 block이라는 딕셔너리 객체를 정렬하고나서 encode()로 하여금 문자열로 인코딩을 한다.
        return hashlib.sha256(block_string).hexdigest()
        # sha256 : 단방향 암호화 해시함수, 64자리(256bit) 암호화 문자열로 출력해준다.
        # sha258을 통해 해당 블록의 해시출력
    @property
    # 데코레이션 property : 해당 데코레이션의 함수는 자동으로 set과 get의 속성을 부여받는 객체가 된다.
    # 즉, 어떤 값을 출력할 때는 get함수, 어떤 값을 입력할 때는 set함수가 사용된다.
    def last_block(self):
        # 마지막 블록에 대한 객체 생성
        return self.chain[-1]
        # 체인의 마지막으로 넣어진 블록을 출력.
    def pow(self, last_proof):
        # 블록을 마이닝할 노드는 반드시 해당 노드가 마이닝할 능력이 됨을 증명해야한다. 
        # 즉, 이에 대한 증명방식이 필요한데 이중하나가 pow(작업증명방식)이다.
        # pow(작업증명방식)은 마이닝을 요청후 해당 마이닝 노드에서 임의의 값들로 컴퓨터 자원을 이용하여 
        # 해당 블록 체인 네트워크에서 문제내는 어떠한 해시값을 추리할때, 해당 해시값을 맞추면
        # 해당 노드가 블록을 생성할 수 있다는 것을 증명했다는것으로 생각하여 해당 노드는 pow을 통과
        # 마이닝할 수 있게되는 것이다.
        proof = 0
        # 여기서 proof는 논스로 pow과정중엣 pow를 만족시키기 위해 계속 값이 올라간다. 
        while self.valid_proof(last_proof, proof) is False:
            proof += 1

        return proof

    @staticmethod
    def valid_proof(last_proof, proof):
        # 고정된 블록의 해시 입력값 + 논스값을 입력하여 pow증명을 해내가는 과정의 함수
        guess = str(last_proof + proof).encode()
        # pow을 하는 노드는 먼저 블록의 해시 입력값 + 논스값을 문자열로 인코딩한다.
        guess_hash = hashlib.sha256(guess).hexdigest()
        # 위에서 인코딩한 문자열 값을 sha256해시함수에 입력값으로 입력하여 64자리 문자열을 입력받고 다시 hexdigest로
        # 해당 64자리 문자열을 16진수로 변환하여 추측pow값을 추출한다.
        return guess_hash[:4] == "0000" 
    # 추측한 64자리가 만약 마지막 4자리가 0000이 되었을때,  


# In[ ]:




