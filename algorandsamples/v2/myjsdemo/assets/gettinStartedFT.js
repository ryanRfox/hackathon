const algosdk = require('algosdk');
const crypto = require('crypto');
// const CryptoJS = require('crypto-js');
// see ASA param conventions here: https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0003.md
const keypress = async () => {
    process.stdin.setRawMode(true)
    return new Promise(resolve => process.stdin.once('data', () => {
        process.stdin.setRawMode(false)
        resolve()
    }))
}
// createAccount
// once created sucessfully, you will need to add funds 
// The Algorand TestNet Dispenser is located here: 
// https://dispenser.testnet.aws.algodev.network/


const createAccount = function () {
    try {
        // let account1_mnemonic = "goat march toilet hope fan federal around nut drip island tooth mango table deal diesel reform lecture warrior tent volcano able wheel marriage absorb minimum";
        // const myaccount = algosdk.mnemonicToSecretKey(account1_mnemonic);
        const myaccount = algosdk.generateAccount();
        console.log("Account Address = " + myaccount.addr);
        let account_mnemonic = algosdk.secretKeyToMnemonic(myaccount.sk);
        console.log("Account Mnemonic = " + account_mnemonic);
        console.log("Account created. Save off Mnemonic and address");
        console.log("Add funds to account using the TestNet Dispenser: ");
        console.log("https://dispenser.testnet.aws.algodev.network/?account=" + myaccount.addr);

        return myaccount;
    }
    catch (err) {
        console.log("err", err);
    }
};

/**
 * Wait until the transaction is confirmed or rejected, or until 'timeout'
 * number of rounds have passed.
 * @param {algosdk.Algodv2} algodClient the Algod V2 client
 * @param {string} txId the transaction ID to wait for
 * @param {number} timeout maximum number of rounds to wait
 * @return {Promise<*>} pending transaction information
 * @throws Throws an error if the transaction is not confirmed or rejected in the next timeout rounds
 */
const waitForConfirmation = async function (algodClient, txId, timeout) {
    if (algodClient == null || txId == null || timeout < 0) {
        throw new Error("Bad arguments");
    }

    const status = (await algodClient.status().do());
    if (status === undefined) {
        throw new Error("Unable to get node status");
    }

    const startround = status["last-round"] + 1;
    let currentround = startround;

    while (currentround < (startround + timeout)) {
        const pendingInfo = await algodClient.pendingTransactionInformation(txId).do();
        if (pendingInfo !== undefined) {
            if (pendingInfo["confirmed-round"] !== null && pendingInfo["confirmed-round"] > 0) {
                //Got the completed Transaction
                return pendingInfo;
            } else {
                if (pendingInfo["pool-error"] != null && pendingInfo["pool-error"].length > 0) {
                    // If there was a pool error, then the transaction has been rejected!
                    throw new Error("Transaction " + txId + " rejected - pool error: " + pendingInfo["pool-error"]);
                }
            }
        }
        await algodClient.statusAfterBlock(currentround).do();
        currentround++;
    }
    throw new Error("Transaction " + txId + " not confirmed after " + timeout + " rounds!");
};
// Function used to print created asset for account and assetid
const printCreatedAsset = async function (algodClient, account, assetid) {
    // note: if you have an indexer instance available it is easier to just use this
    //     let accountInfo = await indexerClient.searchAccounts()
    //    .assetID(assetIndex).do();
    // and in the loop below use this to extract the asset for a particular account
    // accountInfo['accounts'][idx][account]);
    let accountInfo = await algodClient.accountInformation(account).do();
    for (idx = 0; idx < accountInfo['created-assets'].length; idx++) {
        let scrutinizedAsset = accountInfo['created-assets'][idx];
        if (scrutinizedAsset['index'] == assetid) {
            console.log("AssetID = " + scrutinizedAsset['index']);
            let myparms = JSON.stringify(scrutinizedAsset['params'], undefined, 2);
            console.log("parms = " + myparms);
            break;
        }
    }
};
// Function used to print asset holding for account and assetid
const printAssetHolding = async function (algodClient, account, assetid) {
    // note: if you have an indexer instance available it is easier to just use this
    //     let accountInfo = await indexerClient.searchAccounts()
    //    .assetID(assetIndex).do();
    // and in the loop below use this to extract the asset for a particular account
    // accountInfo['accounts'][idx][account]);
    let accountInfo = await algodClient.accountInformation(account).do();
    for (idx = 0; idx < accountInfo['assets'].length; idx++) {
        let scrutinizedAsset = accountInfo['assets'][idx];
        if (scrutinizedAsset['asset-id'] == assetid) {
            let myassetholding = JSON.stringify(scrutinizedAsset, undefined, 2);
            console.log("assetholdinginfo = " + myassetholding);
            break;
        }
    }
};
// async function sha256(message) {
//     // encode as UTF-8
//     const msgBuffer = new TextEncoder().encode(message);
//     // hash the message
//     const hashBuffer = await crypto.sha256('SHA-256', msgBuffer);
//     // convert ArrayBuffer to Array
//     const hashArray = Array.from(new Uint8Array(hashBuffer));
//     // convert bytes to hex string                  
//     const hashHex = hashArray.map(b => ('00' + b.toString(16)).slice(-2)).join('');
//     return hashHex;
// }

async function firstFT() {

    try {
        let myAccount = createAccount();
        console.log("Press any key when the account is funded");
        await keypress();
        // Connect your client
        // const algodToken = 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa';
        // const algodServer = 'http://localhost';
        // const algodPort = 4001;
        const algodToken = '2f3203f21e738a1de6110eba6984f9d03e5a95d7a577b34616854064cf2c0e7b';
        const algodServer = 'https://academy-algod.dev.aws.algodev.network';
        const algodPort = 443;

        let algodClient = new algosdk.Algodv2(algodToken, algodServer, algodPort);

        //Check your balance
        let accountInfo = await algodClient.accountInformation(myAccount.addr).do();
        console.log("Account balance: %d microAlgos", accountInfo.amount);
        let startingAmount = accountInfo.amount;
        // Construct the transaction
        let params = await algodClient.getTransactionParams().do();
        // comment out the next two lines to use suggested fee
        params.fee = 1000;
        params.flatFee = true;

        // let closeout = receiver; //closeRemainderTo
        // WARNING! all remaining funds in the sender account above will be sent to the closeRemainderTo Account 
        // In order to keep all remaning funds in the sender account after tx, set closeout parameter to undefined.
        // For more info see: 
        // https://developer.algorand.org/docs/reference/transactions/#payment-transaction

        let note = undefined; // arbitrary data to be stored in the transaction; here, none is stored
        // Asset creation specific parameters
        // The following parameters are asset specific
        // Throughout the example these will be re-used. 
        // We will also change the manager later in the example
        let addr = myAccount.addr;
        // Whether user accounts will need to be unfrozen before transacting    
        let defaultFrozen = false;

        // Used to display asset units to user    
        let unitName = "ALICECOI";
        // Friendly name of the asset    
        let assetName = "Alice's Artwork Coins";
        // Optional string pointing to a URL relating to the asset
        let assetURL = "http://someurl";
        // Optional hash commitment of some sort relating to the asset. 32 character length.
        // metadata can define the unitName and assetName as well. 
        var metadataJSON = {
            "name": "ALICECOI",
            "description": "Alice's Artwork Coins",
            "image": "https:\/\/s3.amazonaws.com\/your-bucket\/images\/MyPicture.png",
            "image_integrity": "sha256-47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=",
            "properties": {
                "simple_property": "Alice's first artwork",
                "rich_property": {
                    "name": "AliceCoi",
                    "value": "001",
                    "display_value": "001",
                    "class": "emphasis",
                    "css": {
                        "color": "#ffffff",
                        "font-weight": "bold",
                        "text-decoration": "underline"
                    }
                },
                "array_property": {
                    "name": "Artwork Coins",
                    "value": [1, 2, 3, 4],
                    "class": "emphasis"
                }
            }
        };
        var metadataJSONString = JSON.stringify(metadataJSON);
        // let assetMetadataHash = new Uint8Array(sha256(metadataJSONString));
        let assetMetadataHash = new Uint8Array(crypto.createHmac('sha256', "key").update(metadataJSONString).digest('hex'));
        console.log("assetMetadataHash : " + assetMetadataHash.toString());
        // var hash = CryptoJS.SHA256(metadataJSON);
        // const secret = 'abcdefg';
        // const hash = crypto('sha256', secret)
        //                .update('I love cupcakes')
        //                .digest('hex');
        // console.log(hash);


        // let hash   = CryptoJS.SHA256('hello world');
        // let buffer = Buffer.from(hash.toString(CryptoJS.enc.Hex), 'hex');
        // let array  = new Uint8Array(buffer);

        //   let assetMetadataHash = "16efaa3924a6fd9d3a4824799a4ac65d";
        // The following parameters are the only ones
        // that can be changed, and they have to be changed
        // by the current manager
        // Specified address can change reserve, freeze, clawback, and manager
        let manager = myAccount.addr;
        // Specified address is considered the asset reserve
        // (it has no special privileges, this is only informational)
        let reserve = myAccount.addr;
        // Specified address can freeze or unfreeze user asset holdings 
        let freeze = myAccount.addr;
        // Specified address can revoke user asset holdings and send 
        // them to other addresses    
        let clawback = myAccount.addr;

        // Use actual total  > 1 to create a Fungible Token

        // example 1:(fungible Tokens)
        // totalIssuance = 10, decimals = 0, result is 10 total actual 

        // example 2: (fractional NFT, each is 0.1)
        // totalIssuance = 10, decimals = 1, result is 1.0 total actual

        // example 3: (NFT)
        // totalIssuance = 1, decimals = 0, result is 1 total actual 

        // integer number of decimals for asset unit calculation
        let decimals = 0;
        // total number of this asset available for circulation   
        let totalIssuance = 1000000;

        // signing and sending "txn" allows "addr" to create an asset
        let txn = algosdk.makeAssetCreateTxnWithSuggestedParams(addr, note,
            totalIssuance, decimals, defaultFrozen, manager, reserve, freeze,
            clawback, unitName, assetName, assetURL, assetMetadataHash, params);

        let rawSignedTxn = txn.signTxn(myAccount.sk)
        let tx = (await algodClient.sendRawTransaction(rawSignedTxn).do());
        console.log("Transaction : " + tx.txId);
        let assetID = null;
        // wait for transaction to be confirmed
        let confirmedTxn = await waitForConfirmation(algodClient, tx.txId, 4);
        //Get the completed Transaction
        console.log("Transaction " + tx.txId + " confirmed in round " + confirmedTxn["confirmed-round"]);


        let ptx = await algodClient.pendingTransactionInformation(tx.txId).do();
        assetID = ptx["asset-index"];
        // console.log("AssetID = " + assetID);

        await printCreatedAsset(algodClient, myAccount.addr, assetID);
        await printAssetHolding(algodClient, myAccount.addr, assetID);

    }
    catch (err) {
        console.log("err", err);
    }
    process.exit();
};


firstFT();