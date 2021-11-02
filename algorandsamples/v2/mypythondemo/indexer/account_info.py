# account_info.py
import json
# requires Python SDK version 1.3 or higher
from algosdk.v2client import indexer

# instantiate indexer client
myindexer = indexer.IndexerClient(indexer_token="", indexer_address="http://localhost:8981")
# algod_address = "https://testnet-algorand.api.purestake.io/ps2"
# algod_token = ""
# headers = {
#    "X-API-Key": "B3SU4KcVKi94Jap2VXkK83xx38bsv95K5UZm2lab",
# }
# myindexer = indexer.IndexerClient(
#     algod_token, algod_address, headers)

# response = myindexer.account_info(
#     address="7DCJZKC4JDUKM25W7TDJ5XRTWGUTH6DOG5WARVA47DOCXQOTB4GMLNVW7I")
response = myindexer.account_info(
    address="IE4C3BNWT4EYKPUZXGWDOOKBTJFVOYAZKBCWFYRC37U7BJKBIUH6NEB7SQ")
# IE4C3BNWT4EYKPUZXGWDOOKBTJFVOYAZKBCWFYRC37U7BJKBIUH6NEB7SQ
# ZV2CISJONFBUIUIYFKQJ2UISXPDESCJWH6WO6DKRMXUJB7HDBSABOTYFKU
# 7DCJZKC4JDUKM25W7TDJ5XRTWGUTH6DOG5WARVA47DOCXQOTB4GMLNVW7I
# https://betanet-algorand.api.purestake.io/idx2
# response = myindexer.account_info(
#     address="6SKIRCMLFSSY3EJUC6QGFM3TFIJH72ZYUHX7GCUBDBUBYCAHJBJ5PWB344")

print("Account Info: " + json.dumps(response, indent=2, sort_keys=True))
