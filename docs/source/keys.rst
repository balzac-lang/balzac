==================================
Addresses and keys 
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


"""""""""""
Verifying
"""""""""""

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
    

It is possible to use  ``versig`` to check multiple signatures.
For instance, in the following transaction, the output
script ``versig(kApub, kBpub; x, y)``
is true if signature ``x`` has been made by Alice and signature ``y`` has been
made by Bob.



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

Notice that, if the signatures to be checked are more than one, the
address cannot be used, and one must resort to public keys 
(this restriction is due to Bitcoin transaction
format). 

Also, notice that the order in which parameters are passed to ``versig``
matters.  More generally, ``versig`` takes a lists of keys ``lk`` and
a list of expressions ``le``.  Then, it  tries to
verify the last signature in ``le``  with the last key in ``lk``.
If they match, the function  proceeds to verify the previous signature in the
list, otherwise it tries to verify the signature with the previous
key.


Thanks to this behaviour, it is possible to use ``versig`` to model
more subtle conditions, such for instance, a *2-of-3* multi signature
schema. For instance:

.. code-block:: btm

        // Alice's public key
	const kApub = pubkey:03ff41f23b70b1c83b01914eb223d7a97a6c2b24e9a9ef2762bf25ed1c1b83c9c3
	// Bob's public key
	const kBpub = pubkey:03a5aded4cfa04cb4b49d4b19fe8fac0b58802983018cdd895a28b643e7510c1fb
	//Carl's public key
	const kCpub = pubkey:03bd94ee8e570da8815f5660bab86aca010d950ddfb87458bb0dcafbc8ea6f9657

	//versig used to model  a *2-of-3* multi signature schema
	transaction T {
		input = _
		output = 1BTC: fun(x, y). versig(kApub, kBpub, kCpub; x, y)
	}


In this case, output script ``versig(kApub, kBpub, kCpub; x, y)`` evaluates to true
if the two parameters can match two of the three required keys.
For instance, let ``sigC`` be Carl's signature  and ``sigB`` Bob's signature, then
``versig(kApub, kBpub, kCpub; sigB, sigC)`` evaluates to ``true`` but
``versig(kApub, kBpub, kCpub; sigC, sigB)`` does not. 



""""""""
Signing
""""""""

The :ref:`function <label_c_functions>` ``sig`` generates signatures.
There are two constructs for it:

-  ``sig(k)`` is  used inside a transaction and generates the signature of the transaction itself. In this case, the signature is *lazy* and is generated when compiling the transaction.
- ``sig(k) of T`` generates the signature of transaction ``T`` using private key ``k``. It cannot be used inside the definition of a  transaction.


The signing operation signs all the fields of the transaction *but*
the witness fields.  This is necessary not to incurr in
infinite signatures, and it allows multiple signatures to be added
without invalidating previous ones.
   
Consider the following transaction ``A_funds``, redeemable with 
a signature made by Alice.

.. code-block:: btm
		
    // Alice's public key
    const kApub = pubkey:033806fe039c8d149c8e68f4665b4a479acab773b20bddf7df0df59ba4f0567341

    // tx redeemable with Alice's private key
    transaction A_funds {input = _ output = 1BTC: fun(x). versig(kApub; x)}


Let us define transaction ``TA``   to redeem transaction ``A_funds``, like this:
   
.. code-block:: btm

	//Alice's private key	
	const kA = key:cSFqKAaCUGHZoGDrcreo3saZpMv9NvcVmRZVbVddbodEuzWNCDNt

	// defining  transaction TA
	transaction TA {
		input = A_funds : sig(kA)   //signature of the transaction
		output = 1BTC: fun(x). versig(kApub; x) //any condition 
	}

Alternatively, it is possible to use the other construct,  like this:

.. code-block:: btm

	//Alice's private key	
	const kA = key:cSFqKAaCUGHZoGDrcreo3saZpMv9NvcVmRZVbVddbodEuzWNCDNt

	// transaction with witness not specified
	transaction T {
		input = A_funds : _ 
		output = 1BTC: fun(x). versig(kApub; x) //any condition 
	}

	// signing transaction T
	const sigA = sig(kA) of T 

	// defining the actual transaction TA,
	//with the same fields as in T so to use  signature sigA
	transaction TA {
		input = A_funds : sigA 
		output = 1BTC: fun(x). versig(kApub; x) //any condition 
	}


Transaction ``TA`` uses as witness the signature obtained by ``T``,
which has same input and output fields.

Something equivalent can be written with the use of parametric transaction ``T_template``:

.. code-block:: btm

	//Alice's private key	
	const kA = key:cSFqKAaCUGHZoGDrcreo3saZpMv9NvcVmRZVbVddbodEuzWNCDNt

	// template for a parametric transaction
	transaction T_template(s:signature) {
		input = A_funds : s 
		output = 1BTC: fun(x). versig(kApub; x) //any condition 
	}

	// signing transaction T_template, without providing an argument
	const sigA = sig(kA) of T_template(_) 

	//instanciating T_template with the needed argument
	const TA  =  T_template(sigA)


Transaction ``T_template`` is parameteric and asks for a signature to
be used as witness for ``A_funds``. That signature can only be the
signature of ``T_template`` itself. Hence, after having generated
``sigA``, it is inserted into ``T_template`` to obtain final
transaction ``TA``.





When the signatures to be included in a transaction are several, the
protocol is the following: each participant signs a template for the
transaction, and send that signature to someone which collects all the
signatures together.

For instance, let assume Alice, Carl and Bob want to redeem the bitcoin in ``T_origin``:

.. code-block:: btm
		
    //needs three signatures to redeem  1 bitcoin
    transaction T_origin{
        input = _
        output = 1BTC: fun(x, y, z). versig(kApub, kBpub, kCpub; x, y, z)
    }	

Each of them calculates the signature for the redeeming transaction ``T_template``.
Let consider for instance, Alcie's code:

.. code-block:: btm
		
      //Alice's point of view
      // transaction template		
      transaction T_template (sA:signature, sB:signature, sC:signature){
         input = T_origin: sA sB sC
         output = 1BTC: fun(x). versig(kApub; x) //anything
      }
      //Alice's private key
      const kA = key:cSthBXr8YQAexpKeh22LB9PdextVE1UJeahmyns5LzcmMDSy59L4

      //Alice's signature
      const sigA = sig(kA) of T_template(_,_,_)
      //printing the signature
      compile sigA

   
When the compiler outputs ``sigA``, the result is a couple, signature and
public key, with the following syntax:

.. code-block:: btm
		
    sigA		
    sig:30450...3cdb01 [pubkey:03ff41f...9c3]

Every participant sends that piece of information to (say) Alice, who
collects everything to build up ``T_template``:

.. code-block:: btm
		
	//raw signature of T_template made by Alice plus Alice's public key
	const sigA = sig:304502...b01[kApub]
	//raw signature of T_template made by Bob plus Bob's public key
	const sigB = sig:956232...c12[kBpub]
	//raw signature of T_template made by Carl plus Carl's public key
	const sigC = sig:f3h5d6...cdb[kCpub]

	compile T_template(sigA, sigB, sigC)


      
    












