package com.busymachines.commons;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigMergeable;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigOrigin;
import com.typesafe.config.ConfigResolveOptions;
import com.typesafe.config.ConfigValue;

public class ConfigDelegate {

	private Config delegate;

	public ConfigObject root() {
		return delegate.root();
	}

	public ConfigOrigin origin() {
		return delegate.origin();
	}

	public Config withFallback(ConfigMergeable other) {
		return delegate.withFallback(other);
	}

	public Config resolve() {
		return delegate.resolve();
	}

	public Config resolve(ConfigResolveOptions options) {
		return delegate.resolve(options);
	}

	public void checkValid(Config reference, String... restrictToPaths) {
		delegate.checkValid(reference, restrictToPaths);
	}

	public boolean hasPath(String path) {
		return delegate.hasPath(path);
	}

	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	public Set<Entry<String, ConfigValue>> entrySet() {
		return delegate.entrySet();
	}

	public boolean getBoolean(String path) {
		return delegate.getBoolean(path);
	}

	public Number getNumber(String path) {
		return delegate.getNumber(path);
	}

	public int getInt(String path) {
		return delegate.getInt(path);
	}

	public long getLong(String path) {
		return delegate.getLong(path);
	}

	public double getDouble(String path) {
		return delegate.getDouble(path);
	}

	public String getString(String path) {
		return delegate.getString(path);
	}

	public ConfigObject getObject(String path) {
		return delegate.getObject(path);
	}

	public Config getConfig(String path) {
		return delegate.getConfig(path);
	}

	public Object getAnyRef(String path) {
		return delegate.getAnyRef(path);
	}

	public ConfigValue getValue(String path) {
		return delegate.getValue(path);
	}

	public Long getBytes(String path) {
		return delegate.getBytes(path);
	}

	public Long getMilliseconds(String path) {
		return delegate.getMilliseconds(path);
	}

	public Long getNanoseconds(String path) {
		return delegate.getNanoseconds(path);
	}

	public ConfigList getList(String path) {
		return delegate.getList(path);
	}

	public List<Boolean> getBooleanList(String path) {
		return delegate.getBooleanList(path);
	}

	public List<Number> getNumberList(String path) {
		return delegate.getNumberList(path);
	}

	public List<Integer> getIntList(String path) {
		return delegate.getIntList(path);
	}

	public List<Long> getLongList(String path) {
		return delegate.getLongList(path);
	}

	public List<Double> getDoubleList(String path) {
		return delegate.getDoubleList(path);
	}

	public List<String> getStringList(String path) {
		return delegate.getStringList(path);
	}

	public List<? extends ConfigObject> getObjectList(String path) {
		return delegate.getObjectList(path);
	}

	public List<? extends Config> getConfigList(String path) {
		return delegate.getConfigList(path);
	}

	public List<? extends Object> getAnyRefList(String path) {
		return delegate.getAnyRefList(path);
	}

	public List<Long> getBytesList(String path) {
		return delegate.getBytesList(path);
	}

	public List<Long> getMillisecondsList(String path) {
		return delegate.getMillisecondsList(path);
	}

	public List<Long> getNanosecondsList(String path) {
		return delegate.getNanosecondsList(path);
	}

	public Config withOnlyPath(String path) {
		return delegate.withOnlyPath(path);
	}

	public Config withoutPath(String path) {
		return delegate.withoutPath(path);
	}

	public Config atPath(String path) {
		return delegate.atPath(path);
	}

	public Config atKey(String key) {
		return delegate.atKey(key);
	}

	public Config withValue(String path, ConfigValue value) {
		return delegate.withValue(path, value);
	}
	
}
