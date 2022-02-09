package com.algorand.javatest.firsttransaction;

import com.algorand.algosdk.account.Account;
import com.algorand.algosdk.transaction.SignedTransaction;
import com.algorand.algosdk.transaction.Transaction;
import com.algorand.algosdk.util.Encoder;
import com.algorand.algosdk.v2.client.common.AlgodClient;
import com.algorand.algosdk.v2.client.common.Response;
import com.algorand.algosdk.v2.client.model.PendingTransactionResponse;
import com.algorand.algosdk.v2.client.model.PostTransactionsResponse;
import org.json.JSONObject;
import com.algorand.algosdk.v2.client.Utils;


public class NoteField {
    public AlgodClient client = null;

    /**
     * Initialize an algod client
     */
    private AlgodClient connectToNetwork() {
        final String ALGOD_API_ADDR = "localhost";
        final Integer ALGOD_PORT = 4001;
        final String ALGOD_API_TOKEN = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        AlgodClient client = new AlgodClient(ALGOD_API_ADDR, ALGOD_PORT, ALGOD_API_TOKEN);
        return client;
    }



    /**
     * note field example.
     */
    public void gettingStartedNoteFieldExample() throws Exception {

        if (client == null) {
            this.client = connectToNetwork();
        }

        // Import your private key mnemonic and address
        final String PASSPHRASE = "patrol target joy dial ethics flip usual fatigue bulb security prosper brand coast arch casino burger inch cricket scissors shoe evolve eternal calm absorb school";
        com.algorand.algosdk.account.Account myAccount = new Account(PASSPHRASE);
        System.out.println("My Address: " + myAccount.getAddress());
        printBalance(myAccount);

        // Construct the transaction
        final String RECEIVER = "L5EUPCF4ROKNZMAE37R5FY2T5DF2M3NVYLPKSGWTUKVJRUGIW4RKVPNPD4";
        // add some notes to the transaction
        String note = "showing prefix and more";

        Transaction txn = Transaction.PaymentTransactionBuilder()
                .sender(myAccount.getAddress())
                .noteUTF8(note)
                .amount(100000)
                .receiver(RECEIVER)
                .lookupParams(client) // query algod for firstValid, fee, etc
                .build();

        // Sign the transaction
        SignedTransaction signedTxn = myAccount.signTransaction(txn);
        System.out.println("Signed transaction with txid: " + signedTxn.transactionID);

        // Submit the transaction to the network
        byte[] encodedTxBytes = Encoder.encodeToMsgPack(signedTxn);
        Response<PostTransactionsResponse> resp = client.RawTransaction()
            .rawtxn(encodedTxBytes)
            .execute();
        if (!resp.isSuccessful()) {
            throw new Exception(resp.message());
        }
        String id = resp.body().txId;

        // Wait for transaction confirmation
        PendingTransactionResponse pTrx = Utils.waitForConfirmation(client, id, 4);

        System.out.println("Transaction " + id + " confirmed in round " + pTrx.confirmedRound);
        // Read the transaction
        JSONObject jsonObj = new JSONObject(pTrx.toString());
        System.out.println("Transaction information (with notes): " + jsonObj.toString(2));
        System.out.println("Decoded note: " + new String(pTrx.txn.tx.note));
        printBalance(myAccount);
    }

    /**
     * Print  account balance.
     */

    private void printBalance(Account myAccount) throws Exception {
    
        Response<com.algorand.algosdk.v2.client.model.Account> resp = client.AccountInformation(myAccount.getAddress())
                .execute();
        if (!resp.isSuccessful()) {
            throw new Exception(resp.message());
        }
        com.algorand.algosdk.v2.client.model.Account accountInfo = resp.body();
        System.out.println("Account Balance: " + accountInfo.amount + " microAlgos");
    }

    public static void main(String args[]) throws Exception {
        NoteField t = new NoteField();
        t.gettingStartedNoteFieldExample();
    }
}
