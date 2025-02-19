const algosdk = require('algosdk');
const keypress = async () => {
    process.stdin.setRawMode(true)
    return new Promise(resolve => process.stdin.once('data', () => {
        process.stdin.setRawMode(false)
        resolve()
    })) 
}
// Create an account and add funds to it. Copy the address off
// The Algorand TestNet Dispenser is located here: 
// https://dispenser.testnet.aws.algodev.network/

const createAccount =  function (){
    try{  
        const myaccount = algosdk.generateAccount();
        console.log("Account Address = " + myaccount.addr);
        let account_mnemonic = algosdk.secretKeyToMnemonic(myaccount.sk);
        console.log("Account Mnemonic = "+ account_mnemonic);
        console.log("Account created. Save off Mnemonic and address");
        console.log("Add funds to account using the TestNet Dispenser: ");
        console.log("https://dispenser.testnet.aws.algodev.network?account=" + myaccount.addr);

        return myaccount;
    }
    catch (err) {
        console.log("err", err);
    }
};


async function firstTransaction() {

    try {
        let myAccount = createAccount();
        console.log("Press any key when the account is funded");
        await keypress();
        // Connect your client
        // const algodToken = '2f3203f21e738a1de6110eba6984f9d03e5a95d7a577b34616854064cf2c0e7b';
        // const algodServer = 'https://academy-algod.dev.aws.algodev.network/';
        // const algodPort = '';
                // Connect your client
        // const algodServer = 'https://testnet.algoexplorerapi.io';
        // const algodServer = 'https://algoexplorer.io/api-dev/v2';
        const algodToken = '';
        // new endpoint for Algoexplorer api Feb 3 2022
        const algodServer = 'https://node.algoexplorerapi.io/v2';
        const algodPort = '';
        // const algodToken = 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa';
        // const algodServer = 'http://localhost';
        // const algodPort = 4001;

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

        // receiver defined as TestNet faucet address 
        const receiver = "HZ57J3K46JIJXILONBBZOHX6BKPXEM2VVXNRFSUED6DKFD5ZD24PMJ3MVA";
        const enc = new TextEncoder();
        const note = enc.encode("Hello World");
        let amount = 1000000;
        let closeout = receiver; //closeRemainderTo - return remainder to TestNet faucet
        let sender = myAccount.addr;
        let txn = algosdk.makePaymentTxnWithSuggestedParams(sender, receiver, amount, closeout, note, params);
        // WARNING! all remaining funds in the sender account above will be sent to the closeRemainderTo Account 
        // In order to keep all remaning funds in the sender account after tx, set closeout parameter to undefined.
        // For more info see: 
        // https://developer.algorand.org/docs/reference/transactions/#payment-transaction

        // Sign the transaction
        let signedTxn = txn.signTxn(myAccount.sk);
        let txId = txn.txID().toString();
        console.log("Signed transaction with txID: %s", txId);

        // Submit the transaction
        await algodClient.sendRawTransaction(signedTxn).do();

        // Wait for confirmation
        //let confirmedTxn = await waitForConfirmation(algodClient, txId, 4);
        let confirmedTxn = await algosdk.waitForConfirmation(algodClient, txId, 4);
        //Get the completed Transaction
        console.log("Transaction " + txId + " confirmed in round " + confirmedTxn["confirmed-round"]);
        let string = new TextDecoder().decode(confirmedTxn.txn.txn.note);
        console.log("Note field: ", string);
        accountInfo = await algodClient.accountInformation(myAccount.addr).do();
        console.log("Transaction Amount: %d microAlgos", confirmedTxn.txn.txn.amt);        
        console.log("Transaction Fee: %d microAlgos", confirmedTxn.txn.txn.fee);
        let closeoutamt = startingAmount - confirmedTxn.txn.txn.amt - confirmedTxn.txn.txn.fee;     
        console.log("Close To Amount: %d microAlgos", closeoutamt);
        console.log("Account balance: %d microAlgos", accountInfo.amount);
    }
    catch (err) {
        console.log("err", err);
    }
    process.exit();
};

firstTransaction();

