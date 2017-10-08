
Wordful Solver
==============

The game [Wordful](https://play.google.com/store/apps/details?id=com.smartupinc.games.wordful&hl=en_GB)
is an fun mind exercise. Sometimes however, I just can't see the patterns and want to move on.
So, rather than spending a bit longer cracking the puzzle, why not spend a few hours writing
a solver instead?

This is a small program (~130 lines of Scala minus tests) that "walks" a Wordful grid trying to
match words from a dictionary.

Example Usage
-------------

The program itself expects the dictionary of words piped to its standard input. It will search
this based on the linearised representation of a grid of supplied as an argument.

Assuming the following 3x3 grid (Wordful Level 1 of the "London" stage) where we need to find
a 5 letter word, then a 4 letter one.

```
wpe
oha
snc
```

Finding words of any length:

```bash
cat /usr/share/dict/words \
  | scala WordfulSolver.scala wpeohasnc
```

Returns various words including 'an', 'ape', 'cheap' and 'peach'. However, in Wordful, you know the
length of the word you're trying to find, so we can filter using a prior grep.

```bash
cat /usr/share/dict/words \
  | grep '.\{5\}' \
  | scala WordfulSolver.scala wpeohasnc
```

Which would then find the five-letter words 'cheap' and 'peach'. For some grids, there can still be
quite a few possible results here. When this happens, the hint system in Wordful is still useful as
it provides the first letter, and we can use that to further filter the results.

```bash
cat /usr/share/dict/words \
  | grep '.\{5\}' \
  | grep -i '^p.*' \
  | scala WordfulSolver.scala wpeohasnc
```

Which would match all five-letter words beginning with 'p', returning us the correct answer 'peach'.

Finally, once a word has been found, it is removed from the grid and we are left with gaps.
This is not an issue though, as we can simply add placeholder values such as dots or spaces.

Removing 'peach' from the above grid would leave us with:

```
w
o
sn
```

While the answer is obvious in this simple level, it is considerably less so with bigger levels
with grid 5x5 or even 7x7.

Finding the word in this grid can be done with the following placeholder values.

```bash
cat /usr/share/dict/words \
  | scala WordfulSolver.scala "w  o  sn "
```

Providing the answer, 'snow'.
