#!/usr/bin/env python
# coding: utf-8

# In[ ]:


from flask import Flask, request, jsonify
from flask_restful import Resource, Api, reqparse, abort
import json
from time import time
from textwrap import dedent
from uuid import uuid4
import random
# Our blockchain.py API
from blockchain import Blockchain
import apscheduler
from apscheduler.schedulers.background import BackgroundScheduler
from apscheduler.jobstores.base import JobLookupError
import socket
import requests
# 다수의 노드에서의 프로세스를 처리하기 위한 signal 라이브러리
import signal
# /transactions/new : to create a new transaction to a block
# /mine : to tell our server to mine a new block.
# /chain : to return the full Blockchain.
# /nodes/register : to accept a list of new nodes in the form of URLs
# /nodes/resolve : to implement our Consensus Algorithm

blockchain = Blockchain() # 블록체인 생성
# 자신의 외부 ip주소를 플라스크 웹서버로 실행하기 위해 받는 변수

def scheduler():
    taskupdatechain = blockchain.resolve_conflicts()
    # 지속적으로 블록체인 업데이트를 실행하는 스케쥴러를 만든다.
schedule = BackgroundScheduler(daemon=True, timezone='Asia/Seoul')
# 스케쥴러 생성
schedule.add_job(scheduler,'interval', seconds=100,id = 'test')
# 위에서 설정한 스케쥴러 작업을 등록
schedule.start()
# 스케쥴러 시작
app = Flask(__name__)
# Universial Unique Identifier
node_identifier = str(uuid4()).replace('-','')

count = 0
state = 0 # 트랜잭션이 있는지 확인하는 상태 변수
# 트랜잭션들을 마이닝하여 실제 체인에 블록으로 등록시키는 마이닝 트랜잭션
# 어떠한 트랜잭션의 발생시 반드시 실행되도록 설정.
@app.route('/mine/transaction', methods=['GET'])
def mine():
    # 노드의 마이닝을 접근된 시간에 따른 순차적인 마이닝을 수행하기 위해 불러오는 변수 
    global state
    # 현재 마이닝 요청으로 접근된 시각을 해당 지역변수에 저장.
    # 블록체인에 마지막에 넣어진 블록.
    checkmychain = blockchain.resolve_conflicts()
    # 자신의 노드가 최신 노드인지 먼저 확인한 뒤 진행합니다.
    last_block= blockchain.last_block
    # 마지막 마이닝 요청의 POW증명에 대한 값
    last_proof= last_block['proof']
    # 마지막 블록의 증명 값
    # 블록 마이닝을 완료했다면 클라이언트에게도 등록이 완료되었다는 응답 메세지를 전송
    if state== 0:
        return 'missing values', 400
    else:
        # 오직 트랜잭션이 있을때만 블록을 생성
        proof= blockchain.pow(last_proof)
        # 마지막 블록의 증명 값으로 실제 POW를 만족하여 마이닝할수 있는지 확인.
        previous_hash= blockchain.hash(last_block) 
        # 블록 체인의 마지막 블록의 해쉬값
        block= blockchain.new_block(proof, previous_hash)
        # 새로운 블록을 자신의 블록 체인에 블록 업데이트 한다.
        response = {
        'message': 'new block found',
        'index': block['index'],
        'timestamp':block['timestamp'],
        'transaction': block['transaction'],
        'proof': block['proof'],
        'previous_hash': block['previous_hash']
        }
        updateallnode=blockchain.request_update_chain()
        # 모든 노드들을 자신이 방금 업데이트한 블록체인으로 업데이트시킴
        state = 0
        # 트랜잭션을 마이닝했으므로 남아있는 트랜잭션은 없게 된다. 따라서 state는 0이다.
        return jsonify(response)

'''@app.route('/transactions/new', methods = ['POST']) 
def new_transaction():
    values= request.get_json()
    required = ['buyer', 'seller', 'dog_info', 'price'] 
    global state
    if not all(k in values for k in required):
        return 'missing values', 400
    index = blockchain.new_transaction(values['buyer'], values['seller'], values['dog_info'], values['price'])
    response = { 'message': 'Transaction will be added to Block {%s}' %index} 
    state = 1
    return jsonify(response), 201
'''    
# 사용자의 관리자 코드, 이름, email 아이디와 비밀번호를 받고 아이디를 블록으로 등록시키는 함수
@app.route('/transactions/new/id', methods = ['POST'])
def new_transaction_registerid():
    values=request.get_json()
    required=["idcode","idname","emailid", "idpw"]
    index = blockchain.new_transaction_registerid(values["idcode"],values["idname"],values["emailid"],values["idpw"])
    global state
    if not all(k in values for k in required):
        return 'missing values', 400
    response = {'message': 'Transaction will be added to Block {%s}' %index}
    state = 1
    return jsonify(response), 201

# 가입자가 가입 성립시 기록되는 트랙잭션
# 분양시 성립되는 거래를 기록하는 트랜잭션
@app.route('/transactions/new/transaction', methods = ['POST']) 
def new_transaction_transaction():
    values= request.get_json()
    required = ['buyer', 'seller', 'dog_info', 'price','transactioncode'] 
    global state
    if not all(k in values for k in required):
        return 'missing values', 400
    index = blockchain.new_transaction_transaction(values['buyer'], values['seller'], values['dog_info'], values['price'],value['transactioncode'])
    response = { 'message': 'Transaction will be added to Block {%s}' %index} 
    state = 1
    return jsonify(response), 201

# 펫 정보 입력란에서 해당 개의 정보를 입력하여 새로운 개의 정보를 입력하는 트랜잭션
@app.route('/transactions/new/dog', methods = ['POST'])
def new_transaction_dog():
    values=request.get_json()
    required=["ownerid","owner","name", "sex", "species", "url"]
    dog_info = blockchain.get_dog_information(values["ownerid"],values["owner"],values["name"],values["sex"], values["species"], values["url"])
    global state
    if not all(k in values for k in required):
        return 'missing values', 400
    index= blockchain.new_registration_dog(values['ownerid'],dog_info, values['ownerid'])
    response = {'message': 'Transaction will be added to Block {%s}' %index}
    state = 1
    return jsonify(response), 201

# 개의 이미지를 입력하여 해당 개의 정보를 받아오는 트랜잭션
@app.route('/get/dog_info', methods = ['POST'])
def get_dog_info():
    values=request.get_json()
    required=['owner','sex', 'species', 'url', 'img_hash']
    index= blockchain.get_dog_information(values["owner"],values['sex'], values['species'], values['url'], values['img_hash'])
    response = {'message': 'Transaction will be added to Block {%s}' %index}
    return jsonify(response), 201

# 전체 블록체인의 블록들과 그 길이를 가져오는 트랜잭션
@app.route('/chain', methods=['GET'])
def full_chain():
    response = {
        'chain' : blockchain.chain,
        'length': len(blockchain.chain),
    }
    return jsonify(response), 200

# 로그인을 확안하는 함수. 아이디와 비밀번호를 받고 해당 아이디와 
@app.route('/chain/loginsearch', methods = ['POST'])
def login_id():
    values=request.get_json()
    required=['emailid','idpw']
    # 이메일 아이디를 요청으로 입력받음.
    transaction=blockchain.search_transaction('emailid',values['emailid'],'idpw',values['idpw'])
    if transaction:
        response = {'message': 'LoginOK' }
        return jsonify(response), 201
    else:
        response = {'message': 'LoginNOOK' }
        return jsonify(response), 201
    return "Error: Please supply a valid list of nodes", 400

# 사용자가 로그인시에 아이디,비밀번호를 입력받아 체인의 트랜잭션에서 해당 정보를 조회하여 메세지를 전송하는 함수
@app.route('/chain/idsearch', methods = ['POST'])
def search_id():
    values=request.get_json()
    required=['emailid']
    # 이메일 아이디를 요청으로 입력받음.
    transaction=blockchain.search_transaction('emailid',values['emailid'])
    if transaction:
        response = {'message': 'NoCan' }
        return jsonify(response), 201
    else:
        response = {'message': 'Can' }
        return jsonify(response), 201
    return "Error: Please supply a valid list of nodes", 400

# IP노드를 블록체인 네트워크에 가입시키는 함수
@app.route('/nodes/register', methods=['POST'])
def register_nodes():
    values = request.get_json()
    nodes = values.get('nodes')
    if nodes is None: # Bad Request 400
        return "Error: Please supply a valid list of nodes", 400
    # 풀노드로 네트워크안에 있는 노드들 확인
    for node in nodes:
        # 노드들안에 있는 
        blockchain.register_node(node)
    # 노드
    response = {
        'message' : 'New nodes have been added',
        'total_nodes': list(blockchain.nodes),
    }
    return jsonify(response), 201

# 마이닝시 해당 트랜잭션을 블록에 올릴것인지 합의를 거친뒤 그 결과를 반환하는 트랜잭션
@app.route('/nodes/resolve', methods=['GET'])
def consensus():
    # 해당 체인이 유효한지 검사하여 유효하면 해당 체인으로 블록 체인의 체인을 업데이트
    replaced = blockchain.resolve_conflicts() # True False return
    # 해당 함수를 호출하므로써 호출한 노드는 최신 체인으로 업데이트되거나 
    # 혹은 자신이 최신 체인이였을 경우, 나의 노드가 최신 체인임을 확인 가능.
    # 만약 체인이 유효하다면 합의가 완료->해당 체인을 새로운 블록 체인의 체인으로 등록
    if replaced:
        response = {
            'message' : 'Our chain was replaced',
            'new_chain' : blockchain.chain
        }
    # 만약 체인이 유효하지 않다면 기존의 체인을 그대로 유지한다.
    else:
        response = {
            'message' : 'Our chain is authoritative',
            'chain' : blockchain.chain
        }
    return jsonify(response), 200

if __name__ == '__main__':
    app.run(host='0.0.0.0')


# In[ ]:




