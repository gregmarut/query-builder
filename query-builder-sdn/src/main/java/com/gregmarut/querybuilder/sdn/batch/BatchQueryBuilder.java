package com.gregmarut.querybuilder.sdn.batch;

import com.gregmarut.querybuilder.cypher.CypherBuilder;
import com.gregmarut.querybuilder.cypher.LiteralCypherString;
import com.gregmarut.querybuilder.cypher.Merge;
import com.gregmarut.querybuilder.cypher.Variable;
import com.gregmarut.querybuilder.cypher.phrase.SetMerge;
import com.gregmarut.querybuilder.cypher.phrase.Unwind;
import com.gregmarut.querybuilder.sdn.PropertyMapper;
import com.gregmarut.querybuilder.sdn.model.BaseNode;
import com.gregmarut.querybuilder.sdn.model.SDNNode;
import com.gregmarut.querybuilder.sdn.util.SDNUtil;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
}
