package io.metersphere.log.utils.diff.json;


/**
 * Interface that allows filtering patch instructions.
 * 
 * @since 2.0.0
 */
public interface Visitor<E> {

	/**
	 * Should a patch instruction be created for an element like <code>to</code> if its destiny is an element like <code>to</code>?
	 * 
	 * @param from
	 *            - from element
	 * @param to
	 *            - to element
	 * @return if the instruction should be created
	 */
	boolean shouldCreatePatch(E from, E to);
}
