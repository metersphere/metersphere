package io.metersphere.ai.engine.utils;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文本清理工具类
 */
public class TextCleaner {

    // 基础过滤正则（预编译提升性能）
    private static final Pattern BASIC_CLEAN_PATTERN = Pattern.compile(
            "[\\p{Cntrl}\\p{So}\\p{Sk}\\p{Cf}\\x{200B}-\\x{200D}]+",
            Pattern.UNICODE_CHARACTER_CLASS
    );

    // 进阶清理白名单
    private static final String ALLOWED_SYMBOLS = "-_@%&+=/:#.,;!?()[]{}\"'";
    private static final Pattern ADVANCED_CLEAN_PATTERN = Pattern.compile(
            "[^\\p{L}\\p{N}\\s" + Pattern.quote(ALLOWED_SYMBOLS) + "]",
            Pattern.UNICODE_CHARACTER_CLASS
    );

    // HTML标签正则
    private static final Pattern HTML_PATTERN = Pattern.compile("<[^>]+>");

    // URL 正则
    private static final Pattern URL_PATTERN = Pattern.compile(
            "https?://[\\w./?=&%-]+", Pattern.CASE_INSENSITIVE);

    // 表情符号正则（Unicode范围）
    private static final Pattern EMOJI_PATTERN = Pattern.compile(
            "[\\x{1F600}-\\x{1F64F}\\x{1F300}-\\x{1F5FF}\\x{1F680}-\\x{1F6FF}\\x{1F700}-\\x{1F77F}\\x{1F900}-\\x{1F9FF}]",
            Pattern.UNICODE_CHARACTER_CLASS
    );

    // 连续空白字符
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s{2,}");

    /**
     * 基础符号清理
     */
    public static String basicClean(String input) {
        if (input == null) return "";
        return BASIC_CLEAN_PATTERN.matcher(input).replaceAll("");
    }

    /**
     * 进阶语义清理（保留白名单符号）
     */
    public static String advancedClean(String input) {
        if (input == null) return "";
        String stage1 = ADVANCED_CLEAN_PATTERN.matcher(input).replaceAll("");
        return normalizeWhitespace(stage1);
    }

    /**
     * 规范化空白字符
     */
    public static String normalizeWhitespace(String input) {
        if (input == null) return "";
        return WHITESPACE_PATTERN.matcher(input).replaceAll(" ").trim();
    }

    /**
     * 移除HTML/XML���签
     */
    public static String removeHtmlTags(String input) {
        if (input == null) return "";
        return HTML_PATTERN.matcher(input).replaceAll("");
    }

    /**
     * 移除URL链接
     */
    public static String removeUrls(String input) {
        if (input == null) return "";
        return URL_PATTERN.matcher(input).replaceAll("");
    }

    /**
     * 移除表情符号
     */
    public static String removeEmojis(String input) {
        if (input == null) return "";
        return EMOJI_PATTERN.matcher(input).replaceAll("");
    }

    /**
     * 统一符号格式（示例：中文引号转英文）
     */
    public static String unifySymbols(String input) {
        return input.replaceAll("[“”]", "\"")
                .replaceAll("[‘’]", "'")
                .replaceAll("【", "[")
                .replaceAll("】", "]");
    }

    /**
     * 流式处理大文本（内存优化）
     */
    public static String processLargeText(String input) {
        if (input == null) return "";
        try (BufferedReader br = new BufferedReader(new StringReader(input))) {
            return br.lines()
                    .map(line -> advancedClean(basicClean(line)))
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException("文本处理失败: " + e.getMessage(), e);
        }
    }

    /**
     * 完整处理流程示例
     */
    public static String fullClean(String input) {
        if (input == null) return "";
        String cleaned = removeHtmlTags(input);
        cleaned = basicClean(cleaned);
        cleaned = advancedClean(cleaned);
        cleaned = removeEmojis(cleaned);
        cleaned = removeUrls(cleaned);
        cleaned = removeDuplicateLines(cleaned);
        cleaned = deduplicateText(cleaned);

        return unifySymbols(cleaned);
    }

    /**
     * 文本去重处理（调用KeywordDeduplication）
     */
    public static String deduplicateText(String input) {
        if (input == null) return "";
        return KeywordDeduplication.deduplicateText(input);
    }

    /**
     * 准备文���用于摘要（清理后保留句子结构）
     */
    public static String prepareForSummarization(String input) {
        if (input == null) return "";
        String cleaned = removeHtmlTags(input);
        cleaned = removeEmojis(cleaned);
        cleaned = unifySymbols(cleaned);
        cleaned = basicClean(cleaned);
        // 不使用advancedClean以保留句子结构中的标点符号
        return normalizeWhitespace(cleaned);
    }

    /**
     * 截断文本到指定长度
     */
    public static String truncate(String input, int maxLength) {
        if (input == null) return "";
        if (input.length() <= maxLength) return input;
        return input.substring(0, maxLength);
    }

    /**
     * 删除重复行
     */
    public static String removeDuplicateLines(String input) {
        if (input == null) return "";
        Set<String> uniqueLines = new HashSet<>();
        return input.lines()
                .filter(line -> {
                    String trimmed = line.trim();
                    return !trimmed.isEmpty() && uniqueLines.add(trimmed);
                })
                .collect(Collectors.joining("\n"));
    }

/*    public static void main(String[] args) {
        String testText = "<div>用户@张三说❗：订单号#AB-123_已创建！(状态=完成)😊";

        // 完整处理流程
        System.out.println(fullClean(testText));
        // 输出：用户@张三说 订单号#AB-123_已创建 状态=完成

        // 单独使用方法
        String emojiText = "Hello😊World🌍";
        System.out.println(removeEmojis(emojiText)); // 输出：HelloWorld

        // 测试文本去重
        String duplicateText = "这是一段测试文本。这是一段重复的文本。这是一段测试文本。";
        System.out.println(deduplicateText(duplicateText));
    }*/
}