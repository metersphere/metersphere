package io.metersphere.log.utils.diff.json.jsonwrap;

/**
 * Common abstaraction for a json array.
 * 
 * @since 1.0.0
 */
public interface JzonArray extends JzonElement {

	/**
	 * @return array size
	 */
	int size();

	/**
	 * Returns element at given index.
	 * 
	 * @param index
	 *            - index to retreive element from
	 * @return element at given index
	 */
	JzonElement get(int index);

	/**
	 * Inserts element at given index.
	 * 
	 * @param index
	 *            - index to insert element at
	 * @param el
	 *            - element to insert
	 */
	void insert(int index, JzonElement el);

	/**
	 * Sets element at given index.
	 * 
	 * @param index
	 *            - index to set element to
	 * @param el
	 *            - element to set
	 */
	void set(int index, JzonElement el);

	/**
	 * Remove element at given index.
	 * 
	 * @param index
	 *            - index to remove element from
	 */
	void remove(int index);

}
