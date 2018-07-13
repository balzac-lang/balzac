============
Transactions
============

.. highlight:: btm

|langname| provides a simple syntax to express Bitcoin transactions,
which is summarized as follows:

.. code-block:: btm

    transaction txName {
        input = [redeemedTx@outputIndex : witness; ...]
        output = [value: script; ...]
        abslock = ...
        relLock = ...
    }

------
Inputs
------

The ``input`` field of a transactions specifies which outputs it redeems. 
The field contains a list of transaction outputs, which are pairs ``transaction@outputIndex``, denoted by the list delimiters ``[...]``.
Each transaction output is followed by ``: witness``, which can be any expression (see :doc:`expressions`), but commonly contains a signature.

.. Hint::
    When specifying the inputs of a transaction, if ``@outputIndex`` is omitted, the output index is assumed to be 0.


-------
Outputs
-------

The ``output`` field of a transactions specifies its ``productions``.
The field contains a list of out-points,  which are pairs ``value@script``, denoted by the list delimiters ``[...]``.
Each script is a boolean function in the form ``fun(x1,..., xn) . expression`` (see :doc:`expressions`).



.. Hint:: 
   When writing a transaction with only one input and/or one output, the list delimiters ``[...]`` can be omitted.

-------
AbsLock
-------

The field :btm:`absLock` allow to specify when a transaction will be valid.

The time can be expressed in two ways:

*   | :btm:`absLock = block N`
    | where ``N`` an expression of type :btm:`int` representing the **block number** at which the transaction will be valid
    |

*   | :btm:`absLock = date D`
    | where ``D`` an expression of type :btm:`int` representing the **date** (in seconds from ``1970-01-01``) at which the transaction will be valid.

The expression ``N`` and ``D`` are subject to the same constraints of :ref:`label_abslock_exp`.

Refer to :ref:`Dates and Delays <label_date_delays>` for convenient ways for expressing dates.

-------
RelLock
-------

The field :btm:`relLock` allow to specify when a transaction will be valid.

The time can be expressed in two ways:

*   | :btm:`relLock = N block from T`
    | where ``N`` and ``T`` have type respectively :btm:`int` and :btm:`transaction` representing the **number of blocks from T** at which the transaction will be valid
    |

*   | :btm:`relLock = D from T`
    | where ``D`` and ``T`` have type respectively :btm:`int` and :btm:`transaction` representing the **seconds from T** at which the transaction will be valid

The expression ``N`` and ``D`` are subject to the same constraints of :ref:`label_rellock_exp`,
while the expression ``T`` must evaluate to one of the input transaction.

Refer to :ref:`Dates and Delays <label_date_delays>` for convenient ways for expressing delays.
