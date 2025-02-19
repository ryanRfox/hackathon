from algosdk import *
from algosdk.v2client import algod
from algosdk.v2client.models import DryrunSource, DryrunRequest
from algosdk.future.transaction import *
from sandbox import get_accounts
import base64
import os

token = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
url = "http://localhost:4001"

client = algod.AlgodClient(token, url)

def demo():
    # Create acct
    addr, pk = get_accounts()[0]
    print("Using {}".format(addr))

    # Create app
    app_id = create_app(addr, pk)
    print("Created App with id: {}".format(app_id))

    sp = client.suggested_params()
    pooled_group = [
        get_app_call(addr, sp, app_id, []), 
    ]

    signed_group = [txn.sign(pk) for txn in pooled_group]


    write_dryrun(signed_group, addr)
    

    txid = client.send_transactions(signed_group)
    print("Sending grouped transaction: {}".format(txid))

    result = wait_for_confirmation(client, txid, 4)
    print("Result confirmed in round: {}".format(result['confirmed-round']))
    print("Logs: ")
    for log in result['logs']:
        print_log(log)


def write_dryrun(signed_txn, addr):
    path = os.path.dirname(os.path.abspath(__file__))
    # Read in approval teal source
    src = open(os.path.join(path,'approval.teal')).read()

    # Add source
    sources = [DryrunSource(field_name="approv", source=src)]

    # Create request
    drr = DryrunRequest(txns=signed_txn, sources=sources, accounts=[addr])

    # write drr
    file_path = os.path.join(path, "dryrun.msgp")
    data = encoding.msgpack_encode(drr)
    data = base64.b64encode(data.encode())
    with open(file_path, "wb") as f:
        f.write(data)

    print("Created Dryrun file at {} - goto chrome://inspect".format(file_path))

    print("""
      START debugging session
      either use from terminal in this folder or new terminal in same folder
      `tealdbg debug approval.teal --dryrun-req {}.dr`
    """.format(name))


def print_log(log):
    strlog = base64.b64decode(log).decode('UTF-8')
    print("\t{}".format(strlog))

def get_app_call(addr, sp, app_id, args):
    return ApplicationCallTxn(
            addr, sp, app_id, 
            OnComplete.NoOpOC, 
            app_args=args,
    )

def create_app(addr, pk):
    # Get suggested params from network 
    sp = client.suggested_params()

    path = os.path.dirname(os.path.abspath(__file__))

    # Read in approval teal source && compile
    approval = open(os.path.join(path, 'approval.teal')).read()
    app_result = client.compile(approval)
    app_bytes = base64.b64decode(app_result['result'])
    
    # Read in clear teal source && compile 
    clear = open(os.path.join(path, 'clear.teal')).read()
    clear_result = client.compile(clear)
    clear_bytes = base64.b64decode(clear_result['result'])

    # We dont need no stinkin storage
    schema = StateSchema(0, 0)

    # Create the transaction
    create_txn = ApplicationCreateTxn(addr, sp, 0, app_bytes, clear_bytes, schema, schema)

    # Sign it
    signed_txn = create_txn.sign(pk)

    # Ship it
    txid = client.send_transaction(signed_txn)
    
    # Wait for the result so we can return the app id
    result = wait_for_confirmation(client, txid, 4)

    return result['application-index']

if __name__ == "__main__":
    demo()
