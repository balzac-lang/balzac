===========
Escrow
===========

This example is part of the smart contracts presented in [AB+18POST]_.

Assume Alice wants to buy an item from Bob. Since they do not trust
each other, they would like to use a contract to ensure that Bob will
get paid *if and only if* Alice gets her items.

Assume that the needed amount to pay Bob is stored in an actual
transaction redeemable by Alice. We model that transaction in the
editor using a *fake coinbase transaction* ``A_funds``.

.. code-block:: btm
		
    // Alice's public key	
    const kApub = pubkey:03ff41f23b70b1c83b01914eb223d7a97a6c2b24e9a9ef2762bf25ed1c1b83c9c3

    // tx with Alice's funds, redeemable with Alice's private key
    transaction A_funds {input = _ output = 1BTC: fun(x). versig(kApub; x)}


Transaction ``A_funds`` has exactly one output script, which is a
simple P2PKH (Pay to Public Key Hash) for the public key ``kApub`` of
Alice.


----------------
Simple contract
----------------
In a naive attempt to realise a secure contract,  Alice generates the following transaction ``T``:

.. code-block:: btm
		
	// Alice's private key
	const kA = key:cSthBXr8YQAexpKeh22LB9PdextVE1UJeahmyns5LzcmMDSy59L4
	// Alice's public key	
	const kApub = pubkey:03ff41f23b70b1c83b01914eb223d7a97a6c2b24e9a9ef2762bf25ed1c1b83c9c3
	// Bob's public key
	const kBpub = pubkey:03a5aded4cfa04cb4b49d4b19fe8fac0b58802983018cdd895a28b643e7510c1fb
	
	transaction T {
		input = A_funds: sig(kA)
		output = 1BTC: fun(x, y). versig(kApub, kBpub; x, y)
	}


Transaction ``T`` redeems transaction ``A_funds`` using the
signature ``sig(kA)``, and locks the money so that it can be redeemed
only with the joint signatures of both Alice and Bob.

Then, the protocol follows as this. There are other two transactions
which will redeem the sum in ``T`` depending on what happens:
transaction ``T_B`` grants the sum to Bob in case the item has been
received correctly, while transaction ``T_A`` allows Alice to be
refund in case the item has not arrived or has not been shipped.
Both transactions needs to be signed by both participants to redeem ``T``.

So, in case Alice receives the item, she signs
transaction ``T_B`` (that grants the sum to Bob) and sends that
signature to Bob. After also Bob has signed ``T_B``, the transaction
can be appended to the network.

.. code-block:: btm

   transaction T_B(sigA:signature, sigB:signature)  {
	input = T: sigA sigB
        output = 1BTC: fun(x). versig(kBpub; x)
   }

We model ``T_B`` as a parametric transaction, that takes in input two signatures.
Once the signatures have been exchanged, ``T_B`` can be compiled, for instance:

.. code-block:: btm

   //actual signature of T_B made by Alice plus Alice's public key		
   const _sigA = sig:<hex string made by A>[kApub]
   //actual signature of T_B made by Bob plus Bob's public key		
   const _sigB = sig:<hex string made by Bob>[kBpub]

   compile T_B(_sigA, _sigB)


Otherwise, in case Alice does not receive any item, she and Bob will
sign transacton ``T_A`` to allow Alice to be refund.

.. code-block:: btm

   transaction T_B(sigA:signature, sigB:signature)  {
	input = T: sigA sigB
        output = 1BTC: fun(x). versig(kApub; x)
   }

Similarly to the previous transaction, also transaction ``T_B`` is
parametric and can be compiled only once it has received the signatures of 
Alice and Bob.



--------------------
Arbitrated  contract
--------------------

The protocol seen so far has a dangerous vulnerability: it is secure
only if both participants are extremely  honest.  Indeed, either Alice might refuse
to sign ``T_B`` after receiving the item, hence causing Bob to lose
money; or Bob might refuse to sign ``T_A`` while not sending the item,
so causing Alice to lose the money. In both cases, the bitcoin stored
within transaction ``T`` are lost.

A possible solution to this problem is to entitle a third participant the
role of arbiter, to decide in case of problems.  Indeed, transaction ``T`` is
modified into a *2-of-3* multi signature schema:

.. code-block:: btm

	//Carl's public key
	const kCpub = pubkey:02ede655785dacac6d6985588f6558be2d318012ee36067d3227871d350678c132

	transaction T {
		input = A_funds: sig(kA)
		output = 1BTC: fun(x, y). versig(kApub, kBpub, kCpub; x, y)
	}

Transaction ``T`` can be redeemed either with the signatures of Alice and
Bob,  or with the ones of Alice and the arbiter, or with the ones of
Bob and the arbiter.
	

.. code-block:: btm	

	transaction T_B (sig1:signature, sig2:signature) {
		input = T: sig1 sig2
		output = 1BTC: fun(x). versig(kBpub; x)
	}

	transaction T_A (sig1:signature, sig2:signature) {
		input = T: sig1 sig2
		output = 1BTC: fun(x). versig(kApub; x)
	}
		

