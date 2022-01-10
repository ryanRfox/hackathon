from algosdk import mnemonic
from algosdk.kmd import KMDClient

KMD_ADDRESS = "http://localhost:4002"
KMD_TOKEN = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"

KMD_WALLET_NAME = "unencrypted-default-wallet"
KMD_WALLET_PASSWORD = ""

def get_accounts(network):
    if (network == "Testnet"):
        # NAMP6INIE7SEVDLL7WXA6VEY4GICVHY3WPW46G3GRONZDOX4ORQMDODWRA
        passphrase = "inspire sniff vague avoid dice ladder midnight typical sock rate useful stomach elephant lounge inherit punch thrive vessel pool tiger panic lazy slush abstract picnic"
        private_key = mnemonic.to_private_key(passphrase)
        my_address = mnemonic.to_public_key(passphrase)
        print("My address: {}".format(my_address))
        return [(my_address, private_key)]
    else: 
        kmd = KMDClient(KMD_TOKEN, KMD_ADDRESS)
        wallets = kmd.list_wallets()

        walletID = None
        for wallet in wallets:
            if wallet["name"] == KMD_WALLET_NAME:
                walletID = wallet["id"]
                break

        if walletID is None:
            raise Exception("Wallet not found: {}".format(KMD_WALLET_NAME))

        walletHandle = kmd.init_wallet_handle(walletID, KMD_WALLET_PASSWORD)

        try:
            addresses = kmd.list_keys(walletHandle)
            privateKeys = [
                kmd.export_key(walletHandle, KMD_WALLET_PASSWORD, addr)
                for addr in addresses
            ]
            kmdAccounts = [(addresses[i], privateKeys[i]) for i in range(len(privateKeys))]
        finally:
            kmd.release_wallet_handle(walletHandle)

        return kmdAccounts
