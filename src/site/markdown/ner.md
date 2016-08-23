## Named-entity recognition

Named-entity recognition (NER) (also known as entity identification, entity chunking and entity extraction) is a
subtask of information extraction that seeks to locate and classify named entities in text into pre-defined categories
such as the names of persons, organizations, locations, expressions of times, quantities, monetary values,
percentages, etc.

<p align='right'><em>from <a href='https://en.wikipedia.org/wiki/Named-entity_recognition'>Wikipedia</a></em></p>

### Description

The NER included in Tint is based on the
[CRF Classifier](http://nlp.stanford.edu/software/CRF-NER.shtml) included in Stanford CoreNLP.

The model provided with Tint is trained on the
[Italian Content Annotation Bank (I-CAB)](http://ontotext.fbk.eu/icab.html), containing around 180,000
words taken from the Italian newspaper [L'Adige](http://www.ladige.it/), and used for the
[Entity Regognition task at Evalita 2009](http://www.evalita.it/2009/tasks/entity), the
[Temporal Expression Recognition and Normalization Task at Evalita 2007](http://www.evalita.it/2007/tasks/tern) and the
[Named Entity Recognition Task at Evalita 2007](http://www.evalita.it/2007/tasks/ner).

The resource is freely available for research purposes upon acceptance of a
[license agreement](http://ontotext.fbk.eu/i-cab/download-icab.html), therefore the resulting model cannot be used
for commercial purposes.

### Performances

Evaluated on the I-CAB test set, the Tint NER annotator gets 82.11 F1 overall.
On a 2,3 GHz Intel Core i7 with 16 GB of memory, it can tag 30,000 token/second.

### Training

The Tint module for Named Entity Recognition relies on the corresponding module in Stanford CoreNLP.
You can surf to its [FAQ page](http://nlp.stanford.edu/software/crf-faq.html) for more information.

If you want to train your own model from I-CAB, you need to convert the original dataset to the format
accepted by the Stanford `CRFClassifier`.
The words should be tagged by having one token per line, the word and the tag separated by the tab character.
The `O` denotes a non-entity word.
For example:

```
Latte O
al O
seno O
, O
sos O
di O
Pedrotti PER
```

The original I-CAB dataset needs to be downloaded from
[its website](http://ontotext.fbk.eu/icab.html).
After that, you need to run the `eu.fbk.dh.tint.resources.ner.ConvertICAB` class to read the
I-CAB format and transform it to the tab-separated format.

Command parameters:

```
     --debug                   enable verbose output
  -g,--keep-gpe                Keep GPE tags (default is to remove them)
  -h,--help                    display this help message and terminate
  -i,--input <FILE>            Input training/test file in IOB2 format
  -k,--output-text-br <FILE>   Output file one-token-per-line
  -o,--output-stanford <FILE>  Output file for Stanford
  -t,--output-text <FILE>      Output file text only
     --trace                   enable very verbose output
  -v,--version                 display version information and terminate
```

For example, you can run it on the training set of the Evalita 2009 dataset (included in I-CAB):

```
java eu.fbk.dh.tint.resources.ner.ConvertICAB \
    -i /path/to/icab/I-CAB-evalita09-NER-training.iob2 \
    -o output.train.stanford
```

For our annotation, we only consider three classes: PER (person), LOC (location), ORG (organization).
The I-CAB dataset includes also a GPE class, but in Tint it has been merged with LOC.
You can add it again using the `--keep-gpe` option when running `ConvertICAB`.

In training Tint, we add some gazette of names, to help the classifier to recognize entities that are not present in
the training set. In particular, we extracted a list of persons, locations and organizations by querying the
[Airpedia](http://www.airpedia.org) database for Wikipedia pages classified as Person, Place and Organisation,
respectively. The whole data used for training the NER is available for download.

If you want to generate it from the Airpedia dumps, you can use the `eu.fbk.dh.tint.resources.ner.LoadWikipedia`
class.

Command parameters:

```
     --debug                 enable verbose output
  -f,--form-page-path <DIR>  Form-page path from Airpedia
  -h,--help                  display this help message and terminate
  -l,--page-list <FILE>      Page-list from Airpedia
     --label <LABEL>         Label (PER, ORG, LOC, ...)
  -o,--output <FILE>         Output file
  -p,--page-form-path <DIR>  Page-form path from Airpedia
     --trace                 enable very verbose output
  -v,--version               display version information and terminate
```

See the [Airpedia website](http://www.airpedia.org) to obtain the resources needed to run the command.

Both the property file (`italian.ner.model.props`) and the resulting model (`italian.ner.model`) are
included in the Tint distribution as resources.
