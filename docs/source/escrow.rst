===========
Escrow
===========

This example is part of the smart contracts presented in [AB+18POST]_.

Assume Alice wants to buy an item from Bob. Since they do not trust
each other, they would like to use a contract to ensure that Bob will
get paid *if and only if* Alice gets her item.

Assume that the needed amount to pay Bob is stored in an actual
transaction redeemable by Alice. We model that transaction in the
editor using a *fake coinbase transaction* ``A_funds``.

.. code-block:: btm
		
    // Alice's public key	
    const kApub = pubkey:03ff41f23b70b1c83b01914eb223d7a97a6c2b24e9a9ef2762bf25ed1c1b83c9c3

    // tx with Alice's funds, redeemable with Alice's private key
    transaction A_funds {
        input = _ 
        output = 1 BTC: fun(x). versig(kApub; x)
    }

``kApub`` is the public key of Alice and ``versig(kApub; x)`` checks
that ``x`` is a valid signature for ``kApub``.
Assuming that only Alice owns the corresponding private part
of ``kApub``, she is the only one able to spend ``A_funds``.


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
		output = 1 BTC: fun(x, y). versig(kApub, kBpub; x, y)
	}


Transaction ``T`` redeems transaction ``A_funds`` using the
signature ``sig(kA)``, and locks the money so that it can be redeemed
only with the joint signatures of both Alice and Bob. 
In order to spend ``T``, either Alice will send her signature to Bob or viceversa.

Consider the following parametric transaction:

.. code-block:: btm

   transaction T1(sigA:signature, sigB:signature, pubK:pubkey)  {
		input = T: sigA sigB
		output = 1 BTC: fun(x). versig(pubK; x)
   }

It takes the signatures ``sigA`` and ``sigB`` of both Alice and Bob, and 
a public key ``pubK`` used in the output script.
Rationally, Alice will use ``pubK == kApub`` while Bob ``pubK == kBpub``. 

The protocol proceeds as follows: if Alice received her item, she sends
her signature to Bob; if the item has not arrived or has not been
shipped, Bob can send his signature to Alice and she gets back her money.

Alice compute the signature as follows:

.. code-block:: btm

	// signature of T1 that send the money to Bob		
	const sigA = sig(kA) of T1(_,_,kBpub)

Similarly Bob does the same:

.. code-block:: btm
   
	// signature of T1 that send the money to Alice		
	const sigB = sig(kB) of T1(_,_,kApub)

Once only one of the participant receives the signature,
he creates a transaction that spends ``T``.

Suppose Alice received Bob's signature. She completes ``T1`` as follows:

.. code-block:: btm

	const sigB = sig:<hex string made by Bob>[kBpub]
	const sigA = sig(kA) of T1(_,_,kApub)

	compile T1(sigA, sigB, kApub)

Otherwise, if Bob received Alice's signature:

.. code-block:: btm

	const sigA = sig:<hex string made by Alice>[kApub]
	const sigB = sig(kB) of T1(_,_,kBpub)

	compile T1(sigA, sigB, kBpub)

This approach assume that the two participant are honest and they
will send their signature to the other party.
However this is unrealistic: consider the case in which Alice has created
the transaction ``T`` but Bob decided both to not sell the item and to not
refund her. Alice has freezed her bitcoins forever.


--------------------
Arbitrated  contract
--------------------

The protocol seen so far has a dangerous vulnerability: it is secure
only if both participants are honest.  Indeed, either Alice might refuse
to send her signature after receiving the item, hence causing Bob to lose
money; or Bob might refuse to send his one while not sending the item,
so causing Alice to lose the money. In both cases, the bitcoins stored
within transaction ``T`` are lost.

A possible solution to this problem is to entitle a third participant the
role of arbiter, trusted by both Alice and Bob, to decide in case of problems.
Indeed, transaction ``T`` is modified into a *2-of-3* multi signature schema:

.. code-block:: btm

	// Carl's public key
	const kCpub = pubkey:02ede655785dacac6d6985588f6558be2d318012ee36067d3227871d350678c132

	transaction T {
		input = A_funds: sig(kA)
		output = 1 BTC: fun(x, y). versig(kApub, kBpub, kCpub; x, y)
	}

Transaction ``T`` can be redeemed either with the signatures of Alice and
Bob, or with the ones of Alice and the arbiter, or with the ones of
Bob and the arbiter.	
In case of dispute, the arbiter (Carl) will send his signature either to Alice or Bob.

For example, assume he decided to refund Alice. 
In this case, she can instantiate Carl's signature and create the transaction ``T_A``
to get her bitcoins back, as follows:

.. code-block:: btm	

	const sigC = sig:<hex string made by Carl> [kCpub]	

	transaction T_A {
		input = T: sig(kA) sigC
		output = 1 BTC: fun(x). versig(kApub; x)
	}
