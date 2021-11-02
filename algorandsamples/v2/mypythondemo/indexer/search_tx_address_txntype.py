# search_tx_address_txntype.py
import json
# requires Python SDK version 1.3 or higher
from algosdk.v2client import indexer

# instantiate indexer client
myindexer = indexer.IndexerClient(indexer_token="", indexer_address="http://localhost:8981")
response = myindexer.search_transactions_by_address(
    address="NI2EDLP2KZYH6XYLCEZSI5SSO2TFBYY3ZQ5YQENYAGJFGXN4AFHPTR3LXU",
    txn_type="acfg")

print("txn_type: acfg = " +
      json.dumps(response, indent=2, sort_keys=True))
