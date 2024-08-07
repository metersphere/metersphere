package io.metersphere.api.utils.regex.model;

import io.metersphere.api.utils.regex.exception.RegexpIllegalException;
import io.metersphere.api.utils.regex.exception.TypeNotMatchException;
import io.metersphere.api.utils.regex.exception.UninitializedException;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class SingleNode extends BaseNode {

    private Node node;

    private List<Interval> intervals;

    protected SingleNode(List<String> expressionFragments) throws RegexpIllegalException, TypeNotMatchException {
        super(expressionFragments);
    }

    protected SingleNode(List<String> expressionFragments, boolean initialize)
            throws RegexpIllegalException, TypeNotMatchException {
        super(expressionFragments, initialize);
    }


    @Override
    protected boolean test(String expression, List<String> expressionFragments) {
        return expressionFragments != null && expressionFragments.size() == 1;
    }

    @Override
    protected void init(String expression, List<String> expressionFragments)
            throws RegexpIllegalException, TypeNotMatchException {
        if (expression.startsWith("(")) {
            node = new OrdinaryNode(expression.substring(1, expression.length() - 1));
            return;
        }
        if (expression.startsWith("[")) {
            int i = 1;
            Character preChar = null;
            while (i < expression.length() - 1) {
                if (expression.charAt(i) == '\\') {
                    if (i + 1 >= expression.length() - 1) {
                        throw new RegexpIllegalException(expression, i);
                    }
                    if (preChar != null && "dws".contains(expression.charAt(i + 1) + "")) {
                        addIntervals(preChar, null, '-', null);
                        preChar = null;
                    }
                    if (expression.charAt(i + 1) == 'd') {
                        addIntervals('0', '9');
                    } else if (expression.charAt(i + 1) == 'w') {
                        addIntervals('0', '9', 'A', 'Z', 'a', 'z', '_', null);
                    } else if (expression.charAt(i + 1) == 's') {
                        addIntervals(' ', null, '\t', null);
                    } else {
                        if (preChar != null) {
                            addIntervals(preChar, expression.charAt(i + 1));
                            preChar = null;
                        } else if (i + 2 < expression.length() && expression.charAt(i + 2) == '-') {
                            preChar = expression.charAt(i + 1);
                            i++;
                        } else {
                            addIntervals(expression.charAt(i + 1), null);
                        }
                    }
                    i++;
                } else if (preChar != null) {
                    addIntervals(preChar, expression.charAt(i));
                    preChar = null;
                } else if (i + 1 < expression.length() && expression.charAt(i + 1) == '-') {
                    preChar = expression.charAt(i);
                    i++;
                } else {
                    addIntervals(expression.charAt(i), null);
                }
                i++;
            }
            if (preChar != null) {
                addIntervals(preChar, null, '-', null);
            }
        } else if (".".equals(expression)) {
            // 这里仅包含一般字符，参考 ASCII 码表
            addIntervals((char) 33, (char) 126);
        } else if ("\\s".equals(expression)) {
            addIntervals(' ', null, '\t', null);
        } else if ("\\d".equals(expression)) {
            addIntervals('0', '9');
        } else if ("\\w".equals(expression)) {
            addIntervals('0', '9', 'A', 'Z', 'a', 'z', '_', null);
        } else if (expression.startsWith("\\")) {
            addIntervals(expression.charAt(1), null);
        }
    }

    @Override
    protected String random(String expression, List<String> expressionFragments)
            throws RegexpIllegalException, UninitializedException {
        if (node != null) {
            return node.random();
        }
        if (intervals != null && intervals.size() > 0) {
            Character value = randomCharFromInterval(intervals.toArray(new Interval[0]));
            return value == null ? "" : value.toString();
        }
        return expression;
    }

    private Character randomCharFromInterval(Interval... intervals) {
        int count = 0;
        for (Interval interval : intervals) {
            count += interval.end + 1 - interval.start;
        }
        int randomValue = new SecureRandom().nextInt(count);
        for (Interval interval : intervals) {
            if (randomValue < interval.end + 1 - interval.start) {
                return (char) (interval.start + randomValue);
            }
            randomValue -= interval.end + 1 - interval.start;
        }
        return null;
    }

    private void addIntervals(Character... chars) throws RegexpIllegalException {
        if (intervals == null) {
            intervals = new ArrayList<>();
        }
        for (int i = 0; i + 1 < chars.length; i += 2) {
            Character start = chars[i];
            Character end = chars[i + 1] == null ? start : chars[i + 1];
            if (start == null) {
                throw new RegexpIllegalException("Invalid regular expression: "
                        + getExpression() + " : Character class is null");
            }
            if (end < start) {
                throw new RegexpIllegalException("Invalid regular expression: "
                        + getExpression() + " : Range out of order in character class");
            }
            intervals.add(new Interval(start, end));
        }
    }

    private static class Interval {
        private char start;
        private char end;

        private Interval(char start, char end) {
            this.start = start;
            this.end = end;
        }
    }
}
