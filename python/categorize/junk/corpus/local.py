import abc
import hashlib
import os

import typing
from os import PathLike
from os import path as osp

from typing.io import BinaryIO

from categorize.junk.corpus.types import (ImageItem, ImagePublisher, ImageData)


class ImagesDirectory(typing.Iterable[ImageItem], ImagePublisher):
    def __init__(self, root_dir: PathLike, to_subject_name: typing.Callable[[str], str]):
        if not osp.isdir(root_dir):
            raise ValueError(
                "root_dir must be an existing directory with image files"
            )
        self._root_dir: PathLike = root_dir
        self._to_subject_name: typing.Callable[[str], str] = to_subject_name

    def __iter__(self) -> typing.Iterator[ImageItem]:
        dir_path: PathLike
        dir_names: typing.Iterator[str]
        file_names: typing.Iterator[str]
        for dir_path, dir_names, file_names in os.walk(self._root_dir):
            for file_name in file_names:
                rel_file_path: str = osp.join(dir_path, file_name)
                abs_file_path: str = osp.join(self._root_dir, rel_file_path)
                subject_name: str = self._to_subject_name(rel_file_path)
                with open(abs_file_path, "rb") as f:
                    content: bytes = f.read()
                    hex_digest: str = hashlib.sha256(content).hexdigest()
                    yield ImageItem(rel_file_path, subject_name, hex_digest)

    def open(self, rel_file_path: str) -> typing.Optional[BinaryIO]:
        abs_file_path: str = osp.join(self._root_dir, rel_file_path)
        if not osp.isfile(abs_file_path):
            raise FileNotFoundError("No image at " + abs_file_path)
        return open(abs_file_path, "rb")

    def open_verified(self, rel_file_path: str, hex_digest: str) -> typing.Optional[ImageData]:
        abs_file_path: str = osp.join(self._root_dir, rel_file_path)
        if not osp.isfile(abs_file_path):
            raise FileNotFoundError("No image at " + abs_file_path)
        with open(abs_file_path, "rb") as f:
            content: bytes = f.read()
            actual_digest: str = hashlib.sha256(content).hexdigest()
            if hex_digest == actual_digest:
                image_item = ImageItem(rel_file_path, self._to_subject_name(rel_file_path), hex_digest)
                return ImageData(image_item, content)
        return None
