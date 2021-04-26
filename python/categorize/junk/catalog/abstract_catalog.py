import abc

import typing

from categorize.junk.catalog.graph_catalog import CatalogItem
from categorize.junk.corpus.types import ImageCorpus


class _CatalogCallback(abc.ABC):
    def get_labelling(self, lbl: typing.Generator[str]):
        pass

class SequenceBuilder(abc.ABC):
    def __init__(
        self,
        source: ImageCorpus,
        callback: _CatalogCallback,
        sequence: typing.Generator[CatalogItem],
    ):
        self._source: ImageCorpus = source
        self._base_sequence: typing.Generator[CatalogItem] = sequence
        self._next_sequence: typing.Generator[Dict] = (
            {"_internal_item": item} for item in sequence
        )

    @abc.abstractmethod
    def include_labelling(
        self, key: str, labelling_name: str
    ) -> "SequenceBuilder":
        return self

    @abc.abstractmethod
    def include_local_pattern(
        self, key: str, pattern_name: str
    ) -> "SequenceBuilder":
        return self

    @abc.abstractmethod
    def include_index(self, key: str) -> "SequenceBuilder":
        return self

    @abc.abstractmethod
    def include_digest(self, key: str) -> "SequenceBuilder":
        return self

    @abc.abstractmethod
    def include_content(self, key: str) -> "SequenceBuilder":
        return self

    @abc.abstractmethod
    def include_name(self, key: str) -> "SequenceBuilder":
        return self

    @abc.abstractmethod
    def create_labelling(
        self,
        labelling_name: str,
        label_set_name: str,
        labeler: typing.Callable[[int, str], str],
    ) -> "SequenceBuilder":
        return self

    @abc.abstractmethod
    def compute_local_pattern(
        self, pattern_name: str, points: int, radius: int
    ) -> "SequenceBuilder":
        return self




class Catalog(abc.ABC):
    # @abc.abstractmethod
    # def has_digest_order(self) -> bool:
    #     pass

    @abc.abstractmethod
    def build_generator(
        self,
        director: typing.Callable[[SequenceBuilder], None],
        with_validation: bool = True,
    ) -> typing.Generator[typing.Dict]:
        pass

    @abc.abstractmethod
    def pair_labels(
        self,
        graph_name: str,
        left_labels: str,
        right_labels: str,
        pairings: typing.Generator[typing.Tuple[str, str]],
    ) -> None:
        pass

    @abc.abstractmethod
    def pair_labels_weighted(
        self,
        graph_name: str,
        left_labels: str,
        right_labels: str,
        pairings: typing.Generator[typing.Tuple[str, str, int]],
    ) -> None:
        pass
