const algosdk = require('algosdk');



// const token = "<your-api-token>";
// const server = "<http://your-sever>";
// const port = 8080;

// sandbox
const token = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
const server = "http://localhost";
const port = 4001;


//Recover the account
let  mnemonic = "price clap dilemma swim genius fame lucky crack torch hunt maid palace ladder unlock symptom rubber scale load acoustic drop oval cabbage review abstract embark"
let  recoveredAccount = algosdk.mnemonicToSecretKey(mnemonic);
console.log(recoveredAccount.addr);
//check to see if account is valid
let  isValid = algosdk.isValidAddress(recoveredAccount.addr);
console.log("Is this a valid address: " + isValid);

//instantiate the algod wrapper
let algodClient = new algosdk.Algodv2(token, server, port);

//submit the transaction
(async () => {

    //Check your balance
    let accountInfo = await algodClient.accountInformation(recoveredAccount.addr).do();
    console.log("Account balance: %d microAlgos", accountInfo.amount);
    // Construct the transaction
    let params = await algodClient.getTransactionParams().do();
    // comment out the next two lines to use suggested fee
    // params.fee = 1000;
    // params.flatFee = true;
    // receiver defined as TestNet faucet address 
    const receiver = "GD64YIY3TWGDMCNPP553DZPPR6LDUSFQOIJVFDPPXWEG3FVOJCCDBBHU5A";
    // let names = '{"firstName":"John", "lastName":"Doe"}';
    const enc = new TextEncoder();
    let note = enc.encode("Hello World");
    // let note = enc.encode('[{"_id":"60c9dc007b9206a8a641f2e8"]}]');
    console.log(note);      
    let txn = algosdk.makePaymentTxnWithSuggestedParams(recoveredAccount.addr,
        receiver, 100000, undefined, note, params);

    // Sign the transaction
    let signedTxn = txn.signTxn(recoveredAccount.sk);
    let txId = txn.txID().toString();
    console.log("Signed transaction with txID: %s", txId);

    // Submit the transaction
    await algodClient.sendRawTransaction(signedTxn).do();

    // Wait for confirmation
    let confirmedTxn = await algosdk.waitForConfirmation(algodClient, txId, 4);
    //Get the completed Transaction
    console.log("Transaction " + txId + " confirmed in round " + confirmedTxn["confirmed-round"]);
    let mytxinfo = JSON.stringify(confirmedTxn.txn.txn, undefined, 2);
    console.log("Transaction information: %o", mytxinfo);
    let  string = new TextDecoder().decode(confirmedTxn.txn.txn.note);
    console.log("Note field: ", string);
    // const obj = JSON.parse(string);
    // console.log("Note first name: %s", obj.firstName);
})().catch(e => {
    console.log(e);
});