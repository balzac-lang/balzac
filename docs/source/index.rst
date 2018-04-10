
|langname|
===============

*Bitcoin Abstract Language, analyZer and Compiler*

|langname| is an high-level language for writing transactions, verifying their correctness, 
and compiling them into actual Bitcoin transactions. It is based on the formal model proposed in [AB+18FC]_.

You can install the IDE as a Eclipse plugin, or try the `web editor <https://blockchain.unica.it/balzac/>`_.

The project is open source, and you are welcome to contribute to our
`repository <https://github.com/balzac-lang/balzac>`_.

|langname| is developed by the `Blockchain@Unica group <http://blockchain.unica.it/>`_
of the `University of Cagliari <https://www.unica.it/unica/en/homepage.page>`_.

**Contents:**


.. toctree::
    :maxdepth: 3
    :caption: Overview

    overview


.. toctree::
    :maxdepth: 3
    :caption: Language Specification

    file-structure
    transactions
    expressions
    types

.. toctree::
    :maxdepth: 3
    :caption: Installation and Configuration

    eclipse-conf

.. toctree::
    :maxdepth: 3
    :caption: Smart contracts

    oracle
    timed-commitment


..        # with overline, for parts
..        * with overline, for chapters
..        =, for sections
..        -, for subsections
..        ^, for subsubsections
..        ", for paragraphs


.. warning ::
	|langname| is intended for research purposes only. 
	Do not use it to create mainnet transactions, or do it at your own risk.


.. rubric:: References

.. [AB+18FC] N. Atzei, M. Bartoletti, S. Lande, R. Zunino. A formal model of Bitcoin transactions. In Financial Cryptography and Data Security, 2018. Preprint: https://eprint.iacr.org/2017/1124.pdf

.. [AB+18POST] N. Atzei, M. Bartoletti, T. Cimoli, S. Lande, R. Zunino. SoK: unraveling Bitcoin smart contracts. In Principles of Security and Trust (POST), 2018. Preprint: https://eprint.iacr.org/2018/192.pdf
