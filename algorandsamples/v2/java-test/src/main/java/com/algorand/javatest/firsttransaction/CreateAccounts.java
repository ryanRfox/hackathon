package com.algorand.javatest.firsttransaction;
import com.algorand.algosdk.account.Account;
import java.util.Scanner;

public class CreateAccounts
 { 
    static Scanner scan = new Scanner(System.in);
    public static void main(String args[]) throws Exception {
        try {
            Account myAccount1 = new Account();
            System.out.println("My Address1: " + myAccount1.getAddress());
            System.out.println("My Passphrase1: " + myAccount1.toMnemonic());
            Account myAccount2 = new Account();
            System.out.println("My Address2: " + myAccount2.getAddress());
            System.out.println("My Passphrase2: " + myAccount2.toMnemonic());
            Account myAccount3 = new Account();
            System.out.println("My Address3: " + myAccount3.getAddress());
            System.out.println("My Passphrase3: " + myAccount3.toMnemonic()); 

            System.out.println("Navigate to this link:  https://dispenser.testnet.aws.algodev.network/");             
            System.out.println("Copy and paste this TestNet Account Address to Dispense Funds: "); 
            System.out.println(myAccount1.getAddress().toString()); 
            System.out.println("Copy and paste this TestNet Account Address to Dispense Funds: "); 
            System.out.println(myAccount2.getAddress().toString()); 
            System.out.println("Copy and paste this TestNet Account Address to Dispense Funds: "); 
            System.out.println(myAccount3.getAddress().toString()); 
            pressEnterKeyToContinue();
            // Copy off accounts and mnemonics
            // Dispense TestNet Algos to each account:
            // https://dispenser.testnet.aws.algodev.network/
            // resource:
            // https://developer.algorand.org/docs/features/accounts/create/#standalone
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

    }
    private static void pressEnterKeyToContinue()
    { 
        System.out.println("PRESS ENTER KEY TO CONTINUE...");     
        scan.nextLine();
    }  
 }