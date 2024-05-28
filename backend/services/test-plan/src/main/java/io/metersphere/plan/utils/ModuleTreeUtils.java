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
	 * @param allNodes 模块集合
	 * @param nodePathMap 树节点路径集合
	 */
	public static void genPathMap(List<TestPlanBaseModule> allNodes, Map<String, String> nodePathMap, List<String> scanIds) {
		if (MapUtils.isEmpty(nodePathMap)) {
			// 根节点遍历
			List<TestPlanBaseModule> rootNodes = allNodes.stream().filter(node ->
					StringUtils.isBlank(node.getParentId()) || StringUtils.equals(node.getParentId(), "NONE")).toList();
			rootNodes.forEach(node -> nodePathMap.put(node.getId(), MODULE_PATH_PREFIX + node.getName()));
			// 下一级父节点
			scanIds = rootNodes.stream().map(TestPlanBaseModule::getId).toList();
		} else {
			// 非根节点遍历
			List<String> finalScanIds = scanIds;
			List<TestPlanBaseModule> scanNodes = allNodes.stream().filter(node -> finalScanIds.contains(node.getParentId())).toList();
			scanNodes.forEach(node -> nodePathMap.put(node.getId(), nodePathMap.getOrDefault(node.getParentId(), StringUtils.EMPTY) + MODULE_PATH_PREFIX + node.getName()));
			// 下一级父节点
			scanIds = scanNodes.stream().map(TestPlanBaseModule::getId).toList();
		}
		if (CollectionUtils.isEmpty(scanIds)) {
			// 叶子节点不存在, 跳出
			return;
		}
		genPathMap(allNodes, nodePathMap, scanIds);
	}
}
