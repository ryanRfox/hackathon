package com.algorand.javatest.smart_contracts;

import com.algorand.algosdk.util.Encoder;

import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import com.algorand.algosdk.account.Account;
import com.algorand.algosdk.builder.transaction.ApplicationBaseTransactionBuilder;
import com.algorand.algosdk.crypto.Address;
import com.algorand.algosdk.crypto.TEALProgram;
import com.algorand.algosdk.logic.Logic;
import com.algorand.algosdk.logic.StateSchema;
import com.algorand.algosdk.v2.client.algod.AccountInformation;
import com.algorand.algosdk.v2.client.algod.TealCompile;
import com.algorand.algosdk.v2.client.common.AlgodClient;
import com.algorand.algosdk.v2.client.common.Response;
import com.algorand.algosdk.v2.client.model.Application;
import com.algorand.algosdk.v2.client.model.ApplicationLocalState;
import com.algorand.algosdk.v2.client.model.CompileResponse;
import com.algorand.algosdk.v2.client.model.PendingTransactionResponse;
import com.algorand.algosdk.v2.client.model.PostTransactionsResponse;
import com.algorand.algosdk.transaction.SignedTransaction;
import com.algorand.algosdk.transaction.Transaction;
import com.algorand.algosdk.v2.client.model.TransactionParametersResponse;
import com.algorand.algosdk.v2.client.model.TransactionsResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

public class CreateContract {
    public AlgodClient client = null;

    // utility function to connect to a node
    private AlgodClient connectToNetwork() {

        // Initialize an algod client
        final String ALGOD_API_ADDR = "localhost";
        final Integer ALGOD_PORT = 4001;
        final String ALGOD_API_TOKEN = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

        AlgodClient client = (AlgodClient) new AlgodClient(ALGOD_API_ADDR, ALGOD_PORT, ALGOD_API_TOKEN);
        return client;
    }

    // utility function to wait on a transaction to be confirmed
    public void waitForConfirmation(String txID) throws Exception {
        if (client == null)
            this.client = connectToNetwork();
        Long lastRound = client.GetStatus().execute().body().lastRound;
        while (true) {
            try {
                // Check the pending transactions
                Response<PendingTransactionResponse> pendingInfo = client.PendingTransactionInformation(txID).execute();
                if (pendingInfo.body().confirmedRound != null && pendingInfo.body().confirmedRound > 0) {
                    // Got the completed Transaction
                    System.out.println(
                            "Transaction " + txID + " confirmed in round " + pendingInfo.body().confirmedRound);
                    break;
                }
                lastRound++;
                client.WaitForBlock(lastRound).execute();
            } catch (Exception e) {
                throw (e);
            }
        }
    }

    // helper function to compile program source
    public String compileProgram(AlgodClient client, byte[] programSource) {
        Response<CompileResponse> compileResponse = null;
        try {
            compileResponse = client.TealCompile().source(programSource).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(compileResponse.body().result);
        return compileResponse.body().result;
    }

    public Long createApp(AlgodClient client, Account creator, TEALProgram approvalProgramSource,
            TEALProgram clearProgramSource, int globalInts, int globalBytes, int localInts, int localBytes)
            throws Exception {
        // define sender as creator
        Address sender = creator.getAddress();

        // get node suggested parameters
        TransactionParametersResponse params = client.TransactionParams().execute().body();

        // create unsigned transaction
        Transaction txn = Transaction.ApplicationCreateTransactionBuilder()
                                .sender(sender)
                                .suggestedParams(params)
                                .approvalProgram(approvalProgramSource)
                                .clearStateProgram(clearProgramSource)
                                .globalStateSchema(new StateSchema(globalInts, globalBytes))
                                .localStateSchema(new StateSchema(localInts, localBytes))
                                .build();

        // sign transaction
        SignedTransaction signedTxn = creator.signTransaction(txn);
        System.out.println("Signed transaction with txid: " + signedTxn.transactionID);

        // send to network
        byte[] encodedTxBytes = Encoder.encodeToMsgPack(signedTxn);
        String id = client.RawTransaction().rawtxn(encodedTxBytes).execute().body().txId;
        System.out.println("Successfully sent tx with ID: " + id);

        // await confirmation
        waitForConfirmation(id);

        // display results
        PendingTransactionResponse pTrx = client.PendingTransactionInformation(id).execute().body();
        Long appId = pTrx.applicationIndex;
        System.out.println("Created new app-id: " + appId);    
    
        return appId;
    }

    public void optInApp(AlgodClient client, Account account, Long appId) throws Exception {
        // declare sender
        Address sender = account.getAddress();
        System.out.println("OptIn from account: " + sender);
    
        // get node suggested parameters
        TransactionParametersResponse params = client.TransactionParams().execute().body();
    
        // create unsigned transaction
        Transaction txn = Transaction.ApplicationOptInTransactionBuilder()
                                .sender(sender)
                                .suggestedParams(params)
                                .applicationId(appId)
                                .build();
    
        // sign transaction
        SignedTransaction signedTxn = account.signTransaction(txn);

        // send to network
        byte[] encodedTxBytes = Encoder.encodeToMsgPack(signedTxn);
        String id = client.RawTransaction().rawtxn(encodedTxBytes).execute().body().txId;

        // await confirmation
        waitForConfirmation(id);
    
        // display results
        PendingTransactionResponse pTrx = client.PendingTransactionInformation(id).execute().body();
        System.out.println("OptIn to app-id: " + pTrx.txn.tx.applicationId);            
    }

    public void callApp(AlgodClient client, Account account, Long appId, List<byte[]> appArgs) throws Exception {
        // declare sender
        Address sender = account.getAddress();
        System.out.println("Call from account: " + sender);

        TransactionParametersResponse params = client.TransactionParams().execute().body();
    
        // create unsigned transaction
        Transaction txn = Transaction.ApplicationCallTransactionBuilder()
                                .sender(sender)
                                .suggestedParams(params)
                                .applicationId(appId)
                                .args(appArgs)
                                .build();
    
        // sign transaction
        SignedTransaction signedTxn = account.signTransaction(txn);

        // save signed transaction to  a file 
        //Files.write(Paths.get("./callArgs.stxn"), Encoder.encodeToMsgPack(signedTxn));

        // send to network
        byte[] encodedTxBytes = Encoder.encodeToMsgPack(signedTxn);
        String id = client.RawTransaction().rawtxn(encodedTxBytes).execute().body().txId;

        // await confirmation
        waitForConfirmation(id);

        // display results
        PendingTransactionResponse pTrx = client.PendingTransactionInformation(id).execute().body();
        System.out.println("Called app-id: " + pTrx.txn.tx.applicationId);
        if (pTrx.globalStateDelta != null) {
            System.out.println("    Global state: " + pTrx.globalStateDelta.toString());
        }
        if (pTrx.localStateDelta != null) {
            System.out.println("    Local state: " + pTrx.localStateDelta.toString());
        }
    }

    public void readLocalState(AlgodClient client, Account account, Long appId) throws Exception {
        Response<com.algorand.algosdk.v2.client.model.Account> acctResponse = client.AccountInformation(account.getAddress()).execute();
        List<ApplicationLocalState> applicationLocalState = acctResponse.body().appsLocalState;
        for (int i = 0; i < applicationLocalState.size(); i++) { 
            if (applicationLocalState.get(i).id.equals(appId)) {
                System.out.println("User's application local state: " + applicationLocalState.get(i).keyValue.toString());
            }
        }
    }

    public void readGlobalState(AlgodClient client, Account account, Long appId) throws Exception {
        Response<com.algorand.algosdk.v2.client.model.Account> acctResponse = client.AccountInformation(account.getAddress()).execute();
        List<Application> createdApplications = acctResponse.body().createdApps;
        for (int i = 0; i < createdApplications.size(); i++) {
            if (createdApplications.get(i).id.equals(appId)) {
                System.out.println("Application global state: " + createdApplications.get(i).params.globalState.toString());
            }
        }
    }

    public void updateApp(AlgodClient client, Account creator, Long appId, TEALProgram approvalProgramSource, TEALProgram clearProgramSource) throws Exception {
        // define sender as creator
        Address sender = creator.getAddress();

        // get node suggested parameters
        TransactionParametersResponse params = client.TransactionParams().execute().body();

        // create unsigned transaction
        Transaction txn = Transaction.ApplicationUpdateTransactionBuilder()
                                .sender(sender)
                                .suggestedParams(params)
                                .applicationId(appId)
                                .approvalProgram(approvalProgramSource)
                                .clearStateProgram(clearProgramSource)
                                .build();

        // sign transaction
        SignedTransaction signedTxn = creator.signTransaction(txn);

        // send to network
        byte[] encodedTxBytes = Encoder.encodeToMsgPack(signedTxn);
        String id = client.RawTransaction().rawtxn(encodedTxBytes).execute().body().txId;

        // await confirmation
        waitForConfirmation(id);

        // display results
        PendingTransactionResponse pTrx = client.PendingTransactionInformation(id).execute().body();
        System.out.println("Updated new app-id: " + appId);    
    }

    public void closeOutApp(AlgodClient client, Account userAccount, Long appId) throws Exception {
        // define sender as creator
        Address sender = userAccount.getAddress();

        // get node suggested parameters
        TransactionParametersResponse params = client.TransactionParams().execute().body();

        // create unsigned transaction
        Transaction txn = Transaction.ApplicationCloseTransactionBuilder()
                                .sender(sender)
                                .suggestedParams(params)
                                .applicationId(appId)
                                .build();

        // sign transaction
        SignedTransaction signedTxn = userAccount.signTransaction(txn);

        // send to network
        byte[] encodedTxBytes = Encoder.encodeToMsgPack(signedTxn);
        String id = client.RawTransaction().rawtxn(encodedTxBytes).execute().body().txId;

        // await confirmation
        waitForConfirmation(id);

        // display results
        PendingTransactionResponse pTrx = client.PendingTransactionInformation(id).execute().body();
        System.out.println("Closed out from app-id: " + appId);    
    }

    public void clearApp(AlgodClient client, Account userAccount, Long appId) throws Exception {
        // define sender as creator
        Address sender = userAccount.getAddress();

        // get node suggested parameters
        TransactionParametersResponse params = client.TransactionParams().execute().body();

        // create unsigned transaction
        Transaction txn = Transaction.ApplicationClearTransactionBuilder()
                                .sender(sender)
                                .suggestedParams(params)
                                .applicationId(appId)
                                .build();

        // sign transaction
        SignedTransaction signedTxn = userAccount.signTransaction(txn);

        // send to network
        byte[] encodedTxBytes = Encoder.encodeToMsgPack(signedTxn);
        String id = client.RawTransaction().rawtxn(encodedTxBytes).execute().body().txId;

        // await confirmation
        waitForConfirmation(id);

        // display results
        PendingTransactionResponse pTrx = client.PendingTransactionInformation(id).execute().body();
        System.out.println("Cleared local state for app-id: " + appId);    
    }

    public void deleteApp(AlgodClient client, Account creatorAccount, Long appId) throws Exception {
        // define sender as creator
        Address sender = creatorAccount.getAddress();

        // get node suggested parameters
        TransactionParametersResponse params = client.TransactionParams().execute().body();

        // create unsigned transaction
        Transaction txn = Transaction.ApplicationDeleteTransactionBuilder()
                                .sender(sender)
                                .suggestedParams(params)
                                .applicationId(appId)
                                .build();

        // sign transaction
        SignedTransaction signedTxn = creatorAccount.signTransaction(txn);

        // send to network
        byte[] encodedTxBytes = Encoder.encodeToMsgPack(signedTxn);
        String id = client.RawTransaction().rawtxn(encodedTxBytes).execute().body().txId;

        // await confirmation
        waitForConfirmation(id);

        // display results
        PendingTransactionResponse pTrx = client.PendingTransactionInformation(id).execute().body();
        System.out.println("Deleted app-id: " + appId);    
    }

    public void statefulSmartContract() throws Exception {
        // user declared account mnemonics
        //5YF5RSW4TRQY3SLUFCNGOA7AMJZQF3NKDCGTGVXS52KJLJTCEYAHEINPSM
        String creatorMnemonic = "minimum now indoor label car urge puppy basic opinion brisk patrol category neck patient dune build inquiry exchange happy desert eight addict pelican abandon vast";
        //5SJXOVEI3Q5CQZGVJBG2CZCWN6WICAYWULRDGS5V5LHRJRO6QJJTNTKF6U
        String userMnemonic = "combine fox urban badge category asthma price grain milk upgrade identify saddle day birth grain clerk borrow damp stage conduct donkey bronze ostrich abandon thought";

        // declare application state storage (immutable)
        int localInts = 1;
        int localBytes = 1;
        int globalInts = 1;
        int globalBytes = 0;

        // user declared approval program (initial)
        String approvalProgramSourceInitial = "#pragma version 5\n" +
        "txn ApplicationID\n" +
        "bz handle_createapp\n" +
        "txn OnCompletion\n" +
        "int NoOp\n" +
        "==\n" +
        "bnz handle_noop\n" +
        "int 0\n" +
        "return\n" +
        
        "handle_createapp:\n" +
        "int 1\n" +
        "return\n" +
        
        "handle_noop:\n" +
        "txn ApplicationArgs 0\n" +
        "byte \"payme\"\n" +
        "==\n" +
        "assert\n" +
        "itxn_begin\n" +
        "int pay\n" +
        "itxn_field TypeEnum\n" +
        "int 5000\n" +
        "itxn_field Amount\n" +
        "txn Sender\n" +
        "itxn_field Receiver\n" +
        "itxn_submit\n" +
        "int 1\n" +
        "return";

        // declare clear state program source
        String clearProgramSource = "#pragma version 5\n" +
        "int 1\n";

        try {
            // Create an algod client
            if( client == null ) this.client = connectToNetwork();

            // get accounts from mnemonic
            Account creatorAccount = new Account(creatorMnemonic);
            Account userAccount = new Account(userMnemonic);
        
            // compile programs
            String approvalProgram = compileProgram(client, approvalProgramSourceInitial.getBytes("UTF-8"));
            String clearProgram = compileProgram(client, clearProgramSource.getBytes("UTF-8"));

            // create new application
            Long appId = createApp(client, creatorAccount, new TEALProgram(approvalProgram), new TEALProgram(clearProgram), globalInts, globalBytes, localInts, localBytes);
            List<byte[]> appArgs = new ArrayList<byte[]>();
            Address es = Address.forApplication(appId);
            Response < TransactionParametersResponse > resp = client.TransactionParams().execute();
            if (!resp.isSuccessful()) {
                throw new Exception(resp.message());
            }
            TransactionParametersResponse params = resp.body();
            Transaction txn = Transaction.PaymentTransactionBuilder()
            .sender(creatorAccount.getAddress())
            .amount(10000000) // 1 algo = 1000000 microalgos
            .receiver(es)
            .suggestedParams(params)
            .build();

            SignedTransaction signedTxn = creatorAccount.signTransaction(txn);
            System.out.println("Signed transaction with txid: " + signedTxn.transactionID);
            byte[] encodedTxBytes = Encoder.encodeToMsgPack(signedTxn);
            Response < PostTransactionsResponse > rawtxresponse = client.RawTransaction().rawtxn(encodedTxBytes).execute();
            if (!rawtxresponse.isSuccessful()) {
                throw new Exception(rawtxresponse.message());
            }
            String id = rawtxresponse.body().txId;
            // Wait for transaction confirmation
            waitForConfirmation(id);
            System.out.println("Transaction " + id + " confirmed" );
            String pm = new String("payme");
            appArgs.add(pm.getBytes()); 
            // call application without arguments
            callApp(client, userAccount, appId, appArgs);
        
        } catch (Exception e) {
            System.err.println("Exception raised: " + e.getMessage());
        }
    }

    public static void main(String args[]) throws Exception {
        CreateContract t = new CreateContract();
        t.statefulSmartContract();
    }
}