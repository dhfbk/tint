## Tokenization and sentence splitting

In lexical analysis, tokenization is the process of breaking a stream of text up into words, phrases, symbols, or
other meaningful elements called tokens.
The list of tokens becomes input for further processing such as parsing or text mining.
Tokenization is useful both in linguistics (where it is a form of text segmentation), and in computer science, where
it forms part of lexical analysis.

<p align='right'><em>from <a href='https://en.wikipedia.org/wiki/Tokenization_(lexical_analysis)'>Wikipedia</a></em></p>

Sentence boundary disambiguation is the problem in natural language processing of deciding where sentences begin
and end.
Often natural language processing tools require their input to be divided into sentences for a number of reasons.
However sentence boundary identification is challenging because punctuation marks are often ambiguous.
For example, a period may denote an abbreviation, decimal point, an ellipsis, or an email address - not the end of a
sentence.
As well, question marks and exclamation marks may appear in embedded quotations, emoticons, computer code, and slang.

<p align='right'><em>from <a href='https://en.wikipedia.org/wiki/Sentence_boundary_disambiguation'>Wikipedia</a></em></p>

### Description

In the Tint pipeline, the module performing Italian text segmentation (both in terms of words and sentences) is
native, as it does not use a ready-made Stanford CoreNLP annotator.
However, it is written using the CoreNLP paradigm, therefore it can be included in the pipeline as it was native.

The word segmentation module first splits the text into atoms (it groups together letters and numbers, and splits
all other characters), then combines some of them depending on a list of rules.
The rules can be given as abbreviations (such as "S.p.A.", "dott.", "Mr.", ...) or regular expressions (for e-mail
addresses, URLs, emoticons, and so on).
Abbreviations are searched using [tries](https://en.wikipedia.org/wiki/Trie) to speedup the process.

Finally, sentence boundaries are identified using a list of characters contained in the configuration file.

A basic fully-working settings file (`token-settings.xml`) is provided as a `tint-tokenizer` module resource.

### Properties

* `ita_toksent.newlineIsSentenceBreak`: can be set to `true` or `false`; if `true`, the tokenizer always breaks sentence on newline (default `true`)
* `ita_toksent.tokenizeOnlyOnSpace`: can be set to `true` or `false`; if `true`, the text is tokenized on white spaces instead of applying the tokenizer (default `false`)
* `ita_toksent.ssplitOnlyOnNewLine`: can be set to `true` or `false`; if `true`, only newlines are considered as sentence breaks (default `false`)

### Performances

The Tint tokenizer can reach 80,000 token/second in the standard configuration, but one can deactivate regular
expressions to speed up the process.