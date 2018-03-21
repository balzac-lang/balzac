===========
Transaction
===========

.. highlight:: btm

|langname| provides a simple syntax to express Bitcoin transactions,
which is summarized as follows:

.. code-block:: btm

	transaction txName {
		input = redeemedTx@inputNumber : witness
		output = value: script
	}

Let's break down the syntax through a series of examples. 
The following is an example of a coinbase transaction.

.. code-block:: btm

	transaction T {
		input = _
		output = 50 BTC: fun(x) . x==42
	}

Since ``T`` is a coinbase, it generates new bitcoins. As a consequence, its input field is empty (denoted by ``_``).
The transaction has an output of ``50 BTC``, and its script specifies that it can be redeemed by
whoever can provide a value equal to 42 (so, anyone).

Now we will take advantage of our advanced knowledge of transactions and redeem ``T``.
To do so, we create ``T1`` as follows.

.. code-block:: btm

	transaction T1 {
		input = T: 42
		output = [
			20 BTC: fun(x) . x!=0
			30 BTC: fun(x) . x == x*2
			]
	}

``T1`` redeems ``T``, providing 42 as witness. The witness is evaluated against the script in the output of ``T``,
i.e. :code:`fun(x) . x==42`. But, differently from the previous example, the transaction splits the bitcoins
it redeems between two outputs. The first contains 20 BTC and can be redeemed whit a witness different from 0,
while the other contains 30 BTC and can be redeemed solving a simple equation.
