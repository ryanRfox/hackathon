// Example: creating an asset

const algosdk = require('algosdk');
const fs = require('fs/promises');
const crypto = require('crypto');

const utils = require('./utils');

const { ALGOD_INSTANCE, SENDER } = utils.retrieveBaseConfig();
const client = new algosdk.Algodv2(
    "",
    "https://testnet.algoexplorerapi.io",
    443
);

async function main() {
    const sender = algosdk.mnemonicToSecretKey(SENDER.mnemonic);

    const freezeAddr = sender.addr;
    const managerAddr = sender.addr;
    const clawbackAddr = sender.addr;
    const reserveAddr = sender.addr;

    const total = 100; // how many of this asset there will be
    const decimals = 0; // units of this asset are whole-integer amounts
    const assetName = 'ALICECOI@arc3';
    const unitName = 'ALICECOI';
    const url = 'https://s3.amazonaws.com/your-bucket/metadata.json';

    const metadataJSON = await fs.readFile("metadata.json");
    const hash = crypto.createHash('sha256');
    hash.update(metadataJSON);
    const metadata = new Uint8Array(hash.digest());
    const defaultFrozen = false; // whether accounts should be frozen by default

    // create suggested parameters
    const suggestedParams = await client.getTransactionParams().do();

    // create the asset creation transaction
    const txn = algosdk.makeAssetCreateTxnWithSuggestedParamsFromObject({
        from: sender.addr,
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
        suggestedParams,
    });

    // sign the transaction
    const signedTxn = txn.signTxn(sender.sk);

    // save the transaction
    await fs.writeFile("transaction.tx", signedTxn);

    console.log("Transaction saved in transaction.tx");
    console.log("You can check the transaction using:");
    console.log("  goal clerk inspect transaction.tx");
    console.log("Metadata hash should appear as identical to the output of");
    console.log("  cat metadata.json | openssl dgst -sha256 -binary | openssl base64 -A");
    console.log("That is: yzpTR3q7mamjWXjin4OBrvrz6WQmGb//qm+sicTLloM=");

    // print transaction data
    const decoded = algosdk.decodeSignedTransaction(signedTxn);
    console.log(decoded);
}

main().catch(console.error);
