package io.metersphere.plan.utils;

import io.metersphere.plan.dto.TestPlanBaseModule;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 模块树解析相关的工具类
 */
@UtilityClass
public class ModuleTreeUtils {

	public static final String MODULE_PATH_PREFIX = "/";

	/**
	 * 解析并返回模块的全路径
	 * @param filterNodes 模块集合
	 * @param nodePathMap 树节点路径集合
	 */
	private static void realGenPathMap(List<TestPlanBaseModule> filterNodes, Map<String, String> nodePathMap, List<String> scanIds) {
		int lastLoopSize = nodePathMap.size();
		if (MapUtils.isEmpty(nodePathMap)) {
			// 根节点遍历
			List<TestPlanBaseModule> rootNodes = filterNodes.stream().filter(node ->
					StringUtils.isBlank(node.getParentId()) || StringUtils.equals(node.getParentId(), "NONE")).toList();
			rootNodes.forEach(node -> nodePathMap.put(node.getId(), MODULE_PATH_PREFIX + node.getName()));
			// 下一级父节点
			scanIds = rootNodes.stream().map(TestPlanBaseModule::getId).toList();
			filterNodes.removeAll(rootNodes);
		} else {
			// 非根节点遍历
			List<String> finalScanIds = scanIds;
			List<TestPlanBaseModule> scanNodes = filterNodes.stream().filter(node -> finalScanIds.contains(node.getParentId())).toList();
			scanNodes.forEach(node -> nodePathMap.put(node.getId(), nodePathMap.getOrDefault(node.getParentId(), StringUtils.EMPTY) + MODULE_PATH_PREFIX + node.getName()));
			// 下一级父节点
			scanIds = scanNodes.stream().map(TestPlanBaseModule::getId).toList();
			// 每次过滤的数据
			filterNodes.removeAll(scanNodes);
		}
		if (CollectionUtils.isEmpty(scanIds)) {
			// 叶子节点不存在, 跳出
			return;
		}
		if (lastLoopSize == nodePathMap.size()) {
			// 处理前后无新数据产生, 无效的迭代, 跳出;
			return;
		}
		realGenPathMap(filterNodes, nodePathMap, scanIds);
	}

	public static void genPathMap(List<TestPlanBaseModule> allNodes, Map<String, String> nodePathMap, List<String> scanIds) {
		// 过滤掉一些不好的数据
		allNodes.removeIf(node -> StringUtils.equals(node.getId(), "NONE") || StringUtils.equals(node.getId(), node.getParentId()));
		realGenPathMap(allNodes, nodePathMap, scanIds);
	}
}
