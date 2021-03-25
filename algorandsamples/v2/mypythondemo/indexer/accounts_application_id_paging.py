import json
# requires Python SDK version 1.3 or higher
from algosdk.v2client import indexer

# instantiate indexer client

# myindexer_token = 'B3SU4KcVKi94Jap2VXkK83xx38bsv95K5UZm2lab'
# myindexer_address = 'https://testnet-algorand.api.purestake.io/idx2/'
# myindexer_header = {'X-Api-key': myindexer_token}

myindexer_address = 'http://localhost:8981'
indexer_token=""
myindexer = indexer.IndexerClient(
    indexer_token, myindexer_address)

# myindexer_address = "https://testnet-algorand.api.purestake.io/ps2"
# myindexer_token = ""
# headers = {
#    "X-API-Key": "B3SU4KcVKi94Jap2VXkK83xx38bsv95K5UZm2lab",
# }
# myindexer = indexer.IndexerClient(myindexer_token, myindexer_address, headers=headers)
# myindexer = indexer.IndexerClient(
#     indexer_token="", indexer_address=myindexer_address)

nexttoken = ""
num_accounts = 1
# loop using next_page to paginate until there are no more accounts
# in the response
# (max is 100 default
# unless limit is used for max 1000 per request on accounts)
while (num_accounts > 0):
    response = myindexer.accounts(
        application_id=12867764, limit=2, round_num=12227042, next_page=nexttoken)
    accounts = response['accounts']
    num_accounts = len(accounts)
    if (num_accounts > 0):
        nexttoken = response['next-token']
        # Pretty Printing JSON string
        print("Account Info for Application ID: " + json.dumps(response, indent=2, sort_keys=True))
