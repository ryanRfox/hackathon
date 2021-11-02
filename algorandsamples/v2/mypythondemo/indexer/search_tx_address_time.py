# search_tx_address_time.py
import json
# requires Python SDK version 1.3 or higher
from algosdk.v2client import indexer

# instantiate indexer client
myindexer = indexer.IndexerClient(indexer_token="", indexer_address="http://localhost:8981")

# gets transactions for an account after a timestamp
response = myindexer.search_transactions_by_address(
    address="RBSTLLHK2NJDL3ZH66MKSEX3BE2OWQ43EUM7S7YRVBJ2PRDRCKBSDD3YD4", start_time="2020-08-31T02:35:47-05:00")

print("Transaction Start Time 2020-08-31T02:35:47-05:00 = " +
      json.dumps(response, indent=2, sort_keys=True))


