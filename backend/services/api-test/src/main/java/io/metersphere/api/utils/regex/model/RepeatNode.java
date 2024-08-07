package io.metersphere.api.utils.regex.model;

import io.metersphere.api.utils.regex.exception.RegexpIllegalException;
import io.metersphere.api.utils.regex.exception.TypeNotMatchException;
import io.metersphere.api.utils.regex.exception.UninitializedException;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;

public class RepeatNode extends BaseNode {

    private static final int MAX_REPEAT = 16;

    private Node node;
    private int minRepeat = 1;
    private int maxRepeat = 1;

    protected RepeatNode(List<String> expressionFragments) throws RegexpIllegalException, TypeNotMatchException {
        super(expressionFragments);
    }

    protected RepeatNode(List<String> expressionFragments, boolean initialize)
            throws RegexpIllegalException, TypeNotMatchException {
        super(expressionFragments, initialize);
    }

    @Override
    protected boolean test(String expression, List<String> expressionFragments) {
        if (expressionFragments.size() == 2) {
            String token = expressionFragments.get(1);
            return token != null
                    && ("+".equals(token) || "?".equals(token) || "*".equals(token) || token.startsWith("{"));
        }
        return false;
    }

    @Override
    protected void init(String expression, List<String> expressionFragments)
            throws RegexpIllegalException, TypeNotMatchException {
        node = new SingleNode(Collections.singletonList(expressionFragments.get(0)));
        String token = expressionFragments.get(1);
        if ("+".equals(token)) {
            maxRepeat = MAX_REPEAT;
        } else if ("?".equals(token)) {
            minRepeat = 0;
        } else if ("*".equals(token)) {
            minRepeat = 0;
            maxRepeat = MAX_REPEAT;
        } else if (token.startsWith("{")) {
            String[] numbers = token.substring(1, token.length() - 1).split(",", 2);
            minRepeat = maxRepeat = Integer.parseInt(numbers[0]);
            if (numbers.length > 1) {
                maxRepeat = numbers[1].isEmpty() ? Math.max(MAX_REPEAT, minRepeat) : Integer.parseInt(numbers[1]);
                if (maxRepeat < minRepeat) {
                    throw new RegexpIllegalException("Invalid regular expression: "
                            + getExpression() + " : Numbers out of order in {} quantifier");
                }
            }
        }
    }

    @Override
    protected String random(String expression, List<String> expressionFragments)
            throws RegexpIllegalException, UninitializedException {
        int repeat = new SecureRandom().nextInt(maxRepeat - minRepeat + 1) + minRepeat;
        StringBuilder value = new StringBuilder();
        while (repeat-- > 0) {
            value.append(node.random());
        }
        return value.toString();
    }
}
