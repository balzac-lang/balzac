
# Requirements

The documentation is generated in Python (version 3, but it may work with Python 2 too).


Use `pip` to install the following Python packages:

```
pip install sphinx pygments sphinxcontrib-inlinesyntaxhighlight
```

# Available commands

```
make build-doc              # build the documentation
make clean-doc              # clean the documentation
make install-lexer          # install the lexer for pygments
make remove-lexer           # remove the lexer from pygments
```

# Testing

In order to locally browse the documentations, start an HTTP server into the `build/html/` directory:
 
```
cd build/html/ && python2 -m SimpleHTTPServer 8000
```
or, if you are using Python 3,
```
cd build/html/ && python3 -m http.server 8000
```

This is the easiest way to serve local files. 
However, any HTTP server is fine.

Explore the documentations in your browser: <http://localhost:8000>
