----------------
Oracle
----------------

In many concrete scenarios one would like to make the execution of a contract
depend on some real-world events, e.g. results of football matches for a betting
contract, or feeds of flight delays for an insurance contract. However, 
the evaluation of Bitcoin scripts can not depend on the environment, so in these scenarios
one has to resort to a trusted third-party, or oracle, who notifies real-world
events by providing signatures on certain transactions.

For example, assume that Alice wants to transfer 1 BTC to Bob only if a certain
event, notified by an oracle Oscar, happens. To do that, Alice puts on the blockchain
the transaction ``T``  which can be redeemed by a transactions carrying
the signatures of both Bob and Oscar. 
Further, Alice instructs the oracle to provide his
signature to Bob upon the occurrence of the expected event.

The transactions of the protocol are shown below, 
where create a fictional transaction ``T_A`` redeemable by Alice.
	
.. literalinclude:: examples/oracle.btm
   :language: btm
   :linenos:
..   :emphasize-lines: 12,15-18


In this smart contract, Bob waits to receive the signature ``sigO`` from Oscar, 
then he puts ``T_B`` on the blockchain (after setting its witness) to redeem ``T``. 
In practice, oracles like the one
needed in this contract are available as services in the Bitcoin ecosystem.
Notice that, in case the event certified by the oracle never happens, the bitcoins
within ``T`` are frozen forever. To avoid this situation, one can add a time constraint
to the output script of ``T``, as shown below.


.. code-block:: btm

	transaction T {
		input = TA: sig(kA)
		output = 1BTC: fun(sigma, sigO). versig(kB, kO; sigma, sigO)
						|| after date dateD : versig(kA;sigma)
	}