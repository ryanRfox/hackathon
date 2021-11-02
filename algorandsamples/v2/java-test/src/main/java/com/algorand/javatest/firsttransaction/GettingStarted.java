package com.algorand.javatest.firsttransaction;
import com.algorand.algosdk.account.Account;
import java.util.Scanner;
import com.algorand.algosdk.crypto.Address;
import com.algorand.algosdk.transaction.SignedTransaction;
import com.algorand.algosdk.transaction.Transaction;
import com.algorand.algosdk.util.Encoder;
import com.algorand.algosdk.v2.client.common.AlgodClient;
import com.algorand.algosdk.v2.client.common.Response;
import com.algorand.algosdk.v2.client.model.NodeStatusResponse;
import com.algorand.algosdk.v2.client.model.PendingTransactionResponse;
import com.algorand.algosdk.v2.client.model.PostTransactionsResponse;
import com.algorand.algosdk.v2.client.model.TransactionParametersResponse;
import org.json.JSONObject;


class GettingStarted{

    // Create Account
    static Scanner scan = new Scanner(System.in);

    public Account createAccount()  throws Exception {
        try {
            Account myAccount1 = new Account();
            System.out.println("My Address: " + myAccount1.getAddress());
            System.out.println("My Passphrase: " + myAccount1.toMnemonic());
  
            System.out.println("Navigate to this link and dispense:  https://dispenser.testnet.aws.algodev.network?account=" + myAccount1.getAddress());            
            System.out.println("PRESS ENTER KEY TO CONTINUE...");     
            scan.nextLine();
            return myAccount1;
            // Copy off account and mnemonic
            // Dispense TestNet Algos to account:
            // https://dispenser.testnet.aws.algodev.network/
            // resource:
            // https://developer.algorand.org/docs/features/accounts/create/#standalone        
        
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Account creation error " + e.getMessage() );
        }

    } 
    // final Integer ALGOD_PORT = 443;
    // final String ALGOD_API_TOKEN = "";
    // final String ALGOD_API_ADDR = "https://testnet.algoexplorerapi.io/";

    private AlgodClient client = null; 
      // utility function to connect to a node
    private AlgodClient connectToNetwork() {
        final String ALGOD_API_ADDR = "localhost";
        final String ALGOD_API_TOKEN = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        final Integer ALGOD_PORT = 4001;
        // final String ALGOD_API_TOKEN = "2f3203f21e738a1de6110eba6984f9d03e5a95d7a577b34616854064cf2c0e7b";
        // final String ALGOD_API_ADDR = "https://academy-algod.dev.aws.algodev.network/";
        // final Integer ALGOD_PORT = 443;


        AlgodClient client = new AlgodClient(ALGOD_API_ADDR,
            ALGOD_PORT, ALGOD_API_TOKEN);
        return client;
    }
    /**
      * utility function to wait on a transaction to be confirmed
      * the timeout parameter indicates how many rounds do you wish to check pending transactions for
      */
    private PendingTransactionResponse waitForConfirmation(AlgodClient myclient, String txID, Integer timeout)
    throws Exception {
        if (myclient == null || txID == null || timeout < 0) {
            throw new IllegalArgumentException("Bad arguments for waitForConfirmation.");
        }
        Response < NodeStatusResponse > resp = myclient.GetStatus().execute();
        if (!resp.isSuccessful()) {
            throw new Exception(resp.message());
        }
        NodeStatusResponse nodeStatusResponse = resp.body();
        Long startRound = nodeStatusResponse.lastRound + 1;
        Long currentRound = startRound;
        while (currentRound < (startRound + timeout)) {
            // Check the pending transactions                 
            Response < PendingTransactionResponse > resp2 = myclient.PendingTransactionInformation(txID).execute();
            if (resp2.isSuccessful()) {
                PendingTransactionResponse pendingInfo = resp2.body();
                if (pendingInfo != null) {
                    if (pendingInfo.confirmedRound != null && pendingInfo.confirmedRound > 0) {
                        // Got the completed Transaction
                        return pendingInfo;
                    }
                    if (pendingInfo.poolError != null && pendingInfo.poolError.length() > 0) {
                        // If there was a pool error, then the transaction has been rejected!
                        throw new Exception("The transaction has been rejected with a pool error: " + pendingInfo.poolError);
                    }
                }
            }
            resp = myclient.WaitForBlock(currentRound).execute();
            if (!resp.isSuccessful()) {
                throw new Exception(resp.message());
            }
            currentRound++;
        }
        throw new Exception("Transaction not confirmed after " + timeout + " rounds!");
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

    public void firstTransaction(Account myAccount) throws Exception {

        if (client == null)
            this.client = connectToNetwork();

        printBalance(myAccount);

        try {
            // Construct the transaction / dispeser account
            final String RECEIVER = "HZ57J3K46JIJXILONBBZOHX6BKPXEM2VVXNRFSUED6DKFD5ZD24PMJ3MVA";
            String note = "Hello World";
            Response < TransactionParametersResponse > resp = client.TransactionParams().execute();
            if (!resp.isSuccessful()) {
                throw new Exception(resp.message());
            }
            TransactionParametersResponse params = resp.body();
            if (params == null) {
                throw new Exception("Params retrieval error");
            }
            JSONObject jsonObj = new JSONObject(params.toString());
            System.out.println("Algorand suggested parameters: " + jsonObj.toString(2));
            Transaction txn = Transaction.PaymentTransactionBuilder()
                .sender(myAccount.getAddress().toString())
                .note(note.getBytes())
                .amount(1000000) // 1 algo = 1000000 microalgos
                .receiver(new Address(RECEIVER))
                .suggestedParams(params)
                .closeRemainderTo(RECEIVER) 
                .build();
           
            // Sign the transaction
            SignedTransaction signedTxn = myAccount.signTransaction(txn);
            System.out.println("Signed transaction with txid: " + signedTxn.transactionID);

            // Submit the transaction to the network
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
            PendingTransactionResponse pTrx = waitForConfirmation(client, id, 4);

            System.out.println("Transaction " + id + " confirmed in round " + pTrx.confirmedRound);
            // Read the transaction
            JSONObject jsonObj2 = new JSONObject(pTrx.toString());
            System.out.println("Transaction information (with notes): " + jsonObj2.toString(2));
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

    public static void main(String args[]) throws Exception {
        GettingStarted t = new GettingStarted();
        Account myAccount1 = t.createAccount();
        t.firstTransaction(myAccount1);
    }  
  } 
 
  


















// System.out.println("Navigate to this link:  https://dispenser.testnet.aws.algodev.network/?account=" + myAccount1.getAddress().toString());      


//   .closeRemainderTo(RECEIVER) // WARNING! all remaining funds in the sender account will be sent to the closeRemainderTo Account, omit RECEIVER account when in use otherwise all funds from the sender account will be sent to that account.
//   .build();
// CloseRemainder can be used to reset sender account to 0.
// Normally this would be omitted. For more info see: 
// https://developer.algorand.org/docs/reference/transactions/#payment-transaction
