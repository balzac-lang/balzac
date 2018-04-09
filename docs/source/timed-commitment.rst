----------------
Timed Commitment
----------------

Assume that Alice wants to choose a secret ``s``, and reveal it after some time --
while guaranteeing that the revealed value corresponds to the chosen secret (or paying
a penalty otherwise). This can be obtained through a timed commitment, a
protocol with applications e.g. in gambling games, where the secret
contains the player move, and the delay in the revelation of the secret is intended
to prevent other players from altering the outcome of the game. 

Intuitively, Alice starts by exposing the hash of the secret, i.e. ``h = H(s)``, and at
the same time depositing some amount ``deposit`` in a transaction. The participant Bob
has the guarantee that after the date ``dateD``, 
he will either know the secret s, or he will be able to redeem ``deposit BTC``.

The transactions of the protocol are shown below, 
where create a fictional transaction ``A_funds`` redeemable by Alice.
	
.. literalinclude:: examples/tc.btm
   :language: btm
   :linenos:
..   :emphasize-lines: 12,15-18

Alice starts by putting the transaction ``T_commit`` on the blockchain. Note
that within this transaction Alice is committing the hash of the chosen secret:
indeed, ``h`` is encoded within the script of the output of the transaction.
This transaction can be redeemed either by Alice by revealing the secret,
or by Bob, but only when the date ``dateD`` has passed.
This constraint is encoded in the script with the expression ``after date dateD : ...``.

After ``T_commit`` appears on the blockchain,
Alice chooses whether to reveal the secret, or do nothing. 
In the first case, she must put the transaction ``T_reveal`` on the blockchain.
Since it redeems  ``T_commit`` , she needs to write in its witness both the secret ``s`` 
and her signature, so making the former public.

In this smart contract, Bob waits for the ``T_commit`` to appear in the blockchain.
If, after date ``dateD``, Alice has not published ``T_reveal`` yet, Bob can
proceed to put ``T_timeout`` on the blockchain, writing his own signatures in the
witness. Otherwise, Bob retrieves ``T_reveal`` from the blockchain, from which he can
obtain the secret.