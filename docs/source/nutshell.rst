=========================
|langname| in a nutshell
=========================

.. highlight:: btm

BALZaC (for Bitcoin Abstract Language, Analyzer and Compilter)
is domain-specific language to write Bitcoin transactions,
based on the paper [AB+18POST]_.
|langname| features a simple syntax to express Bitcoin transactions.
We illustrate  it through a series of examples, that you can experiment with in the `online editor <http://blockchain.unica.it/balzac/>`_.


.. _label_t_modeling:

"""""""""""""""""""""
A basic transaction 
"""""""""""""""""""""

Bitcoin transactions transfer currency, the *bitcoins* (BTC).
Each transaction has one or more inputs, from where it takes the bitcoins,
and one or more outputs, which specify the recipient(s).
|langname| also allows for  transactions  with  no inputs:
even thought these transactions cannot be appended *as is* to the actual
Bitcoin blockchain, they are useful to  refer to transactions which are
not known at specification time. 
An example of transaction with no inputs is the following:


.. code-block:: btm

    transaction T {
        input = _    // no input 
        output = 50 BTC: fun(x) . x==42
    }

The output field of transaction ``T`` contains a value, ``50 BTC``, and 
an *output script*,  ``fun(x) . x==42``.
This means that  50 bitcoins will  be transferred to any transaction
which provides a *witness*  ``x``  such that ``x == 42``.

To append ``T`` to the Bitcoin blockchain,
the placeholder ``_`` for the input must be replaced with the identifier
of an unspent transaction already on the blockchain,
which has at least 50 BTC.  

You can use the `web editor <http://blockchain.unica.it/balzac/>`_  to write
|langname| transactions,   to check their syntax, and to compile them  into
actual Bitcoin  transactions.
The output of the compiler is a serialized transaction for the Bitcoin
test network (testnet).
To generate transactions for the main network (mainnet), one must specify the network as follows:  

.. code-block:: btm

    network mainnet  // default is testnet


For instance, let us paste transaction ``T`` into the editor and then let us add command ``eval T`` to it. 
Now, if we hit the button [Compile], the web editor shows in the output box the transaction ``T``  in  Bitcoin (testnet) serialization format.

.. figure:: _static/img/compiling_t.png
    :scale: 100 %
    :class: img-border
    :align: center

The serialized transaction can  be sent to the Bitcoin network using the Bitcoin client command ``bitcoin-cli sendrawtransaction``.
Before sending it, the unspecified input must be substituted
by an actual transaction identifier, as in the following example:

.. code-block:: btm

        //actual Bitcoin transaction identifier 
        const T_ref = txid:<actualBitcoinIdentifier>

        transaction T_alt {
               input = T_ref: _
               output = 50 BTC: fun(x) . x==42
        }

.. Tip ::

    Transaction identifiers have the same format *both in the testnet and
    in the mainnet*. Our tool fetches the transaction body
    by looking into the specified :btm:`network`. 
    A common mistake is to look for a transaction id into the wrong
    network.

    Alternatively, you can fetch the body of a transaction on your
    own and use it as in the following example:

    .. code-block:: btm

        const T_ref = tx:0200000001644bbaf0....c00000000


    Please note the difference between prefixes ``tx:`` and ``txid:``.

.. _label_transaction_redeeming:

"""""""""""""""""""""""""""""""
Redeeming a transaction
"""""""""""""""""""""""""""""""
If one needs to use the bitcoin stored within  ``T``, she  can
redeem it with the following transaction: 

.. code-block:: btm

    transaction T1 {
        input = T: 42
        output = 50 BTC: fun(x). x != 0  // any constraint chosen by the user
    }

Transaction ``T1`` redeems  ``T`` by indicating it  in the  ``input`` field,
and by providing the number 42 as *witness*. 
The value 42 is the actual parameter which  replaces the formal parameter ``x`` in the  output script :code:`fun(x) . x == 42`,  and makes the script evaluate to true.
Any other witness would make the script evaluate to false,
and would prevent the transaction ``T1`` from being added to the blockchain. 
A transaction cannot be spent twice:
hence, once ``T1`` is on the blockchain,
no other transaction having ``T`` as input can be appended.

Note that ``T1`` is redeeming exactly the ``50 BTC`` deposited in ``T``:
in practice, to be able to append ``T1`` to the blockchain,
the value in output of a transaction must be strictly less
than the value in input.
The difference is retained by Bitcoin miners as a fee for their work.
Currently, transactions with zero fee are not likely to be added to the blockchain. 

Now, let us insert both ``T`` and ``T1`` in the editor.  While we
write, the editor performs some static checks and signals the
errors. For instance, if instead of the value ``42`` we provide another
witness for ``T`` (say for instance value ``4``), the editor will
display a warning. If the input field of ``T1`` has a wrong reference
(say ``T3``), or if the total amount of outgoing bitcoins is greater
than the incoming one, the editor will signal the error.

.. _label_t_signature_modeling:

"""""""""""""""""""""""""""""""
Signature verification 
"""""""""""""""""""""""""""""""

The output scripts of ``T`` and ``T1`` are  naive,
since anyone can produce the right witnesses.
Usually, one wants to transfer bitcoins to a specific user.
For instance, the following transaction ``T2``  makes the 50 BTC of  ``T1``
redeemable only by user Alice: 

.. code-block:: btm

    // Alice's address
    const addrA = address:mpkcxdWqT8WVeiWzMKBQosn5t8LMYL7Z3c 

    transaction T2 {
        input = T1: 12
        output = 50 BTC: fun(x) . versig(addrA; x)
    }


The constant ``addrA`` declares Alice's *address*:
basically, it is the hash of Alice's public key.
The address is prefixed by the keyword ``address:`` to indicate its type.
The format is *wif* :doc:`Wallet Import Format types <types>` [#f1]_.
Users may generate as many addresses as they want.

The :ref:`predicate <label_c_functions>` ``versig(addrA; x)``
in the output script of ``T2`` is true  if ``x`` is a valid signature
of the transaction which redeems ``T3``, 
done with Alice's private key. 

The transaction ``T2`` can be redeemed by a transaction ``T3`` made as follows:

.. code-block:: btm

    // Alice's address
    const addrA = address:mpkcxdWqT8WVeiWzMKBQosn5t8LMYL7Z3c
    //Alice's private key    
    const kA = key:cQu93pLnEtyhkEMUxiRHP2ocPXi1LRbnZZ3PLz2gp6yu11tWKUaW

    transaction T3 {
        input = T2: sig(kA)
        output = 50 BTC: fun(x) . versig(addrA; x) // any condition chosen by Alice
    }

The witness ``sig(kA)`` is the :ref:`signature <label_c_functions>`
of transaction ``T3`` (without considering the witness itself)
using the private key ``kA``.

You can use the online form on the sidebar to generate new addresses and keys.


.. figure:: _static/img/sidebar.png
    :scale: 100 %
    :class: img-border
    :align: center  

.. _label_t1_modeling:

"""""""""""""""""""""""""""""""
Multiple inputs and outputs
"""""""""""""""""""""""""""""""
Transactions can have more than one output, in order to split the money on different recipients. 
For instance, the amount of bitcoins in ``T4`` is split in two parts: 

.. code-block:: btm

    //Alice's private key
    const kA = key:cQu93pLnEtyhkEMUxiRHP2ocPXi1LRbnZZ3PLz2gp6yu11tWKUaW
    // Alice's address
    const addrA = address:mpkcxdWqT8WVeiWzMKBQosn5t8LMYL7Z3c 
    //Alice's other address
    const addrA2 = address:n3A4KGgZD9bW6k2pPccN4rUfX3CgYCPERb

    transaction T4 {
        input = T3:sig(kA) 
        output = [
                         40 BTC: fun(x) . versig(addrA; x);
                         10 BTC: fun(x) . versig(addrA2; x)
            ]
    }


In this transaction, the output field has two items, that can be redeemed separately. 

Transactions can have more than one input, in case they need to gather money from several sources.
For each input, the transaction must provide a suitable witness. In case inputs refer to a transaction with multiple outputs, their outputs are numbered starting from 0. 
For instance:

.. code-block:: btm

    // Alice's address
    const addrA = address:mpkcxdWqT8WVeiWzMKBQosn5t8LMYL7Z3c 
        //Alice's private key
    const kA = key:cQu93pLnEtyhkEMUxiRHP2ocPXi1LRbnZZ3PLz2gp6yu11tWKUaW
    //Alice's second private key
    const kA2= key:cNzPt3Wad4ymq15AZ2omAmmSv5DBe99pRgsUBCQoeFPeeP57VJkm

    transaction T5 {
        input = [
            T4@0: sig(kA);
            T4@1: sig(kA2)
            ]
        output = 50 BTC: fun(x) . versig(addrA; x)
    }

which calculates  the signature of  transaction ``T5``
using the private key ``k``.   (see :ref:`function list <label_c_functions>` ). 


"""""""""""""""""""""""
Parametric transactions
"""""""""""""""""""""""
Transaction definition can be parametric.
For instance, in the following example ``T6`` takes one parameter
of type ``pubkey`` and uses it in the output script.


.. code-block:: btm
        
    // parametric transaction
    transaction T6(k:pubkey) {
        input = _
        output = 1BTC: fun(x). versig(k;x)
    }

To be able to evaluate ``T6``, one must instantiate that one parameter, like:
    
.. code-block:: btm
        
    // Alice's public key
    const kApub = pubkey:037d33fad6067e7a76671be01f697c7667d81be0aef334385cdab2b6b8f9f484c1    
    eval T6(kApub)

One can also use T6 in the definition of its redeeming transaction, as follows:
    
.. code-block:: btm

    // Alice's public key
    const kApub = pubkey:037d33fad6067e7a76671be01f697c7667d81be0aef334385cdab2b6b8f9f484c1
    //Alice's private key
    const kA = key:cQu93pLnEtyhkEMUxiRHP2ocPXi1LRbnZZ3PLz2gp6yu11tWKUaW
    // Bob's public key
    const kBpub = pubkey:03a5aded4cfa04cb4b49d4b19fe8fac0b58802983018cdd895a28b643e7510c1fb
    
    transaction T7 {
        input = T6(kApub):sig(kA)
        output = 1BTC: fun(x). versig(kBpub;x)
    }

In case the parameter is a witness, it can be left unspecified as long
as it is needed, using the symbol ``_``. For instance, transaction
``T9`` is obtained by ``T8``, without providing a witness :

.. code-block:: btm

    transaction T8(s:signature, n:int) {
        input = T7:s 
        output = 1BTC: fun(x, m). versig(kApub;x) && m == sha256( n )
    }
    //transaction with empty signature
    const T9 = T8(_, 4)


The generation of a signature inside a transaction is done at
compilation time, so that all the parameters have been instantiated.
Indeed:    
    
.. code-block:: btm

    transaction T9_bis(n:int) {
        input = T6(kApub):sig(kA)
        output = 1BTC: fun(x, m). versig(kBpub;x) && m == sha256( n )
    }
    //sig(kA) is calculated now
    eval T9_bis(4)


.. rubric:: References

.. [#f1] https://bitcoin.org/en/glossary/wallet-import-format

