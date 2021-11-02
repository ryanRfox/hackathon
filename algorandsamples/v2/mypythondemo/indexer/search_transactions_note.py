# search_transactions_note.py
import base64
import json
# requires Python SDK version 1.3 or higher
from algosdk.v2client import indexer

# instantiate indexer client
# headers = {
#    "Authorization": "Bearer KegWFLYQnBNVeP4oHCX64dObBk8VemzYdNqsnAOIxYQ8aqJLQTYeVDQyZNnx1PZA"
# }
# myindexer = indexer.IndexerClient(indexer_token="", indexer_address="https://indexer-testnet-green.aws.algodev.network", headers=headers)

# algod_address = "https://testnet-algorand.api.purestake.io/idx2"
# algod_token = ""
# headers = {
#    "X-API-Key": "WpYvadV1w53mSODr6Xrq77tw0ODcgHAx9iJBn5tb",
# }
# myindexer = indexer.IndexerClient(
#     algod_token, algod_address, headers)
# myindexer = indexer.IndexerClient(indexer_token="", indexer_address="http://localhost:8981")
# myindexer = indexer.IndexerClient(indexer_token="YddOUGbAjHLr1uPZtZwHOvMDmXvR1Zvw1f3Roj2PT1ufenXbNyIxIz0IeznrLbDsF", indexer_address="https://indexer-testnet.internal.aws.algodev.network")

myindexer = indexer.IndexerClient(indexer_token="", indexer_address="https://testnet.algoexplorerapi.io/idx2/")

import base64

# note_prefix = '{"firstName":"JohnChr"'.encode()
note_prefix = 'JohnChr'.encode()
print(base64.b64encode('JohnChr'.encode()))
# print(base64.b64encode('{"firstName":"JohnChr"'.encode()))
# note_prefix = 'Hello Rus'.encode()
# 14,556,497
# response = myindexer.search_transactions(
#     note_prefix=note_prefix, min_round=14556000, address="GD64YIY3TWGDMCNPP553DZPPR6LDUSFQOIJVFDPPXWEG3FVOJCCDBBHU5A")
response = myindexer.search_transactions(
    note_prefix=note_prefix,min_round=14556000)


print("note_prefix = " +
      json.dumps(response, indent=2, sort_keys=True))


# print first note that matches
if (len(response["transactions"]) > 0):
    print("Decoded note: {}".format(base64.b64decode(
        response["transactions"][0]["note"]).decode()))
    # person_dict = json.loads(base64.b64decode(
    #     response["transactions"][0]["note"]).decode())
    # print("First Name = {}".format(person_dict['firstName']))
