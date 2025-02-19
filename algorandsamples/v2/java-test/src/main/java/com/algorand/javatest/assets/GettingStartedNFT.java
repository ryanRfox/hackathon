package com.algorand.javatest.assets;

import com.algorand.algosdk.account.Account;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
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
import com.algorand.algosdk.util.CryptoProvider;
import com.algorand.algosdk.v2.client.Utils;

// see ASA param conventions here: https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0003.md

public class GettingStartedNFT {
        
  
    // Create Account
    static Scanner scan = new Scanner(System.in);
    public String DISPENSER = "HZ57J3K46JIJXILONBBZOHX6BKPXEM2VVXNRFSUED6DKFD5ZD24PMJ3MVA";

    private static final String SHA256_ALG = "SHA256";

    public static byte[] digest(byte[] data) throws NoSuchAlgorithmException {
        CryptoProvider.setupIfNeeded();
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance(SHA256_ALG);
        digest.update(Arrays.copyOf(data, data.length));
        return digest.digest();
    }

    public Account createAccount() throws Exception {
        try {
            Account myAccount1 = new Account();
            System.out.println("My Address: " + myAccount1.getAddress());
            System.out.println("My Passphrase: " + myAccount1.toMnemonic());
            System.out.println("Navigate to this link:  https://dispenser.testnet.aws.algodev.network/?account="
                    + myAccount1.getAddress().toString());
            System.out.println("Copy TestNet Account Address to Dispense Funds to: ");
            System.out.println(myAccount1.getAddress().toString());
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
            throw new Exception("Account creation error " + e.getMessage());
        }
    }

    public AlgodClient client = null;

    // utility function to connect to a node
    private AlgodClient connectToNetwork() {
        final String ALGOD_API_TOKEN = "2f3203f21e738a1de6110eba6984f9d03e5a95d7a577b34616854064cf2c0e7b";
        final String ALGOD_API_ADDR = "https://academy-algod.dev.aws.algodev.network";
        final Integer ALGOD_PORT = 443;
        AlgodClient client = new AlgodClient(ALGOD_API_ADDR, ALGOD_PORT, ALGOD_API_TOKEN);
        return client;
    }
    public Long createNFTAsset(Account aliceAccount) throws Exception {
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
        String unitName = "ALICEART";
        String assetName = "Alice's Artwork@arc3";
        String url = "https://s3.amazonaws.com/your-bucket/metadata.json";
        byte[] imageFile = Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/NFT/alice-nft.png"));
        byte[] imgHash = digest(imageFile); 
        String imgSRI = "sha256-" + Base64.getEncoder().encodeToString(imgHash);
        System.out.println("image_integrity : " + String.format(imgSRI));
        // Use imgSRI as the metadata  for "image_integrity": 
        // "sha256-/tih/7ew0eziEZIVD4qoTWb0YrElAuRG3b40SnEstyk=",

    
        byte[] metadataFILE = Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/NFT/metadata.json"));
        // use this to verify that the metadatahash displayed in the asset creation response is correct
        // cat metadata.json | openssl dgst -sha256 -binary | openssl base64 -A     
        byte[] assetMetadataHash = digest(metadataFILE); 
        // String assetMetadataSRI = Base64.getEncoder().encodeToString(assetMetadataHash);
        // System.out.println(String.format(assetMetadataSRI));

       
        String assetMetadataHashString = "16efaa3924a6fd9d3a4824799a4ac65d";

        // var metadataJSON = {
        //     "name": "ALICEART",
        //     "description": "Alice's Artwork",
        //     "image": "https:\/\/s3.amazonaws.com\/your-bucket\/images\/alice-nft.png",
        //     "image_integrity": "sha256-/tih/7ew0eziEZIVD4qoTWb0YrElAuRG3b40SnEstyk=",
        //     "properties": {
        //         "simple_property": "Alice's first artwork",
        //         "rich_property": {
        //             "name": "AliceArt",
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
        //             "name": "Artwork",
        //             "value": [1, 2, 3, 4],
        //             "class": "emphasis"
        //         }
        //     }
        // }


        Address manager = aliceAccount.getAddress();  // OPTIONAL: FOR DEMO ONLY, USED TO DESTROY ASSET WITHIN THIS SCRIPT
        Address reserve = null;
        Address freeze = null;
        Address clawback = null;
        //
        // For non-fractional NFT ASA simply set the 
        // assetTotal to 1 and decimals to 0
   
        // or
        // A fractional non-fungible token (fractional NFT)
        // has the following properties:

        // Total Number of Units (t) MUST be a power of 10 larger than 1: 10, 100, 1000,
        // ...
        // Number of Digits after the Decimal Point MUST be equal to the
        // logarithm in base 10 of total number of units.
        // In other words, the total supply of the ASA is exactly 1.

        // fractional example for total number of this asset available
        // for circulation 100 units of .01 fraction each.
        // fractional example
        // Integer decimals = 2;
        // BigInteger assetTotal = BigInteger.valueOf(100);

        // Use actual total > 1 to create a Fungible Token

        // example 1:(fungible Tokens)
        // assetTotal = 10, decimals = 0, result is 10 total actual

        // example 2: (fractional NFT, each is 0.1)
        // assetTotal = 10, decimals = 1, result is 1.0 total actual

        // example 3: (NFT)
        // assetTotal = 1, decimals = 0, result is 1 total actual

        // set quantity and decimal placement
        BigInteger assetTotal = BigInteger.valueOf(1);
        // decimals and assetTotal
        Integer decimals = 0;

        Transaction tx = Transaction.AssetCreateTransactionBuilder()
                .sender(aliceAccount.getAddress().toString())
                .assetTotal(assetTotal)
                .assetDecimals(decimals)
                .assetUnitName(unitName)
                .assetName(assetName)
                .url(url)
                .metadataHashUTF8(assetMetadataHashString)
                .manager(manager)
                .reserve(reserve)
                .freeze(freeze)
                .defaultFrozen(defaultFrozen)
                .clawback(clawback)
                .suggestedParams(params)
                .build();

        // Sign the Transaction with creator account
        SignedTransaction signedTxn = aliceAccount.signTransaction(tx);
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
            PendingTransactionResponse pTrx = Utils.waitForConfirmation(client,id,4);          
            System.out.println("Transaction " + id + " confirmed in round " + pTrx.confirmedRound);

            assetID = pTrx.assetIndex;
            System.out.println("AssetID = " + assetID);
            printCreatedAsset(aliceAccount, assetID);
            printAssetHolding(aliceAccount, assetID);
            return assetID;
        } catch (Exception e) {
            e.printStackTrace();
            return assetID;
        }

    }

    public void destroyNFTAsset(Account alice, Long myAssetID) throws Exception {
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
        Transaction tx = Transaction.AssetDestroyTransactionBuilder()
                .sender(alice.getAddress())
                .assetIndex(myAssetID)
                .suggestedParams(params)
                .build();
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
    // send funds back to dispenser
    public void closeoutAccount(Account alice) throws Exception {
        System.out.println(String.format(""));
        System.out.println(String.format("==> CLOSE OUT ALICE'S ALGOS TO DISPENSER"));

        if (client == null)
            this.client = connectToNetwork();

        printBalance(alice);

        try {
            // Construct the transaction
            final String RECEIVER = DISPENSER;
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
                .sender(alice.getAddress().toString())
                .note(note.getBytes())
                .amount(0) // 
                .receiver(alice.getAddress().toString())
                .suggestedParams(params)
                .closeRemainderTo(RECEIVER) 
                .build();
           
            // Sign the transaction
            SignedTransaction signedTxn = alice.signTransaction(txn);
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
            PendingTransactionResponse pTrx = Utils.waitForConfirmation(client, id, 4);

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
            printBalance(alice);

        } catch (Exception e) {
            System.err.println("Exception when calling algod#transactionInformation: " + e.getMessage());
        }
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
        GettingStartedNFT t = new GettingStartedNFT();
        Account aliceAccount = t.createAccount();
        Long assetID = t.createNFTAsset(aliceAccount);
        t.destroyNFTAsset(aliceAccount, assetID);
        t.closeoutAccount(aliceAccount);
    }
}
