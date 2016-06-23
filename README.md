# Meta-Predicates for Event-B in Rodin
An experimental plugin for Rodin that extends the predicate syntax for guards and invariants by allowing to reference other guards.

Examples will follow once the plugin is less experimental.

[![Build Status](https://travis-ci.org/wysiib/RodinMetaPredicatesPlugin.svg)](https://travis-ci.org/wysiib/RodinMetaPredicatesPlugin)


## Building
Maven 3 is required to build the project:
  <pre>
  cd de.stups.hhu.rodinaxiompos.parent
  mvn clean verify
  </pre>  

This will produce an updatesite in de.stups.hhu.rodinaxiompos.repository/target

We autmatically produce nightly builds that can be installed using the update site located at  https://www3.hhu.de/stups/rodin/meta_predicates/nightly/
.

## Contributing/Bugs
Pull requests are very welcome. Suggestions for new extensions and known bugs are tracked on [Github](https://github.com/wysiib/RodinMetaPredicatesPlugin/issues)
