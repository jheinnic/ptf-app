from neo4j import GraphDatabase
from plot_points import check_proportions, compute_dimensions

uri = "neo4j://localhost:7687"
driver = GraphDatabase.driver(uri, auth=("neo4j", "mySecretPassword"))

def register_plot_points(tx, height, bottom, top, width, left, right, fix_by='square'):
    (bottom2, top2, left2, right2) = check_proportions(height, bottom, top, width, left, right, fix_by='square')
    (height_dim, width_dim) = compute_dimensions(height, bottom2, top2, width, left2, right2)
    if (top2 == top) and (right2 == right):
        tx.run("MERGE (r:Resolution {height: $height, width: $width}) "
               "MERGE (f:Frame {bottom: $bottom, top: $top, left: $left, right: $right}) "
               "MERGE (p:PlotPlan {density: $density, vertical: $height_dim, horizontal: $width_dim}) "
               "ON CREATE SET p.created_at = timestamp() "
               "MERGE (f)-[:CONFORMS_TO {at_density: $density}]->(r) "
               "MERGE (p)-[:AT_SIZE]->(r) "
               "MERGE (p)-[:PAINTS_FROM {aspect: 'true'}]->(f) "
               "RETURN p.created_at, p.density", height=height, width=width,
               bottom=bottom, top=top, left=left, right=right,
               density=(height/(top-bottom)),
               height_dim=height_dim.tolist(), width_dim=width_dim.tolist())
    else:
        tx.run("MERGE (r:Resolution {height=$height, width=$width})"
               "MERGE (fa:Frame {bottom=$bottom2, top=$top2, left=$left2, right=$right2}"
               "MERGE (ff:Frame {bottom=$bottom, top=$top, left=$left, right=$right}"
               "MERGE (p:PlotPlan {density=$density, vertical=$height_dim, horizontal=$width_dim})"
               "ON CREATE SET p.created_at = timestamp()"
               "MERGE (fa)-[:CONFORMS_TO {at_density=$density}]->(r)"
               "MERGE (ff)-[:ADJUSTS_TO {height=$height, width=$width, fix_by=$fix_by}]->(fa)"
               "MERGE (p)-[:AT_SIZE]->(r)"
               "MERGE (p)-[:PAINTS_FROM {aspect='true'}]->(fa)"
               "MERGE (p)-[:PAINTS_FROM {aspect=$fix_by}]->(ff)"
               "RETURN p.created_at, p.density", height=height, width=width,
               bottom=bottom, top=top, left=left, right=right,
               bottom2=bottom2, top2=top2, left2=left2, right2=right2,
               density=(height/(top2-bottom2)),
               height_dim=height_dim.tolist(), width_dim=width_dim.tolist())


with driver.session() as session:
    session.write_transaction(register_plot_points, 892, -1, 1, 892, -1, 1)

driver.close()
