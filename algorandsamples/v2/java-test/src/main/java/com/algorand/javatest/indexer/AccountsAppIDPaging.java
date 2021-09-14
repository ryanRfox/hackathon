package com.algorand.javatest.indexer;

import com.algorand.algosdk.v2.client.common.IndexerClient;

import com.algorand.algosdk.v2.client.model.AccountsResponse;
import com.algorand.algosdk.v2.client.common.Response;
import com.algorand.algosdk.v2.client.common.Client;

import org.json.JSONObject;
import org.json.JSONArray;

public class AccountsAppIDPaging {
   
    public Client indexerInstance = null;
    // utility function to connect to a node
    private Client connectToNetwork() {
        final String INDEXER_API_ADDR = "localhost";
        final int INDEXER_API_PORT = 8981;
        IndexerClient indexerClient = new IndexerClient(INDEXER_API_ADDR,INDEXER_API_PORT); 
        return indexerClient;
    }
    public static void main(String args[]) throws Exception {
        AccountsAppIDPaging ex = new AccountsAppIDPaging();
        IndexerClient indexerClientInstance = (IndexerClient) ex.connectToNetwork();        
        Long app_id = Long.valueOf(15974179);
        String nexttoken = "";
        Integer numaccounts = 1;
        Long limit = Long.valueOf(2);                   
        // loop until there are no more accounts in the response
        // for the limit (100 is default, with max limit of 1000 per account request)     
        Response<AccountsResponse> response = indexerClientInstance
            .searchForAccounts()
            .applicationId(app_id)
            .limit(limit).execute();  
          
        while (numaccounts > 0) {
          if (!response.isSuccessful()){
                throw new Exception(response.message());  
          }
          else{                
                JSONObject jsonObj = new JSONObject(response.body().toString());
                JSONArray jsonArray = (JSONArray) jsonObj.get("accounts");
                numaccounts = jsonArray.length();
                if (numaccounts > 0) {
                    nexttoken = jsonObj.get("next-token").toString();
                    JSONObject jsonObjAll = new JSONObject(response.body().toString());
                    System.out.println("Account Info for Application ID : " + jsonObjAll.toString(2)); // pretty print json
                    // note, if nexttoken is an empty string this call will fail
                    response = indexerClientInstance
                        .searchForAccounts()
                        .applicationId(app_id)
                        .next(nexttoken)
                        .limit(limit).execute();    
                }
            } 

        }
       
    }
 }