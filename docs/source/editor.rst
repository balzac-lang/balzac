
=============
Editor syntax
=============

"""""""
Package
"""""""
The package declaration specify where the generated files must be saved.
This functionality is more useful when using the local eclipse plugin, respect to the online version.
By default, generated files are saved in ``src-gen`` with a directory structure reflecting the package name.
For example, declaring package ``com.example.test``, the generated files will be store in ``src-gen/com/example/test/``.

.. code-block:: btm

        package com.example.test

The package declaration is optional, but recommended, and must appear at the beginning of the file. The name of the package follows the same rules and conventions of
`Java packages <https://docs.oracle.com/javase/tutorial/java/package/namingpkgs.html>`_.


""""""""
Comments
""""""""
Single-line and multi-lines comments are supported. Their syntax is the same of Java.

.. code-block:: btm

	// <-- single line comment

	/*
	 * <-- multi-line comment
	 */



"""""""
Network
"""""""

Several static checks depend on the network you are considering.
The Wallet Import Format [#f1]_, used to represent private keys and addresses,
include a network identifier which made it wrong outside that network.
Furthermore, since we are generating transactions, the network declaration is
needed for serialization.

The network declaration is **optional**. If omitted, the **testnet** network is considered by default.
If specified, the network declaration *must appear right after the package declaration*.

.. container:: codecompare

	.. code-block:: btm

		package com.example.test

		// <-- you can put comments here

		network testnet


	.. code-block:: btm

		package com.example.test

		/* <-- you can put multi-line comments here */

		network mainnet


"""""""""
Constants
"""""""""

Constants are immutable global variables. The syntax is ``const __name__ [: __type__] = __expression__``.
For example:

.. container :: codecompare

	.. code-block:: btm

		const n = 42


	.. code-block:: btm

		const n:int = 42

The type can be omitted, because it is inferred by the expression. Refer to :doc:`types` and :doc:`expressions` for details.

""""""""""""
Transactions
""""""""""""

See :doc:`transactions`.


.. Tip :: 
	
	Constant and transaction declarations can be mixed together.

	It is allowed to write:

	.. code-block:: btm

		const n = 42

		transaction T { /* ... */ }

		const s = "Hello world"


.. Tip :: 

	Cross-references are resolved independently from the declaration order, 
	so it is allowed to write:

	.. code-block:: btm

		const n = 21 * m
		const m = 2

"""""""
Compile
"""""""

The :btm:`eval` statement takes a list of expression and evaluate them.
The compiled expressions are saved in text format in ``src-gen/_package-path_/transactions``.

.. code-block:: btm

	transaction T { /* ... */ }
	transaction T1(a:int) { /* ... */ }
	const n = 11

	eval 
		32,          // evaluates 32
		T,           // evaluates T as a bitcoin transaction
		T1(42),      // evaluates T1(42) as a bitcoin transaction
		(n + 5)      // evaluates 16


------------------------------------------------------------------

.. rubric:: References

.. [#f1] https://bitcoin.org/en/glossary/wallet-import-format
