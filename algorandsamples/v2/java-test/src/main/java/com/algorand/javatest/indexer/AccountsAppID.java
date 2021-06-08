// AccountsAppID.java
// requires java-algorand-sdk 1.4.0 or higher (see pom.xml)
package com.algorand.javatest.indexer;

import com.algorand.algosdk.v2.client.common.IndexerClient;

import com.algorand.algosdk.v2.client.model.AccountsResponse;
import com.algorand.algosdk.v2.client.common.Response;
import com.algorand.algosdk.v2.client.common.Client;

import org.json.JSONObject;

public class AccountsAppID {
    public Client indexerInstance = null;
    // utility function to connect to a node
    private Client connectToNetwork() {
        
        // TODO  remove    
        // final String INDEXER_API_ADDR = "https://indexer-internal-betanet.aws.algodev.network/";
        // final String INDEXER_TOKEN = "YddOUGbAjHLr1uPZtZwHOvMDmXvR1Zvw1f3Roj2PT1ufenXbNyIxIz0IeznrLbDsF";
        // final int INDEXER_API_PORT = 443;
// https://node-testnet.internal.aws.algodev.network
// YddOUGbAjHLr1uPZtZwHOvMDmXvR1Zvw1f3Roj2PT1ufenXbNyIxIz0IeznrLbDsF
// https://indexer-testnet.internal.aws.algodev.network

        // final String INDEXER_API_ADDR = "https://indexer-testnet.internal.aws.algodev.network";
        // final String INDEXER_TOKEN = "YddOUGbAjHLr1uPZtZwHOvMDmXvR1Zvw1f3Roj2PT1ufenXbNyIxIz0IeznrLbDsF";
        // final int INDEXER_API_PORT = 8980;
        // IndexerClient indexerClient = new IndexerClient(INDEXER_API_ADDR,INDEXER_API_PORT,INDEXER_TOKEN); 

        final String INDEXER_API_ADDR = "localhost";  
        final int INDEXER_API_PORT = 8981;  
        IndexerClient indexerClient = new IndexerClient(INDEXER_API_ADDR,INDEXER_API_PORT); 
        return indexerClient;
    }
    public static void main(String args[]) throws Exception {
        AccountsAppID ex = new AccountsAppID();
        IndexerClient indexerClientInstance = (IndexerClient)ex.connectToNetwork();
        Long app_id = Long.valueOf(15974179);
        Response<AccountsResponse> response = indexerClientInstance
            .searchForAccounts()
            .applicationId(app_id).execute();
        if (!response.isSuccessful()) {
            throw new Exception(response.message());
        }    

        JSONObject jsonObj = new JSONObject(response.body().toString());
        System.out.println("Pretty Print of Accounts for Application: " + jsonObj.toString(2)); // pretty print json         

    }
 }