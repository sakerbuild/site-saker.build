# Comments

The language allows comments to be specified in the script files. Both single and multi-line comments can be declared. They are created by prefixing or enclosing them in `'#'` character(s).

```sakerscript
#single line comment
expression() # starting after an expression

###
multi line comments are enclosed by triple '#'
###

expression() ###
multi line comments can start after an expression and end before one
### expression()
```

Single line comments can appear anywhere in the code. They are prefixed by a single `'#'` character, and last until the next line.

Multi line comments start with triple hashmark `'###'` and end with the same.

This comment syntax also support the [shebang](https://en.wikipedia.org/wiki/Shebang_(Unix)) interpreter directive.