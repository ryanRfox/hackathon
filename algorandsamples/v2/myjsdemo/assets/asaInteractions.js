const algosdk = require('algosdk');
const crypto = require('crypto');

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

async function createAsset(algodClient, alice) {
    //Check account balance    
    let accountInfo = await algodClient.accountInformation(alice.addr).do();
    let startingAmount = accountInfo.amount;
    console.log("Alice account balance: %d microAlgos", startingAmount);

    // Construct the transaction
    let params = await algodClient.getTransactionParams().do();
    // comment out the next two lines to use suggested fee
    // params.fee = 1000;
    // params.flatFee = true;
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
    let addr = alice.addr;
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
    // see ASA metadata conventions here: https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0003.md
    let metadataJSON = {
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
    let metadataJSONString = JSON.stringify(metadataJSON);
    let hash = crypto.createHash('sha256');
    hash.update(new Buffer.from(metadataJSONString), 'utf8');
    let assetMetadataHash = hash.digest('hex');
    // console.log("assetMetadataHash : " + assetMetadataHash.toString());
    const metadata = new Uint8Array(
        Buffer.from(
            assetMetadataHash,
            'hex'
        )
    ); // should be a 32-byte hash

    // The following parameters are the only ones
    // that can be changed, and they have to be changed
    // by the current manager
    // Specified address can change reserve, freeze, clawback, and manager
    // If they are set to undefined at creation time, you will not be able to modify these later
    let manager = alice.addr;
    // Specified address is considered the asset reserve
    // (it has no special privileges, this is only informational)
    let reserve = alice.addr;
    // Specified address can freeze or unfreeze user asset holdings 
    let freeze = alice.addr;
    // Specified address can revoke user asset holdings and send 
    // them to other addresses    
    let clawback = alice.addr;

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
    let totalIssuance = 999;

    // signing and sending "txn" allows "addr" to create an asset
    let txn = algosdk.makeAssetCreateTxnWithSuggestedParams(addr, note,
        totalIssuance, decimals, defaultFrozen, manager, reserve, freeze,
        clawback, unitName, assetName, assetURL, metadata, params);
    let rawSignedTxn = txn.signTxn(alice.sk);
    let tx = (await algodClient.sendRawTransaction(rawSignedTxn).do());
    let assetID = null;
    // wait for transaction to be confirmed
    let confirmedTxn = await waitForConfirmation(algodClient, tx.txId, 4);
    //Get the completed Transaction
    console.log("Transaction " + tx.txId + " confirmed in round " + confirmedTxn["confirmed-round"]);
    let ptx = await algodClient.pendingTransactionInformation(tx.txId).do();
    assetID = ptx["asset-index"];
    // console.log("AssetID = " + assetID);
    await printCreatedAsset(algodClient, alice.addr, assetID);
    await printAssetHolding(algodClient, alice.addr, assetID);
    return { assetID };
}


async function gettingStartedASAInteractions() {

    try {
        // CREATE ACCOUNTS
        let alice = createAccount();
        console.log("Account created. Save off Mnemonic and address");
        console.log("Add funds to account using the TestNet Dispenser: ");
        console.log("https://dispenser.testnet.aws.algodev.network/?account=" + alice.addr);
        console.log("Press any key when the account is funded");
        await keypress();
        let bob = createAccount();

        // Connect your client
        // const algodToken = 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa';
        // const algodServer = 'http://localhost';
        // const algodPort = 4001;
        const algodToken = '2f3203f21e738a1de6110eba6984f9d03e5a95d7a577b34616854064cf2c0e7b';
        const algodServer = 'https://academy-algod.dev.aws.algodev.network';
        const algodPort = 443;

        let algodClient = new algosdk.Algodv2(algodToken, algodServer, algodPort);

        // ---------------------        
        // CREATE ASSET
        // ---------------------
        let { assetID } = await createAsset(algodClient, alice);

        // ---------------------
        // TRANSFER ALGOS TO BOB 
        // ---------------------
        await transferAlgosToBob(algodClient, bob, alice);

        // ---------------------
        // BOB OPTS IN 
        // ---------------------
        await optIn(algodClient, bob, assetID);

        // ----------------------------------
        // TRANSFER ASSETS FROM ALICE TO BOB
        // ----------------------------------
        await transferAssets(algodClient, alice, bob, assetID);

        // --------------------------------------
        // TRANSFER ASSETS BACK TO ALICE FROM BOB
        // --------------------------------------
        await transferAssetsBack(algodClient, bob, alice, assetID);

        // -------------
        // DESTROY ASSET
        // -------------
        await destroyAsset(algodClient, alice, assetID, bob);

        // --------------------------------------------------
        // CLOSEOUT ALGOS - Bob closes out Alogs to dispenser
        // --------------------------------------------------
        await closeoutBobAlgos(algodClient, bob, alice);

        // ---------------------
        // CLOSEOUT ALGOS - Alice closes out Alogs to dispenser
        // ---------------------
        await closeoutAliceAlgos(algodClient, alice);
    }
    catch (err) {
        console.log("err", err);
    }
    process.exit();


};

// todo
// read in metadata file

gettingStartedASAInteractions();


async function closeoutAliceAlgos(algodClient, alice ) {
    let accountInfo = await algodClient.accountInformation(alice.addr).do();
    console.log("Alice Account balance: %d microAlgos", accountInfo.amount);
    let startingAmount = accountInfo.amount;
    // Construct the transaction
    let params = await algodClient.getTransactionParams().do();
    // comment out the next two lines to use suggested fee
    // params.fee = 1000;
    // params.flatFee = true;
    // For more info see: 
    // https://developer.algorand.org/docs/reference/transactions/#payment-transaction
    // receiver account to send to
    let receiver = "HZ57J3K46JIJXILONBBZOHX6BKPXEM2VVXNRFSUED6DKFD5ZD24PMJ3MVA";
    let enc = new TextEncoder();
    let amount = 0;
    let sender = alice.addr;
    // closeToRemainder will remove the assetholding from the account
    let closeRemainderTo = "HZ57J3K46JIJXILONBBZOHX6BKPXEM2VVXNRFSUED6DKFD5ZD24PMJ3MVA";
    let txn = algosdk.makePaymentTxnWithSuggestedParams(sender, receiver,
        amount, closeRemainderTo, undefined, params);
    // Sign the transaction
    let rawSignedTxn = txn.signTxn(alice.sk);
    console.log("Signed transaction with txID: %s", txn.txId);
    // Submit the transaction
    let tx = (await algodClient.sendRawTransaction(rawSignedTxn).do());

    // Wait for confirmation
    let confirmedTxn = await waitForConfirmation(algodClient, tx.txId, 4);
    //Get the completed Transaction
    console.log("Transaction " + tx.txId + " confirmed in round " + confirmedTxn["confirmed-round"]);
    // let mytxinfo = JSON.stringify(confirmedTxn.txn.txn, undefined, 2);
    // console.log("Transaction information: %o", mytxinfo);
    accountInfo = await algodClient.accountInformation(alice.addr).do();
    if (confirmedTxn.txn.txn.amt == undefined) {
        console.log("Transaction Amount: %d microAlgos", 0);
    }
    else {
        console.log("Transaction Amount: %d microAlgos", confirmedTxn.txn.txn.amt);

    }
    console.log("Transaction Fee: %d microAlgos", confirmedTxn.txn.txn.fee);
    let closeoutamt = startingAmount - confirmedTxn.txn.txn.amt - confirmedTxn.txn.txn.fee;
    // console.log("Close To Amount: %d microAlgos", closeoutamt);
    console.log("Bobs Account balance: %d microAlgos", accountInfo.amount);
    return;
}

async function closeoutBobAlgos(algodClient, bob, alice) {
    let accountInfo = await algodClient.accountInformation(bob.addr).do();
    console.log("Bob Account balance: %d microAlgos", accountInfo.amount);
    let startingAmount = accountInfo.amount;
    // Construct the transaction
    let params = await algodClient.getTransactionParams().do();
    // comment out the next two lines to use suggested fee
    // params.fee = 1000;
    // params.flatFee = true;
    let receiver = alice.addr;
    let amount = 0;
    let sender = bob.addr;
    // add remaining funds go back to dispenser
    // closeRemainderTo will remove the assetholding from the account
    let closeRemainderTo = alice.addr;
    let txn = algosdk.makePaymentTxnWithSuggestedParams(sender, receiver,
        amount, closeRemainderTo, undefined, params);
    // Sign the transaction
    let rawSignedTxn = txn.signTxn(bob.sk);
    // Submit the transaction
    let tx = (await algodClient.sendRawTransaction(rawSignedTxn).do());
    // Wait for confirmation
    let confirmedTxn = await waitForConfirmation(algodClient, tx.txId, 4);
    //Get the completed Transaction
    console.log("Transaction " + tx.txId + " confirmed in round " + confirmedTxn["confirmed-round"]);
    // let mytxinfo = JSON.stringify(confirmedTxn.txn.txn, undefined, 2);
    // console.log("Transaction information: %o", mytxinfo);
    accountInfo = await algodClient.accountInformation(bob.addr).do();
    if (confirmedTxn.txn.txn.amt == undefined) {
        console.log("Transaction Amount: %d microAlgos", 0);
    }
    else {
        console.log("Transaction Amount: %d microAlgos", confirmedTxn.txn.txn.amt);

    }
    console.log("Transaction Fee: %d microAlgos", confirmedTxn.txn.txn.fee);
    closeoutamt = startingAmount - confirmedTxn.txn.txn.amt - confirmedTxn.txn.txn.fee;
    // console.log("Close To Amount: %d microAlgos", closeoutamt);
    console.log("Bobs Account balance: %d microAlgos", accountInfo.amount);
    return;

}

// Destroy Asset:
async function destroyAsset(algodClient, alice, assetID, bob) {
    // All of the created assets should now be back in the creators
    // Account so we can delete the asset.
    // If this is not the case the asset deletion will fail
    let params = await algodClient.getTransactionParams().do();
    // Comment out the next two lines to use suggested fee
    // params.fee = 1000;
    // params.flatFee = true;
    // The address for the from field must be the manager account
    let addr = alice.addr;
    // if all assets are held by the asset creator,
    // the asset creator can sign and issue "txn" to remove the asset from the ledger. 
    let txn = algosdk.makeAssetDestroyTxnWithSuggestedParams(addr, undefined, assetID, params);
    // The transaction must be signed by the manager which 
    // is currently set to alice
    let rawSignedTxn = txn.signTxn(alice.sk);
    let tx = (await algodClient.sendRawTransaction(rawSignedTxn).do());
    // Wait for confirmation
    let confirmedTxn = await waitForConfirmation(algodClient, tx.txId, 4);
    //Get the completed Transaction
    console.log("Transaction " + tx.txId + " confirmed in round " + confirmedTxn["confirmed-round"]);
    // The account3 and account1 should no longer contain the asset as it has been destroyed
    console.log("Asset ID: " + assetID);
    console.log("Alice = " + alice.addr);
    await printCreatedAsset(algodClient, alice.addr, assetID);
    await printAssetHolding(algodClient, alice.addr, assetID);
    console.log("Bob = " + bob.addr);
    await printAssetHolding(algodClient, bob.addr, assetID);
    return;
    // Notice that although the asset was destroyed, the asset id and associated 
    // metadata still exists in account holdings for any account that optin. 
    // When you destroy an asset, the global parameters associated with that asset
    // (manager addresses, name, etc.) are deleted from the creator's account.
    // However, holdings are not deleted automatically -- users still need to 
    // use the closeToAccount on the call makePaymentTxnWithSuggestedParams of the deleted asset.
    // This is necessary for technical reasons because we currently can't have a single transaction touch potentially 
    // thousands of accounts (all the holdings that would need to be deleted).
}

async function transferAssetsBack(algodClient, bob, alice, assetID) {
    let params = await algodClient.getTransactionParams().do();
    //comment out the next two lines to use suggested fee
    // params.fee = 1000;
    // params.flatFee = true;
    let sender = bob.addr;
    let recipient = alice.addr;
    let revocationTarget = undefined;
    let closeRemainderTo = alice.addr;
    //Amount of the asset to transfer
    let amount = 100;
    let note = undefined; // arbitrary data to be stored in the transaction; here, none is stored

    // signing and sending "txn" will send "amount" assets from "sender" to "recipient"
    let xtxn = algosdk.makeAssetTransferTxnWithSuggestedParams(sender, recipient, closeRemainderTo, revocationTarget,
        amount, note, assetID, params);
    // Must be signed by the account sending the asset  
    let rawSignedTxn = xtxn.signTxn(bob.sk);
    let tx = (await algodClient.sendRawTransaction(rawSignedTxn).do());

    // Wait for confirmation
    let confirmedTxn = await waitForConfirmation(algodClient, tx.txId, 4);
    //Get the completed Transaction
    console.log("Transaction " + tx.txId + " confirmed in round " + confirmedTxn["confirmed-round"]);
    // You should see 999 assets in Alice' account
    console.log("Alice Account = " + alice.addr);
    await printAssetHolding(algodClient, alice.addr, assetID);
    // You should now see the 0 assets listed in Bob's account information
    console.log("Bob Account = " + bob.addr);
    await printAssetHolding(algodClient, bob.addr, assetID);
    return;
}

async function transferAssets(algodClient, alice, bob, assetID) {

    // Now that Bob can recieve the new tokens 
    // we can tranfer tokens in from the creator Alice to Bob
    // First update changing transaction parameters
    let params = await algodClient.getTransactionParams().do();
    // comment out the next two lines to use suggested fee
    // params.fee = 1000;
    // params.flatFee = true;
    let sender = alice.addr;
    let recipient = bob.addr;
    let revocationTarget = undefined;
    let closeRemainderTo = undefined;
    //Amount of the asset to transfer
    let amount = 100;
    let note = undefined; // arbitrary data to be stored in the transaction; here, none is stored
    // signing and sending "txn" will send "amount" assets from "sender" to "recipient"
    let xtxn = algosdk.makeAssetTransferTxnWithSuggestedParams(sender, recipient, closeRemainderTo, revocationTarget,
        amount, note, assetID, params);
    // Must be signed by the account sending the asset account
    let rawSignedTxn = xtxn.signTxn(alice.sk);
    let tx = (await algodClient.sendRawTransaction(rawSignedTxn).do());
    // wait for transaction to be confirmed
    let confirmedTxn = await waitForConfirmation(algodClient, tx.txId, 4);
    // Get the completed Transaction
    console.log("Transaction " + tx.txId + " confirmed in round " + confirmedTxn["confirmed-round"]);
    // You should see 899 assets in Alice' account
    console.log("Alice Account = " + alice.addr);
    await printAssetHolding(algodClient, alice.addr, assetID);
    // You should now see the 100 assets listed in Bob's account information
    console.log("Bob Account = " + bob.addr);
    await printAssetHolding(algodClient, bob.addr, assetID);
    return;
}
// OPT-IN
async function optIn(algodClient, bob, assetID) {

        // Opting in to transact with the new asset
        // Allow accounts that want recieve the new asset
        // Have to opt in. To do this they send an asset transfer
        // of the new asset to themseleves 

        // First update changing transaction parameters
        // We will account for changing transaction parameters
        // before every transaction in this example
    let params = await algodClient.getTransactionParams().do();
    //comment out the next two lines to use suggested fee
    params.fee = 1000;
    params.flatFee = true;

    let sender = bob.addr;
    let recipient = bob.addr;
    // We set revocationTarget to undefined as 
    // This is not a clawback operation
    let revocationTarget = undefined;
    // CloseReaminerTo is set to undefined as
    // we are not closing out an asset
    let closeRemainderTo = undefined;
    // We are sending 0 assets to opt in
    let amount = 0;
    let note = undefined; // arbitrary data to be stored in the transaction; here, none is stored
    // signing and sending "txn" allows sender to begin accepting asset specified by creator and index
    let opttxn = algosdk.makeAssetTransferTxnWithSuggestedParams(sender, recipient, closeRemainderTo, revocationTarget,
        amount, note, assetID, params);
    // Must be signed by the account wishing to opt in to the asset    
    let rawSignedTxn = opttxn.signTxn(bob.sk);
    let tx = (await algodClient.sendRawTransaction(rawSignedTxn).do());
    // wait for transaction to be confirmed
    let confirmedTxn = await waitForConfirmation(algodClient, tx.txId, 4);
    //Get the completed Transaction
    console.log("Transaction " + tx.txId + " confirmed in round " + confirmedTxn["confirmed-round"]);
    //You should now see the new asset listed in the account information
    console.log("Bob's Account Opts In = " + bob.addr);
    await printAssetHolding(algodClient, bob.addr, assetID);
    return;
}

async function transferAlgosToBob(algodClient, bob, alice) {
    let accountInfo = await algodClient.accountInformation(bob.addr).do();
    console.log("Bob Account balance: %d microAlgos", accountInfo.amount);
    let startingAmount = accountInfo.amount;
    // Construct the transaction
    let params = await algodClient.getTransactionParams().do();
    // comment out the next two lines to use suggested fee
    // params.fee = 1000;
    // params.flatFee = true;
    // For more info see: 
    // https://developer.algorand.org/docs/reference/transactions/#payment-transaction
    // receiver account to send to
    let receiver = bob.addr;
    let enc = new TextEncoder();
    let note = enc.encode("Hello World");
    let amount = 1000000;
    let sender = alice.addr;
    let txn = algosdk.makePaymentTxnWithSuggestedParams(sender, receiver,
        amount, undefined, note, params);

    // Sign the transaction
    let rawSignedTxn = txn.signTxn(alice.sk);

    // Submit the transaction
    let tx = (await algodClient.sendRawTransaction(rawSignedTxn).do());
    // Wait for confirmation
    let confirmedTxn = await waitForConfirmation(algodClient, tx.txId, 4);
    //Get the completed Transaction
    console.log("Transaction " + tx.txId + " confirmed in round " + confirmedTxn["confirmed-round"]);
    // let mytxinfo = JSON.stringify(confirmedTxn.txn.txn, undefined, 2);
    // console.log("Transaction information: %o", mytxinfo);
    var string = new TextDecoder().decode(confirmedTxn.txn.txn.note);
    console.log("Note field: ", string);
    accountInfo = await algodClient.accountInformation(bob.addr).do();
    console.log("Transaction Amount: %d microAlgos", confirmedTxn.txn.txn.amt);
    console.log("Transaction Fee: %d microAlgos", confirmedTxn.txn.txn.fee);
    let closeoutamt = startingAmount - confirmedTxn.txn.txn.amt - confirmedTxn.txn.txn.fee;
    // console.log("Close To Amount: %d microAlgos", closeoutamt);
    console.log("Bobs Account balance: %d microAlgos", accountInfo.amount);
    return;
}


