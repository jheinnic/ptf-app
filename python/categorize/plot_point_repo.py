import base64
import hashlib
from enum import Enum
from typing import Union

from neo4j import Transaction
from plot_points import check_proportions, compute_dimensions


class FixByEnum(Enum):
    MATCH_ASPECT = "match_aspect"
    FILL_SPACE = "to_fill"
    FIT_SPACE = "to_fit"


class TermStyle(Enum):
    PRINTABLE_ENCODED = "printable_string"
    PRINTABLE_DECODED = "printable_bytes"
    BASE64_ENCODED = "binary_string"
    BINARY_DECODED = "binary_bytes"


def _register_plot_points(tx: Transaction, height: int, bottom: int, top: int, width: int, left: int, right: int,
                          fix_by: FixByEnum = FixByEnum.MATCH_ASPECT, pixel_unit: int = 1):
    (bottom2, top2, left2, right2) = check_proportions(height, bottom, top, width, left, right, fix_by='square')
    (height_dim, width_dim) = compute_dimensions(height, bottom2, top2, width, left2, right2, pixel_unit=pixel_unit)

    plot_hash = hashlib.sha1()
    plot_hash.update(b'Vertical')
    for ii in height_dim:
        plot_hash.update(ii.tobytes())
    plot_hash.update(b'Horizontal')
    for ii in width_dim:
        plot_hash.update(ii.tobytes())
    plot_hash = plot_hash.hexdigest()

    # TODO: Commit the dimension lists to an object store using plot_hash for 
    #       a key

    if (top2 == top) and (right2 == right):
        tx.run("MERGE (r:Resolution {height: $height, width: $width}) "
               "MERGE (f:Frame {bottom: $bottom, top: $top, left: $left, right: $right})-[:CONFORMS_TO {at_density: $density}]->(r) "
               "MERGE (p:PlotPlan {plot_hash: $plot_hash})-[:AT_SIZE {at_density: $density}]->(r) "
               "ON CREATE SET p.created_at = timestamp() "
               "MERGE (p)-[:PAINTS_FROM {aspect: 'true'}]->(f) "
               "RETURN p.created_at, p.density",
               density=(height / (top - bottom)), height=height, width=width,
               bottom=bottom, top=top, left=left, right=right, plot_hash=plot_hash)
    else:
        tx.run("MERGE (r:Resolution {height: $height, width: $width})"
               "MERGE (fa:Frame {bottom: $bottom2, top: $top2, left: $left2, right: $right2})-[:CONFORMS_TO {at_density: $density}]->(r) "
               "MERGE (ff:Frame {bottom: $bottom, top: $top, left: $left, right: $right})-[:ADJUSTS_TO {height=$height, width=$width, fix_by=$fix_by}]->(fa)"
               "MERGE (p:PlotPlan {plot_hash: $plot_hash})-[:AT_SIZE {at_density: $density}]->(r) "
               "ON CREATE SET p.created_at = timestamp() "
               "MERGE (p)-[:PAINTS_FROM {aspect='true'}]->(fa) "
               "MERGE (p)-[:PAINTS_FROM {aspect=$fix_by}]->(ff) "
               "RETURN p.created_at, p.density",
               density=(height / (top2 - bottom2)), height=height, width=width,
               bottom2=bottom2, top2=top2, left2=left2, right2=right2,
               bottom=bottom, top=top, left=left, right=right, plot_hash=plot_hash)


def _enroll_corpus(tx: Transaction, corpus: str, salt: bytes) -> None:
    tx.run("MERGE (c:Corpus {uri: $uri, salt: $salt}) "
           "RETURN $uri as uri", uri=corpus, salt=salt)


def _enroll_printable_term(tx: Transaction, corpus: str, term: str) -> (str, int, str):
    tx.run("MATCH (c:Corpus {uri: 'uri'}) "
           "WITH c as c, apoc.util.sha1([c.salt, $word_bytes]) as key "
           "OPTIONAL MATCH (c)-[:INCLUDES]->(t:Term {key: key}) "
           "WHERE t.raw <> $word_bytes "
           "WITH c as c, key as key, count(t) as collisions "
           "MERGE (c)-[:INCLUDES]->(t:Term:PrintableTerm {key: key, index: (collisions)}) "
           "ON CREATE SET t.raw = $word_bytes, t.printable = $word, t.created_at = timestamp() "
           "RETURN t.key, t.index, $word as word ", uri=corpus, word=term, word_bytes=term.encode())


def _enroll_binary_term(tx: Transaction, corpus: str, bin_term: bytes) -> (str, int, str):
    tx.run("MATCH (c:Corpus {uri: 'uri'}) "
           "WITH c as c, apoc.util.sha1([c.salt, $bin_term]) as key "
           "OPTIONAL MATCH (c)-[:INCLUDES]->(t:Term {key: key}) "
           "WHERE t.raw <> $bin_term "
           "WITH c as c, key as key, count(t) as collisions "
           "MERGE (c)-[:INCLUDES]->(t:Term:BinaryTerm {key: key, index: (collisions)}) "
           "ON CREATE SET t.raw = $bin_term, t.display_as = $display_term, t.created_at = timestamp() "
           "RETURN t.key, t.index, $display_term as display_term ",
           uri=corpus, bin_term=bin_term, display_term=base64.b64encode(bin_term))


def _validate_term_style(role: str, term: Union[bytes, str], style: TermStyle) -> bytes:
    if style == TermStyle.BASE64_ENCODED or style == TermStyle.PRINTABLE_ENCODED:
        if not isinstance(term, str):
            raise ValueError(f"{role} {term} must be a string to use string-encoding {style}")
        elif style == TermStyle.BASE64_ENCODED:
            return base64.b64decode(term)
        else:
            return term.encode()
    elif style == TermStyle.BINARY_DECODED or style == TermStyle.BINARY_DECODED:
        if not isinstance(term, bytes):
            raise ValueError(f"{role} {term} must be bytes to use bytes-encoding {style}")
    else:
        raise ValueError(f"{role} {term} provided with unrecognized expression style, {style}")
    return term


def _enroll_term_pair(tx: Transaction, corpus: str, pair_uuid: str,
                      prefix_bytes: Union[bytes, str], prefix_style: TermStyle,
                      suffix_bytes: Union[bytes, str], suffix_style: TermStyle) -> None:
    prefix: bytes = _validate_term_style("Prefix", prefix_bytes, prefix_style)
    suffix: bytes = _validate_term_style("Suffix", suffix_bytes, suffix_style)
    tx.run("MATCH (c:Corpus {uri: 'uri'}) "
           "WITH c as c, apoc.util.sha1([c.salt, $prefix_term]) as prefix_key, "
           "     apoc.util.sha1([c.salt, $suffix_term]) as suffix_key "
           "MATCH (c)-[:INCLUDES]->(pt:term {key: prefix_key, raw: $prefix_term}) "
           "MATCH (c)-[:INCLUDES]->(st:term {key: suffix_key, raw: $suffix_term}) "
           "WHERE NOT MATCH (c)-[:OWNS]->(e:PaintableTermPair)-[:HAS_PREFIX]->pt "
           "  AND NOT MATCH (c)-[:OWNS]->(e)-[:HAS_SUFFIX]->st "
           "CREATE (c)-[:OWNS]->(p:PaintableTermPair {uuid: pair_uuid, created_at: timestamp()}) "
           "MERGE (p)-[:HAS_PREFIX]->(pt) "
           "MERGE (p)-[:HAS_SUFFIX]->(st) "
           "RETURN p.uuid, p.created_at", uri=corpus, prefix_term=prefix, suffix_term=suffix)