package com.example;

import com.example.factory.TransactionFactory;

import it.unica.tcs.lib.ITransactionBuilder;

//import com.example.factory.TransactionFactory;

public class Main {

	
	public static void main(String[] args) {

		ITransactionBuilder t = TransactionFactory.tx_T();
		ITransactionBuilder t1 = TransactionFactory.tx_T1(42);
		ITransactionBuilder t2 = TransactionFactory.tx_T2();
		
		System.out.println(t);
		System.out.println(t1);
		System.out.println(t2);
		
		
		
	}
}
