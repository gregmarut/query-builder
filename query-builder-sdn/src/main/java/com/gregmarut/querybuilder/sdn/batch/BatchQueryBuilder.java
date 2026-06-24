package com.gregmarut.querybuilder.sdn.batch;

import com.gregmarut.querybuilder.cypher.CypherBuilder;
import com.gregmarut.querybuilder.cypher.CypherQuery;
import com.gregmarut.querybuilder.cypher.Identifiable;
import com.gregmarut.querybuilder.cypher.IdentifierGenerator;
import com.gregmarut.querybuilder.cypher.LiteralCypherString;
import com.gregmarut.querybuilder.cypher.Merge;
import com.gregmarut.querybuilder.cypher.Path;
import com.gregmarut.querybuilder.cypher.Relationship;
import com.gregmarut.querybuilder.cypher.Variable;
import com.gregmarut.querybuilder.cypher.condition.NotEqualsCondition;
import com.gregmarut.querybuilder.cypher.function.Collect;
import com.gregmarut.querybuilder.cypher.node.Node;
import com.gregmarut.querybuilder.cypher.phrase.Delete;
import com.gregmarut.querybuilder.cypher.phrase.ForEach;
import com.gregmarut.querybuilder.cypher.phrase.Match;
import com.gregmarut.querybuilder.cypher.phrase.SetMerge;
import com.gregmarut.querybuilder.cypher.phrase.Unwind;
import com.gregmarut.querybuilder.cypher.phrase.With;
import com.gregmarut.querybuilder.sdn.PropertyMapper;
import com.gregmarut.querybuilder.sdn.model.BaseNode;
import com.gregmarut.querybuilder.sdn.model.SDNNode;
import com.gregmarut.querybuilder.sdn.util.SDNUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Builds batch Cypher queries for merging and linking {@link BaseNode} objects in Neo4j.
 * All methods are static; this class is not meant to be instantiated.
 */
public class BatchQueryBuilder
{
	private BatchQueryBuilder()
	{
	}
	
	/**
	 * Builds a list of {@link BatchMergeQuery} objects that upsert the given nodes using UNWIND-based Cypher queries.
	 * Nodes are grouped by class so that heterogeneous collections each produce a separate query.
	 *
	 * @param nodes the collection of nodes to merge
	 * @param <N>   the type of node
	 * @return one {@link BatchMergeQuery} per unique node class
	 */
	public static <N extends BaseNode> List<BatchMergeQuery> buildBatchMergeQueries(final Collection<N> nodes)
	{
		//group nodes by class so heterogeneous collections each get their own UNWIND query
		final Map<Class<? extends BaseNode>, List<N>> byClass = nodes.stream().collect(
			Collectors.groupingBy(
				SDNUtil::getOriginalClass,
				LinkedHashMap::new,
				Collectors.toList()
			));
		
		return byClass.entrySet().stream().map((e) -> {
			final var nodeClass = e.getKey();
			final var nodeList = e.getValue();
			
			//build a list of property maps, one per node, with type conversions applied
			final var rows = nodeList.stream()
				.map(node -> PropertyMapper.map(SDNUtil.extractModifiedProperties(node)))
				.toList();
			
			final var unwind = new Unwind<>(Variable.of(rows), "row");
			final var row = unwind.getAlias();
			final var n = SDNNode.of(nodeClass).named("n");
			n.withProperty(n.getIdField(), new LiteralCypherString(row.getAlias() + "." + n.getIdField()));
			
			final var query = CypherBuilder.create()
				.unwind(unwind)
				.merge(new Merge(n))
				.set(new SetMerge(n, row))
				.build();
			
			return new BatchMergeQuery(query, n.getLabel(), nodeList.size());
		}).toList();
	}
	
	/**
	 * Builds a list of {@link BatchLinkQuery} objects that create relationships between nodes using UNWIND-based Cypher queries.
	 * Each query targets a single relationship type and produces a statement of the form:
	 * <pre>
	 * UNWIND $rows AS row
	 * MATCH (a:RelatedLabel{id: row.fromId})
	 * MATCH (b:ParentLabel{id: row.toId})
	 * MERGE (b)-[:REL_TYPE]->(a)
	 * </pre>
	 * Relationships are grouped by type, direction, parent node class, and related node class so that
	 * heterogeneous collections produce separate queries per unique combination.
	 * <p>
	 * This method always uses additive MERGE semantics — it never removes existing edges. To use
	 * replace semantics for singular relationships, call {@link #buildBatchReplaceLinkQueries(Collection)} instead.
	 * </p>
	 *
	 * @param nodes the collection of nodes whose modified relationships should be linked
	 * @param <N>   the type of node
	 * @return one {@link BatchLinkQuery} per unique relationship group
	 */
	public static <N extends BaseNode> List<BatchLinkQuery> buildBatchLinkQueries(final Collection<N> nodes)
	{
		return buildLinkQueries(nodes, false);
	}
	
	/**
	 * Builds a list of {@link BatchLinkQuery} objects using replace semantics for singular relationships.
	 * For plural relationships (the declaring field is a {@link Collection}) the query is a simple MERGE,
	 * identical to {@link #buildBatchLinkQueries(Collection)}. For singular relationships the query first
	 * removes any stale edge before merging the new one, ensuring at most one peer exists at any time:
	 * <pre>
	 * UNWIND $rows AS row
	 * MATCH (a:RelatedLabel{id: row.fromId})
	 * MATCH (b:ParentLabel{id: row.toId})
	 * OPTIONAL MATCH (stale:RelatedLabel)-[:REL_TYPE]->(b) WHERE stale &lt;&gt; a
	 * WITH a, b, COLLECT(staleRel) AS staleRels
	 * FOREACH (x IN staleRels | DELETE x)
	 * MERGE (a)-[:REL_TYPE]->(b)
	 * </pre>
	 * Relationships are grouped by type, direction, parent node class, and related node class so that
	 * heterogeneous collections produce separate queries per unique combination.
	 *
	 * @param nodes the collection of nodes whose modified relationships should be linked
	 * @param <N>   the type of node
	 * @return one {@link BatchLinkQuery} per unique relationship group
	 */
	public static <N extends BaseNode> List<BatchLinkQuery> buildBatchReplaceLinkQueries(final Collection<N> nodes)
	{
		return buildLinkQueries(nodes, true);
	}
	
	/**
	 * Shared implementation for {@link #buildBatchLinkQueries(Collection)} and {@link #buildBatchReplaceLinkQueries(Collection)}.
	 * When {@code replaceSemantics} is {@code true}, singular relationships use the OPTIONAL MATCH + COLLECT + FOREACH
	 * pattern to remove stale edges before merging. When {@code false}, all relationships use a plain MERGE.
	 *
	 * @param nodes            the collection of nodes whose modified relationships should be linked
	 * @param replaceSemantics whether to use replace semantics for singular relationships
	 * @return one {@link BatchLinkQuery} per unique relationship group
	 */
	private static <N extends BaseNode> List<BatchLinkQuery> buildLinkQueries(final Collection<N> nodes, final boolean replaceSemantics)
	{
		// Groups all relationship rows by the combination of relationship type + direction + node classes.
		// Each unique combination becomes its own UNWIND query so that the MATCH labels and relationship
		// type are always homogeneous within a single query.
		record GroupKey(
			String relationshipValue,
			org.springframework.data.neo4j.core.schema.Relationship.Direction direction,
			Class<? extends BaseNode> relatedClass,  // the node on the other end of the relationship
			Class<? extends BaseNode> parentClass    // the node that declares the @Relationship field
		)
		{
		}
		
		// Each entry maps a GroupKey to the list of {fromId, toId} row maps that will be passed to UNWIND.
		// fromId = id of the related (other-end) node; toId = id of the parent (declaring) node.
		final Map<GroupKey, List<Map<String, Object>>> byGroup = new LinkedHashMap<>();
		
		for (final N node : nodes)
		{
			final var parentIdField = SDNUtil.getIDField(node);
			final var parentId = PropertyMapper.map(
				SDNUtil.extractProperties(node, Set.of(parentIdField), false).get(parentIdField.getName()));
			
			for (final var rel : SDNUtil.extractModifiedRelationships(node))
			{
				final var relatedNode = rel.getTarget();
				final var relatedIdField = SDNUtil.getIDField(relatedNode);
				final var relatedId = PropertyMapper.map(
					SDNUtil.extractProperties(relatedNode, Set.of(relatedIdField), false).get(relatedIdField.getName()));
				
				final var key = new GroupKey(
					rel.getAnnotation().value(),
					rel.getAnnotation().direction(),
					SDNUtil.getOriginalClass(relatedNode),
					SDNUtil.getOriginalClass(node)
				);
				
				// Each row carries the two IDs needed to MATCH and MERGE both ends of the relationship.
				final var row = new HashMap<String, Object>();
				row.put("fromId", relatedId);  // used to MATCH node (a) — the related node
				row.put("toId", parentId);     // used to MATCH node (b) — the parent node
				
				byGroup.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
			}
		}
		
		return byGroup.entrySet().stream().map(e -> {
			final var key = e.getKey();
			final var rows = e.getValue();
			
			final var unwind = new Unwind<>(Variable.of(rows), "row");
			final var row = unwind.getAlias();
			
			// (a) represents the related node — matched by row.fromId
			final var a = SDNNode.of(key.relatedClass()).named("a");
			a.withProperty(a.getIdField(), new LiteralCypherString(row.getAlias() + ".fromId"));
			
			// (b) represents the parent node — matched by row.toId
			final var b = SDNNode.of(key.parentClass()).named("b");
			b.withProperty(b.getIdField(), new LiteralCypherString(row.getAlias() + ".toId"));
			
			final var relationship = new Relationship(key.relationshipValue());
			
			// The @Relationship direction is relative to the parent class:
			//   OUTGOING → (b)-[:REL]->(a)  i.e. parent points to related
			//   INCOMING → (a)-[:REL]->(b)  i.e. related points to parent
			final Path mergePath = switch (key.direction())
			{
				case OUTGOING -> Path.start(b).out(relationship).to(a).build();
				case INCOMING -> Path.start(a).out(relationship).to(b).build();
			};
			
			//use replace semantics when opted in and the field is singular
			final CypherQuery query = replaceSemantics && SDNUtil.isSingularRelationship(key.parentClass(), key.relationshipValue(), key.direction())
				? buildSingularLinkQuery(unwind, a, key.relatedClass(), b, key.relationshipValue(), key.direction(), mergePath)
				: CypherBuilder.create()
				.unwind(unwind)
				.match(new Match(a))
				.match(new Match(b))
				.merge(new Merge(mergePath))
				.build();
			
			return new BatchLinkQuery(query, key.relationshipValue(), rows.size());
		}).toList();
	}
	
	/**
	 * Builds a {@link CypherQuery} for a singular relationship that removes any stale edge before merging the new one.
	 * Singular relationships have exactly one peer at a time; a plain MERGE would accumulate edges when the peer changes.
	 * <p>
	 * Uses OPTIONAL MATCH + COLLECT + FOREACH to safely delete any stale edge. COLLECT converts the
	 * potentially-null OPTIONAL MATCH result into an empty list so that FOREACH is a no-op when no
	 * stale edge exists — avoiding a {@code DELETE null} error on Neo4j 5.x.
	 * </p>
	 *
	 * @param unwind    the UNWIND clause already built in the caller
	 * @param a         the related node matched by row.fromId
	 * @param aClass    the Java class for the related node
	 * @param b         the parent node matched by row.toId (already bound; reused so the context omits its label in OPTIONAL MATCH)
	 * @param relValue  the relationship type string (e.g. "RECEIVED")
	 * @param direction the direction relative to the parent node
	 * @param mergePath the path used for the final MERGE clause
	 * @return a {@link CypherQuery} implementing the replace-then-merge pattern
	 */
	private static CypherQuery buildSingularLinkQuery(final Unwind<?> unwind, final Node a, final Class<? extends BaseNode> aClass,
		final Node b, final String relValue, final org.springframework.data.neo4j.core.schema.Relationship.Direction direction, final Path mergePath)
	{
		final var generator = new IdentifierGenerator();
		
		//use next() for stale identifiers so they never collide with the hardcoded "a" and "b" in scope
		final var staleNodeAlias = generator.next();
		final var staleRelAlias = generator.next();
		
		final var staleNode = SDNNode.of(aClass).named(staleNodeAlias);
		final var staleRel = Relationship.of(relValue, staleRelAlias);
		
		//b is already bound from the preceding MATCH, so using the same instance causes the context
		//to render it as just (b) — no label — avoiding the redundant label deprecation in Neo4j 5.x
		final Path stalePath = switch (direction)
		{
			case OUTGOING -> Path.start(b).out(staleRel).to(staleNode).build();
			case INCOMING -> Path.start(staleNode).out(staleRel).to(b).build();
		};
		
		//WHERE stale <> a — skip when the existing peer is already the correct node
		final var whereNotSame = new NotEqualsCondition(
			LiteralCypherString.of(staleNodeAlias),
			LiteralCypherString.of(a.getRequiredIdentifier())
		);
		
		//collect so FOREACH is a no-op (iterates empty list) when no stale edge exists, avoiding DELETE null
		final var staleRelsAlias = generator.next();
		final var collectedStaleRels = Collect.of(LiteralCypherString.of(staleRelAlias)).as(staleRelsAlias);
		
		final var loopVarName = generator.next();
		final Identifiable loopVar = () -> loopVarName;
		
		return CypherBuilder.create()
			.unwind(unwind)
			.match(new Match(a))
			.match(new Match(b))
			.match(Match.optional(stalePath).where(whereNotSame))
			.with(new With().add(a).add(b).add(collectedStaleRels))
			.forEach(new ForEach(loopVar, LiteralCypherString.of(staleRelsAlias), Delete.delete(loopVar)))
			.merge(new Merge(mergePath))
			.build();
	}
}
