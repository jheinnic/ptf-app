import hashlib
from neo4j import GraphDatabase
from plot_points import check_proportions, compute_dimensions

uri = "neo4j://localhost:7687"
driver = GraphDatabase.driver(uri, auth=("neo4j", "mySecretPassword"))

def register_plot_points(tx, height, bottom, top, width, left, right, fix_by='square'):
    (bottom2, top2, left2, right2) = check_proportions(height, bottom, top, width, left, right, fix_by='square')
    (height_dim, width_dim) = compute_dimensions(height, bottom2, top2, width, left2, right2)

    plot_hash = hashlib.sha1()
    plot_hash.update(b'Vertical')
    for ii in height_dim:
        plot_hash.update(ii.tobytes())
    plot_hash.update(b'Horizontal')
    for ii in width_dim:
        plot_hash.update(ii.tobytes())
    plot_hash = plot_hash.hexdigest()

    # height_dim = [{"index": ii[0][0], "point": ii[1]} for ii in np.nedumerate(height_dim)]
    # width_dim = [{"index": ii[0][0], "point": ii[1]} for ii in np.nedumerate(width_dim)]
    height_dim = ':'.join((str(ii) for ii in height_dim))
    width_dim = ':'.join((str(ii) for ii in width_dim))

    if (top2 == top) and (right2 == right):
        tx.run("MERGE (r:Resolution {height: $height, width: $width}) "
                "MERGE (f:Frame {bottom: $bottom, top: $top, left: $left, right: $right})-[:CONFORMS_TO {at_density: $density}]->(r) "
                "MERGE (p:PlotPlan {plot_hash: $plot_hash})-[:AT_SIZE {at_density: $density}]->(r) "
               "ON CREATE SET p.created_at = timestamp() "
               "MERGE (p)-[:PAINTS_FROM {aspect: 'true'}]->(f) "
               "RETURN p.created_at, p.density",
               density=(height/(top-bottom)), height=height, width=width,
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
               density=(height/(top2-bottom2)), height=height, width=width,
               bottom2=bottom2, top2=top2, left2=left2, right2=right2,
               bottom=bottom, top=top, left=left, right=right, plot_hash=plot_hash)


with driver.session() as session:
    session.write_transaction(register_plot_points, 892, -1, 1, 892, -1, 1)

driver.close()
