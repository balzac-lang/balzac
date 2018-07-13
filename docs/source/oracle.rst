======
Oracle
======

This example is part of the smart contracts presented in [AB+18POST]_.

In many concrete scenarios one would like to make the execution of a contract
depend on some real-world events, e.g. results of football matches for a betting
contract, or feeds of flight delays for an insurance contract. However, 
the evaluation of Bitcoin scripts can not depend on the environment, so in these scenarios
one has to resort to a trusted third-party, or oracle, who notifies real-world
events by providing signatures on certain transactions.

For example, assume that Alice wants to transfer 1 BTC to Bob only if a certain
event, notified by the oracle Oscar, happens. To do that, Alice puts on the blockchain
the transaction ``T``  which can be redeemed by a transactions carrying
the signatures of both Bob and Oscar. 
Further, Alice instructs the oracle to provide her
signature to Bob upon the occurrence of the expected event.

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

Then, Alice creates the transaction ``T`` that spends ``A_funds``:

.. code-block:: btm

    // Alice's private key
    const kA = key:cSthBXr8YQAexpKeh22LB9PdextVE1UJeahmyns5LzcmMDSy59L4
    // Oscar's public key
    const kOpub = pubkey:029c5f6f5ef0095f547799cb7861488b9f4282140d59a6289fbc90c70209c1cced
    // Bob's public key
    const kBpub = pubkey:03859a0f601cf485a72ec097fddd798c694b0257f69f0229506f8ea923bc600c5e

    transaction T {
        input = A_funds: sig(kA)
        output = 1 BTC: fun(sigB, sigO). versig(kBpub, kOpub; sigB, sigO)
    }

Transaction ``T`` can be redeemed providing both the signatures of Bob and
Oscar, respectively ``sigB`` and ``sigO``. For example, Bob creates the transaction ``T1`` as follows:

.. code-block:: btm

    const kB = key:cQmSz3Tj3usor9byskhpCTfrmCM5cLetLU9Xw6y2csYhxSbKDzUn

    transaction T1(sigO) {
        input = T: sig(kB) sigO
        output = 1 BTC: fun(x). versig(kB; x)
    }

The parametric transaction ``T1`` must be completed with Oscar's signature.
Oscar computes a valid signature as follows:

.. code-block:: btm
    
    // Oscar's private key
    const kO = key:cPCE8spaGuXbp4JEDR4G16hL47SP2GavdgWoDTaqQGCvNbdxZdeT
    // the signature to send to Bob
    const sigO = sig(kO) of T1(_)

Once Bob receives ``sigO``, he computes ``T1(sigO)`` and spends the transaction ``T``.

To conclude, oracles like the one needed in this contract are available as
services in the Bitcoin ecosystem.
Notice that, in case the event certified by Oscar never happens, the bitcoins
within ``T`` are **frozen forever**.

------------------
Timeout constraint
------------------

In the previous case, if the event certified by the oracle never happens, the bitcoins
within ``T`` are frozen forever.
To solve this problem, Alice would like to take back her bitcoins after a given deadline.
In order to do this, Alice can add a time constraint
to the output script of ``T`` (we call this new one ``Ttimed``), as shown below:

.. code-block:: btm

    const dateD = 2018-12-31

    transaction Ttimed {
        input = A_funds: sig(kA)
        output = 1 BTC: fun(sigma, sigO). versig(kBpub, kOpub; sigma, sigO)
                        || checkDate dateD : versig(kApub;sigma)
    }

After the end of the year, Alice can redeem ``Ttimed``, since the output script
enables the second part of the or expression.
