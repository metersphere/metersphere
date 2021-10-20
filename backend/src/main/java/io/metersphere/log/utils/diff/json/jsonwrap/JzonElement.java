package io.metersphere.log.utils.diff.json.jsonwrap;

/**
 * Common abstraction for json elements.
 * 
 * @since 1.0.0
 */
public interface JzonElement {

	/**
	 * @return if this element is an object
	 */
	boolean isJsonObject();

	/**
	 * @return if this element is an array
	 */
	boolean isJsonArray();

	/**
	 * @return if this element is a primitive value
	 */
	boolean isJsonPrimitive();

	/**
	 * @return if this element is null
	 */
	boolean isJsonNull();

	/**
	 * @return the underlying implementation element
	 */
	Object unwrap();

}
