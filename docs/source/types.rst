=====
Types
=====

|langname| is a statically typed language, i.e. the type of each variable is determined at compile time.

The table below shows the list of types 


.. table:: List of types and examples
   :widths: 20 40 10 30

   ==================== ================================================================== ======== =========
   Type                 Description                                                        Hashable Example
   ==================== ================================================================== ======== =========
   ``int``              64-bit signed number                                               Y        ``42``
   ``string``           A string of characters                                             Y        ``"foo"``

                                                                                                    ``'bar'``
   ``boolean``          Either true or false value                                         Y        ``true`` ``false``
   ``hash``             A string of bytes in hexadecimal                                   Y        ``hash:c51b66bced5e4491001bd702669770dccf440982``
                        representation
   ``key``              A Bitcoin private key as Wallet Input Format                       N        ``wif:KzKP2XkH93yuXTLFPMYE89WvviHSmgKF3CjYKfpkZn6qij1pWuMW``
                        (`WIF <https://bitcoin.org/en/glossary/wallet-import-format>`__)
   ``address``          A Bitcoin address as Wallet Input Format                           N        ``wif:1GT4D2wfwu7gJguvEdZXAKcENyPxinQqpz``
                        (`WIF <https://bitcoin.org/en/glossary/wallet-import-format>`__)
   ``pubkey``           A raw public key as hexadecimal                                    N        ``pubkey:032b6cb7aa033a063dd01e20a971d6d4f85eb27ad0793b...``
   ``signature``        A raw signature as hexadecimal                                     N        ``sig:30450221008319289238e5ddb1aefa26db06a5f40b8a212d1...``
   ``transaction``      A Bitcoin transaction, serialized as hexadecimal string or         N        ``txid:0d7748674c8395cf288500b1c64330605fec54ae0dfdb22a...``
                        fetched from a trusted node using the txid

                                                                                                    ``tx:0100000001cab433976b8a3dfeeb82fe6a10a59381d2f91341...``
   ==================== ================================================================== ======== =========

The column **Hashable** indicates if a type can be hashed.

--------------
Hash Functions
--------------

There are four different *builtin function* that
can be used to generate a valid hash:

- ``sha256(x: <HashableType>) : hash``
- ``ripemd160(x: <HashableType>) : hash``
- ``hash256(x: <HashableType>) : hash``
- ``hash160(x: <HashableType>) : hash``


.. Hint:: 
   **Tipe Coercion**

   Type coercion is an automatic type conversion by the compiler.
   In other words, some types can be *safely converted* to other ones:

   - ``key`` can be used within expressions/statements where a type ``pubkey`` or ``address`` is expected;
   - ``pubkey`` can be used where a type ``address`` is expected.

.. Hint:: 
   **Tipe Inference**

   The type can be declared explicitly (left box) 
   or it can be omitted (right box) if the type checker can statically infer the
   expression type.


   .. container:: codecompare

      .. code-block:: btm
         
         const n:int = 42

      .. code-block:: btm
         
         const n = 42