==================================
Transaction signatures
==================================  

Users interact with  Bitcoin through pseudonyms, which are 
public keys or *addresses* (namely, hashes of public keys).
Users can obtain as many pseudonyms as they want, by 
generating pairs of public/private keys.
Pseudonyms are used in transactions to specify
who receives bitcoins.  
|langname| allows users to generate keys and addresses  through
the  sidebar of the `web editor <http://blockchain.unica.it/btm/>`_.
 
In |langname|,   keys and addresses are typed:
the type is ``pubkey`` for public keys, ``key`` for private keys, and
``address`` for  addresses. 

.. code-block:: btm

	// Alice's public key
	const kApub = pubkey:033806fe039c8d149c8e68f4665b4a479acab773b20bddf7df0df59ba4f0567341
	// Alice's private key
	const kA = key:cUSH4x3Uq9uMgeZGdpTFvr5gVGYcAg4vrTNe9QvWsU8Dq3deym6Z
	// Alice's address
	const addrA = address:my6NmTELHBMVUsAWb34iRoGYDQpcYJvVZV

Within transactions, users certify their identity with the  function :ref:`sig <label_c_functions>`,
and verify other users' identity with the predicate
:ref:`versig <label_c_functions>`.


""""""""""""""""""""
Verifying signatures
""""""""""""""""""""

The :ref:`predicate <label_c_functions>` ``versig(kpub; x)`` takes two parameters: a public key ``kpub`` and the signature ``x`` of the redeeming transaction.
The predicate  is  true if the signature ``x``  has been made with the
private key corresponding to ``kpub``.
For instance, the following transaction transfers 1BTC to a transaction
signed by Alice:

.. code-block:: btm
		
    // Alice's public key
    const kApub = pubkey:033806fe039c8d149c8e68f4665b4a479acab773b20bddf7df0df59ba4f0567341

    // tx redeemable with Alice's private key
    transaction A_funds {input = _ output = 1BTC: fun(x). versig(kApub; x)}


Alternatively,  one can  use an  address instead of a public key. For instance:

.. code-block:: btm
		    
    // Alice's address
    const addrA = address:my6NmTELHBMVUsAWb34iRoGYDQpcYJvVZV

    // tx redeemable with Alice's signature
    transaction T {input = _ output = 1BTC: fun(x). versig(addrA; x)}
    

One can use   ``versig`` to check multiple signatures.
For instance, in the following transaction the predicate ``versig(kApub, kBpub; x, y)`` is true if  ``x`` is  Alice's signature and  ``y`` is Bob's.


.. code-block:: btm
		
	// Alice's public key	
	const kApub = pubkey:03ff41f23b70b1c83b01914eb223d7a97a6c2b24e9a9ef2762bf25ed1c1b83c9c3
	// Bob's public key
	const kBpub = pubkey:03a5aded4cfa04cb4b49d4b19fe8fac0b58802983018cdd895a28b643e7510c1fb

	// tx redeemable with two signatures: Alice's and Bob's
	transaction T {
		input = _
		output = 1BTC: fun(x, y). versig(kApub, kBpub; x, y)
	}

In cases (like the one above) where ``versig`` checks multiple signatures,
one cannot use addresses.

In general, ``versig (lk;ls)`` verifies the list ``ls`` of signatures
against the list ``lk`` of keys.  
The order of elements in these lists matters.
Indeed,  ``versig`` tries  to verify the last signature in ``ls``
with the last key in ``lk``.
If they match, it    verifies  the previous signature in the
list against the previous key;
otherwise it verifies the same signature with the previous
key.

In this way, ``versig``  can model complex  conditions, like
a *2-of-3* multi signature scheme: 

.. code-block:: btm

        // Alice's public key
	const kApub = pubkey:03ff41f23b70b1c83b01914eb223d7a97a6c2b24e9a9ef2762bf25ed1c1b83c9c3
	// Bob's public key
	const kBpub = pubkey:03a5aded4cfa04cb4b49d4b19fe8fac0b58802983018cdd895a28b643e7510c1fb
	//Carl's public key
	const kCpub = pubkey:03bd94ee8e570da8815f5660bab86aca010d950ddfb87458bb0dcafbc8ea6f9657

	transaction T {
		input = _
		output = 1BTC: fun(x, y). versig(kApub, kBpub, kCpub; x, y)
	}


The predicate  ``versig(kApub, kBpub, kCpub; x, y)`` is true
if  ``x`` and ``y``  can match two of the three  keys.
For instance, if  ``sigC`` and ``sigB`` are  Carl's and  Bob's signatures, then
``versig(kApub, kBpub, kCpub; sigB, sigC)`` is true, while
``versig(kApub, kBpub, kCpub; sigC, sigB)`` is false. 



""""""""""""""""""""
Signing transactions
""""""""""""""""""""
Assume we have a transaction ``A_funds``, redeemable with 
a signature made by Alice:

.. code-block:: btm
		
    // Alice's public key
    const kApub = pubkey:033806fe039c8d149c8e68f4665b4a479acab773b20bddf7df0df59ba4f0567341

    // tx redeemable with Alice's private key
    transaction A_funds {input = _ output = 1BTC: fun(x). versig(kApub; x)}


We can redeem ``A_funds`` with a  transaction ``TA`` made as follows:
   
.. code-block:: btm

	//Alice's private key	
	const kA = key:cSFqKAaCUGHZoGDrcreo3saZpMv9NvcVmRZVbVddbodEuzWNCDNt

	transaction TA {
		input = A_funds : sig(kA)               //Alice's signature of TA
		output = 1BTC: fun(x). versig(kApub; x) //any condition 
	}

The value ``sig(kA)`` within the ``input`` field is the signature of Alice
on ``TA``.
The signature applies to all the fields of the transaction *but* the witnesses.
The actual signature is generated when compiling the transaction.

Alternatively, we can use ``sig(kA) of TA`` to generate the signature
outside the transaction:

.. code-block:: btm

	//Alice's private key	
	const kA = key:cSFqKAaCUGHZoGDrcreo3saZpMv9NvcVmRZVbVddbodEuzWNCDNt

	transaction T {
		input = A_funds : _                     // unspecified witness
		output = 1BTC: fun(x). versig(kApub; x) //any condition 
	}

	// Alice's signature of T
	const sigA = sig(kA) of T 

	transaction TA {
		input = A_funds : sigA                          //Alice's signature of T
		output = 1BTC: fun(x). versig(kApub; x)         //any condition 
	}

Note that the witness in ``TA`` is Alice's signature of ``T``:
indeed, the two transactions
have the same signature, since their input and output fields are the same.

The construct ``sig(k) of T`` also applies to parametric transactions.
This is especially useful when the parameter is the witness, like in the
following example:

.. code-block:: btm

	//Alice's private key	
	const kA = key:cSFqKAaCUGHZoGDrcreo3saZpMv9NvcVmRZVbVddbodEuzWNCDNt

	// template for a parametric transaction
	transaction T_template(s:signature) {
		input = A_funds : s 
		output = 1BTC: fun(x). versig(kApub; x) //any condition 
	}

	// signs T_template, without providing an argument
	const sigA = sig(kA) of T_template(_) 

	// instantiates T_template with the needed argument
	const TA  =  T_template(sigA)

The witness in ``T_template`` is a parameter ``s``,
which must be instantiated with Alice's signature.
Alice first signs ``T_template``,
and then she instantiates the parameter of ``T_template`` with her signature.
The obtained transaction ``TA`` can redeem ``A_funds``.

When a transaction needs the signatures of many participants,
each of them signs a template of the transaction,
and sends the signature to a participant who collects them.

For instance, assume that ``T_ABC`` requires the signatures of Alice, Bob and Carl:

.. code-block:: btm
		
    //needs three signatures to redeem  1 bitcoin
    transaction T_ABC{
        input = _
        output = 1BTC: fun(x, y, z). versig(kApub, kBpub, kCpub; x, y, z)
    }	

First, all participants agree on a parametric transaction to redeem ``T_ABC``:
    
.. code-block:: btm

    transaction T_template (sA:signature, sB:signature, sC:signature){
	input = T_ABC: sA sB sC
	output = 1BTC: fun(x). versig(kApub; x)
    }


Then, each participant signs ``T_template``.
For instance, Alice performs the following actions:

.. code-block:: btm
		
      //Alice's private key
      const kA = key:cSthBXr8YQAexpKeh22LB9PdextVE1UJeahmyns5LzcmMDSy59L4

      //Alice's signature
      const sigA = sig(kA) of T_template(_,_,_)
		
      //prints the signature
      eval sigA

   
The compiler outputs a pair, containing the signature and the public key:

.. code-block:: btm
		
    sigA		
    sig:30450...3cdb01 [pubkey:03ff41f...9c3]

Now, all participants send their pair to (say) Alice,
who uses them to instantiate ``T_template`` with the actual signatures:

.. code-block:: btm
		
	//signature of T_template made by Alice plus Alice's public key
	const sigA = sig:304502...b01[kApub]
	//signature of T_template made by Bob plus Bob's public key
	const sigB = sig:956232...c12[kBpub]
	//signature of T_template made by Carl plus Carl's public key
	const sigC = sig:f3h5d6...cdb[kCpub]

	eval T_template(sigA, sigB, sigC)

Finally, the instantiated ``T_template`` can be appended to the blockchain
to redeem ``T_ABC``.
      
    


      
    












