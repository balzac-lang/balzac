=====
Types
=====

|langname| is a statically typed language, i.e. the type of each variable is determined at compile time.

The table below shows the list of types 


.. table:: List of types and examples
   :widths: 20 40 10 30

   ==================== ================================================================== =========
   Type                 Description                                                        Example
   ==================== ================================================================== =========
   :btm:`int`           64-bit signed number                                               :btm:`42`
   :btm:`string`        A string of characters                                             :btm:`"foo"`

                                                                                           :btm:`'bar'`
   :btm:`boolean`       Either true or false value                                         :btm:`true` :btm:`false`
   :btm:`hash`          A string of bytes in hexadecimal representation                    :btm:`hash:c51b66bced5e4491001bd702669770dccf440982`
   :btm:`key`           A Bitcoin private key in the Wallet Input Format [#f1]_            :btm:`key:KzKP2XkH93yuXTLFPMYE89WvviHSmgKF3CjYKfpkZn6qij1pWuMW`
   :btm:`address`       A Bitcoin address in the Wallet Input Format [#f1]_                :btm:`address:1GT4D2wfwu7gJguvEdZXAKcENyPxinQqpz`

   :btm:`pubkey`        A raw public key as hexadecimal string                             :btm:`pubkey:032b6cb7aa033a063dd01e20a971d6d4f85eb27ad0793b...`
   :btm:`signature`     A raw signature as hexadecimal string                              :btm:`sig:30450221008319289238e5ddb1aefa26db06a5f40b8a212d1...`
   :btm:`transaction`   A Bitcoin transaction, as hex payload or txid                      :btm:`tx:0100000001cab433976b8a3dfeeb82fe6a10a59381d2f91341...`

                                                                                           :btm:`txid:0d7748674c8395cf288500b1c64330605fec54ae0dfdb22a...`
   ==================== ================================================================== =========

.. Hint:: 
   **Type Coercion**

   Type coercion is an automatic type conversion by the compiler.
   In other words, some types can be *safely converted* to other ones:

   - :btm:`key` can be used within expressions/statements where a type :btm:`pubkey` or :btm:`address` is expected;
   - :btm:`pubkey` can be used where a type :btm:`address` is expected.

.. Hint:: 
   **Type Inference**

   The type can be declared explicitly (left box) 
   or it can be omitted (right box) if the type checker can statically infer the
   expression type.


   .. container:: codecompare

      .. code-block:: btm
         
         const n:int = 42

      .. code-block:: btm
         
         const n = 42

.. rubric:: References

.. [#f1] https://bitcoin.org/en/glossary/wallet-import-format
