from collections import Iterator, Iterable

from luminostics.categorize.corpus.types import PixelDimensions, ImageItem


class ImageCorpusService(Iterable[ImageItem]):
    """
    Query model counterpart of an aggregate root from Generated Artwork domain model that models
    collections of generated imagery that are created by a selecting any number of previously
    derived artwork resources from any number of ImageOrigin collections its owner has permission
    to publish from.  Curated resources do not need to share anything in common about how their
    ImageOrigin containers derive seeds or name their resources, but each selected image must
    have the same image resolution.  ImageCorpus collections are immutable once created.  Anyone
    can create another ImageCorpus with most of the same selections, but a few differences, however
    both "versions" will co-exist as independent collections.

    ImageCorpus does not allow selection from ImagePopulation collections because its own immutability
    depends on only taking references to artwork children that cannot later change or get deleted.
    ImageOrigin collections are compatible because they only change by adding new members, not by
    modifying or removing existing children.
    """

    def __init__(
        self,
        name: str,
        size_rule: PixelDimensions,
        contents: Iterable[ImageItem],
    ):
        self._name = name
        self._size_rule = size_rule
        self.contents = contents

    @property
    def name(self) -> str:
        return self._name

    @property
    def size_rule(self) -> PixelDimensions:
        return self._size_rule

    def __iter__(self) -> Iterator[ImageItem]:
        """
        Iterate through the identities of all images a corpus includes, returning a name and hash digest
        associated with each.

        TODO: To expose this by as a domain aggregate by query
              RPC, this should expose pagination instead.
        :return: Iterator over the resource location and validation
                 hashes for each image a corpous
        """
        return self.contents.__iter__()
