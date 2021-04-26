from typing import Union

from mode import Service
from neo4j import GraphDatabase, Neo4jDriver, Session

from plot_point_repo import _register_plot_points, _enroll_corpus, _enroll_binary_term, _enroll_printable_term, \
    _enroll_term_pair, check_proportions, compute_dimensions, FixByEnum, TermStyle


class WordArtRepository(Service):
    _driver: Neo4jDriver

    def __init__(self, driver: Neo4jDriver):
        Service.__init__(self)
        self._driver: Neo4jDriver = driver

    async def on_stop(self):
        driver.close()

    def register_plot_points(self, pixel_height: int, pixel_width: int, left: int, right: int,
                             pixel_unit: int = 1, fix_by: FixByEnum = FixByEnum.MATCH_ASPECT) -> None:
        session: Session
        with self._driver.session() as session:
            session.write_transaction(_register_plot_points, pixel_height, bottom, top,
                                      pixel_width, left, right, pixel_unit=pixel_unit, fix_by=fix_by)

    def enroll_corpus(self, corpus_url: str, resource_salt: str) -> None:
        session: Session
        with self._driver.session() as session:
            session.write_transaction(_enroll_corpus, corpus_url, resource_salt)

    def enroll_printable_term(self, corpus_url: str, word_term: str) -> None:
        session: Session
        with self._driver.session() as session:
            session.write_transaction(_enroll_printable_term, corpus_url, word_term)

    def enroll_binary_term(self, corpus_url: str, bin_term: bytes) -> None:
        session: Session
        with self._driver.session() as session:
            session.write_transaction(_enroll_binary_term, corpus_url, bin_term)

    def enroll_paint_subject(self, corpus_url: str, subject_url: str,
                             prefix: Union[bytes, str], prefix_style: TermStyle,
                             suffix: Union[bytes, str], suffix_style: TermStyle) -> None:
        session: Session
        with self._driver.session() as session:
            session.write_transaction(_enroll_term_pair, corpus_url, pair_uuid,
                                      prefix, prefix_style, suffix, suffix_style)

    def accept_painted_subject(self, corpus_url: str, subject_url: str, ):
uri = "neo4j://localhost:7687"
driver = GraphDatabase.driver(uri, auth=("neo4j", "mySecretPassword"))

WordArtRepository(driver)
