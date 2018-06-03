================
Timed Commitment
================

Assume that Alice wants to choose a secret ``s``, and reveal it after some time --
while guaranteeing that the revealed value corresponds to the chosen secret (or paying
a penalty otherwise). This can be obtained through a timed commitment, a
protocol with applications e.g. in gambling games, where the secret
contains the player move, and the delay in the revelation of the secret is intended
to prevent other players from altering the outcome of the game. 

Intuitively, Alice starts by exposing the hash of the secret, i.e. ``h = H(s)``, and at
the same time depositing some amount ``deposit`` in a transaction. The participant Bob
has the guarantee that after a date ``deadline``, 
either he will know the secret ``s``, or he will be able to redeem the ``deposit``.

Firstly, we present this protocol from Alice's point of view, that creates two transactions:
``T_commit`` commits the hash of her secret; ``T_reveal`` is a transaction that spends it
revealing the secret.

Then we present Bob's point of view. Bob wants to read the secret (no transaction is needed)
or he will publish a new transaction ``T_timeout`` that spends ``T_commit``.

------------
Alice's view
------------

Alice starts defining some constants: 

- ``fee`` is the miner's fee, i.e. the amount of bitcoins earned by the node that will mine the transaction; 
- ``secret`` and ``h`` are the Alice's secret and its hashed value respectively; 
- ``deposit`` is the amount of bitcoins that Alice will lose if she will not reveal her secret; 
- ``deadline`` is the deadline for revealing her secret.

.. code-block:: btm

	// some constants
	const fee = 0.00113 BTC     // miner's fee
	const secret = "42"         // Alice's secret
	const h = hash:73475cb40a568e8da8a045ced110137e159f890ac4da883b6b17dc651b3a8049   // hash of the secret - sha256(secret)
	const deposit = 10 BTC      // Alice's deposit
	const deadline = 2018-06-11 // deadline to reveal the secret

Funds
^^^^^
The timed commitment requires Alice to own an amount of money to use as a deposit.
In |langname|, one can use real funds or simulate them via *fake coinbase transactions*.

For example, assume that there exists a transaction ``A_funds`` on the blockchain and that 
it has exactly one output script, which is a simple P2PKH (Pay to Public Key Hash)
for the public key ``kApub`` of Alice.

.. code-block:: btm

	// Alice's public key
	const kApub = pubkey:03ff41f23b70b1c83b01914eb223d7a97a6c2b24e9a9ef2762bf25ed1c1b83c9c3

It is possible to refer the **real** transaction ``A_funds`` declaring:

- ``const A_funds = txid:TRANSACTION_ID`` or
- ``const A_funds = tx:TRANSACTION-HEX``.

However, if we don't have a real transaction, we can create a *fake coinbase transaction* that behaves in the same way.
This transaction cannot be really published on the blockchain, but it is useful to check if the transactions
we are building are correct.

.. code-block:: btm

	// tx with Alice's funds, redeemable with kA
	transaction A_funds { input = _ output = deposit: fun(sigma) . versig(kApub; sigma)}

Now Alice owns a ``deposit``, stored in ``A_funds``, that she can pawn for the timed commitment protocol.

Commit and reveal
^^^^^^^^^^^^^^^^^

Once the transaction ``A_funds`` is defined, either real or not,
Alice can create the transaction ``T_commit`` that spends her deposit.
To do so, Alice use the private key ``kA`` (from which ``kApub`` is
derived) to create a valid signature ``sig(kA)``. This signature
is set as witness in ``T_commit``.

The transaction ``T_commit`` looks like:

.. code-block:: btm

	// Alice's private key
	const kA = wif:cSthBXr8YQAexpKeh22LB9PdextVE1UJeahmyns5LzcmMDSy59L4

	// Bob's public key
	const kBpub = pubkey:03a5aded4cfa04cb4b49d4b19fe8fac0b58802983018cdd895a28b643e7510c1fb

	transaction T_commit {
	    input = A_funds: sig(kA)
	    output = deposit - fee:
	        fun(x,s:string) . sha256(s) == h && versig(kApub;x)
	            || after date deadline : versig(kBpub;x)
	}

Within this transaction Alice is committing the hash of the chosen secret:
indeed, ``h`` is encoded within the output script of the transaction.
This transaction can be redeemed either by Alice by revealing her secret,
or by Bob, but only after the ``deadline`` has passed.
This constraint is encoded in the script with the expression :btm:`after date deadline : ...`.

Once the transaction ``T_commit`` is on the blockchain, 
Alice chooses whether to reveal the secret, or do nothing. 
In the first case, she can create the transaction ``T_reveal`` and put it on the blockchain.
Since it redeems  ``T_commit`` , she needs to provide the ``secret`` and her signature, 
so making the former public.

.. code-block:: btm

	transaction T_reveal {
	    input =  T_commit: sig(kA) secret
	    output = deposit - fee*2: fun(x) . versig(kA;x)
	}

We can compile Alice's transactions as follows.

.. code-block:: btm

	compile T_commit T_reveal

To sum up, the whole file is:

.. code-block:: btm
	
	// some constants
	const fee = 0.00113 BTC     // miner's fee
	const secret = "42"         // Alice's secret
	const h = hash:73475cb40a568e8da8a045ced110137e159f890ac4da883b6b17dc651b3a8049   // hash of the secret - sha256(secret)
	const deposit = 10 BTC      // Alice's deposit
	const deadline = 2018-06-11 // deadline to reveal the secret

	// Alice's private key
	const kA = wif:cSthBXr8YQAexpKeh22LB9PdextVE1UJeahmyns5LzcmMDSy59L4
	
	// Alice's public key
	const kApub = pubkey:03ff41f23b70b1c83b01914eb223d7a97a6c2b24e9a9ef2762bf25ed1c1b83c9c3

	// Bob's public key
	const kBpub = pubkey:03a5aded4cfa04cb4b49d4b19fe8fac0b58802983018cdd895a28b643e7510c1fb

	// tx with Alice's funds, redeemable with kA
	transaction A_funds { input = _ output = deposit: fun(sigma) . versig(kApub; sigma)}

	transaction T_commit {
	    input = A_funds: sig(kA)
	    output = deposit - fee:
	        fun(x,s:string) . sha256(s) == h && versig(kApub;x)
	            || after date deadline : versig(kBpub;x)
	}

	transaction T_reveal {
	    input =  T_commit: sig(kA) secret
	    output = deposit - fee*2: fun(x) . versig(kA;x)
	}

	compile T_commit T_reveal


----------
Bob's view
----------

Bob waits that ``T_reveal`` is appended to the blockchain: if this happen within the deadline, 
he can learn Alice's ``secret`` by inspecting the witness of ``T_reveal``. 
Otherwise, he redeems Alice's deposit by appending the transaction ``T_timeout``, specified below.

Once Alice publishes ``T_commit``, Bob can construct ``T_timeout`` in the event she does not reveal her secret.
Bob needs:

- the serialized transaction ``T_commit``;
- the output script of the transaction ``T_commit``.

The first condition is quite obvious, since we need to specify which transaction is ``T_timeout`` spending.
One can specify ``T_commit`` as follows:

.. code-block:: btm

	const T_commit = tx:02000000010bb...    // specify the transaction body

Note that ``T_commit`` is public on the blockchain.

The second condition is more sneaky.
The output script of the transaction ``T_commit`` is encoded as a P2SH (Pay to Script Hash)
since it contains complex expressions. It means that ``T_commit`` stores
**only the hash of the script** and, in order to spend it, 
**the redeeming transaction must pass the corresponding script as witness**.

In |langname| it is possible to specify the script enclosed within square brackets,
e.g. ``[fun(x) . x == 42]``, alongside the witnesses.

The example below shows how to create Bob's ``T_timeout`` transaction.

.. code-block:: btm
	
	// some constants
	const fee = 0.00113 BTC     // miner's fee
	const deposit = 10 BTC      // Alice's deposit
	const deadline = 2018-06-11 // deadline to reveal the secret
	const h = hash:73475cb40a568e8da8a045ced110137e159f890ac4da883b6b17dc651b3a8049   // hash of Alice's secret

	// Alice's commit transaction
	const T_commit = tx:02000000010bbd1756430fdd65b55f02f135a1d657ef5742f4b0ae3f1aed10baedd53c5b20000000006b483045022100ef81428e14f58cf6bcf34bd169b2ebcfc90611aac00c900ec30ad9eea9792051022029870f1cc257e08b52db93339423451d2a2288e8aa4376137ff7f5795d75a3f9012103ff41f23b70b1c83b01914eb223d7a97a6c2b24e9a9ef2762bf25ed1c1b83c9c3ffffffff019810993b0000000017a914904be77bfb6521b19e7d7712a5214c61c951f1668700000000

	// Alice's public key
	const kApub = pubkey:03ff41f23b70b1c83b01914eb223d7a97a6c2b24e9a9ef2762bf25ed1c1b83c9c3

	// Bob's public key
	const kBpub = pubkey:03a5aded4cfa04cb4b49d4b19fe8fac0b58802983018cdd895a28b643e7510c1fb

	// Bob's private key
	const kB = wif:cQtkW1zgFCckRYvJ2Nm8rryV825GyDJ51qoJCw72rhHG4YmGfYgZ

	transaction T_timeout {
	    input = T_commit: sig(kB) "" [fun(x,s:string) . sha256(s) == h && versig(kApub;x) || after date deadline : versig(kBpub;x)]
	    output = deposit - fee*2: fun(x) . versig(kB;x)
	    timelock = after date deadline
	}

	compile T_timeout


