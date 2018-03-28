.. highlight:: btm

===================
Expressions
===================

The following expression can be used in the scripts of transaction outputs and in constant declarations.

------------------------
Conditional 
------------------------

   ==========================================================   ======================================================================================
   Function                                   					Description
   ==========================================================   ======================================================================================
   ``if (exp: bool) then exp': a' else exp'': a' -> a'``		If the evaluation of ``exp`` yields ``true``, executes ``exp'``, otherwise ``exp''``

   ==========================================================   ======================================================================================

----------------
Hash functions
----------------

   ========================================   ======================================================================================
   Function                                   Description
   ========================================   ======================================================================================
   ``sha256(exp: Hashable) -> hash``			The evaluation of ``exp`` is hashed using SHA-256
   ``ripemd160(exp: Hashable) -> hash``			The evaluation of ``exp`` is hashed using RIPEMD-160.
   ``hash160(exp: Hashable) -> hash``			The evaluation of ``exp`` is hashed first with SHA-256 and then with RIPEMD-160.
   ``hash256(exp: Hashable) -> hash``			The evaluation of ``exp`` is is hashed two times with SHA-256.

   ========================================   ======================================================================================

``Hashable`` is any of these types: ``int``, ``string``, ``boolean``, ``hash``.

--------------------------
Cryptographic functions
--------------------------

   ===================================================================================   ======================================================================================
   Function																					Description
   ===================================================================================   ======================================================================================
   ``sig(k: key)[mod: Modifier] -> signature``												Compute a transaction signature
   ``versig(k: key; sig: signature) -> bool``												Single signature verification
   ``versig(k1: key, ..., kn: key; sig1: signature, ..., sign: signature) -> bool``			Multi-signature verification

   ===================================================================================   ======================================================================================

The expression ``sig`` appears within a witness. It computes the signature of the transaction that contains it, under a private ``k``.
The signature modifier ``[mod]`` is an optional parameter and be one of the following:

	============================================ ==================================================================
	Modifier 									  Signature Hash Type [BW]_
	============================================ ==================================================================
	``AIAO``										``SIGHASH_ALL``
	``AISO``										``SIGHASH_SINGLE``
	``AINO``										``SIGHASH_NONE``
	``SIAO``										``SIGHASH_ALL | SIGHASH_ANYONECANPAY``
	``SISO``										``SIGHASH_SINGLE | SIGHASH_ANYONECANPAY``
	``SINO``										``SIGHASH_NONE | SIGHASH_ANYONECANPAY``
	============================================ ==================================================================

Each modifier is composed by two parts, ``*I`` and ``*O``, indicating respectively the subset of inputs and of outputs being signed.
The first letter of each part represents all, single, or none. A formal specification can be found in Section 3.3 of [ABLZ]_.

The expression ``versig`` appears within the script of a transaction output. It verifies that ``sig`` is a valid signature under the private key ``key``.
The message being verified is the transaction that redeems the output that contains ``sig``.
The expression ``versig`` also executes multi-signature verification when it receives a list of keys and a list of signatures as input.
It implementation is the same as Bitcoin: the function tries to verify the last signature with the last key. If they match, the function
ver proceeds to verify the previous signature in the sequence, otherwise it tries to verify the signature with the previous key.

-----------------------------
Numerical functions
-----------------------------

   ======================================================   ======================================================================================
   Function                                   				Description
   ======================================================   ======================================================================================
   ``min(a: int, b: int) -> int``							Returns the smallest of two arguments.
   ``max(a: int, b: int) -> int``							Returns the greatest of two arguments.
   ``between(x: int, min: int, max: int) -> bool``			Checks if ``x`` is in the range ``[min, max]``
   ``size(exp: any) -> int``								Returns the size of the evaluation of ``exp``
   ======================================================   ======================================================================================

-----------------------------------
Time constraints
-----------------------------------

   ======================================================   ======================================================================================
   Function                                   				Description
   ======================================================   ======================================================================================
   ``after date exp:int : exp```							Evaluates ``exp`` if the constraints is satisfied, otherwise stops the evaluation
   ``after block exp:int : exp```							Evaluates ``exp`` if the constraints is satisfied, otherwise stops the evaluation
   ======================================================   ======================================================================================

Time constraints are a special category of expression as: 

* they cannot be used in constant declaration
* they stop the evaluation if not met (similarly to an exception).

They enforce that a transaction output cannot be redeemed before a certain time
by checking the ``timelock`` on the redeeming transaction. 
The constraint can be either specified as a ``date`` or as a ``block`` number.


.. rubric:: References

.. [BW] https://bitcoin.org/en/developer-guide#signature-hash-types
.. [ABLZ] https://eprint.iacr.org/2017/1124.pdf