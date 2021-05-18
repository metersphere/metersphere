package io.metersphere.log.utils.dff;

import java.util.EnumSet;

public enum DiffFlags {

    /**
     * This flag omits the <i>value</i> field on remove operations.
     * This is a default flag.
     */
    OMIT_VALUE_ON_REMOVE,

    /**
     * This flag omits all {@link Operation#MOVE} operations, leaving only
     * {@link Operation#ADD}, {@link Operation#REMOVE}, {@link Operation#REPLACE}
     * and {@link Operation#COPY} operations. In other words, without this flag,
     * {@link Operation#ADD} and {@link Operation#REMOVE} operations are not normalized
     * into {@link Operation#MOVE} operations.
     */
    OMIT_MOVE_OPERATION,

    /**
     * This flag omits all {@link Operation#COPY} operations, leaving only
     * {@link Operation#ADD}, {@link Operation#REMOVE}, {@link Operation#REPLACE}
     * and {@link Operation#MOVE} operations. In other words, without this flag,
     * {@link Operation#ADD} operations are not normalized into {@link Operation#COPY}
     * operations.
     */
    OMIT_COPY_OPERATION,

    /**
     * This flag adds a <i>fromValue</i> field to all {@link Operation#REPLACE} operations.
     * <i>fromValue</i> represents the the value replaced by a {@link Operation#REPLACE}
     * operation, in other words, the original value. This can be useful for debugging
     * output or custom processing of the diffs by downstream systems.
     * Please note that this is a non-standard extension to RFC 6902 and will not affect
     * how patches produced by this library are processed by this or other libraries.
     *
     * @since 0.4.1
     */
    ADD_ORIGINAL_VALUE_ON_REPLACE,

    /**
     * This flag normalizes a {@link Operation#REPLACE} operation into its respective
     * {@link Operation#REMOVE} and {@link Operation#ADD} operations. Although it adds
     * a redundant step, this can be useful for auditing systems in which immutability
     * is a requirement.
     * <p>
     * For the flag to work, {@link DiffFlags#ADD_ORIGINAL_VALUE_ON_REPLACE} has to be
     * enabled as the new instructions in the patch need to grab the old <i>fromValue</i>
     * {@code "op": "replace", "fromValue": "F1", "value": "F2" }
     * The above instruction will be split into
     * {@code "op":"remove", "value":"F1" } and {@code "op":"add", "value":"F2"} respectively.
     * <p>
     * Please note that this is a non-standard extension to RFC 6902 and will not affect
     * how patches produced by this library are processed by this or other libraries.
     *
     * @since 0.4.11
     */
    ADD_EXPLICIT_REMOVE_ADD_ON_REPLACE,

    /**
     * This flag instructs the diff generator to emit {@link Operation#TEST} operations
     * that validate the state of the source document before each mutation. This can be
     * useful if you want to ensure data integrity prior to applying the patch.
     * The resulting patches are standard per RFC 6902 and should be processed correctly
     * by any compliant library; due to the associated space and performance costs,
     * however, this isn't default behavior.
     *
     * @since 0.4.8
     */
    EMIT_TEST_OPERATIONS;


    public static EnumSet<DiffFlags> defaults() {
        return EnumSet.of(OMIT_VALUE_ON_REMOVE);
    }

    public static EnumSet<DiffFlags> dontNormalizeOpIntoMoveAndCopy() {
        return EnumSet.of(OMIT_MOVE_OPERATION, OMIT_COPY_OPERATION);
    }
}
