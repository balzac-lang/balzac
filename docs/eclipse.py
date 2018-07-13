# -*- coding: utf-8 -*-
"""
    pygments.styles.eclipse
    ~~~~~~~~~~~~~~~~~~~~~~~
    Style similar to the style used in the Borland IDEs.
    :copyright: Copyright 2006-2010 by the Pygments team, see AUTHORS.
    :license: BSD, see LICENSE for details.

    From: https://github.com/ceci/pygments-hack/blob/master/pygments/styles/eclipse.py
"""

from pygments.style import Style
from pygments.token import Keyword, Name, Comment, String, Error, \
     Number, Operator, Generic, Whitespace


class EclipseStyle(Style):
    """
    Style similar to the style used in Eclipse IDEs. (Jevon)
    """

    default_style = ''

    styles = {
        Whitespace:             '#bbbbbb',

        Comment:                   "nobold #3F7F5F",        # 63,127,95
        Comment.Multiline:         "#3F5FBF",            # assume all multiline comments = Javadoc comments, 63,95,191
        Comment.Preproc:           'italic #666',        # e.g. XML preprocessing instruction
    
        Keyword:                   "bold #7F0055",        # 127,0,85
        Keyword.Pseudo:            "nobold #003366",
        Keyword.Type:              "bold #003366",        # e.g. int

        Operator:                  "#000",                # e.g. +, -, (, )

        Number:                    "#b32d00",

        Name.Class:                "nobold #000",
        Name.Namespace:            "nobold #000",
        Name.Exception:            "nobold #000",
        Name.Entity:               "nobold #000",
        Name.Tag:                  "bold #7F0055",        # XML/HTML tags - 127,0,85
        Name.Function:             "nobold #000",
        Name.Attribute:            "nobold #000",            # XML/HTML attributes
        Name.Decorator:            "nobold #646464",            # @Annotations - 100,100,100
        Name.Tag:                  "nobold #b32d00",

        String:                    "#2A00FF",        # 42,0,255

        Generic.Heading:           "bold",
        Generic.Subheading:        "bold",
        Generic.Emph:              "italic",
        Generic.Strong:            "bold",
        Generic.Prompt:            "bold",
        
        Error:                     "border:#FF0000"
}
