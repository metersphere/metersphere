package io.metersphere.functional.utils;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ext.tables.*;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import io.metersphere.functional.constants.CaseMdTitleConstants;
import io.metersphere.functional.constants.FunctionalCaseTypeConstants;
import io.metersphere.functional.dto.FunctionalCaseAIStep;
import io.metersphere.functional.dto.FunctionalCaseAiDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author song-cc-rock
 */
public class MdUtil {

	public static final String MD_START_TAG = "featureCaseStart";

	/**
	 * Markdown内容转换为Case对象
	 *
	 * @param content Markdown内容
	 * @return Case对象
	 */
	public static FunctionalCaseAiDTO transformToCaseDTO(String content) {
		try {
			MutableDataSet options = new MutableDataSet();
			options.set(Parser.EXTENSIONS, List.of(TablesExtension.create()));
			Parser parser = Parser.builder(options).build();
			Node document = parser.parse(content);
			FunctionalCaseAiDTO caseDTO = new FunctionalCaseAiDTO();
			String currentSection = "";
			for (Node node : document.getChildren()) {
				if (node instanceof Heading) {
					int level = ((Heading) node).getLevel();
					String headText = ((Heading) node).getText().toString();
					if (level == 2) {
						caseDTO.setName(headText.trim());
					} else if (level == 3) {
						currentSection = headText.trim();
					}
				} else {
					if (currentSection.contains(CaseMdTitleConstants.PRE_REQUISITE)) {
						caseDTO.setPrerequisite(appendHtml(caseDTO.getPrerequisite(), toHtml(node)));
					} else if (currentSection.contains(CaseMdTitleConstants.TEXT_DESCRIPTION)) {
						caseDTO.setTextDescription(appendHtml(caseDTO.getTextDescription(), toHtml(node)));
						caseDTO.setCaseEditType(FunctionalCaseTypeConstants.CaseEditType.TEXT.name());
					} else if (currentSection.contains(CaseMdTitleConstants.EXPECTED_RESULT)) {
						caseDTO.setExpectedResult(appendHtml(caseDTO.getExpectedResult(), toHtml(node)));
					} else if (currentSection.contains(CaseMdTitleConstants.STEP_DESCRIPTION)) {
						List<FunctionalCaseAIStep> steps = new ArrayList<>();
						stepNodeTableToObj(node, steps);
						caseDTO.setSteps(JSON.toJSONString(steps));
						caseDTO.setCaseEditType(FunctionalCaseTypeConstants.CaseEditType.STEP.name());
					} else if (currentSection.contains(CaseMdTitleConstants.DESCRIPTION)) {
						caseDTO.setDescription(appendHtml(caseDTO.getDescription(), toHtml(node)));
					}
				}
			}

			return caseDTO;
		} catch (Exception e) {
			throw new MSException(e.getMessage());
		}
	}

	/**
	 * 批量转换Markdown内容为Case对象列表
	 *
	 * @param content Markdown内容
	 * @return Case对象列表
	 */
	public static List<FunctionalCaseAiDTO> batchTransformToCaseDTO(String content) {
		String[] contentBlock = content.split(MD_START_TAG);
		List<FunctionalCaseAiDTO> aiCases = new ArrayList<>();
		for (String block : contentBlock) {
			if (StringUtils.isEmpty(block.trim())) {
				continue;
			}
			FunctionalCaseAiDTO caseAiDTO = transformToCaseDTO(block);
			aiCases.add(caseAiDTO);
		}
		return aiCases;
	}

	/**
	 * Markdown步骤描述表格节点转换为步骤对象
	 *
	 * @param node     Markdown节点
	 * @param steps    用例步骤列表
	 */
	private static void stepNodeTableToObj(Node node, List<FunctionalCaseAIStep> steps) {
		if (node instanceof TableBlock) {
			for (Node tableNode : node.getChildren()) {
				if (tableNode instanceof TableHead) {
					continue;
				}
				if (tableNode instanceof TableBody) {
					int index = 0;
					for (Node rowNode : tableNode.getChildren()) {
						if (rowNode instanceof TableRow) {
							List<String> cells = new ArrayList<>();
							for (Node cell : rowNode.getChildren()) {
								if (cell instanceof TableCell) {
									cells.add(((TableCell) cell).getText().toString().replaceAll("(?i)<br\\s*/?>", "\n"));
								}
							}
							if (cells.size() >= 2) {
								FunctionalCaseAIStep step = new FunctionalCaseAIStep();
								step.setNum(index);
								step.setDesc(cells.get(0));
								step.setResult(cells.get(1));
								steps.add(step);
								index++;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 将Markdown节点转换为HTML字符串
	 * @param node 节点
	 * @return HTML字符串
	 */
	private static String toHtml(Node node) {
		MutableDataSet options = new MutableDataSet();
		options.set(Parser.EXTENSIONS, List.of(TablesExtension.create()));
		Parser parser = Parser.builder(options).build();
		HtmlRenderer renderer = HtmlRenderer.builder(options).build();
		BasedSequence fragment = node.getChars();
		Node parsedFragment = parser.parse(fragment.toString());
		return renderer.render(parsedFragment);
	}

	/**
	 * 追加html
	 *
	 * @param original 原有字符串
	 * @param addition 追加的字符串
	 * @return 合并后的字符串
	 */
	private static String appendHtml(String original, String addition) {
		if (original == null) {
			return addition;
		}
		return original + addition;
	}
}
