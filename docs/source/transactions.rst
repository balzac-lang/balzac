======================
Transactions
======================

.. highlight:: btm

|langname| provides a simple syntax to express Bitcoin transactions,
which is summarized as follows:

.. code-block:: btm

	transaction txName {
		input = [redeemedTx@outputIndex : witness; ...]
		output = [value: script; ...]
		timelock = after ...
	}

-------------------
Inputs
-------------------

The ``input`` field of a transactions specifies which outputs it redeems. 
The field contains a list of transaction outputs, which are pairs ``transaction@outputIndex``, denoted by the list delimiters ``[...]``.
Each transaction output is followed by ``: witness``, which can be any expression (see :doc:`expressions`), but commonly contains a signature.

.. Hint::
	When specifying the inputs of a transaction, if ``@outputIndex`` is omitted, the output index is assumed to be 0.


-------------------
Outputs
-------------------

The ``output`` field of a transactions specifies its ``productions``.
The field contains a list of out-points,  which are pairs ``value@script``, denoted by the list delimiters ``[...]``.
Each script is a boolean function in the form ``fun(x1,..., xn) . expression`` (see :doc:`expressions`).



.. Hint:: 
   When writing a transaction with only one input and/or one output, the list delimiters ``[...]`` can be omitted.

-------------------
Timelocks
-------------------

The field ``timelock`` allow to specify when a transaction will be valid. 
The time can be expressed either as block number, with ``timelock = after block 456192``,
or as a date, as in ``timelock = after date 2018-04-09``.
A date can also specify precisely the time, with  ``timelock = after date 2018-04-09T10:15:30``.
The date formats accepted by the language are the ones parsed by `Java DateTime <https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html>`_.

