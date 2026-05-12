# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build all modules
mvn clean install

# Run tests for all modules
mvn test

# Run tests for a single module
mvn -pl query-builder-cypher test
mvn -pl query-builder-sdn test

# Increment version (updates all pom.xml files)
./increment-version.sh minor   # x.Y.0
./increment-version.sh patch   # x.y.Z

# Publish to Maven Central (requires GPG key configured)
mvn deploy -P sign
```

## Architecture

This is a Maven multi-module Java 21 library (groupId `com.gregmarut.querybuilder`, current version in parent `pom.xml`) that provides a fluent API for constructing database queries across multiple backends. There is no application entry point — all modules are library artifacts.

### Module Dependency Graph

```
query-builder-core          (predicates, QueryBuilder base, Sort, Path)
    ↑
query-builder-jpa           (JPA Criteria API)
query-builder-mongodb       (Spring Data MongoDB)
query-builder-cypher        (Cypher query string builder → CypherQuery)
    ↑
query-builder-sdn           (Spring Data Neo4j executor, NodeProxy, BatchQueryBuilder)

neo4j-sdnmodelgen           (annotation processor → *_ metamodel for @Node classes)
mongodb-modelgen            (annotation processor → *_ metamodel for @Document classes)
```

### query-builder-core

Defines the shared predicate model used by JPA and MongoDB implementations: `Predicate`, `AndPredicate`, `OrPredicate`, `EqualsPredicate`, `InPredicate`, `FuzzyMatchPredicate`, etc. `QueryBuilder<B,E>` is the abstract base builder that accumulates `Predicate` instances and a `Sort` list. `Path` represents a dotted field path rooted at a named entity alias.

### query-builder-cypher

Produces `CypherQuery` (an immutable `(query: String, params: Map<String,Object>)` pair) via `CypherBuilder`, a fluent builder. Key concepts:

- **Phrases** (`phrase/` package): `Match`, `Where`, `With`, `Unwind`, `Return`, `Merge`, `Delete`, `Remove`, `SetMerge` — each implements `CypherPhrase` and knows how to render itself to a Cypher fragment and emit its named parameters.
- **Conditions** (`condition/` package): `EqualsCondition`, `AndCondition`, `OrCondition`, `InCondition`, etc. — composable predicates for `WHERE` clauses.
- **Nodes** (`node/` package): `Node` (anonymous), `LabeledNode`, `TypedNode` (typed to an SDN entity class), `MutableNode` (carries properties for MERGE/SET).
- **`IdentifierGenerator`**: assigns short sequential variable names (`_v0`, `_v1`, …) to node/relationship patterns. Variables are parameterized with the `$` prefix.
- **`QueryBuilderContext`**: controls statement separator character and optional pretty-print mode (`QueryBuilderContext.defaultPrettyPrint`).
- `CypherBuilder.build()` assembles all phrases in order, then appends `RETURN`, `ORDER BY`, `SKIP`, `LIMIT`.

### query-builder-sdn

Spring Data Neo4j integration layer. Depends on `query-builder-cypher`.

- **`SDNNode`**: a `TypedNode` subclass that reads the `@Node` label and `@Id` field from an SDN entity class at runtime using reflection.
- **`BaseNode`**: must be extended by all SDN `@Node` entity classes in consuming projects. Implements `equals`/`hashCode` via the `@Id` field.
- **`NodeProxy`** (ByteBuddy): wraps a `BaseNode` subclass instance with a dynamically generated subclass that carries a hidden `__snapshot` field. `SDNUtil.extractModifiedProperties()` and `extractModifiedRelationships()` diff the current state against the snapshot to identify what changed — this is what powers selective `SET` in upserts and batch merge operations.
- **`BatchQueryBuilder`**: produces UNWIND-based bulk Cypher queries. `buildBatchMergeQueries()` groups nodes by class and emits one `UNWIND … MERGE … SET` per class. `buildBatchLinkQueries()` groups relationships by type+direction+node-class pair and emits `UNWIND … MATCH … MATCH … MERGE (path)` queries.
- **`SDNQueryExecutor`** / **`QueryExecutor`**: Spring `@Service` that wraps `Neo4jTemplate`. Call `prepare(CypherQuery)` → `PreparedCypherQuery` (a `Runnable`) or `prepare(TypedCypherQuery<T>)` → `PreparedResultCypherQuery<T>` (a `Supplier<List<T>>`).

### Annotation Processors (modelgen modules)

`neo4j-sdnmodelgen` and `mongodb-modelgen` are Java annotation processors. They scan for `@Node` / `@Document` classes at compile time and generate a `ClassName_` metamodel class with `public static final String FIELD_NAME = "fieldName"` constants (field names in `UPPER_SNAKE_CASE`, values as the original camelCase string). Add these as `annotationProcessorPaths` in consuming projects.

## Key Conventions

- All SDN entity classes used with `query-builder-sdn` must extend `BaseNode`.
- `NodeProxy.createProxy(entity)` must be called before tracking changes; the proxy subclass is cached per entity class via `ConcurrentHashMap`.
- `SDNUtil.getOriginalClass()` unwraps ByteBuddy proxy classes to get the real entity class — always use this instead of `getClass()` when looking up SDN metadata.
- Tests use JUnit 5. The Cypher module tests rely on `QueryBuilderContext.defaultPrettyPrint = true` (set in `@BeforeAll`) to produce readable multi-line assertions.
