# -*- coding: utf-8 -*-
"""
    pygments.lexers.jvm
    ~~~~~~~~~~~~~~~~~~~

    Pygments lexers for BTM language. Based on JavaLexer

    :copyright: Copyright 2018 Nicola Atzei.
    :copyright: Copyright 2006-2017 by the Pygments team, see AUTHORS.
    :license: BSD, see LICENSE for details.
"""

import re

from pygments.lexer import Lexer, RegexLexer, include, bygroups, using, this, combined, default, words
from pygments.token import Text, Comment, Operator, Keyword, Name, String, Number, Punctuation
from pygments.util import shebang_matches
from pygments import unistring as uni

__all__ = ['BtmLexer']


class BtmLexer(RegexLexer):
    """
    For `Java <http://www.sun.com/java/>`_ source code.
    """

    name = 'Java'
    aliases = ['java']
    filenames = ['*.java']
    mimetypes = ['text/x-java']

    flags = re.MULTILINE | re.DOTALL | re.UNICODE

    tokens = {
        'root': [
            (r'[^\S\n]+', Text),
            (r'//.*?\n', Comment.Single),
            (r'/\*.*?\*/', Comment.Multiline),
            # keywords: go before method names to avoid lexing "throw new XYZ"
            # as a method signature
            #(r'(assert|break|case|catch|continue|default|do|else|finally|for|if|goto|instanceof|new|return|switch|this|throw|try|while)\b', Keyword),
            (r'(BTC)\b', Keyword),
            # method names
            (r'((?:(?:[^\W\d]|\$)[\w.\[\]$<>]*\s+)+?)'  # return arguments
             r'((?:[^\W\d]|\$)[\w$]*)'                  # method name
             r'(\s*)(\()',                              # signature start
             bygroups(using(this), Name.Function, Text, Operator)),
            (r'@[^\W\d][\w.]*', Name.Decorator),
            #(r'(abstract|const|enum|extends|final|implements|native|private|protected|public|static|strictfp|super|synchronized|throws|transient|volatile)\b', Keyword.Declaration),
            #(r'(boolean|byte|char|double|float|int|long|short|void)\b', Keyword.Type),
            (r'(bool|boolean|string|hash|int|signature|transaction|address|key|pubkey)\b', Keyword.Type),
            (r'(package)(\s+)', bygroups(Keyword.Namespace, Text), 'import'),
            (r'(true|false|null)\b', Keyword.Constant),
            (r'(transaction|const)(\s+)', bygroups(Keyword.Declaration, Text), 'declaration'),
            (r'(import(?:\s+static)?)(\s+)', bygroups(Keyword.Namespace, Text),
             'import'),
            (r'"(\\\\|\\"|[^"])*"', String),
            (r"'\\.'|'[^\\]'|'\\u[0-9a-fA-F]{4}'", String.Char),
            (r'(\.)((?:[^\W\d]|\$)[\w$]*)', bygroups(Operator, Name.Attribute)),
            (r'^\s*([^\W\d]|\$)[\w$]*:', Name.Label),
            (r'([^\W\d]|\$)[\w$]*', Name),
            (r'([0-9][0-9_]*\.([0-9][0-9_]*)?|'
             r'\.[0-9][0-9_]*)'
             r'([eE][+\-]?[0-9][0-9_]*)?[fFdD]?|'
             r'[0-9][eE][+\-]?[0-9][0-9_]*[fFdD]?|'
             r'[0-9]([eE][+\-]?[0-9][0-9_]*)?[fFdD]|'
             r'0[xX]([0-9a-fA-F][0-9a-fA-F_]*\.?|'
             r'([0-9a-fA-F][0-9a-fA-F_]*)?\.[0-9a-fA-F][0-9a-fA-F_]*)'
             r'[pP][+\-]?[0-9][0-9_]*[fFdD]?', Number.Float),
            (r'0[xX][0-9a-fA-F][0-9a-fA-F_]*[lL]?', Number.Hex),
            #(r'0[bB][01][01_]*[lL]?', Number.Bin),
            #(r'0[0-7_]+[lL]?', Number.Oct),
            (r'0|[1-9][0-9_]*[lL]?', Number.Integer),
            (r'[~^*!%&\[\](){}<>|+=:;,./?-]', Operator),
            (r'\n', Text)
        ],
        'declaration': [
            (r'([^\W\d]|\$)[\w$]*', Name.Variable.Global, '#pop')
        ],
        'import': [
            (r'[\w.]+\*?', Name.Namespace, '#pop')
        ],
    }