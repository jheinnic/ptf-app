import appdirs
import typing

import categorize
from os import path as osp, makedirs, rename, PathLike
from singleton_decorator import singleton
from yaml import safe_load, dump

try:
    from yaml import CLoader as Loader, CDumper as Dumper
except ImportError:
    from yaml import Loader, Dumper


@singleton
class Registry:
    def __init__(self):
        self.config_dir: PathLike = osp.normpath(
            appdirs.user_state_dir(
                "categorize", "lumins", categorize.__version__
            )
        )
        self.catalog_store: PathLike = osp.join(
            self.config_dir, "catalogs.yaml"
        )
        if not osp.isdir(self.config_dir):
            makedirs(self.config_dir)
        if osp.isfile(self.catalog_store):
            with open(self.catalog_store, "rb") as file:
                self.catalog_dict: typing.Dict = safe_load(file, loader=Loader)
        else:
            self.catalog_dict: typing.Dict = {}

    def lookup(self, name: str) -> (str, str):
        if name in self.catalog_dict:
            entry = self.catalog_dict[name]
            return entry["type"], entry["location"]
        raise NameError("No such catalog")

    def register(self, name: str, type: str, location: str) -> None:
        if name in self.catalog_dict:
            raise ValueError("Name already registered")
        if type != "graph" and type != "files":
            raise ValueError("Type is graph or files")
        self.catalog_dict[name] = {"type": type, "location": location}

        self._save_changes()

    def _save_changes(self) -> None:
        temp_file = osp.join(self.catalog_store, ".temp")
        with open(temp_file, "wb") as file:
            dump(
                self.catalog_store,
                file,
                Dumper,
                default_flow_style=False,
                encoding="utf-8",
            )
        rename(temp_file, self.catalog_catalog_store)
