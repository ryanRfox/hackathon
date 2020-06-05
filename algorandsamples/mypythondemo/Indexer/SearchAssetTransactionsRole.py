import json
# requires Python SDK version 1.3 or higher
from algosdk.v2client import indexer

data = {
    "indexer_token": "",
    "indexer_address": "http://localhost:8980"
}

# instantiate indexer client
myindexer = indexer.IndexerClient(**data)
# gets accounts with a min balance of 100 that have a particular AssetID
data = {
    "asset_id": "2044572",
    "address_role": "receiver",
    "address": "UF7ATOM6PBLWMQMPUQ5QLA5DZ5E35PXQ2IENWGZQLEJJAAPAPGEGC3ZYNI"
}
response = myindexer.search_asset_transactions(**data)
print(json.dumps(response, indent=2, sort_keys=True))
