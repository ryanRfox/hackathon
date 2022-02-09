package com.algorand.javatest.assets;

import com.algorand.algosdk.account.Account;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Scanner;
import com.algorand.algosdk.v2.client.common.AlgodClient;
import com.algorand.algosdk.v2.client.model.*;
import org.json.JSONArray;
import org.json.JSONObject;
import com.algorand.algosdk.v2.client.common.*;
import com.algorand.algosdk.crypto.Address;
import com.algorand.algosdk.transaction.SignedTransaction;
import com.algorand.algosdk.transaction.Transaction;
import com.algorand.algosdk.util.Encoder;
// import java.nio.file.Files;
// import java.nio.file.Paths;
import com.algorand.algosdk.util.CryptoProvider;
import com.algorand.algosdk.v2.client.Utils;



// see ASA param conventions here: https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0003.md

public class GettingStartedASASecurityTokens {

    static Scanner scan = new Scanner(System.in);
    public String DISPENSER = "HZ57J3K46JIJXILONBBZOHX6BKPXEM2VVXNRFSUED6DKFD5ZD24PMJ3MVA";
    public AlgodClient client = null;

    // Create Account
    public Account createAccount(boolean dispense) throws Exception {
        try {
            Account myAccount1 = new Account();
            System.out.println("My Address: " + myAccount1.getAddress());
            System.out.println("My Passphrase: " + myAccount1.toMnemonic());
            if (dispense) {
                System.out.println("Navigate to this link and dispense TestNet Algos:  https://dispenser.testnet.aws.algodev.network/?account="
                        + myAccount1.getAddress().toString());
                System.out.println("PRESS ENTER KEY TO CONTINUE...");
                scan.nextLine();
            }

            return myAccount1;
            // Copy off account and mnemonic
            // Dispense TestNet Algos to account:
            // https://dispenser.testnet.aws.algodev.network/
            // resource:
            // https://developer.algorand.org/docs/features/accounts/create/#standalone
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Account creation error " + e.getMessage());
        }
    }

    private static final String SHA256_ALG = "SHA256";

    public static byte[] digest(byte[] data) throws NoSuchAlgorithmException {
        CryptoProvider.setupIfNeeded();
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance(SHA256_ALG);
        digest.update(Arrays.copyOf(data, data.length));
        return digest.digest();
    }
    // Create Asset
    public Long createFTAsset(Account alice) throws Exception {
        System.out.println(String.format(""));
        System.out.println(String.format("==> CREATE ASSET"));        
        if (client == null)
            this.client = connectToNetwork();

        // get changing network parameters for each transaction
        Response<TransactionParametersResponse> resp = client.TransactionParams().execute();
        if (!resp.isSuccessful()) {
            throw new Exception(resp.message());
        }
        TransactionParametersResponse params = resp.body();
        if (params == null) {
            throw new Exception("Params retrieval error");
        }
        JSONObject jsonObj = new JSONObject(params.toString());
        System.out.println("Algorand suggested parameters: " + jsonObj.toString(2));

        // Create the Asset:

        boolean defaultFrozen = false;
        String unitName = "ALICECOI";
        String assetName = "Alice's Coins@arc3";
        String url = "https://s3.amazonaws.com/your-bucket/yourdata.json";
        Address manager = alice.getAddress();
        Address reserve = alice.getAddress();
        Address freeze = alice.getAddress();
        Address clawback = alice.getAddress();
        // Use actual total > 1 to create a Fungible Token

        // example 1:(fungible Tokens)
        // assetTotal = 10, decimals = 0, result is 10 total actual

        // example 2: (fractional NFT, each is 0.1)
        // assetTotal = 10, decimals = 1, result is 1.0 total actual

        // example 3: (NFT)
        // assetTotal = 1, decimals = 0, result is 1 total actual

        // set quantity and decimal placement
        BigInteger assetTotal = BigInteger.valueOf(999);
        // integer number of decimals for asset unit calculation
        Integer decimals = 0; 
        // read transaction from file
        // System.out.println("Working Directory = " + System.getProperty("user.dir"));
        // byte[] metadataFILE = Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/FT/metadata.json"));    
        CryptoProvider.setupIfNeeded();
        // byte[] assetMetadataHash = digest(metadataFILE); 
        String assetMetadataHashstr = "16efaa3924a6fd9d3a4824799a4ac65d";

        // var metadataJSON = {
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



        Transaction tx = Transaction.AssetCreateTransactionBuilder()
                .sender(alice.getAddress().toString())
                .assetTotal(assetTotal)
                .assetDecimals(decimals)
                .assetUnitName(unitName)
                .assetName(assetName)
                .url(url)
                .metadataHashUTF8(assetMetadataHashstr)
                .manager(manager)
                .reserve(reserve)
                .freeze(freeze)
                .defaultFrozen(defaultFrozen)
                .clawback(clawback)
                .suggestedParams(params).build();

        // Sign the Transaction with creator account
        SignedTransaction signedTxn = alice.signTransaction(tx);
        Long assetID = null;
        try {
            // Submit the transaction to the network
            String[] headers = { "Content-Type" };
            String[] values = { "application/x-binary" };
            // Submit the transaction to the network
            byte[] encodedTxBytes = Encoder.encodeToMsgPack(signedTxn);
            Response<PostTransactionsResponse> rawtxresponse = client.RawTransaction().rawtxn(encodedTxBytes)
                    .execute(headers, values);
            if (!rawtxresponse.isSuccessful()) {
                throw new Exception(rawtxresponse.message());
            }
            String id = rawtxresponse.body().txId;

            // Wait for transaction confirmation
            PendingTransactionResponse pTrx = Utils.waitForConfirmation(client, id, 4);
            System.out.println("Transaction " + id + " confirmed in round " + pTrx.confirmedRound);

            assetID = pTrx.assetIndex;
            System.out.println("AssetID = " + assetID);
            printCreatedAsset(alice, assetID);
            printAssetHolding(alice, assetID);
            return assetID;
        } catch (Exception e) {
            e.printStackTrace();
            return assetID;
        }

    }

    // Transfer funds
    public void transferAlgos(Account alice, Account bob) throws Exception {
        System.out.println(String.format(""));
        System.out.println(String.format("==> TRANSFER ALGO TO BOB"));
        if (client == null)
            this.client = connectToNetwork();

        final String RECEIVER = bob.getAddress().toString();
        try {
            // Construct the transaction
            System.out.println(String.format("Bob's account balance before transaction:"));
            printBalance(bob);
            String SENDER = alice.getAddress().toString();

            Response<TransactionParametersResponse> resp = client.TransactionParams().execute();
            if (!resp.isSuccessful()) {
                throw new Exception(resp.message());
            }
            TransactionParametersResponse params = resp.body();
            if (params == null) {
                throw new Exception("Params retrieval error");
            }
            // System.out.println("Algorand suggested parameters: " + params);
            String note = "Hello World";
            Transaction txn = Transaction.PaymentTransactionBuilder()
                    .sender(SENDER)
                    .note(note.getBytes())
                    // minimum balance 100000, plus 100000 for asset optin,
                    // plus 3000 for 3 tx (optin, transfer, algo closeout) = 203000 microalgos
                    .amount(203000)
                    .receiver(new Address(RECEIVER))
                    .suggestedParams(params).build();

            // Sign the transaction
            SignedTransaction signedTxn = alice.signTransaction(txn);
            System.out.println("Signed transaction with txid: " + signedTxn.transactionID);
            String[] headers = { "Content-Type" };
            String[] values = { "application/x-binary" };
            // Submit the transaction to the network
            byte[] encodedTxBytes = Encoder.encodeToMsgPack(signedTxn);
            Response<PostTransactionsResponse> rawtxresponse = client.RawTransaction().rawtxn(encodedTxBytes)
                    .execute(headers, values);
            if (!rawtxresponse.isSuccessful()) {
                throw new Exception(rawtxresponse.message());
            }
            String id = rawtxresponse.body().txId;

            // Wait for transaction confirmation
            PendingTransactionResponse pTrx = Utils.waitForConfirmation(client, id, 4);
            System.out.println("Transaction " + id + " confirmed in round " + pTrx.confirmedRound);
            // Read the transaction
            // JSONObject jsonObj = new JSONObject(pTrx.toString());
            // System.out.println("Transaction information (with notes): " +
            // jsonObj.toString(2));
            System.out.println("Decoded note: " + new String(pTrx.txn.tx.note));
            System.out.println(
                    String.format("Bob's account balance after transaction w minimum balance of 203000 microalgos:"));
            printBalance(bob);
        } catch (Exception e) {
            System.err.println("Exception when calling algod#transactionInformation: " + e.getMessage());
        }
    }

    // Opt-in
    public void optIn(Account bob, Long assetID) throws Exception {
        System.out.println(String.format(""));
        System.out.println(String.format("==> BOB OPTS IN"));
        // OPT-IN
        // Opt in to Receiving the Asset
        // get changing network parameters for each transaction
        Response<TransactionParametersResponse> resp = client.TransactionParams().execute();
        if (!resp.isSuccessful()) {
            throw new Exception(resp.message());
        }
        TransactionParametersResponse params = resp.body();
        if (params == null) {
            throw new Exception("Params retrieval error");
        }
        // System.out.println("Algorand suggested parameters: " + params);
        // configuration changes must be done by
        // the manager account - changing manager of the asset
        Transaction tx = Transaction.AssetAcceptTransactionBuilder().acceptingAccount(bob.getAddress())
                .assetIndex(assetID).suggestedParams(params).build();
        // The transaction must be signed by the current manager account
        SignedTransaction signedTx = bob.signTransaction(tx);
        // send the transaction to the network and
        try {

            String[] headers = { "Content-Type" };
            String[] values = { "application/x-binary" };
            // Submit the transaction to the network
            byte[] encodedTxBytes = Encoder.encodeToMsgPack(signedTx);
            Response<PostTransactionsResponse> rawtxresponse = client.RawTransaction().rawtxn(encodedTxBytes)
                    .execute(headers, values);
            if (!rawtxresponse.isSuccessful()) {
                throw new Exception(rawtxresponse.message());
            }
            String id = rawtxresponse.body().txId;
            // Wait for transaction confirmation
            PendingTransactionResponse pTrx = Utils.waitForConfirmation(client, id, 4);

            // We list the account information for acct1
            // and check that the asset is no longer exist
            System.out.println("Transaction " + id + " confirmed in round " + pTrx.confirmedRound);

            // We can now list the account information for acct3
            // and see that it can accept the new asset
            System.out.println("Account  = " + bob.getAddress().toString());
            printBalance(bob);
            printAssetHolding(bob, assetID);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    // transfer funds
    public void transferAsset(Account alice, Account bob, Long assetID) throws Exception {
        System.out.println(String.format(""));
        System.out.println(String.format("==> TRANSFER ASSETS FROM ALICE TO BOB"));
        // TRANSFER ASSET
        // Transfer the Asset:
        Response<TransactionParametersResponse> resp = client.TransactionParams().execute();
        if (!resp.isSuccessful()) {
            throw new Exception(resp.message());
        }
        TransactionParametersResponse params = resp.body();
        if (params == null) {
            throw new Exception("Params retrieval error");
        }
        // System.out.println("Algorand suggested parameters: " + params);
        // params.fee = 0 means use suggested fee per byte -
        params.fee = (long) 0;
        BigInteger assetAmount = BigInteger.valueOf(100);

        Transaction tx = Transaction.AssetTransferTransactionBuilder()
                .sender(alice.getAddress())
                .assetReceiver(bob.getAddress())
                .assetAmount(assetAmount)
                .assetIndex(assetID)
                .suggestedParams(params)
                .build();

        // The transaction must be signed by the current manager account
        SignedTransaction signedTx = alice.signTransaction(tx);
        // send the transaction to the network and
        try {

            String[] headers = { "Content-Type" };
            String[] values = { "application/x-binary" };
            // Submit the transaction to the network
            byte[] encodedTxBytes = Encoder.encodeToMsgPack(signedTx);
            Response<PostTransactionsResponse> rawtxresponse = client.RawTransaction().rawtxn(encodedTxBytes)
                    .execute(headers, values);
            if (!rawtxresponse.isSuccessful()) {
                throw new Exception(rawtxresponse.message());
            }
            String id = rawtxresponse.body().txId;
            // Wait for transaction confirmation
            PendingTransactionResponse pTrx = Utils.waitForConfirmation(client, id, 4);

            // We list the account information for acct1
            // and check that the asset is no longer exist
            System.out.println("Transaction " + id + " confirmed in round " + pTrx.confirmedRound);

            // We can now list the account information for acct3
            // and see that it can accept the new asset
            System.out.println("Alice Account = " + alice.getAddress().toString());
            printAssetHolding(alice, assetID);
            printBalance(alice);
            System.out.println("Bob Account = " + bob.getAddress().toString());
            printAssetHolding(bob, assetID);
            printBalance(bob);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public void freezeAsset(Account alice, Account bob, Long assetID) throws Exception {
        System.out.println(String.format(""));
        System.out.println(String.format("==> FREEZE BOB'S ASSETS"));
        // FREEZE ASSET

        Response<TransactionParametersResponse> resp = client.TransactionParams().execute();
        if (!resp.isSuccessful()) {
            throw new Exception(resp.message());
        }
        TransactionParametersResponse params = resp.body();
        if (params == null) {
            throw new Exception("Params retrieval error");
        }
        // System.out.println("Algorand suggested parameters: " + params);
        // params.fee = 0 means use suggested fee per byte -
        // params.fee = (long) 0;

        boolean freezeState = true;
        Transaction tx = Transaction.AssetFreezeTransactionBuilder()
                .sender(alice.getAddress())
                .freezeTarget(bob.getAddress())
                .freezeState(freezeState)
                .assetIndex(assetID)
                .suggestedParams(params)
                .build();
        // The transaction must be signed by the current manager account
        SignedTransaction signedTx = alice.signTransaction(tx);
        // send the transaction to the network and
        try {

            String[] headers = { "Content-Type" };
            String[] values = { "application/x-binary" };
            // Submit the transaction to the network
            byte[] encodedTxBytes = Encoder.encodeToMsgPack(signedTx);
            Response<PostTransactionsResponse> rawtxresponse = client.RawTransaction().rawtxn(encodedTxBytes)
                    .execute(headers, values);
            if (!rawtxresponse.isSuccessful()) {
                throw new Exception(rawtxresponse.message());
            }
            String id = rawtxresponse.body().txId;
            // Wait for transaction confirmation
            PendingTransactionResponse pTrx = Utils.waitForConfirmation(client, id, 4);

            // We list the account information for acct1
            // and check that the asset is no longer exist
            System.out.println("Transaction " + id + " confirmed in round " + pTrx.confirmedRound);

            System.out.println("Alice Account = " + alice.getAddress().toString());
            printAssetHolding(alice, assetID);
            printBalance(alice);
            System.out.println("Bob Account = " + bob.getAddress().toString());
            printAssetHolding(bob, assetID);
            printBalance(bob);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public void revokeAsset(Account alice, Account bob, Long assetID) throws Exception {
        System.out.println(String.format(""));
        System.out.println(String.format("==> REVOKE (CLAWBACK) BOB'S ASSETS"));
        // REVOKE ASSET

        Response<TransactionParametersResponse> resp = client.TransactionParams().execute();
        if (!resp.isSuccessful()) {
            throw new Exception(resp.message());
        }
        TransactionParametersResponse params = resp.body();
        if (params == null) {
            throw new Exception("Params retrieval error");
        }

        BigInteger assetAmount = BigInteger.valueOf(100);
        Transaction tx = Transaction.AssetClawbackTransactionBuilder()
        .sender(alice.getAddress())
                .assetClawbackFrom(bob.getAddress())
                .assetReceiver(alice.getAddress())
                .assetAmount(assetAmount)
                .assetIndex(assetID)
                .suggestedParams(params).build();

        // The transaction must be signed by the current manager account
        SignedTransaction signedTx = alice.signTransaction(tx);
        // send the transaction to the network and
        try {

            String[] headers = { "Content-Type" };
            String[] values = { "application/x-binary" };
            // Submit the transaction to the network
            byte[] encodedTxBytes = Encoder.encodeToMsgPack(signedTx);
            Response<PostTransactionsResponse> rawtxresponse = client.RawTransaction().rawtxn(encodedTxBytes)
                    .execute(headers, values);
            if (!rawtxresponse.isSuccessful()) {
                throw new Exception(rawtxresponse.message());
            }
            String id = rawtxresponse.body().txId;
            // Wait for transaction confirmation
            PendingTransactionResponse pTrx = Utils.waitForConfirmation(client, id, 4);

            // We list the account information for acct1
            // and check that the asset is no longer exist
            System.out.println("Transaction " + id + " confirmed in round " + pTrx.confirmedRound);

            System.out.println("Alice Account = " + alice.getAddress().toString());
            printAssetHolding(alice, assetID);
            printBalance(alice);
            System.out.println("Bob Account = " + bob.getAddress().toString());
            printAssetHolding(bob, assetID);
            printBalance(bob);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    // transfer back from Bob to Alice and closeout asset
    public void closeoutAssetHoldings(Account bob, Account alice, Long assetID, BigInteger assetAmount) throws Exception {
        // System.out.println(String.format(""));
        // System.out.println(String.format("==> ATTEMPT TO TRANSFER ASSETS BACK FROM BOB TO ALICE"));   
        // ATTEMP TO TRANSFER ASSETS BACK TO ALICE FROM BOB
        // THIS SHOULD FAIL for Amount > 0 
        // TRANSFER ASSET

        Response<TransactionParametersResponse> resp = client.TransactionParams().execute();
        if (!resp.isSuccessful()) {
            throw new Exception(resp.message());
        }
        TransactionParametersResponse params = resp.body();
        if (params == null) {
            throw new Exception("Params retrieval error");
        }
        // System.out.println("Algorand suggested parameters: " + params);
        // BigInteger assetAmount = BigInteger.valueOf(100000);

        Transaction tx = Transaction.AssetTransferTransactionBuilder()
                .sender(bob.getAddress())
                .assetReceiver(alice.getAddress())
                .assetAmount(assetAmount)
                .assetIndex(assetID)
                .assetCloseTo(alice.getAddress())
                .suggestedParams(params).build();

        // The transaction must be signed by the current manager account
        SignedTransaction signedTx = bob.signTransaction(tx);
        // send the transaction to the network and
        try {

            String[] headers = { "Content-Type" };
            String[] values = { "application/x-binary" };
            // Submit the transaction to the network
            byte[] encodedTxBytes = Encoder.encodeToMsgPack(signedTx);
            Response<PostTransactionsResponse> rawtxresponse = client.RawTransaction().rawtxn(encodedTxBytes)
                    .execute(headers, values);
            if (!rawtxresponse.isSuccessful()) {
                throw new Exception(rawtxresponse.message());
            }
            String id = rawtxresponse.body().txId;
            // Wait for transaction confirmation
            PendingTransactionResponse pTrx = Utils.waitForConfirmation(client, id, 4);

            // We list the account information for acct1
            // and check that the asset is no longer exist
            System.out.println("Transaction " + id + " confirmed in round " + pTrx.confirmedRound);

            // We can now list the account information for acct3
            // and see that it can accept the new asset
            System.out.println("Account  sender= " + bob.getAddress().toString());
            printAssetHolding(bob, assetID);
            printBalance(bob);
            System.out.println("Account  receiver= " + alice.getAddress().toString());
            printAssetHolding(alice, assetID);
            printBalance(alice);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    // destroy account
    public void destroyFTAsset(Account alice, Long myAssetID) throws Exception {
        System.out.println(String.format(""));
        System.out.println(String.format("==> DESTROY ASSET"));  
        if (client == null)
            this.client = connectToNetwork();

        // DESTROY

        // Destroy the Asset:
        // All assets should be in creators account
        // get changing network parameters for each transaction
        Response<TransactionParametersResponse> resp = client.TransactionParams().execute();
        if (!resp.isSuccessful()) {
            throw new Exception(resp.message());
        }
        TransactionParametersResponse params = resp.body();
        if (params == null) {
            throw new Exception("Params retrieval error");
        }
        // JSONObject jsonObj = new JSONObject(params.toString());
        // System.out.println("Algorand suggested parameters: " + jsonObj.toString(2));

        // set destroy asset specific parameters
        // The manager must sign and submit the transaction
        // asset close to
        Transaction tx = Transaction.AssetDestroyTransactionBuilder().sender(alice.getAddress()).assetIndex(myAssetID)
                .suggestedParams(params).build();
        // The transaction must be signed by the manager account
        SignedTransaction signedTxn = alice.signTransaction(tx);
        // send the transaction to the network
        try {
            // Submit the transaction to the network
            String[] headers = { "Content-Type" };
            String[] values = { "application/x-binary" };
            // Submit the transaction to the network
            byte[] encodedTxBytes = Encoder.encodeToMsgPack(signedTxn);
            Response<PostTransactionsResponse> rawtxresponse = client.RawTransaction().rawtxn(encodedTxBytes)
                    .execute(headers, values);
            if (!rawtxresponse.isSuccessful()) {
                throw new Exception(rawtxresponse.message());
            }
            String id = rawtxresponse.body().txId;
            // Wait for transaction confirmation
            PendingTransactionResponse pTrx = Utils.waitForConfirmation(client, id, 4);

            // We list the account information for acct1
            // and check that the asset is no longer exist
            System.out.println("Transaction " + id + " confirmed in round " + pTrx.confirmedRound);
            System.out.println("Account = " + alice.getAddress().toString());
            System.out.println("AssetID destroyed  = " + myAssetID.toString());
            // System.out.println("Closing Amount = " + pTrx.closingAmount.toString());

            // Read the transaction
            // JSONObject jsonObj2 = new JSONObject(pTrx.toString());
            // System.out.println("Transaction information : " + jsonObj2.toString(2));
            String accountInfo = client.AccountInformation(alice.getAddress()).execute().toString();
            JSONObject jsonObj2 = new JSONObject(accountInfo.toString());
            System.out.println("Account information (with assets destroyed) : " + jsonObj2.toString(2));

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

    }

    // closeout Bobs and Alice funds - send back to dispenser
    public void closeoutAlgos(Account alice, Account bob) throws Exception {
        System.out.println(String.format(""));
        System.out.println(String.format("==> CLOSE OUT BOB'S ALGOS TO DISPENSER"));

        if (client == null)
            this.client = connectToNetwork();
        printBalance(bob);
        System.out.println(String.format("Close out Bob's account"));
        closeoutAccountAlgos(bob);
        System.out.println(String.format("Bob's account balance after closeout:"));
        printBalance(bob);

        System.out.println(String.format(""));
        System.out.println(String.format("==> CLOSE OUT ALICE'S ALGOS TO DISPENSER"));

        System.out.println(String.format("Close out Alice's account"));
        closeoutAccountAlgos(alice);
        System.out.println(String.format("Alice's account balance after closeout:"));
        printBalance(alice);

    }

    // closeout funds to faucet
    private void closeoutAccountAlgos(Account closeoutAccount) {
        final String SENDER = closeoutAccount.getAddress().toString();
        try {
            // Construct the transaction
            Response<TransactionParametersResponse> resp = client.TransactionParams().execute();
            if (!resp.isSuccessful()) {
                throw new Exception(resp.message());
            }
            TransactionParametersResponse params = resp.body();
            if (params == null) {
                throw new Exception("Params retrieval error");
            }
            // System.out.println("Algorand suggested parameters: " + params);
            String note = "Return to Dispenser";
            Transaction txn = Transaction.PaymentTransactionBuilder()
                    .sender(SENDER)
                    .note(note.getBytes())
                    .amount(0)
                    .closeRemainderTo(DISPENSER)
                    .receiver(new Address(DISPENSER))
                    .suggestedParams(params).build();

            // Sign the transaction
            SignedTransaction signedTxn = closeoutAccount.signTransaction(txn);
            System.out.println("Signed transaction with txid: " + signedTxn.transactionID);
            String[] headers = { "Content-Type" };
            String[] values = { "application/x-binary" };
            // Submit the transaction to the network
            byte[] encodedTxBytes = Encoder.encodeToMsgPack(signedTxn);
            Response<PostTransactionsResponse> rawtxresponse = client.RawTransaction().rawtxn(encodedTxBytes)
                    .execute(headers, values);
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

        } catch (Exception e) {
            System.err.println("Exception when calling algod#transactionInformation: " + e.getMessage());
        }
    }

    // utility function to connect to a node
    private AlgodClient connectToNetwork() {
        final String ALGOD_API_TOKEN = "2f3203f21e738a1de6110eba6984f9d03e5a95d7a577b34616854064cf2c0e7b";
        final String ALGOD_API_ADDR = "https://academy-algod.dev.aws.algodev.network";
        final Integer ALGOD_PORT = 443;
        AlgodClient client = new AlgodClient(ALGOD_API_ADDR, ALGOD_PORT, ALGOD_API_TOKEN);
        return client;
    }

    // utility function to print created asset
    public void printCreatedAsset(Account account, Long assetID) throws Exception {
        if (client == null)
            this.client = connectToNetwork();
        // String myAddress = account.getAddress().toString();
        Response<com.algorand.algosdk.v2.client.model.Account> respAcct = client
                .AccountInformation(account.getAddress()).execute();
        if (!respAcct.isSuccessful()) {
            throw new Exception(respAcct.message());
        }
        com.algorand.algosdk.v2.client.model.Account accountInfo = respAcct.body();
        JSONObject jsonObj = new JSONObject(accountInfo.toString());
        JSONArray jsonArray = (JSONArray) jsonObj.get("created-assets");
        if (jsonArray.length() > 0)
            try {
                for (Object o : jsonArray) {
                    JSONObject ca = (JSONObject) o;
                    Integer myassetIDInt = (Integer) ca.get("index");
                    if (assetID.longValue() == myassetIDInt.longValue()) {
                        System.out.println("Created Asset Info: " + ca.toString(2)); // pretty print
                        break;
                    }
                }
            } catch (Exception e) {
                throw (e);
            }
    }

    // utility function to print asset holding
    public void printAssetHolding(Account account, Long assetID) throws Exception {
        if (client == null)
            this.client = connectToNetwork();

        // String myAddress = account.getAddress().toString();
        Response<com.algorand.algosdk.v2.client.model.Account> respAcct = client
                .AccountInformation(account.getAddress()).execute();
        if (!respAcct.isSuccessful()) {
            throw new Exception(respAcct.message());
        }
        com.algorand.algosdk.v2.client.model.Account accountInfo = respAcct.body();
        JSONObject jsonObj = new JSONObject(accountInfo.toString());
        JSONArray jsonArray = (JSONArray) jsonObj.get("assets");
        if (jsonArray.length() > 0)
            try {
                for (Object o : jsonArray) {
                    JSONObject ca = (JSONObject) o;
                    Integer myassetIDInt = (Integer) ca.get("asset-id");
                    if (assetID.longValue() == myassetIDInt.longValue()) {
                        System.out.println("Asset Holding Info: " + ca.toString(2)); // pretty print
                        break;
                    }
                }
            } catch (Exception e) {
                throw (e);
            }
    }

 

    // Print Balance for Account
    private void printBalance(Account myAccount) throws Exception {

        Response<com.algorand.algosdk.v2.client.model.Account> respAcct = client
                .AccountInformation(myAccount.getAddress()).execute();
        if (!respAcct.isSuccessful()) {
            throw new Exception(respAcct.message());
        }
        com.algorand.algosdk.v2.client.model.Account accountInfo = respAcct.body();
        System.out.println(String.format("Account Balance: %d microAlgos for account %S", accountInfo.amount,
                accountInfo.address.toString()));

    }

    public static void main(String args[]) throws Exception {
        GettingStartedASASecurityTokens t = new GettingStartedASASecurityTokens();

        System.out.println(String.format(""));
        System.out.println(String.format("==> CREATE ALICE'S ACCOUNT"));
        Account Alice = t.createAccount(true); // Alice
        System.out.println(String.format(""));
        System.out.println(String.format("==> CREATE BOB'S ACCOUNT"));
        // Bob created w 0 Algos
        Account Bob = t.createAccount(false); 

        // Use Alice account as a faucet to fund Bob
        // fund with 202000 to cover min=100000, plus 100000 for asset,
        // plus 3000 for three transactions (optin , transfer asset, close out account)
        t.transferAlgos(Alice, Bob);
        Long assetID = t.createFTAsset(Alice); // Alice creates Asset

        // Bob Opts in
        t.optIn(Bob, assetID);
        // Alice transfers asset to Bob
        t.transferAsset(Alice, Bob, assetID);

        t.freezeAsset(Alice, Bob, assetID);
        // try to transaction
        try{
            System.out.println(String.format(""));
            System.out.println(String.format("==> ATTEMP TO TRANSFER ASSETS BACK TO ALICE FROM BOB"));
            System.out.println(String.format("==> THIS SHOULD FAIL AS BOB'S ACCOUNT IS FROZEN"));            
            t.closeoutAssetHoldings(Bob, Alice, assetID, BigInteger.valueOf(100));
        }
        catch (Exception e) {
            System.err.println("Exception when calling transfer from frozen account: " + e.getMessage());
        }
        System.out.println(String.format(""));
        System.out.println(String.format("==> TRANSFER NOT ALLOWED WHEN FROZEN"));

        t.revokeAsset(Alice, Bob, assetID);
        //  close out Bobs holding account
        System.out.println(String.format(""));
        System.out.println(String.format("==> BOB CLOSEOUT ASSETS TO ALICE"));
        t.closeoutAssetHoldings(Bob, Alice, assetID, BigInteger.valueOf(0));

        // Asset can be only de destroyed if Alice (creator) has all the Assets
        t.destroyFTAsset(Alice, assetID);
        t.closeoutAlgos(Alice, Bob);
    }
}
