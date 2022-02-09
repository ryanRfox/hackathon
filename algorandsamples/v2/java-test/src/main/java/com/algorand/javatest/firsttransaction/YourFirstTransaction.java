package com.algorand.javatest.firsttransaction;

import com.algorand.algosdk.account.Account;
import com.algorand.algosdk.crypto.Address;
import com.algorand.algosdk.transaction.SignedTransaction;
import com.algorand.algosdk.transaction.Transaction;
import com.algorand.algosdk.util.Encoder;
import com.algorand.algosdk.v2.client.common.AlgodClient;
import com.algorand.algosdk.v2.client.common.Response;
import com.algorand.algosdk.v2.client.Utils;
//import com.algorand.algosdk.v2.client.model.NodeStatusResponse;
import com.algorand.algosdk.v2.client.model.PendingTransactionResponse;
import com.algorand.algosdk.v2.client.model.PostTransactionsResponse;
import com.algorand.algosdk.v2.client.model.TransactionParametersResponse;
import org.json.JSONObject;


public class YourFirstTransaction {
    public AlgodClient client = null;

    // utility function to connect to a node
    private AlgodClient connectToNetwork() {

        // final String ALGOD_API_ADDR = "https://testnet-algorand.api.purestake.io/ps2";
        // Initialize an algod client
        // AlgodClient client = (AlgodClient) new AlgodClient(ALGOD_API_ADDR, ALGOD_PORT, ALGOD_API_TOKEN);
        // hackathon - demos instance
        final String ALGOD_API_ADDR = "http://hackathon.algodev.network";
        final Integer ALGOD_PORT = 9100;
        final String ALGOD_API_TOKEN = "ef920e2e7e002953f4b29a8af720efe8e4ecc75ff102b165e0472834b25832c1";
        // final String ALGOD_API_ADDR = "localhost";
        // final Integer ALGOD_PORT = 4001;
        // final String ALGOD_API_TOKEN = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        // final String ALGOD_API_ADDR = "https://testnet.algoexplorerapi.io/";
        // final Integer ALGOD_PORT = 443;
        // final String ALGOD_API_TOKEN = "";
        // final String ALGOD_API_TOKEN = "2f3203f21e738a1de6110eba6984f9d03e5a95d7a577b34616854064cf2c0e7b";
        // final String ALGOD_API_ADDR = "https://academy-algod.dev.aws.algodev.network/";
        // final Integer ALGOD_PORT = 443;
        AlgodClient client = new AlgodClient(ALGOD_API_ADDR,
            ALGOD_PORT, ALGOD_API_TOKEN);
        return client;
    }



    public void gettingStartedExample() throws Exception {

        if (client == null)
            this.client = connectToNetwork();
        // Import your private key mnemonic and address
        final String PASSPHRASE = "catch mushroom provide tired doctor monitor blue order impose illegal venture sorry dwarf boost change deposit float curtain illness almost example carbon aunt absent canal";
        com.algorand.algosdk.account.Account myAccount = new Account(PASSPHRASE);
      
        System.out.println("My Address: " + myAccount.getAddress());
        System.out.println("My mnemonic: " + myAccount.toMnemonic());
        String myAddress = printBalance(myAccount);

        try {
            // Construct the transaction
            final String RECEIVER = "L5EUPCF4ROKNZMAE37R5FY2T5DF2M3NVYLPKSGWTUKVJRUGIW4RKVPNPD4";
            String note = "Hello World";
            Response < TransactionParametersResponse > resp = client.TransactionParams().execute();
            if (!resp.isSuccessful()) {
                throw new Exception(resp.message());
            }
            TransactionParametersResponse params = resp.body();
            if (params == null) {
                throw new Exception("Params retrieval error");
            }
            System.out.println("Algorand suggested parameters: " + params);
            Transaction txn = Transaction.PaymentTransactionBuilder()
                .sender(myAddress)
                .note(note.getBytes())
                .amount(100000)
                .receiver(new Address(RECEIVER))
                .suggestedParams(params)
                .build();

            // Sign the transaction
            SignedTransaction signedTxn = myAccount.signTransaction(txn);
            System.out.println("Signed transaction with txid: " + signedTxn.transactionID);
            String[] headers = {"Content-Type"};
            String[] values = {"application/x-binary"};
            // Submit the transaction to the network
            byte[] encodedTxBytes = Encoder.encodeToMsgPack(signedTxn);
            Response < PostTransactionsResponse > rawtxresponse = client.RawTransaction().rawtxn(encodedTxBytes).execute(headers, values);
            if (!rawtxresponse.isSuccessful()) {
                throw new Exception(rawtxresponse.message());
            }
            String id = rawtxresponse.body().txId;

            // Wait for transaction confirmation
            PendingTransactionResponse pTrx = Utils.waitForConfirmation(client, id, 4);
     
            System.out.println("Transaction " + id + " confirmed in round " + pTrx.confirmedRound);
            // Read the transaction
            JSONObject jsonObj = new JSONObject(pTrx.toString());
            System.out.println("Transaction information (with notes): " + jsonObj.toString(2));
            System.out.println("Decoded note: " + new String(pTrx.txn.tx.note));
            System.out.println("Amount: " + new String(pTrx.txn.tx.amount.toString()));
            System.out.println("Fee: " + new String(pTrx.txn.tx.fee.toString()));
            if (pTrx.closingAmount != null){
                System.out.println("Closing Amount: " + new String(pTrx.closingAmount.toString()));
            }
            printBalance(myAccount);
        } catch (Exception e) {
            System.err.println("Exception when calling algod#transactionInformation: " + e.getMessage());
        }
    }

    private String printBalance(com.algorand.algosdk.account.Account myAccount) throws Exception {
        String myAddress = myAccount.getAddress().toString();
        Response < com.algorand.algosdk.v2.client.model.Account > respAcct = client.AccountInformation(myAccount.getAddress()).execute();
        if (!respAcct.isSuccessful()) {
            throw new Exception(respAcct.message());
        }
        com.algorand.algosdk.v2.client.model.Account accountInfo = respAcct.body();
        System.out.println(String.format("Account Balance: %d microAlgos", accountInfo.amount));
        return myAddress;
    }

    public static void main(String args[]) throws Exception {
        YourFirstTransaction t = new YourFirstTransaction();
        t.gettingStartedExample();
    }
}