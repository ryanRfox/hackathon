const algosdk = require('algosdk');
const crypto = require('crypto');
const fs = require('fs');
// const fs = require('fs/promises');
// for JavaScript SDK doc see: https://algorand.github.io/js-algorand-sdk/
const keypress = async () => {
    process.stdin.setRawMode(true)
    return new Promise(resolve => process.stdin.once('data', () => {
        process.stdin.setRawMode(false)
        resolve()
    }))
}

// createAccount
// once created successfully, you will need to add funds 
// The Algorand TestNet Dispenser is located here: 
// https://dispenser.testnet.aws.algodev.network/

const DISPENSERACCOUNT = "HZ57J3K46JIJXILONBBZOHX6BKPXEM2VVXNRFSUED6DKFD5ZD24PMJ3MVA";
const createAccount = function () {
    try {

        // const account1_mnemonic = "goat march toilet hope fan federal around nut drip island tooth mango table deal diesel reform lecture warrior tent volcano able wheel marriage absorb minimum";
        // const myaccount = algosdk.mnemonicToSecretKey(account1_mnemonic);
        const myaccount = algosdk.generateAccount();
        console.log("Account Address = " + myaccount.addr);
        const account_mnemonic = algosdk.secretKeyToMnemonic(myaccount.sk);
        console.log("Account Mnemonic = " + account_mnemonic);
        return myaccount;
    }
    catch (err) {
        console.log("err", err);
    }
    // Sample Output after both calls 
    // ==> CREATE ALICE ACCOUNT
    // Account Address = RA6RAUNDQGHRWTCR5YRL2YJMIXTHWD5S3ZYHVBGSNA76AVBAYELSNRVKEI
    // Account Mnemonic = tiger panther garlic jaguar noise divorce tent catch tip gaze wood nurse bike crumble arch scissors absent crush major bracket street game reopen ability use
    // Account created. Save off Mnemonic and address
    // Add funds to account using the TestNet Dispenser: 
    // https://dispenser.testnet.aws.algodev.network/?account=RA6RAUNDQGHRWTCR5YRL2YJMIXTHWD5S3ZYHVBGSNA76AVBAYELSNRVKEI
    // Press any key when the account is funded

    // ==> CREATE BOB ACCOUNT
    // Account Address = YC3UYV4JLHD344OC3G7JK37DRVSE7X7U2NOZVWSQNVKNEGV4M3KFA7WZ44
    // Account Mnemonic = width air mammal spirit victory travel rifle skin confirm rack worth asthma city volcano permit bus clump knock human sauce easy fiscal mountain absent road

};

async function createAsset(algodClient, alice) {
    console.log("");
    console.log("==> CREATE ASSET");
    //Check account balance    
    const accountInfo = await algodClient.accountInformation(alice.addr).do();
    const startingAmount = accountInfo.amount;
    console.log("Alice account balance: %d microAlgos", startingAmount);

    // Construct the transaction
    const params = await algodClient.getTransactionParams().do();
    // comment out the next two lines to use suggested fee
    // params.fee = 1000;
    // params.flatFee = true;
    // const closeout = receiver; //closeRemainderTo
    // WARNING! all remaining funds in the sender account above will be sent to the closeRemainderTo Account 
    // In order to keep all remaining funds in the sender account after tx, set closeout parameter to undefined.
    // For more info see: 
    // https://developer.algorand.org/docs/reference/transactions/#payment-transaction
    // Asset creation specific parameters
    // The following parameters are asset specific
    // Throughout the example these will be re-used. 

    // Whether user accounts will need to be unfrozen before transacting    
    const defaultFrozen = false;
    // Used to display asset units to user    
    const unitName = "ALICECOI";
    // Friendly name of the asset    
    const assetName = "Alice's Artwork Coins@arc3";
    // Optional string pointing to a URL relating to the asset
    const url = "https://s3.amazonaws.com/your-bucket/yourdata.json";
    // Optional hash commitment of some sort relating to the asset. 32 character length.
    // metadata can define the unitName and assetName as well.
    // see ASA metadata conventions here: https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0003.md


    // const metadataJSON = {
    //     "name": "ALICECOI",
    //     "description": "Alice's Artwork Coins",
    //     "image": "https:\/\/s3.amazonaws.com\/your-bucket\/images\/MyPicture.png",
    //     "image_integrity": "sha256-47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=",
    //     "properties": {
    //         "simple_property": "Alice's first artwork",
    //         "rich_property": {
    //             "name": "AliceCoi",
    //             "value": "001",
    //             "display_value": "001",
    //             "class": "emphasis",
    //             "css": {
    //                 "color": "#ffffff",
    //                 "font-weight": "bold",
    //                 "text-decoration": "underline"
    //             }
    //         },
    //         "array_property": {
    //             "name": "Artwork Coins",
    //             "value": [1, 2, 3, 4],
    //             "class": "emphasis"
    //         }
    //     }
    // };

    // The following parameters are the only ones
    // that can be changed, and they have to be changed
    // by the current manager
    // Specified address can change reserve, freeze, clawback, and manager
    // If they are set to undefined at creation time, you will not be able to modify these later
    const managerAddr = alice.addr;
    // Specified address is considered the asset reserve
    // (it has no special privileges, this is only informational)
    const reserveAddr = alice.addr; 
    // Specified address can freeze or unfreeze user asset holdings   
    const freezeAddr = alice.addr;
    // Specified address can revoke user asset holdings and send 
    // them to other addresses    
    const clawbackAddr = alice.addr;
    
    // Use actual total  > 1 to create a Fungible Token
    // example 1:(fungible Tokens)
    // totalIssuance = 10, decimals = 0, result is 10 total actual 
    // example 2: (fractional NFT, each is 0.1)
    // totalIssuance = 10, decimals = 1, result is 1.0 total actual
    // example 3: (NFT)
    // totalIssuance = 1, decimals = 0, result is 1 total actual 
    // integer number of decimals for asset unit calculation
    const decimals = 0; 
    const total = 999; // how many of this asset there will be

    // temp fix for replit    
    //const metadata2 = "16efaa3924a6fd9d3a4824799a4ac65d";
    const fullPath =  __dirname + '/aliceAssetMetaData.json'; 
    const metadatafile = (await fs.readFileSync(fullPath)).toString();
    const hash = crypto.createHash('sha256');
    hash.update(metadatafile);
    const metadata = new Uint8Array(hash.digest());
    // signing and sending "txn" allows "addr" to create an asset 
    const txn = algosdk.makeAssetCreateTxnWithSuggestedParamsFromObject({
        from: alice.addr,
        total,
        decimals,
        assetName,
        unitName,
        assetURL: url,
        assetMetadataHash: metadata,
        defaultFrozen,
        freeze: freezeAddr,
        manager: managerAddr,
        clawback: clawbackAddr,
        reserve: reserveAddr,
        suggestedParams: params,});

    const rawSignedTxn = txn.signTxn(alice.sk);
    const tx = (await algodClient.sendRawTransaction(rawSignedTxn).do());
    let assetID = null;
    // wait for transaction to be confirmed
    const confirmedTxn = await waitForConfirmation(algodClient, tx.txId, 4);
    //Get the completed Transaction
    console.log("Transaction " + tx.txId + " confirmed in round " + confirmedTxn["confirmed-round"]);
    const ptx = await algodClient.pendingTransactionInformation(tx.txId).do();
    assetID = ptx["asset-index"];
    // console.log("AssetID = " + assetID);

    await printCreatedAsset(algodClient, alice.addr, assetID);
    await printAssetHolding(algodClient, alice.addr, assetID);
    console.log("You can verify the metadata-hash above in the asset creation details");
    console.log("Using terminal the Metadata hash should appear as identical to the output of");
    console.log("cat aliceAssetMetaData.json | openssl dgst -sha256 -binary | openssl base64 -A");
    console.log("That is: YV/SWW7Hjy/RknglGIN2ahGJ9QdCis3LJXwLVOi8RCo=");

    return { assetID };
 
    // Sample Output
    // ==> CREATE ASSET
    // Alice account balance: 10000000 microAlgos
    // Transaction DM2QAJQ34AHOIH2XPOXB3KDDMFYBTSDM6CGO6SCM6A6VJYF5AUZQ confirmed in round 16833515
    // AssetID = 28291127
    // parms = {
    //   "clawback": "RA6RAUNDQGHRWTCR5YRL2YJMIXTHWD5S3ZYHVBGSNA76AVBAYELSNRVKEI",
    //   "creator": "RA6RAUNDQGHRWTCR5YRL2YJMIXTHWD5S3ZYHVBGSNA76AVBAYELSNRVKEI",
    //   "decimals": 0,
    //   "default-frozen": false,
    //   "freeze": "RA6RAUNDQGHRWTCR5YRL2YJMIXTHWD5S3ZYHVBGSNA76AVBAYELSNRVKEI",
    //   "manager": "RA6RAUNDQGHRWTCR5YRL2YJMIXTHWD5S3ZYHVBGSNA76AVBAYELSNRVKEI",
    //   "metadata-hash": "WQ4GxK4WqdklhWD9zJMfYH+Wgk+rTnqJIdW08Y7eD1U=",
    //   "name": "Alice's Artwork Coins",
    //   "name-b64": "QWxpY2UncyBBcnR3b3JrIENvaW5z",
    //   "reserve": "RA6RAUNDQGHRWTCR5YRL2YJMIXTHWD5S3ZYHVBGSNA76AVBAYELSNRVKEI",
    //   "total": 999,
    //   "unit-name": "ALICECOI",
    //   "unit-name-b64": "QUxJQ0VDT0k=",
    //   "url": "http://someurl",
    //   "url-b64": "aHR0cDovL3NvbWV1cmw="
    // }
    // assetholdinginfo = {
    //   "amount": 999,
    //   "asset-id": 28291127,
    //   "creator": "RA6RAUNDQGHRWTCR5YRL2YJMIXTHWD5S3ZYHVBGSNA76AVBAYELSNRVKEI",
    //   "is-frozen": false
    // }
}
async function transferAlgosToBob(algodClient, bob, alice) {
    console.log("");
    console.log("==> TRANSFER ALGO TO BOB");
    let accountInfo = await algodClient.accountInformation(bob.addr).do();
    console.log("Bob Account balance: %d microAlgos", accountInfo.amount);
    // Construct the transaction
    const params = await algodClient.getTransactionParams().do();
    // comment out the next two lines to use suggested fee
    // params.fee = 1000;
    // params.flatFee = true;
    // For more info see: 
    // https://developer.algorand.org/docs/reference/transactions/#payment-transaction

    const receiver = bob.addr;
    const enc = new TextEncoder();
    const note = enc.encode("Hello World");
    // minimum balance 100000, plus 100000 for asset optin,
    // plus 3000 for 3 tx (optin, transfer, algo closeout) = 203000 microalgos
    const amount = 203000;
    const sender = alice.addr;
    const closeToReaminder = undefined;
    const txn = algosdk.makePaymentTxnWithSuggestedParamsFromObject({
        from: sender, 
        to: receiver,
        amount, 
        closeToReaminder, 
        note, 
        suggestedParams: params});

    // Sign the transaction
    const rawSignedTxn = txn.signTxn(alice.sk);

    // Submit the transaction
    const tx = (await algodClient.sendRawTransaction(rawSignedTxn).do());
    // Wait for confirmation
    const confirmedTxn = await waitForConfirmation(algodClient, tx.txId, 4);
    //Get the completed Transaction
    console.log("Transaction " + tx.txId + " confirmed in round " + confirmedTxn["confirmed-round"]);
    // const mytxinfo = JSON.stringify(confirmedTxn.txn.txn, undefined, 2);
    // console.log("Transaction information: %o", mytxinfo);
    var string = new TextDecoder().decode(confirmedTxn.txn.txn.note);
    console.log("Note field: ", string);
    accountInfo = await algodClient.accountInformation(bob.addr).do();
    console.log("Transaction Amount: %d microAlgos", confirmedTxn.txn.txn.amt);
    console.log("Transaction Fee: %d microAlgos", confirmedTxn.txn.txn.fee);
    console.log("Bobs Account balance: %d microAlgos", accountInfo.amount);
    return;
    // Sample Output
    // ==> TRANSFER ALGOS TO BOB
    // Bob Account balance: 0 microAlgos
    // Transaction PCGHEPTYBGKVXDNEZMFPU7XSNYOZFBWF3Q5VGXEWV75EORRTYXQA confirmed in round 16833519
    // Note field:  Hello World
    // Transaction Amount: 1000000 microAlgos
    // Transaction Fee: 1000 microAlgos
    // Bobs Account balance: 1000000 microAlgos
}
// OPT-IN
async function optIn(algodClient, bob, assetID) {
    console.log("");
    console.log("==> BOB OPTS IN");
    // Opting in to transact with the new asset
    // Allow accounts that want receive the new asset
    // Have to opt in. To do this they send an asset transfer
    // of the new asset to themselves  

    // First update changing transaction parameters
    // We will account for changing transaction parameters
    // before every transaction in this example
    const params = await algodClient.getTransactionParams().do();
    // comment out the next two lines to use suggested fee
    // params.fee = 1000;
    // params.flatFee = true;

    const sender = bob.addr;
    const recipient = bob.addr;
    // We set revocationTarget to undefined as 
    // This is not a clawback operation
    const revocationTarget = undefined;
    // CloseReaminerTo is set to undefined as
    // we are not closing out an asset
    const closeRemainderTo = undefined;
    // We are sending 0 assets to opt in
    const amount = 0;
    const note = undefined; // arbitrary data to be stored in the transaction; here, none is stored
    // signing and sending "txn" allows sender to begin accepting asset specified by creator and index
    const opttxn = algosdk.makeAssetTransferTxnWithSuggestedParamsFromObject({
        from: sender, 
        to: recipient, 
        closeRemainderTo, 
        revocationTarget,
        amount, 
        note, 
        assetIndex: assetID, 
        suggestedParams: params});
    // Must be signed by the account wishing to opt in to the asset    
    const rawSignedTxn = opttxn.signTxn(bob.sk);
    const tx = (await algodClient.sendRawTransaction(rawSignedTxn).do());
    // wait for transaction to be confirmed
    const confirmedTxn = await waitForConfirmation(algodClient, tx.txId, 4);
    //Get the completed Transaction
    console.log("Transaction " + tx.txId + " confirmed in round " + confirmedTxn["confirmed-round"]);
    //You should now see the new asset listed in the account information
    console.log("Bob's Account Opts In = " + bob.addr);
    await printAssetHolding(algodClient, bob.addr, assetID);
    return;

    // ==> BOB OPTS IN
    // Transaction WEL6ZMVV5M4K6GPYQWC2UIC25VP4P2GQJ4DOQWJEDWI47LMU4CFQ confirmed in round 16833526
    // Bob's Account Opts In = YC3UYV4JLHD344OC3G7JK37DRVSE7X7U2NOZVWSQNVKNEGV4M3KFA7WZ44
    // assetholdinginfo = {
    //   "amount": 0,
    //   "asset-id": 28291127,
    //   "creator": "RA6RAUNDQGHRWTCR5YRL2YJMIXTHWD5S3ZYHVBGSNA76AVBAYELSNRVKEI",
    //   "is-frozen": false
    // }
}
async function transferAssets(algodClient, alice, bob, assetID) {
    console.log("");
    console.log("==> TRANSFER ASSETS FROM ALICE TO BOB");
    // Now that Bob can receive the new tokens 
    // we can transfer tokens in from the creator Alice to Bob
    // First update changing transaction parameters
    const params = await algodClient.getTransactionParams().do();
    // comment out the next two lines to use suggested fee
    // params.fee = 1000;
    // params.flatFee = true;
    const sender = alice.addr;
    const recipient = bob.addr;
    const revocationTarget = undefined;
    const closeRemainderTo = undefined;
    //Amount of the asset to transfer
    const amount = 100;
    const note = undefined; // arbitrary data to be stored in the transaction; here, none is stored
    // signing and sending "txn" will send "amount" assets from "sender" to "recipient"
    const xtxn = algosdk.makeAssetTransferTxnWithSuggestedParamsFromObject({
        from: sender, 
        to: recipient, 
        closeRemainderTo, 
        revocationTarget,
        amount, 
        note, 
        assetIndex: assetID, 
        suggestedParams : params 
    });
    // Must be signed by the account sending the asset account
    const rawSignedTxn = xtxn.signTxn(alice.sk);
    const tx = (await algodClient.sendRawTransaction(rawSignedTxn).do());
    // wait for transaction to be confirmed
    const confirmedTxn = await waitForConfirmation(algodClient, tx.txId, 4);
    // Get the completed Transaction
    console.log("Transaction " + tx.txId + " confirmed in round " + confirmedTxn["confirmed-round"]);
    // You should see 899 assets in Alice' account
    console.log("Alice Account = " + alice.addr);
    await printAssetHolding(algodClient, alice.addr, assetID);
    // You should now see the 100 assets listed in Bob's account information
    console.log("Bob Account = " + bob.addr);
    await printAssetHolding(algodClient, bob.addr, assetID);
    return;
    // Sample Output
    // ==> TRANSFER ASSETS FROM ALICE TO BOB
    // Transaction U6V5FLQDM6C3X2SMUXFRWWLXDHFA6HBTVL42QMRBMQQMBU7NK7CA confirmed in round 16833529
    // Alice Account = RA6RAUNDQGHRWTCR5YRL2YJMIXTHWD5S3ZYHVBGSNA76AVBAYELSNRVKEI
    // assetholdinginfo = {
    //   "amount": 899,
    //   "asset-id": 28291127,
    //   "creator": "RA6RAUNDQGHRWTCR5YRL2YJMIXTHWD5S3ZYHVBGSNA76AVBAYELSNRVKEI",
    //   "is-frozen": false
    // }
    // Bob Account = YC3UYV4JLHD344OC3G7JK37DRVSE7X7U2NOZVWSQNVKNEGV4M3KFA7WZ44
    // assetholdinginfo = {
    //   "amount": 100,
    //   "asset-id": 28291127,
    //   "creator": "RA6RAUNDQGHRWTCR5YRL2YJMIXTHWD5S3ZYHVBGSNA76AVBAYELSNRVKEI",
    //   "is-frozen": false
    // }
}
async function transferAssetsBack(algodClient, bob, alice, assetID) {
    console.log("");
    console.log("==> TRANSFER ASSETS BACK FROM BOB TO ALICE");
    const params = await algodClient.getTransactionParams().do();
    //comment out the next two lines to use suggested fee
    // params.fee = 1000;
    // params.flatFee = true;
    const sender = bob.addr;
    const recipient = alice.addr;
    const revocationTarget = undefined;
    const closeRemainderTo = alice.addr;
    //Amount of the asset to transfer
    const amount = 100;
    const note = undefined; // arbitrary data to be stored in the transaction; here, none is stored

    // signing and sending "txn" will send "amount" assets from "sender" to "recipient"
    const xtxn = algosdk.makeAssetTransferTxnWithSuggestedParamsFromObject({
        from: sender, 
        to: recipient, 
        closeRemainderTo, 
        revocationTarget,
        amount, 
        note, 
        assetIndex: assetID, 
        suggestedParams: params
    });
    // Must be signed by the account sending the asset  
    const rawSignedTxn = xtxn.signTxn(bob.sk);
    const tx = (await algodClient.sendRawTransaction(rawSignedTxn).do());

    // Wait for confirmation
    const confirmedTxn = await waitForConfirmation(algodClient, tx.txId, 4);
    //Get the completed Transaction
    console.log("Transaction " + tx.txId + " confirmed in round " + confirmedTxn["confirmed-round"]);
    // You should see 999 assets in Alice' account
    console.log("Alice Account = " + alice.addr);
    await printAssetHolding(algodClient, alice.addr, assetID);
    // You should now see the 0 assets listed in Bob's account information
    console.log("Bob Account = " + bob.addr);
    await printAssetHolding(algodClient, bob.addr, assetID);
    return;
    // Sample Output
    // ==> TRANSFER ASSETS BACK FROM BOB TO ALICE
    // Transaction LB3BMQTXKCSDXKSQ5JPUGANKLEI3KLGAGAMFBPXUXQFXCAHBCR6Q confirmed in round 16833533
    // Alice Account = RA6RAUNDQGHRWTCR5YRL2YJMIXTHWD5S3ZYHVBGSNA76AVBAYELSNRVKEI
    // assetholdinginfo = {
    //   "amount": 999,
    //   "asset-id": 28291127,
    //   "creator": "RA6RAUNDQGHRWTCR5YRL2YJMIXTHWD5S3ZYHVBGSNA76AVBAYELSNRVKEI",
    //   "is-frozen": false
    // }
    // Bob Account = YC3UYV4JLHD344OC3G7JK37DRVSE7X7U2NOZVWSQNVKNEGV4M3KFA7WZ44
    
}
// Destroy Asset:
async function destroyAsset(algodClient, alice, assetID, bob) {
    console.log("");
    console.log("==> DESTROY ASSET");
    // All of the created assets should now be back in the creators
    // Account so we can delete the asset.
    // If this is not the case the asset deletion will fail
    const params = await algodClient.getTransactionParams().do();
    // Comment out the next two lines to use suggested fee
    // params.fee = 1000;
    // params.flatFee = true;
    // The address for the from field must be the manager account
    const addr = alice.addr;
    // if all assets are held by the asset creator,
    // the asset creator can sign and issue "txn" to remove the asset from the ledger. 
    const txn = algosdk.makeAssetDestroyTxnWithSuggestedParamsFromObject({
        from: addr, 
        note: undefined, 
        assetIndex: assetID, 
        suggestedParams: params
    });
    // The transaction must be signed by the manager which 
    // is currently set to alice
    const rawSignedTxn = txn.signTxn(alice.sk);
    const tx = (await algodClient.sendRawTransaction(rawSignedTxn).do());
    // Wait for confirmation
    const confirmedTxn = await waitForConfirmation(algodClient, tx.txId, 4);
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

    // ==> DESTROY ASSET
    // Transaction QCE52AAX75VBSGDL36VHMNVT6LXSR5M6V5JUNSKE6BXQGLQEMLDA confirmed in round 16833536
    // Asset ID: 28291127
    // Alice = RA6RAUNDQGHRWTCR5YRL2YJMIXTHWD5S3ZYHVBGSNA76AVBAYELSNRVKEI
    // Bob = YC3UYV4JLHD344OC3G7JK37DRVSE7X7U2NOZVWSQNVKNEGV4M3KFA7WZ44  
}
async function closeoutBobAlgos(algodClient, bob, alice) {
    console.log("");
    console.log("==> CLOSE OUT BOB'S ALGOS TO DISPENSER");
    let accountInfo = await algodClient.accountInformation(bob.addr).do();
    console.log("Bob Account balance: %d microAlgos", accountInfo.amount);
    const startingAmount = accountInfo.amount;
    // Construct the transaction
    const params = await algodClient.getTransactionParams().do();
    // comment out the next two lines to use suggested fee
    // params.fee = 1000;
    // params.flatFee = true;
    const receiver = bob.addr;
    const amount = 0;
    const sender = bob.addr;
    // add remaining funds go back to dispenser
    // closeRemainderTo will remove the assetholding from the account
    const closeRemainderTo = DISPENSERACCOUNT;
    const txn = algosdk.makePaymentTxnWithSuggestedParamsFromObject({
        from: sender, 
        to: receiver,
        amount, 
        closeRemainderTo, 
        note: undefined, 
        suggestedParams: params});
    // Sign the transaction
    const rawSignedTxn = txn.signTxn(bob.sk);
    // Submit the transaction
    const tx = (await algodClient.sendRawTransaction(rawSignedTxn).do());
    // Wait for confirmation
    const confirmedTxn = await waitForConfirmation(algodClient, tx.txId, 4);
    //Get the completed Transaction
    console.log("Transaction " + tx.txId + " confirmed in round " + confirmedTxn["confirmed-round"]);
    // const mytxinfo = JSON.stringify(confirmedTxn.txn.txn, undefined, 2);
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
    console.log("Bob Account balance: %d microAlgos", accountInfo.amount);
    return;
    // Sample Output
    // ==> CLOSE OUT BOB'S ALGOS TO DISPENSER
    // Bob Account balance: 998000 microAlgos
    // Transaction APVJ35KIG6DMFUDAJV7NNWF5PBFAWVCMHB64OVZVU27VAI3HPARA confirmed in round 16833540
    // Transaction Amount: 0 microAlgos
    // Transaction Fee: 1000 microAlgos
    // Bobs Account balance: 0 microAlgos

}
async function closeoutAliceAlgos(algodClient, alice) {
    console.log("");
    console.log("==> CLOSE OUT ALICE'S ALGOS TO DISPENSER");
    let accountInfo = await algodClient.accountInformation(alice.addr).do();
    console.log("Alice Account balance: %d microAlgos", accountInfo.amount);
    const startingAmount = accountInfo.amount;
    // Construct the transaction
    const params = await algodClient.getTransactionParams().do();
    // comment out the next two lines to use suggested fee
    // params.fee = 1000;
    // params.flatFee = true;
    // For more info see: 
    // https://developer.algorand.org/docs/reference/transactions/#payment-transaction
    // receiver account to send to
    const receiver = alice.addr;
    const enc = new TextEncoder();
    const amount = 0;
    const sender = alice.addr;
    // closeToRemainder will remove the assetholding from the account
    const closeRemainderTo = DISPENSERACCOUNT;
    const txn = algosdk.makePaymentTxnWithSuggestedParamsFromObject({
        from: sender, 
        to: receiver,
        amount, 
        closeRemainderTo, 
        note: undefined, 
        suggestedParams: params});
    // Sign the transaction
    const rawSignedTxn = txn.signTxn(alice.sk);
    // Submit the transaction
    const tx = (await algodClient.sendRawTransaction(rawSignedTxn).do());
    // Wait for confirmation
    const confirmedTxn = await waitForConfirmation(algodClient, tx.txId, 4);
    //Get the completed Transaction
    console.log("Transaction " + tx.txId + " confirmed in round " + confirmedTxn["confirmed-round"]);
    // const mytxinfo = JSON.stringify(confirmedTxn.txn.txn, undefined, 2);
    // console.log("Transaction information: %o", mytxinfo);
    accountInfo = await algodClient.accountInformation(alice.addr).do();
    let txAmount = confirmedTxn.txn.txn.amt;
    if (confirmedTxn.txn.txn.amt == undefined) {
        console.log("Transaction Amount: %d microAlgos", 0);
        txAmount=0;
    }
    else {
        console.log("Transaction Amount: %d microAlgos", confirmedTxn.txn.txn.amt);

    }
    console.log("Transaction Fee: %d microAlgos", confirmedTxn.txn.txn.fee);
    const closeoutamt = startingAmount - txAmount - confirmedTxn.txn.txn.fee;
    console.log("Close To Amount: %d microAlgos", closeoutamt);
    console.log("Bobs Account balance: %d microAlgos", accountInfo.amount);
    return;
    // Sample Output
    // ==> CLOSE OUT ALICE'S ALGOS TO DISPENSER
    // Alice Account balance: 8996000 microAlgos
    // Transaction IC6IQVUOFLTTXNWZWD4F6L5CZXOFBTD3EY2QJUY5MHUOQSAX3CEA confirmed in round 16833543
    // Transaction Amount: 0 microAlgos
    // Transaction Fee: 1000 microAlgos
    // Bobs Account balance: 0 microAlgos
}

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
    //     const accountInfo = await indexerClient.searchAccounts()
    //    .assetID(assetIndex).do();
    // and in the loop below use this to extract the asset for a particular account
    // accountInfo['accounts'][idx][account]);
    const accountInfo = await algodClient.accountInformation(account).do();
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
    //     const accountInfo = await indexerClient.searchAccounts()
    //    .assetID(assetIndex).do();
    // and in the loop below use this to extract the asset for a particular account
    // accountInfo['accounts'][idx][account]);
    const accountInfo = await algodClient.accountInformation(account).do();
    for (idx = 0; idx < accountInfo['assets'].length; idx++) {
        let scrutinizedAsset = accountInfo['assets'][idx];
        if (scrutinizedAsset['asset-id'] == assetid) {
            let myassetholding = JSON.stringify(scrutinizedAsset, undefined, 2);
            console.log("assetholdinginfo = " + myassetholding);
            break;
        }
    }
};

async function gettingStartedASAInteractions() {

    try {
        // CREATE ACCOUNTS
        console.log("");
        console.log("==> CREATE ALICE ACCOUNT");
        const alice = createAccount();
        console.log("Account created. Save off Mnemonic and address");
        console.log("Add funds to account using the TestNet Dispenser: ");
        console.log("https://dispenser.testnet.aws.algodev.network/?account=" + alice.addr);
        console.log("Press any key when the account is funded");
        await keypress();
        console.log("");
        console.log("==> CREATE BOB ACCOUNT");
        const bob = createAccount();

        // Connect your client
        // const algodToken = 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa';
        // const algodServer = 'http://localhost';
        // const algodPort = 4001;
        const algodToken = '2f3203f21e738a1de6110eba6984f9d03e5a95d7a577b34616854064cf2c0e7b';
        const algodServer = 'https://academy-algod.dev.aws.algodev.network';
        const algodPort = 443;

        const algodClient = new algosdk.Algodv2(algodToken, algodServer, algodPort);
        // CREATE ASSET
        const { assetID } = await createAsset(algodClient, alice);
        // TRANSFER ALGOS TO BOB 
        await transferAlgosToBob(algodClient, bob, alice);
        // BOB OPTS IN 
        await optIn(algodClient, bob, assetID);
        // TRANSFER ASSETS FROM ALICE TO BOB
        await transferAssets(algodClient, alice, bob, assetID);
        // TRANSFER ASSETS BACK TO ALICE FROM BOB
        await transferAssetsBack(algodClient, bob, alice, assetID);
        // DESTROY ASSET
        await destroyAsset(algodClient, alice, assetID, bob);
        // CLOSEOUT ALGOS - Bob closes out Algos to dispenser
        await closeoutBobAlgos(algodClient, bob, alice);
        // CLOSEOUT ALGOS - Alice closes out Alogs to dispenser
        await closeoutAliceAlgos(algodClient, alice);
    }
    catch (err) {
        console.log("err", err);
    }
    process.exit();


};

gettingStartedASAInteractions();












