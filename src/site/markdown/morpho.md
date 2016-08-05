## Morphological analysis and lemmatization

In computational linguistics, lemmatisation is the algorithmic process of determining the lemma for a given word.
Since the process may involve complex tasks such as understanding context and determining the part of speech of a
word in a sentence (requiring, for example, knowledge of the grammar of a language) it can be a hard task to
implement a lemmatiser for a new language.

In many languages, words appear in several inflected forms.
For example, in English, the verb 'to walk' may appear as 'walk', 'walked', 'walks', 'walking'.
The base form, 'walk', that one might look up in a dictionary, is called the lemma for the word.
The combination of the base form with the part of speech is often called the lexeme of the word.

The word "meeting" can be either the base form of a noun or a form of a verb ("to meet") depending on the context,
e.g., "in our last meeting" or "We are meeting again tomorrow".
The morphological analyser provides the full list of morphological features for a given form.
Lemmatisation can in principle select the appropriate lemma depending on the context.

<p align='right'><em>from <a href='https://en.wikipedia.org/wiki/Lemmatisation'>Wikipedia</a></em></p>

### Description

The morphological analyzer used by Tint (called DigiMorph) has been trained with the
[Morph-it lexicon](http://sslmitdev-online.sslmit.unibo.it/linguistics/morph-it.php), but it’s possible to extend or
retrain it with other Italian datasets.
In order to grant fast performance, the model storage has been implemented with the
[mapDB Java library](http://www.mapdb.org/) that provides an excellent variation of the Cassandra’s Sorted String
Table.
To extend the coverage of the results, especially for the complex forms, such as “porta-ce-ne”, “portar-glie-lo” or
“bi-direzionale”, the module tries to decompose the token into prefix-root-infix-suffix and attempts to resolve the
root form.

The module for the lemmatization is a rule-based system that works by combining the Part-of-Speech output and the
results of the Morphological Analyzer so to disambiguate the morphological features using the grammatical annotation.
In order to increase the accuracy of the results, the module tries to detect the genre of noun lemmas relying to the
analysis of their processed articles.
For instance, for the correct lemmatization of “il latte/the milk”, the module uses the singular article “il” to
identify the correct gender/number of the lemma “latte” and returns “latte/milk” (male, singular) instead of
“latta/metal sheet” (female, which plural form is “latte”).

The model provided with Tint (in mapDB format) is already included in the jar package as a resource, and is released
under the Creative Commons [Attribution-ShareAlike 3.0](https://creativecommons.org/licenses/by-sa/3.0/) license.

### Performances

The DigiMorph module can analyze 97,000 token/second.

### Training

Under construction.