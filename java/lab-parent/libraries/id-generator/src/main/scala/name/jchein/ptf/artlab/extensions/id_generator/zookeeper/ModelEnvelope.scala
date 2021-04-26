package name.jchein.ptf.artlab.extensions.id_generator.zookeeper

final case class SchemaVersion(major: Int, minor: Int, patch: Int, label: String)

final case class ModelEnvelope[Model](
  schemaVersion: SchemaVersion, schemaName: String, payload: Model
)