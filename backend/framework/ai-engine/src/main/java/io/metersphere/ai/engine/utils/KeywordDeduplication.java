package io.metersphere.ai.engine.utils;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于关键词提取的文本去重工具
 * <p>
 * 关键词覆盖 + Ansj
 */
public class KeywordDeduplication {

    /**
     * 对文本列表进行关键词去重
     */
    public static List<String> deduplicateTexts(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return Collections.emptyList();
        }

        // 提取关键词
        List<TextWithKeywords> textInfoList = extractKeywords(texts);

        // 去重并返回结果
        return removeDuplicates(textInfoList);
    }

    /**
     * 从文本中提取关键词（名词和动词）
     */
    private static List<TextWithKeywords> extractKeywords(List<String> texts) {
        return texts.stream().map(text -> {
            // 去除标点符号后分词
            Result result = ToAnalysis.parse(text.replaceAll("[，。、；：？！\"'《》【】()]", ""));
            // 筛选名词和动词作为关键词
            Set<String> keywords = result.getTerms().stream()
                    .filter(term -> term.getNatureStr().startsWith("n") || term.getNatureStr().startsWith("v"))
                    .map(Term::getName)
                    .collect(Collectors.toSet());

            return new TextWithKeywords(text, keywords);
        }).collect(Collectors.toList());
    }

    /**
     * 基于关键词覆盖关系进行去重
     */
    private static List<String> removeDuplicates(List<TextWithKeywords> textInfoList) {
        List<String> uniqueTexts = new ArrayList<>();
        Set<Integer> duplicateIndices = new HashSet<>();

        for (int i = 0; i < textInfoList.size(); i++) {
            if (duplicateIndices.contains(i)) {
                continue;
            }

            TextWithKeywords current = textInfoList.get(i);
            uniqueTexts.add(current.text);

            // 标记关键词是当前文本子集的文本
            for (int j = 0; j < textInfoList.size(); j++) {
                if (i != j && !duplicateIndices.contains(j) &&
                        current.keywords.containsAll(textInfoList.get(j).keywords)) {
                    duplicateIndices.add(j);
                }
            }
        }

        return uniqueTexts;
    }

    /**
     * 存储文本及其关键词的内部类
     */
    private record TextWithKeywords(String text, Set<String> keywords) {
    }

    /**
     * 对单��文本进行去重
     *
     * @param text 需要去重的文本
     * @return 去重后的文本
     */
    public static String deduplicateText(String text) {
        // 同时处理中英文逗号、句号等标点
        String[] segments = text.split("[，,。;；]");
        List<String> texts = Arrays.asList(segments);
        List<String> uniqueTexts = deduplicateTexts(texts);

        // 使用中文逗号连接去重后的结果
        return String.join("，", uniqueTexts);
    }

    /*public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入要去重的文本：");
        String text = scanner.nextLine();

        String result = deduplicateText(text);
        System.out.println("去重后的文本：" + result);
        scanner.close();
    }*/
}