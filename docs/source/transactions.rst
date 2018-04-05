======================
Transactions
======================

.. highlight:: btm

|langname| provides a simple syntax to express Bitcoin transactions,
which is summarized as follows:

.. code-block:: btm

	transaction txName {
		input = [redeemedTx@inputIndex : witness; ...]
		output = [value: script; ...]
		timelock = timelocks
	}

.. Hint:: 
   When writing a transaction with only one input and/or one output, the list delimiters ``[...]`` can be omitted.

.. Hint::
	When specifying the inputs of a transaction, if ``@inputNumber`` is omitted, the input index is assumed to be 0.
