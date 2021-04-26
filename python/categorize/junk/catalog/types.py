import typing


class CatalogItem:
    def __init__(
        self,
        file_path: str,
        hex_digest: str,
        content: typing.Optional[bytes],
        index: int,
    ):
        self._file_path: str = file_path
        self._hex_digest: str = hex_digest
        self._index: int = index
        self._content: typing.Optional[bytes] = content

    @property
    def file_path(self) -> str:
        return self._file_path

    @property
    def hex_digest(self) -> str:
        return self._hex_digest

    @property
    def index(self) -> int:
        return self._index

    @property
    def has_content(self) -> bool:
        return self._content is not None

    @property
    def content(self) -> bytes:
        if self._content is None:
            raise ValueError("No content")
        return self._content

    @property
    def maybe_content(self) -> typing.Optional[bytes]:
        return self._content


class LocalBinaryPattern:
    def __init__(self, data: typing.List[float]):
        self._data = data


class EnrichedCatalogItem(CatalogItem):
    def __init__(
        self,
        file_path: str,
        hex_digest: str,
        content: typing.Optional[bytes],
        index: int,
        labels: typing.Dict[str, str],
        patterns: typing.Dict[str, typing.List[float]]
    ):
        CatalogItem.__init__(self, file_path, hex_digest, content, index)
        self._labels: typing.Dict[str, str] = labels
        self._patterns: typing.Dict[str, typing.List[float]] = patterns

    @property
    def labels(self) -> typing.Dict[str, str]:
        return self._labels.copy()

    def label(self, labelling_name: str) -> str:
        if labelling_name not in self._labels.keys:
            raise NameError("No labelling named " + labelling_name + " in current enrichment")
        return self._labels[labelling_name]

    def pattern(self, pattern_name: str) -> LocalBinaryPattern:

class AppliedLabel
