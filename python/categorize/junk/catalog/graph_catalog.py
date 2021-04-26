import typing

from categorize.junk.catalog.abstract_catalog import SequenceBuilder, Catalog
from categorize.junk.corpus.types import ImageCorpus


class CatalogItem(object):
    pass


class GraphSequenceBuilder(SequenceBuilder):
    def __init__(self, source: ImageCorpus, sequence: typing.Generator[CatalogItem]):
        self._source: ImageCorpus = source
        self._base_sequence: typing.Generator[CatalogItem] = sequence
        self._next_sequence: typing.Generator[Dict] = ({"_internal_item": item} for item in sequence)

    def include_labelling(self, key: str, labelling_name: str) -> "SequenceBuilder":
        pass

    def include_local_pattern(self, key: str, pattern_name: str) -> "SequenceBuiilder":
        pass

    def include_index(self, key: str) -> "SequenceBuilder":
        pass

    def include_digest(self, key: str) -> "SequenceBuilder":
        pass

    def include_content(self, key: str) -> "SequenceBuilder":
        pass

    def include_name(self, key: str) -> "SequenceBuilder":
        pass

    def create_labelling(self, labelling_name: str, label_set_name: str,
                         labeler: typing.Function[typing.tuple[int, str], str]) -> "SequenceBuilder":
        pass

    def compute_local_pattern(self, pattern_name: str, points: int, radius: int) -> "SequenceBuilder":
        pass


class GraphCatalog(Catalog):
