from typing import Union


from rdflib.parser import Parser
from rdflib.graph import Graph, BNode
from rdflib import RDF, RDFS, Literal
from rdflib import URIRef

from mode import Service
from neo4j import GraphDatabase, Neo4jDriver, Session

from plot_point_repo import _register_plot_points, _enroll_term_pair, TermStyle
from window_point import WindowPoint


class WordArtRepository(Service):
    _driver: Neo4jDriver

    def __init__(self, _driver: Neo4jDriver):
        Service.__init__(self)
        self._driver: Neo4jDriver = _driver

    async def on_stop(self):
        driver.close()

    def register_plot_points(self, pixel_height: int, pixel_width: int,
                             window_y: float, window_x: float, window_width: float,
                             window_point: WindowPoint, pixel_unit: int = 1) -> None:
        session: Session
        with self._driver.session() as session:
            session.write_transaction(_register_plot_points, pixel_height, pixel_width,
                                      window_y, window_x, window_width, window_point,
                                      pixel_unit=pixel_unit)

    def enroll_paint_subject(self, corpus_url: str, subject_url: str,
                             prefix: Union[bytes, str], prefix_style: TermStyle,
                             suffix: Union[bytes, str], suffix_style: TermStyle) -> None:
        session: Session
        with self._driver.session() as session:
            session.write_transaction(_enroll_term_pair, corpus_url, subject_url,
                                      prefix, prefix_style, suffix, suffix_style)

    # def enroll_corpus(self, corpus_url: str, resource_salt: str) -> None:
    #     session: Session
    #     with self._driver.session() as session:
    #         session.write_transaction(_enroll_corpus, corpus_url, resource_salt)
    #
    # def enroll_printable_term(self, corpus_url: str, word_term: str) -> None:
    #     session: Session
    #     with self._driver.session() as session:
    #         session.write_transaction(_enroll_printable_term, corpus_url, word_term)
    #
    # def enroll_binary_term(self, corpus_url: str, bin_term: bytes) -> None:
    #     session: Session
    #     with self._driver.session() as session:
    #         session.write_transaction(_enroll_binary_term, corpus_url, bin_term)
    #
    # def enroll_paint_subject(self, corpus_url: str, subject_url: str,
    #                          prefix: Union[bytes, str], prefix_style: TermStyle,
    #                          suffix: Union[bytes, str], suffix_style: TermStyle) -> None:
    #     session: Session
    #     with self._driver.session() as session:
    #         session.write_transaction(_enroll_term_pair, corpus_url, subject_url,
    #                                   prefix, prefix_style, suffix, suffix_style)
    #
    # def accept_painted_subject(self, corpus_url: str, subject_url: str):
    #     pass



rdf = Parser().parse("./RandomArtMVP.owl")
print(rdf)


uri = "neo4j://localhost:7687"
driver = GraphDatabase.driver(uri, auth=("neo4j", "mySecretPassword"))

WordArtRepository(driver)
