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
import com.fasterxml.jackson.databind.node.NullNode;

import java.util.EnumSet;
import java.util.Iterator;

/**
 * User: gopi.vishwakarma
 * Date: 31/07/14
 */
public final class JsonPatch {

    private JsonPatch() {
    }

    private static JsonNode getPatchAttr(JsonNode jsonNode, String attr) {
        JsonNode child = jsonNode.get(attr);
        if (child == null)
            throw new InvalidJsonPatchException("Invalid JSON Patch payload (missing '" + attr + "' field)");
        return child;
    }

    private static JsonNode getPatchAttrWithDefault(JsonNode jsonNode, String attr, JsonNode defaultValue) {
        JsonNode child = jsonNode.get(attr);
        if (child == null)
            return defaultValue;
        else
            return child;
    }

    private static void process(JsonNode patch, JsonPatchProcessor processor, EnumSet<CompatibilityFlags> flags)
            throws InvalidJsonPatchException {

        if (!patch.isArray())
            throw new InvalidJsonPatchException("Invalid JSON Patch payload (not an array)");
        Iterator<JsonNode> operations = patch.iterator();
        while (operations.hasNext()) {
            JsonNode jsonNode = operations.next();
            if (!jsonNode.isObject()) throw new InvalidJsonPatchException("Invalid JSON Patch payload (not an object)");
            Operation operation = Operation.fromRfcName(getPatchAttr(jsonNode, Constants.OP).textValue());
            JsonPointer path = JsonPointer.parse(getPatchAttr(jsonNode, Constants.PATH).textValue());

            try {
                switch (operation) {
                    case REMOVE: {
                        processor.remove(path);
                        break;
                    }

                    case ADD: {
                        JsonNode value;
                        if (!flags.contains(CompatibilityFlags.MISSING_VALUES_AS_NULLS))
                            value = getPatchAttr(jsonNode, Constants.VALUE);
                        else
                            value = getPatchAttrWithDefault(jsonNode, Constants.VALUE, NullNode.getInstance());
                        processor.add(path, value.deepCopy());
                        break;
                    }

                    case REPLACE: {
                        JsonNode value;
                        if (!flags.contains(CompatibilityFlags.MISSING_VALUES_AS_NULLS))
                            value = getPatchAttr(jsonNode, Constants.VALUE);
                        else
                            value = getPatchAttrWithDefault(jsonNode, Constants.VALUE, NullNode.getInstance());
                        processor.replace(path, value.deepCopy());
                        break;
                    }

                    case MOVE: {
                        JsonPointer fromPath = JsonPointer.parse(getPatchAttr(jsonNode, Constants.FROM).textValue());
                        processor.move(fromPath, path);
                        break;
                    }

                    case COPY: {
                        JsonPointer fromPath = JsonPointer.parse(getPatchAttr(jsonNode, Constants.FROM).textValue());
                        processor.copy(fromPath, path);
                        break;
                    }

                    case TEST: {
                        JsonNode value;
                        if (!flags.contains(CompatibilityFlags.MISSING_VALUES_AS_NULLS))
                            value = getPatchAttr(jsonNode, Constants.VALUE);
                        else
                            value = getPatchAttrWithDefault(jsonNode, Constants.VALUE, NullNode.getInstance());
                        processor.test(path, value.deepCopy());
                        break;
                    }
                }
            }
            catch (JsonPointerEvaluationException e) {
                throw new JsonPatchApplicationException(e.getMessage(), operation, e.getPath());
            }
        }
    }

    public static void validate(JsonNode patch, EnumSet<CompatibilityFlags> flags) throws InvalidJsonPatchException {
        process(patch, NoopProcessor.INSTANCE, flags);
    }

    public static void validate(JsonNode patch) throws InvalidJsonPatchException {
        validate(patch, CompatibilityFlags.defaults());
    }

    public static JsonNode apply(JsonNode patch, JsonNode source, EnumSet<CompatibilityFlags> flags) throws JsonPatchApplicationException {
        CopyingApplyProcessor processor = new CopyingApplyProcessor(source, flags);
        process(patch, processor, flags);
        return processor.result();
    }

    public static JsonNode apply(JsonNode patch, JsonNode source) throws JsonPatchApplicationException {
        return apply(patch, source, CompatibilityFlags.defaults());
    }

    public static void applyInPlace(JsonNode patch, JsonNode source) {
        applyInPlace(patch, source, CompatibilityFlags.defaults());
    }

    public static void applyInPlace(JsonNode patch, JsonNode source, EnumSet<CompatibilityFlags> flags) {
        InPlaceApplyProcessor processor = new InPlaceApplyProcessor(source, flags);
        process(patch, processor, flags);
    }
}
