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
from pygments.token import Generic, Text, Comment, Operator, Keyword, Name, String, Number, Punctuation
from pygments.util import shebang_matches
from pygments import unistring as uni

__all__ = ['BtmLexer']


class BtmLexer(RegexLexer):
    """
    For `Java <http://www.sun.com/java/>`_ source code.
    """

    name = 'BTM'
    aliases = ['btm']
    filenames = ['*.btm']

    flags = re.MULTILINE | re.DOTALL | re.UNICODE

    tokens = {
        'root': [
            (r'[^\S\n]+', Text),
            (r'\s*\.\.\.\s*', Text),
            (r'//.*?\n', Comment.Single),
            (r'/\*.*?\*/', Comment.Multiline),
            # prefixes: go before keywords
            (r'hash:|sig:|address:|key:|pubkey:|tx:|txid:', Name.Constant, 'literal'),
            # keywords: go before method names to avoid lexing "throw new XYZ"
            # as a method signature
            (r'(network|mainnet|testnet|regtest|input|output|absLock|relLock|after|block|date|from|if|then|else|sig|of|versig|fun|BTC|hash160|hash256|ripemd160|sha256|sha1|min|max|between|size|eval|checkBlock|checkDate|checkBlockDelay|checkTimeDelay|toAddress|toPubkey|AIAO|AISO|AINO|SIAO|SISO|SINO)\b', Keyword),
            (r'(sig)'                           # sig
             r'\((([^\W\d]|\$)[\w\'$]*)\)',      # key
             bygroups(using(this), Text)),
            # method names
            (r'((?:(?:[^\W\d]|\$)[\w.\[\]$<>]*\s+)+?)'  # return arguments
             r'((?:[^\W\d]|\$)[\w$]*)'                  # method name
             r'(\s*)(\()',                              # signature start
             bygroups(using(this), Name.Function, Text, Operator)),
            (r'\s*@\s*', Generic.Emph),
            (r'(transaction|const)(\s+)', bygroups(Keyword.Declaration, Text), 'declaration'),
            #(r'(abstract|const|enum|extends|final|implements|native|private|protected|public|static|strictfp|super|synchronized|throws|transient|volatile)\b', Keyword.Declaration),
            (r'(bool|boolean|string|hash|int|signature|transaction|address|key|pubkey)\b', Keyword.Type),
            (r'(package)(\s+)', bygroups(Keyword.Namespace, Text), 'import'),
            (r'(true|false|null)\b', Keyword.Constant),
            (r'(import(?:\s+static)?)(\s+)', bygroups(Keyword.Namespace, Text), 'import'),
            (r'[0-9]{4}-[0-9]{2}-[0-9]{2}(T[0-9]{2}:[0-9]{2}(:[0-9]{2})?((\+|-)[0-9]{2}:[0-9]{2})?)?', Number.Integer),
            (r'"(\\\\|\\"|[^"])*"', String),
            (r'\'(\\\\|\\\'|[^\'])*\'', String),
            (r'0[xX][0-9a-fA-F][0-9a-fA-F_]*', Number.Hex),
            (r'0|[1-9][0-9_]*(days|day|d|hours|hour|h|minutes|minute|min|m)?', Number.Integer),
            (r'(\.)((?:[^\W\d]|\$)[\w$]*)', bygroups(Operator, Name.Attribute)),
            (r'^\s*([^\W\d]|\$)[\w$]*:', Name.Label),
            (r'([^\W\d]|\$)[\w$]*', Name),
            (r'[~^*!%&\[\](){}<>|+=:;,./?-]', Operator),
            (r'\n', Text)
        ],
        'declaration': [
            (r'([^\W\d]|\$)[\w\'$]*', Name.Variable.Global, '#pop')
        ],
        'import': [
            (r'[\w.]+\*?', Name.Namespace, '#pop')
        ],
        'literal': [
            (r'[A-Za-z0-9.< >]+', String, '#pop')
        ],
    }


"""
    Text:                          '',
    Whitespace:                    'w',
    Escape:                        'esc',
    Error:                         'err',
    Other:                         'x',

    Keyword:                       'k',
    Keyword.Constant:              'kc',
    Keyword.Declaration:           'kd',
    Keyword.Namespace:             'kn',
    Keyword.Pseudo:                'kp',
    Keyword.Reserved:              'kr',
    Keyword.Type:                  'kt',

    Name:                          'n',
    Name.Attribute:                'na',
    Name.Builtin:                  'nb',
    Name.Builtin.Pseudo:           'bp',
    Name.Class:                    'nc',
    Name.Constant:                 'no',
    Name.Decorator:                'nd',
    Name.Entity:                   'ni',
    Name.Exception:                'ne',
    Name.Function:                 'nf',
    Name.Property:                 'py',
    Name.Label:                    'nl',
    Name.Namespace:                'nn',
    Name.Other:                    'nx',
    Name.Tag:                      'nt',
    Name.Variable:                 'nv',
    Name.Variable.Class:           'vc',
    Name.Variable.Global:          'vg',
    Name.Variable.Instance:        'vi',

    Literal:                       'l',
    Literal.Date:                  'ld',

    String:                        's',
    String.Backtick:               'sb',
    String.Char:                   'sc',
    String.Doc:                    'sd',
    String.Double:                 's2',
    String.Escape:                 'se',
    String.Heredoc:                'sh',
    String.Interpol:               'si',
    String.Other:                  'sx',
    String.Regex:                  'sr',
    String.Single:                 's1',
    String.Symbol:                 'ss',

    Number:                        'm',
    Number.Bin:                    'mb',
    Number.Float:                  'mf',
    Number.Hex:                    'mh',
    Number.Integer:                'mi',
    Number.Integer.Long:           'il',
    Number.Oct:                    'mo',

    Operator:                      'o',
    Operator.Word:                 'ow',

    Punctuation:                   'p',

    Comment:                       'c',
    Comment.Hashbang:              'ch',
    Comment.Multiline:             'cm',
    Comment.Preproc:               'cp',
    Comment.PreprocFile:           'cpf',
    Comment.Single:                'c1',
    Comment.Special:               'cs',

    Generic:                       'g',
    Generic.Deleted:               'gd',
    Generic.Emph:                  'ge',
    Generic.Error:                 'gr',
    Generic.Heading:               'gh',
    Generic.Inserted:              'gi',
    Generic.Output:                'go',
    Generic.Prompt:                'gp',
    Generic.Strong:                'gs',
    Generic.Subheading:            'gu',
    Generic.Traceback:             'gt'
"""
