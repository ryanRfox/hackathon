# account_info_block.py
import json
# requires Python SDK version 1.3 or higher
from algosdk.v2client import indexer

# instantiate indexer client
# myindexer = indexer.IndexerClient(indexer_token="", indexer_address="http://localhost:8980")
algod_address = "https://testnet-algorand.api.purestake.io/ps2"
algod_token = ""
headers = {
   "X-API-Key": "B3SU4KcVKi94Jap2VXkK83xx38bsv95K5UZm2lab",
}
myindexer = indexer.IndexerClient(
    algod_token, algod_address, headers)

# myindexer = indexer.IndexerClient(
#     indexer_token = "", indexer_address = "http://localhost:8980")



# https://betanet-algorand.api.purestake.io/idx2
# response = myindexer.account_info(
#     address="7WENHRCKEAZHD37QMB5T7I2KWU7IZGMCC3EVAO7TQADV7V5APXOKUBILCI", block=6127822)
response = myindexer.account_info(
    address="NI2EDLP2KZYH6XYLCEZSI5SSO2TFBYY3ZQ5YQENYAGJFGXN4AFHPTR3LXU", block=5161645)
print("Account Info: " + json.dumps(response, indent=2, sort_keys=True))

