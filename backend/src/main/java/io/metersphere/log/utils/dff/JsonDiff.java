/*
 * Copyright 2016 flipkart.com zjsonpatch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.metersphere.log.utils.dff;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.collections4.ListUtils;

import java.util.*;

/**
 * User: gopi.vishwakarma
 * Date: 30/07/14
 */
public final class JsonDiff {

    private final List<Diff> diffs = new ArrayList<Diff>();
    private final EnumSet<DiffFlags> flags;

    private JsonDiff(EnumSet<DiffFlags> flags) {
        this.flags = flags.clone();
    }

    public static JsonNode asJson(final JsonNode source, final JsonNode target) {
        return asJson(source, target, DiffFlags.defaults());
    }

    public static JsonNode asJson(final JsonNode source, final JsonNode target, EnumSet<DiffFlags> flags) {
        JsonDiff diff = new JsonDiff(flags);
        if (source == null && target != null) {
            // return add node at root pointing to the target
            diff.diffs.add(Diff.generateDiff(Operation.ADD, JsonPointer.ROOT, target));
        }
        if (source != null && target == null) {
            // return remove node at root pointing to the source
            diff.diffs.add(Diff.generateDiff(Operation.REMOVE, JsonPointer.ROOT, source));
        }
        if (source != null && target != null) {
            diff.generateDiffs(JsonPointer.ROOT, source, target);

            if (!flags.contains(DiffFlags.OMIT_MOVE_OPERATION))
                // Merging remove & add to move operation
                diff.introduceMoveOperation();

            if (!flags.contains(DiffFlags.OMIT_COPY_OPERATION))
                // Introduce copy operation
                diff.introduceCopyOperation(source, target);

            if (flags.contains(DiffFlags.ADD_EXPLICIT_REMOVE_ADD_ON_REPLACE))
                // Split replace into remove and add instructions
                diff.introduceExplicitRemoveAndAddOperation();
        }
        return diff.getJsonNodes();
    }

    public static List<Diff> jsonDiff(final JsonNode source, final JsonNode target) {
        return diff(source, target);
    }

    public static List<Diff> diff(final JsonNode source, final JsonNode target) {
        JsonDiff diff = new JsonDiff(DiffFlags.defaults());
        if (source == null && target != null) {
            // return add node at root pointing to the target
            diff.diffs.add(Diff.generateDiff(Operation.ADD, JsonPointer.ROOT, target));
        }
        if (source != null && target == null) {
            // return remove node at root pointing to the source
            diff.diffs.add(Diff.generateDiff(Operation.REMOVE, JsonPointer.ROOT, source));
        }
        if (source != null && target != null) {
            diff.generateDiffs(JsonPointer.ROOT, source, target);

            if (!DiffFlags.defaults().contains(DiffFlags.OMIT_MOVE_OPERATION))
                // Merging remove & add to move operation
                diff.introduceMoveOperation();

            if (!DiffFlags.defaults().contains(DiffFlags.OMIT_COPY_OPERATION))
                // Introduce copy operation
                diff.introduceCopyOperation(source, target);

            if (DiffFlags.defaults().contains(DiffFlags.ADD_EXPLICIT_REMOVE_ADD_ON_REPLACE))
                // Split replace into remove and add instructions
                diff.introduceExplicitRemoveAndAddOperation();
        }
        return diff.getDiffs();
    }

    private static JsonPointer getMatchingValuePath(Map<JsonNode, JsonPointer> unchangedValues, JsonNode value) {
        return unchangedValues.get(value);
    }

    private void introduceCopyOperation(JsonNode source, JsonNode target) {
        Map<JsonNode, JsonPointer> unchangedValues = getUnchangedPart(source, target);

        for (int i = 0; i < diffs.size(); i++) {
            Diff diff = diffs.get(i);
            if (Operation.ADD != diff.getOperation()) continue;

            JsonPointer matchingValuePath = getMatchingValuePath(unchangedValues, diff.getValue());
            if (matchingValuePath != null && isAllowed(matchingValuePath, diff.getPath())) {
                // Matching value found; replace add with copy
                if (flags.contains(DiffFlags.EMIT_TEST_OPERATIONS)) {
                    // Prepend test node
                    diffs.add(i, new Diff(Operation.TEST, matchingValuePath, diff.getValue()));
                    i++;
                }
                diffs.set(i, new Diff(Operation.COPY, matchingValuePath, diff.getPath()));
            }
        }
    }

    private static boolean isNumber(String str) {
        int size = str.length();

        for (int i = 0; i < size; i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }

        return size > 0;
    }

    // TODO this is quite unclear and needs some serious documentation
    private static boolean isAllowed(JsonPointer source, JsonPointer destination) {
        boolean isSame = source.equals(destination);
        int i = 0;
        int j = 0;
        // Hack to fix broken COPY operation, need better handling here
        while (i < source.size() && j < destination.size()) {
            JsonPointer.RefToken srcValue = source.get(i);
            JsonPointer.RefToken dstValue = destination.get(j);
            String srcStr = srcValue.toString();
            String dstStr = dstValue.toString();
            if (isNumber(srcStr) && isNumber(dstStr)) {

                if (srcStr.compareTo(dstStr) > 0) {
                    return false;
                }
            }
            i++;
            j++;

        }
        return !isSame;
    }

    private static Map<JsonNode, JsonPointer> getUnchangedPart(JsonNode source, JsonNode target) {
        Map<JsonNode, JsonPointer> unchangedValues = new HashMap<JsonNode, JsonPointer>();
        computeUnchangedValues(unchangedValues, JsonPointer.ROOT, source, target);
        return unchangedValues;
    }

    private static void computeUnchangedValues(Map<JsonNode, JsonPointer> unchangedValues, JsonPointer path, JsonNode source, JsonNode target) {
        if (source.equals(target)) {
            if (!unchangedValues.containsKey(target)) {
                unchangedValues.put(target, path);
            }
            return;
        }

        final NodeType firstType = NodeType.getNodeType(source);
        final NodeType secondType = NodeType.getNodeType(target);

        if (firstType == secondType) {
            switch (firstType) {
                case OBJECT:
                    computeObject(unchangedValues, path, source, target);
                    break;
                case ARRAY:
                    computeArray(unchangedValues, path, source, target);
                    break;
                default:
                    /* nothing */
            }
        }
    }

    private static void computeArray(Map<JsonNode, JsonPointer> unchangedValues, JsonPointer path, JsonNode source, JsonNode target) {
        final int size = Math.min(source.size(), target.size());

        for (int i = 0; i < size; i++) {
            JsonPointer currPath = path.append(i);
            computeUnchangedValues(unchangedValues, currPath, source.get(i), target.get(i));
        }
    }

    private static void computeObject(Map<JsonNode, JsonPointer> unchangedValues, JsonPointer path, JsonNode source, JsonNode target) {
        final Iterator<String> firstFields = source.fieldNames();
        while (firstFields.hasNext()) {
            String name = firstFields.next();
            if (target.has(name)) {
                JsonPointer currPath = path.append(name);
                computeUnchangedValues(unchangedValues, currPath, source.get(name), target.get(name));
            }
        }
    }

    /**
     * This method merge 2 diffs ( remove then add, or vice versa ) with same value into one Move operation,
     * all the core logic resides here only
     */
    private void introduceMoveOperation() {
        for (int i = 0; i < diffs.size(); i++) {
            Diff diff1 = diffs.get(i);

            // if not remove OR add, move to next diff
            if (!(Operation.REMOVE == diff1.getOperation() ||
                    Operation.ADD == diff1.getOperation())) {
                continue;
            }

            for (int j = i + 1; j < diffs.size(); j++) {
                Diff diff2 = diffs.get(j);
                if (!diff1.getValue().equals(diff2.getValue())) {
                    continue;
                }

                Diff moveDiff = null;
                if (Operation.REMOVE == diff1.getOperation() &&
                        Operation.ADD == diff2.getOperation()) {
                    JsonPointer relativePath = computeRelativePath(diff2.getPath(), i + 1, j - 1, diffs);
                    moveDiff = new Diff(Operation.MOVE, diff1.getPath(), relativePath);

                } else if (Operation.ADD == diff1.getOperation() &&
                        Operation.REMOVE == diff2.getOperation()) {
                    JsonPointer relativePath = computeRelativePath(diff2.getPath(), i, j - 1, diffs); // diff1's add should also be considered
                    moveDiff = new Diff(Operation.MOVE, relativePath, diff1.getPath());
                }
                if (moveDiff != null) {
                    diffs.remove(j);
                    diffs.set(i, moveDiff);
                    break;
                }
            }
        }
    }

    /**
     * This method splits a {@link Operation#REPLACE} operation within a diff into a {@link Operation#REMOVE}
     * and {@link Operation#ADD} in order, respectively.
     * Does nothing if {@link Operation#REPLACE} op does not contain a from value
     */
    private void introduceExplicitRemoveAndAddOperation() {
        List<Diff> updatedDiffs = new ArrayList<Diff>();
        for (Diff diff : diffs) {
            if (!diff.getOperation().equals(Operation.REPLACE) || diff.getSrcValue() == null) {
                updatedDiffs.add(diff);
                continue;
            }
            //Split into two #REMOVE and #ADD
            updatedDiffs.add(new Diff(Operation.REMOVE, diff.getPath(), diff.getSrcValue()));
            updatedDiffs.add(new Diff(Operation.ADD, diff.getPath(), diff.getValue()));
        }
        diffs.clear();
        diffs.addAll(updatedDiffs);
    }

    //Note : only to be used for arrays
    //Finds the longest common Ancestor ending at Array
    private static JsonPointer computeRelativePath(JsonPointer path, int startIdx, int endIdx, List<Diff> diffs) {
        List<Integer> counters = new ArrayList<Integer>(path.size());
        for (int i = 0; i < path.size(); i++) {
            counters.add(0);
        }

        for (int i = startIdx; i <= endIdx; i++) {
            Diff diff = diffs.get(i);
            //Adjust relative path according to #ADD and #Remove
            if (Operation.ADD == diff.getOperation() || Operation.REMOVE == diff.getOperation()) {
                updatePath(path, diff, counters);
            }
        }
        return updatePathWithCounters(counters, path);
    }

    private static JsonPointer updatePathWithCounters(List<Integer> counters, JsonPointer path) {
        List<JsonPointer.RefToken> tokens = path.decompose();
        for (int i = 0; i < counters.size(); i++) {
            int value = counters.get(i);
            if (value != 0) {
                int currValue = tokens.get(i).getIndex();
                tokens.set(i, new JsonPointer.RefToken(Integer.toString(currValue + value)));
            }
        }
        return new JsonPointer(tokens);
    }

    private static void updatePath(JsonPointer path, Diff pseudo, List<Integer> counters) {
        //find longest common prefix of both the paths

        if (pseudo.getPath().size() <= path.size()) {
            int idx = -1;
            for (int i = 0; i < pseudo.getPath().size() - 1; i++) {
                if (pseudo.getPath().get(i).equals(path.get(i))) {
                    idx = i;
                } else {
                    break;
                }
            }
            if (idx == pseudo.getPath().size() - 2) {
                if (pseudo.getPath().get(pseudo.getPath().size() - 1).isArrayIndex()) {
                    updateCounters(pseudo, pseudo.getPath().size() - 1, counters);
                }
            }
        }
    }

    private static void updateCounters(Diff pseudo, int idx, List<Integer> counters) {
        if (Operation.ADD == pseudo.getOperation()) {
            counters.set(idx, counters.get(idx) - 1);
        } else {
            if (Operation.REMOVE == pseudo.getOperation()) {
                counters.set(idx, counters.get(idx) + 1);
            }
        }
    }

    private ArrayNode getJsonNodes() {
        JsonNodeFactory FACTORY = JsonNodeFactory.instance;
        final ArrayNode patch = FACTORY.arrayNode();
        for (Diff diff : diffs) {
            ObjectNode jsonNode = getJsonNode(FACTORY, diff, flags);
            patch.add(jsonNode);
        }
        return patch;
    }
    private  List<Diff> getDiffs() {
        return diffs;
    }

    private static ObjectNode getJsonNode(JsonNodeFactory FACTORY, Diff diff, EnumSet<DiffFlags> flags) {
        ObjectNode jsonNode = FACTORY.objectNode();
        jsonNode.put(Constants.OP, diff.getOperation().rfcName());

        switch (diff.getOperation()) {
            case MOVE:
            case COPY:
                jsonNode.put(Constants.FROM, diff.getPath().toString());    // required {from} only in case of Move Operation
                jsonNode.put(Constants.PATH, diff.getToPath().toString());  // destination Path
                break;

            case REMOVE:
                jsonNode.put(Constants.PATH, diff.getPath().toString());
                if (!flags.contains(DiffFlags.OMIT_VALUE_ON_REMOVE))
                    jsonNode.set(Constants.VALUE, diff.getValue());
                break;

            case REPLACE:
                if (flags.contains(DiffFlags.ADD_ORIGINAL_VALUE_ON_REPLACE)) {
                    jsonNode.set(Constants.FROM_VALUE, diff.getSrcValue());
                }
            case ADD:
            case TEST:
                jsonNode.put(Constants.PATH, diff.getPath().toString());
                jsonNode.set(Constants.VALUE, diff.getValue());
                break;

            default:
                // Safety net
                throw new IllegalArgumentException("Unknown operation specified:" + diff.getOperation());
        }

        return jsonNode;
    }

    private void generateDiffs(JsonPointer path, JsonNode source, JsonNode target) {
        if (!source.equals(target)) {
            final NodeType sourceType = NodeType.getNodeType(source);
            final NodeType targetType = NodeType.getNodeType(target);

            if (sourceType == NodeType.ARRAY && targetType == NodeType.ARRAY) {
                //both are arrays
                compareArray(path, source, target);
            } else if (sourceType == NodeType.OBJECT && targetType == NodeType.OBJECT) {
                //both are json
                compareObjects(path, source, target);
            } else {
                //can be replaced
                if (flags.contains(DiffFlags.EMIT_TEST_OPERATIONS))
                    diffs.add(new Diff(Operation.TEST, path, source));
                diffs.add(Diff.generateDiff(Operation.REPLACE, path, source, target));
            }
        }
    }

    private void compareArray(JsonPointer path, JsonNode source, JsonNode target) {
        List<JsonNode> lcs = getLCS(source, target);
        int srcIdx = 0;
        int targetIdx = 0;
        int lcsIdx = 0;
        int srcSize = source.size();
        int targetSize = target.size();
        int lcsSize = lcs.size();

        int pos = 0;
        while (lcsIdx < lcsSize) {
            JsonNode lcsNode = lcs.get(lcsIdx);
            JsonNode srcNode = source.get(srcIdx);
            JsonNode targetNode = target.get(targetIdx);


            if (lcsNode.equals(srcNode) && lcsNode.equals(targetNode)) { // Both are same as lcs node, nothing to do here
                srcIdx++;
                targetIdx++;
                lcsIdx++;
                pos++;
            } else {
                if (lcsNode.equals(srcNode)) { // src node is same as lcs, but not targetNode
                    //addition
                    JsonPointer currPath = path.append(pos);
                    diffs.add(Diff.generateDiff(Operation.ADD, currPath, targetNode));
                    pos++;
                    targetIdx++;
                } else if (lcsNode.equals(targetNode)) { //targetNode node is same as lcs, but not src
                    //removal,
                    JsonPointer currPath = path.append(pos);
                    if (flags.contains(DiffFlags.EMIT_TEST_OPERATIONS))
                        diffs.add(new Diff(Operation.TEST, currPath, srcNode));
                    diffs.add(Diff.generateDiff(Operation.REMOVE, currPath, srcNode));
                    srcIdx++;
                } else {
                    JsonPointer currPath = path.append(pos);
                    //both are unequal to lcs node
                    generateDiffs(currPath, srcNode, targetNode);
                    srcIdx++;
                    targetIdx++;
                    pos++;
                }
            }
        }

        while ((srcIdx < srcSize) && (targetIdx < targetSize)) {
            JsonNode srcNode = source.get(srcIdx);
            JsonNode targetNode = target.get(targetIdx);
            JsonPointer currPath = path.append(pos);
            generateDiffs(currPath, srcNode, targetNode);
            srcIdx++;
            targetIdx++;
            pos++;
        }
        pos = addRemaining(path, target, pos, targetIdx, targetSize);
        removeRemaining(path, pos, srcIdx, srcSize, source);
    }

    private void removeRemaining(JsonPointer path, int pos, int srcIdx, int srcSize, JsonNode source) {
        while (srcIdx < srcSize) {
            JsonPointer currPath = path.append(pos);
            if (flags.contains(DiffFlags.EMIT_TEST_OPERATIONS))
                diffs.add(new Diff(Operation.TEST, currPath, source.get(srcIdx)));
            diffs.add(Diff.generateDiff(Operation.REMOVE, currPath, source.get(srcIdx)));
            srcIdx++;
        }
    }

    private int addRemaining(JsonPointer path, JsonNode target, int pos, int targetIdx, int targetSize) {
        while (targetIdx < targetSize) {
            JsonNode jsonNode = target.get(targetIdx);
            JsonPointer currPath = path.append(pos);
            diffs.add(Diff.generateDiff(Operation.ADD, currPath, jsonNode.deepCopy()));
            pos++;
            targetIdx++;
        }
        return pos;
    }

    private void compareObjects(JsonPointer path, JsonNode source, JsonNode target) {
        Iterator<String> keysFromSrc = source.fieldNames();
        while (keysFromSrc.hasNext()) {
            String key = keysFromSrc.next();
            if (!target.has(key)) {
                //remove case
                JsonPointer currPath = path.append(key);
                if (flags.contains(DiffFlags.EMIT_TEST_OPERATIONS))
                    diffs.add(new Diff(Operation.TEST, currPath, source.get(key)));
                diffs.add(Diff.generateDiff(Operation.REMOVE, currPath, source.get(key)));
                continue;
            }
            JsonPointer currPath = path.append(key);
            generateDiffs(currPath, source.get(key), target.get(key));
        }
        Iterator<String> keysFromTarget = target.fieldNames();
        while (keysFromTarget.hasNext()) {
            String key = keysFromTarget.next();
            if (!source.has(key)) {
                //add case
                JsonPointer currPath = path.append(key);
                diffs.add(Diff.generateDiff(Operation.ADD, currPath, target.get(key)));
            }
        }
    }

    private static List<JsonNode> getLCS(final JsonNode first, final JsonNode second) {
        return ListUtils.longestCommonSubsequence(InternalUtils.toList((ArrayNode) first), InternalUtils.toList((ArrayNode) second));
    }
}
