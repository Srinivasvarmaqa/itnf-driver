package com.itt.context;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

public class ITTDriverContext implements IDriverContext {
	
	private Map<String, Object> driver_attributes = Maps.newHashMap();

	@Override
	public Object getAttribute(String name) {
		return driver_attributes.get(name);
	}

	@Override
	public Set<String> getAttributeNames() {
		return driver_attributes.keySet();
	}

	@Override
	public void setAttribute(String name, Object value) {
		driver_attributes.put(name, value);
	}

	@Override
	public Object removeAttribute(String name) {
		return driver_attributes.remove(name);
	}



}
