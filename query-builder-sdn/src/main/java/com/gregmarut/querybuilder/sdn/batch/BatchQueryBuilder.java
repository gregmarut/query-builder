package com.gregmarut.querybuilder.sdn.batch;

import com.gregmarut.querybuilder.cypher.CypherBuilder;
import com.gregmarut.querybuilder.cypher.LiteralCypherString;
import com.gregmarut.querybuilder.cypher.Merge;
import com.gregmarut.querybuilder.cypher.Path;
import com.gregmarut.querybuilder.cypher.Relationship;
import com.gregmarut.querybuilder.cypher.Variable;
import com.gregmarut.querybuilder.cypher.phrase.Match;
import com.gregmarut.querybuilder.cypher.phrase.SetMerge;
import com.gregmarut.querybuilder.cypher.phrase.Unwind;
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

public class BatchQueryBuilder
{
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
	 *
	 * @param nodes the collection of nodes whose modified relationships should be linked
	 * @return one {@link BatchLinkQuery} per unique relationship group
	 */
	public static <N extends BaseNode> List<BatchLinkQuery> buildBatchLinkQueries(final Collection<N> nodes)
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
			
			final var query = CypherBuilder.create()
				.unwind(unwind)
				.match(new Match(a))
				.match(new Match(b))
				.merge(new Merge(mergePath))
				.build();
			
			return new BatchLinkQuery(query, key.relationshipValue(), rows.size());
		}).toList();
	}
}
