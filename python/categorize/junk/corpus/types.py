import abc
from typing import Optional
from typing.io import BinaryIO


class PixelDimensions:
    """
    Value class representing an image size measured in pixels
    """

    def __init__(self, height: int, width: int):
        self._height = height
        self._width = width

    def height(self) -> int:
        return self._height

    def width(self) -> int:
        return self._width


class ImageItem:
    def __init__(self, name: str, subject_name: str, hex_digest: str):
        self._name: str = name
        self._hex_digest = hex_digest
        self._subject_name: str = subject_name

    @property
    def name(self) -> str:
        return self._name

    @property
    def hex_digest(self):
        return self._hex_digest

    @property
    def subject_name(self):
        return self._subject_name


class ImageData:
    def __init__(self, identity: ImageItem, content: bytes):
        self._identity = identity
        self._content = content

    @property
    def identity(self) -> ImageItem:
        return self._identity

    @property
    def content(self) -> bytes:
        return self._content


class ImagePublisher(abc.ABC):
    @abc.abstractmethod
    def open(self, rel_path: str) -> BinaryIO:
        """
        :param rel_path:
        :return: Returns the image at relative path rel_path, otherwise null.
        :raises: IO exceptions if the file is not found or cannot be read
        """

    @abc.abstractmethod
    def open_verified(self, rel_path: str, hex_digest: str) -> Optional[ImageData]:
        """
        :param rel_path:
        :param hex_digest:
        :return: Returns the image at relative path rel_path if its SHA256 matches hex_digest,
                 otherwise null.
        :raises: IO exceptions if the file is not found or cannot be read
        """
        return None
