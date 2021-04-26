from os import makedirs, path as osp

class EnrichmentWriteService:
    def __init__(self, state_dir, cache_dir):
        self._cache_dir = cache_dir
        self._state_dir = state_dir
        self.cache_catalog_file = osp.join(cache_dir, "catalog.dat")
        if osp.isfile:
            self.catalog = None
        else:
            self.catalog = { }

        self.cached_corpus = { corpus -> for corpus from }
        self.state_layers = { }

    def create_enrichment_layer(self, name: str, corpus_name: str) ->:
        new_cache_dir = osp.join(self._cache_dir, corpus_name)
        if not corpus_name in self.cached_corpus:
            makedirs(new_cache_dir)
        if osp.exists(new_env_dir):
            raise NameError("Cannot create new layer due to a filesystem naming clonflict")
        makedirs(new_env_dir)
        self.state_layers[name] = {
            corpus_name: corpus_name
        }



