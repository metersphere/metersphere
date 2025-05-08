package io.metersphere.ai.engine.utils;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 基于 TextRank 算法的文本摘要工具
 */
public class TextRankSummarizer {

    /**
     * 生成文本摘要
     *
     * @param text          原始文本
     * @param sentenceCount 需要的摘要句子数
     * @return 摘要文本
     */
    public static String summarize(String text, int sentenceCount) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // 1. 分句
        List<String> sentences = splitSentences(text);
        if (sentences.size() <= sentenceCount) {
            return text;
        }

        // 2. 计算句子相似度矩阵
        float[][] similarityMatrix = calculateSimilarityMatrix(sentences);

        // 3. TextRank 评分
        float[] scores = calculateTextRankScores(similarityMatrix);

        // 4. 选择最高分句子
        int[] topIndices = getTopIndices(scores, sentenceCount);

        // 5. 按原始顺序排序并连接
        Arrays.sort(topIndices);
        return topIndices.length > 0 ?
                Arrays.stream(topIndices)
                        .mapToObj(sentences::get)
                        .collect(Collectors.joining("。")) + "。" : "";
    }

    /**
     * 默认提取一句话作为摘要
     */
    public static String summarize(String text) {
        return summarize(text, 1);
    }

    /**
     * 分割句子，支持多种中文标点
     */
    private static List<String> splitSentences(String text) {
        String[] parts = text.split("[。！？!?]");
        return Arrays.stream(parts)
                .filter(s -> !s.trim().isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 计算句子间的相似度矩阵
     */
    private static float[][] calculateSimilarityMatrix(List<String> sentences) {
        int size = sentences.size();
        float[][] matrix = new float[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i != j) {
                    matrix[i][j] = calculateSimilarity(sentences.get(i), sentences.get(j));
                }
            }
        }
        return matrix;
    }

    /**
     * 计算两个句子的相似度，使用 Ansj 分词
     */
    private static float calculateSimilarity(String s1, String s2) {
        Set<String> words1 = tokenizeWithAnsj(s1);
        Set<String> words2 = tokenizeWithAnsj(s2);

        // 计算 Jaccard 相似度
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);

        return union.isEmpty() ? 0 : (float) intersection.size() / union.size();
    }

    /**
     * 使用 Ansj 进行中文分词
     */
    private static Set<String> tokenizeWithAnsj(String text) {
        Result result = ToAnalysis.parse(text);
        return result.getTerms().stream()
                .map(Term::getName)
                .filter(word -> word.length() > 1)  // 过滤单字词
                .collect(Collectors.toSet());
    }

    /**
     * 使用 TextRank 算法计算句子重要性分数
     */
    private static float[] calculateTextRankScores(float[][] similarityMatrix) {
        int size = similarityMatrix.length;
        float[] scores = new float[size];
        Arrays.fill(scores, 1.0f);

        float d = 0.85f;  // 阻尼系数
        int iterations = 20;  // 迭代次数

        for (int iter = 0; iter < iterations; iter++) {
            for (int i = 0; i < size; i++) {
                float sum = 0;
                for (int j = 0; j < size; j++) {
                    if (j != i && similarityMatrix[j][i] > 0) {
                        float weightSum = 0;
                        for (int k = 0; k < size; k++) {
                            if (k != j) {
                                weightSum += similarityMatrix[j][k];
                            }
                        }
                        if (weightSum > 0) {
                            sum += similarityMatrix[j][i] / weightSum * scores[j];
                        }
                    }
                }
                scores[i] = (1 - d) + d * sum;
            }
        }

        return scores;
    }

    /**
     * 获取分数最高的几个句子索引
     */
    private static int[] getTopIndices(float[] scores, int count) {
        return IntStream.range(0, scores.length)
                .boxed()
                .sorted((i, j) -> Float.compare(scores[j], scores[i]))
                .limit(count)
                .mapToInt(Integer::intValue)
                .toArray();
    }

    /**
     * 测试方法
     */
/*    public static void main(String[] args) {
        // 预设的测试文本
        String text = "自然语言处理是人工智能的一个重要分支。它研究计算机处理人类语言的方法和技术。自然语言处理可以帮助计算机理解、解释和生成人类语言。现代自然语言处理技术大多基于机器学习，特别是深度学习。近年来，大型语言模型如GPT和BERT在自然语言处理领域取得了突破性进展。这些模型能够执行机器翻译、文本摘要、情感分析等多种任务。";

        // 测试不同摘要长度
        System.out.println("原文：" + text);
        System.out.println("\n提取1个句子：" + summarize(text, 1));
        System.out.println("\n提取2个句子：" + summarize(text, 2));
        System.out.println("\n提取3个句子：" + summarize(text, 3));

        // 交互式测试
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n\n请输入要提取摘要的文本（按回车结束）：");
        String userText = scanner.nextLine();

        System.out.println("请输入需要提取的句子数量：");
        int count = scanner.nextInt();
        scanner.nextLine(); // 消耗换行符

        System.out.println("\n摘要结果：");
        System.out.println(summarize(userText, count));
        scanner.close();
    }*/
}