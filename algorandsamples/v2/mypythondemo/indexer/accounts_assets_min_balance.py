# accounts_assets_min_balance.py
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

# gets accounts that have a particular AssetID with a min asset balance of 100
response = myindexer.accounts(
    asset_id=12270668, min_balance=100)
print("Account Info: " + json.dumps(response, indent=2, sort_keys=True))

