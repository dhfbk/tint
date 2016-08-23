## Dependency Parsing

Dependency parsing is the process of extract the grammatical structure of a sentence in a form of a "tree".
A node in the tree is a word in the sentence.
Edges correspond to relations between the words.
Each edge connect a "head" word with a word that modify that head.

### Description

The Dependency Parser included in Tint is based on the
[Neural Network Dependency Parser](http://nlp.stanford.edu/software/nndep.shtml) included in Stanford CoreNLP.

The model provided with Tint is trained on the ISTD (Italian Stanford Dependency Treebank), released for the
[dependency parsing shared task of Evalita-2014](http://www.evalita.it/2014/tasks/dep_par4IE) and containing 316,660
tokens.
The original resource is released under the Creative Commons
[Attribution-NonCommercial-ShareAlike 3.0](https://creativecommons.org/licenses/by-nc-sa/3.0/) license, therefore
the resulting model cannot be used for commercial purposes.

### Performances

Evaluated on the Universal Dependencies test set, the Italian dependency parser included in Tint gives: 84.67 (LAS)
and 87.05 (UAS).
On a 2,3 GHz Intel Core i7 with 16 GB of memory, it can tag 9,000 token/second.

### Training

The Tint module for dependency parsing relies on the corresponding module in Stanford CoreNLP.
You can surf to its [main page](http://nlp.stanford.edu/software/nndep.shtml) for more information on how to use it.

In order to retrain the Stanford Parser using the ISTD dataset, you need the training data to be in CoNLL format.
While the ISTD is already given in the correct format, we need to parse it to meet the tokenization rules used by
the Tint tokenizer, that are slightly different from the ones used in the dataset.
See the [Universal Dependencies Italian page]() for more information.

The training data should be in the CoNLL format. For example:

```
1	Evacuata	evacuare	VERB	VERB	_	3	acl	3	acl
2	la	il	DET	DET	_	3	det	3	det
3	Tate	Tate	PROPN	PROPN	_	0	root	0	root
4	Gallery	Gallery	PROPN	PROPN	_	3	name	3	name
5	.	.	PUNCT	PUNCT	_	3	punct	3	punct
```

The CoreNLP class used is `nndep.DependencyParser`.

The ISTD dataset needs to be downloaded from the
[Universal Dependencies](http://universaldependencies.org/) website.
After that, you need to run the `eu.fbk.dh.tint.resources.parse.CreateTrainingForStanfordParser` class to read the
CoNLLU format and transform it to the standard CoNLL format.

Command parameters:

```
     --column <NUM>   Column for POS (default 3)
     --debug          enable verbose output
  -h,--help           display this help message and terminate
  -i,--input <FILE>   Input file
  -o,--output <FILE>  Output file
     --trace          enable very verbose output
  -v,--version        display version information and terminate
```

For example, you can run it on the training set of the Universal Dependencies by using:

```
java eu.fbk.dh.tint.resources.parse.CreateTrainingForStanfordParser \
    -i /path/to/ud/it-ud-train.conllu \
    -o output.train.stanford.parse
```

The column parameter can be used to choose between the POS universal tags (`--column 3`, default) or the
[EAGLES](http://www.ilc.cnr.it/EAGLES96/home.html) standard POS tags (`--column 4`).

This Stanford parser is powered by a neural network which accepts word embedding inputs: in our model, these word
embeddings are built on the [Paisà corpus](http://www.corpusitaliano.it),
that contains 250M tokens of freely available and distributable texts harvested from the web.
The embeddings are available on the download page.

Both the property file (`italian.parser.model.props`) and the resulting model (`italian.parser.model`) are
included in the Tint distribution as resources.
