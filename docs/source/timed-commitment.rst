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
has the guarantee that after the date ``deadline``, 
he will either know the secret ``s``, or he will be able to redeem the ``deposit``.

.. literalinclude:: examples/tc.btm
   :language: btm
   :linenos:
..   :emphasize-lines: 12,15-18


The example above starts defining some constants: ``fee`` is the miner's fee, i.e. the amount
of Bitcoin earned by the node that will mine the transaction; ``secret`` and ``h`` are the Alice's
secret and its hashed value respectively; ``deposit`` is the amount of Bitcoin that Alice will lose 
if she will not reveal her secret; ``deadline`` is the date within which she will have to reveal her secret.

------------
Alice's view
------------

The constant ``kA`` and ``kApub`` are a valid key pair of Alice, while ``kBpub`` is Bob's
public key.

Firstly, we need a transaction that Alice can spend.
``A_funds`` is a fictional coinbase transaction that creates ``10 BTC`` redeemable
by anyone able to provide a valid signature for the public key ``kApub``. We assume that only Alice knows
``kA`` and can redeem ``A_funds``.

Alice starts by putting the transaction ``T_commit`` on the blockchain, that redeems ``A_funds``.
Note that within this transaction Alice is committing the hash of the chosen secret:
indeed, ``h`` is encoded within the script of the output of the transaction.
This transaction can be redeemed either by Alice by revealing her secret,
or by Bob, but only when the ``deadline`` has passed.
This constraint is encoded in the script with the expression :btm:`after date deadline : ...`.

After ``T_commit`` appears on the blockchain,
Alice chooses whether to reveal the secret, or do nothing. 
In the first case, she must put the transaction ``T_reveal`` on the blockchain.
Since it redeems  ``T_commit`` , she needs to provide the secret ``s`` and her signature, 
so making the former public.

----------
Bob's view
----------

In this smart contract, Bob waits for the ``T_commit`` to appear in the blockchain.
If Alice has not published ``T_reveal`` after the ``deadline``, Bob can
proceed to put ``T_timeout`` on the blockchain, using his own signatures.
Otherwise, Bob retrieves ``T_reveal`` from the blockchain, from which he can
obtain Alice's secret ``s``.