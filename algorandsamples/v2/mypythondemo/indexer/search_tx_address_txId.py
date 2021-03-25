# search_tx_address_txId.py
import json
# requires Python SDK version 1.3 or higher
from algosdk.v2client import indexer

# instantiate indexer client
myindexer = indexer.IndexerClient(indexer_token="", indexer_address="http://localhost:8981")
# response = myindexer.search_transactions_by_address(
#     address="XIU7HGGAJ3QOTATPDSIIHPFVKMICXKHMOR2FJKHTVLII4FAOA3CYZQDLG4",
#     txid="QZS3B2XBBS47S6X5CZGKKC2FC7HRP5VJ4UNS7LPGHP24DUECHAAA")

response = myindexer.search_transactions(
    txid="XXPBHAP2WFAMZLLNO7RSJM5DU7JO3QJTF2WF7FKC2663K3IWP74A")

print("txid: QZS3B2XBBS47S6X5CZGKKC2FC7HRP5VJ4UNS7LPGHP24DUECHAAA = " +
      json.dumps(response, indent=2, sort_keys=True))
