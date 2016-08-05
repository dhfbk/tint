## Part-of-speech tagging

In linguistics, part-of-speech tagging (POS tagging), is the process of marking up a word in a text (corpus) as
corresponding to a particular part of speech, based on both its definition and its context — i.e., its relationship
with adjacent and related words in a phrase, sentence, or paragraph.
A simplified form of this is commonly taught to school-age children, in the identification of words as nouns, verbs,
adjectives, adverbs, etc.

<p align='right'><em>from <a href='https://en.wikipedia.org/wiki/Part-of-speech_tagging'>Wikipedia</a></em></p>

### Description

The POS tagger included in Tint is based on the
[Maximum Entropy Part-of-speech Tagger](http://nlp.stanford.edu/software/tagger.shtml) included in Stanford CoreNLP.

The model provided with Tint is trained on the ISTD (Italian Stanford Dependency Treebank), released for the
[dependency parsing shared task of Evalita-2014](http://www.evalita.it/2014/tasks/dep_par4IE) and containing 316,660
tokens.
The original resource is released under the Creative Commons
[Attribution-NonCommercial-ShareAlike 3.0](https://creativecommons.org/licenses/by-nc-sa/3.0/) license, therefore
the resulting model cannot be used for commercial purposes.

### Performances

Evaluated on the Universal Dependencies test set, the POS tagger gets 96.24% accuracy overall and 82.32% accuracy
on unknown words.
On a 2,3 GHz Intel Core i7 with 16 GB of memory, it can tag 80,000 token/second.

### Training

The Tint module for Part-of-speech tagging relies on the corresponding module in Stanford CoreNLP.
You can surf to its [FAQ page](http://nlp.stanford.edu/software/pos-tagger-faq.shtml) for more information.

If you want to retrain the POS tagger using the ISTD dataset, you need to convert the original dataset to the format
accepted by the Stanford `MaxentTagger`.
The words should be tagged by having the word and the tag separated by the underscore character.
For example:

```
Evacuata_VERB la_DET Tate_PROPN Gallery_PROPN ._PUNCT
```

The ISTD dataset needs to be downloaded from the
[Universal Dependencies](http://universaldependencies.org/) website.
After that, you need to run the `eu.fbk.dh.tint.resources.pos.CreateTrainingForStanfordPOS` class to read the
CoNLLU format and transform it to the underscore-separated format.

Command parameters:

```
  -c,--conll <FILE>        Output in CoNLL format
     --column <FILE>       Column for POS (default 3)
  -D,--verbose             enable verbose output
  -h,--help                display this help message and terminate
  -i,--input <FILE>        Input file
  -o,--output <FILE>       Output file
  -p,--only-pos <FILE>     Output file for pos
  -t,--only-tokens <FILE>  Output file for tokens
  -V,--very verbose        enable very verbose output
  -v,--version             display version information and terminate
  -x,--text <FILE>         Output text
```

For example, you can run it on the training set of the Universal Dependencies by using:

```
java eu.fbk.dh.tint.resources.pos.CreateTrainingForStanfordPOS \
    -i /path/to/ud/it-ud-train.conllu \
    -o output.train.stanford
```

The column parameter can be used to choose between the universal tags (`--column 3`, default) or the
[EAGLES](http://www.ilc.cnr.it/EAGLES96/home.html) standard tags (`--column 4`).

Both the property file (`italian-fast.tagger.model.props`) and the resulting model (`italian-fast.tagger.model`) are
included in the Tint distribution as resources.
