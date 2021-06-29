package com.itt.context;

import java.util.Set;

public interface IDriverAttributes {

	/**
	 * @param name The name of the attribute to return
	 */
	public Object getAttribute(String name);

	/**
	 * Set a custom attribute.
	 */
	public void setAttribute(String name, Object value);

	/**
	 * @return all the attributes names.
	 */
	public Set<String> getAttributeNames();

	/**
	 * Remove the attribute
	 *
	 * @return the attribute value if found, null otherwise
	 */
	 Object removeAttribute(String name);

}
