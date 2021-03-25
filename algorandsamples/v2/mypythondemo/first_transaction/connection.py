# Create a python file called connection.py and paste the following code. 
# To get the url and the token, sign up on purestake.com
from algosdk.v2client import algod
import os
url = "https://testnet-algorand.api.purestake.io/ps2"
algod_token = "WpYvadV1w53mSODr6Xrq77tw0ODcgHAx9iJBn5tb"


def connect():
    # Here I use a third party API service to connect to the algod client 
    # since I don't enough resources to run a full node. 
    # Algorand has provided more information on how you can connect
    # to the client even if you cannot run a node and still have
    # access to a lot of features just as if you're running a node. 
    # https://developer.algorand.org

    algod_url = url
    # algod_auth = os.environ.get('ALGODTOKEN')
    headers = {"X-API-Key": algod_token}
    try:
        return algod.AlgodClient(algod_token, algod_url, headers)
    except Exception as e:
        print(e)


algod_client = connect()
params = algod_client.suggested_params()

