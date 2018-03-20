=====
Types
=====

|langname| is a statically typed language, i.e. the type of each variable is determined at compile time.
The type can be declared explicitly, e.g. ``const x:int = 42``, or it can be omitted if the type checker
can statically infer the type, for example ``s`` has type ``string`` in the expression ``const s: = "Hello world!"``.

.. table:: Types
   :widths: auto

   ==================== ======== =========
   Type                 Hashable Example
   ==================== ======== =========
   ``int``              Y        ``42``
   ``string``           Y        ``"foo"``

                                 ``'bar'``
   ``boolean``          Y        ``true`` ``false``
   ``hash``             Y        ``hash:c51b66bced5e4491001bd702669770dccf440982``
   ``key``              N        ``wif:KzKP2XkH93yuXTLFPMYE89WvviHSmgKF3CjYKfpkZn6qij1pWuMW``
   ``address``          N        ``wif:1GT4D2wfwu7gJguvEdZXAKcENyPxinQqpz``
   ``pubkey``           N        ``pubkey:032b6cb7aa033a063dd01e20a971d6d4f85eb27ad0793b...``
   ``signature``        N        ``sig:30450221008319289238e5ddb1aefa26db06a5f40b8a212d1...``
   ``transaction``      N        ``txid:0d7748674c8395cf288500b1c64330605fec54ae0dfdb22a...``

                                 ``tx:0100000001cab433976b8a3dfeeb82fe6a10a59381d2f91341...``
   ==================== ======== =========

The column **Hashable** indicates if a type can be hashed. There are four different *builtin function* that
can be used to generate a valid hash:

- ``sha256(x: <HashableType>) : hash``
- ``ripemd160(x: <HashableType>) : hash``
- ``hash256(x: <HashableType>) : hash``
- ``hash160(x: <HashableType>) : hash``

-------------
Type coercion
-------------

Type coercion is an automatic type conversion by the compiler.
In other words, some types can be *safely converted* to other ones.

|langname| permits to:

- use ``key`` within expressions/statements where a type ``pubkey`` or ``address`` is expected;
- use ``pubkey`` where a type ``address`` is expected.

