rootProject.name = "datamaintain"
include(
        "modules:core",
        "modules:cli",
        "modules:driver-mongo",
        "modules:driver-mongo-mapping:driver-mongo-mapping-test",
        "modules:driver-mongo-mapping:driver-mongo-mapping-serialization",
        "modules:driver-mongo-mapping:driver-mongo-mapping-jackson",
        "modules:driver-mongo-mapping:driver-mongo-mapping-gson",
        "modules:driver-jdbc",
        "modules:test",
        "samples:java-mongo",
        "samples:java-postgresql"
)
