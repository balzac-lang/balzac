package it.unica.tcs.lib;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class Env implements EnvI<Env> {
	
	private static final long serialVersionUID = 1L;
	private final Map<String,Class<?>> variablesType = new HashMap<>();
	private final Map<String,Object> variablesBinding = new HashMap<>();
	
	@Override
	public boolean hasVariable(String name) {
		return variablesType.containsKey(name);
	}

	@Override
	public boolean isFree(String name) {
		checkNotNull(name, "'name' cannot be null");
		checkArgument(hasVariable(name), "'name' is not a variable");
		return !isBound(name);
	}

	@Override
	public boolean isBound(String name) {
		checkNotNull(name, "'name' cannot be null");
		checkArgument(hasVariable(name), "'name' is not a variable");
		return variablesBinding.containsKey(name);
	}

	@Override
	public Class<?> getType(String name) {
		checkNotNull(name, "'name' cannot be null");
		checkArgument(hasVariable(name), "'name' is not a variable");
		return variablesType.get(name);
	}
	
	@Override
	public Object getValue(String name) {
		checkArgument(hasVariable(name), "'name' is not a variable");
		return variablesBinding.get(name);
	}
	
	@Override
	public <T> T getValue(String name, Class<T> clazz) {
		Class<?> expectedType = getType(name);
		checkState(expectedType.equals(clazz));
		return clazz.cast(variablesBinding.get(name));
	}

	@Override
	public Env addVariable(String name, Class<?> type) {
		checkNotNull(name, "'name' cannot be null");
		checkNotNull(type, "'type' cannot be null");
		checkArgument(!hasVariable(name) || type.equals(variablesType.get(name)), "'"+name+"' is already associated with class '"+variablesType.get(name)+"'");
		variablesType.put(name, type);
		return this;
	}

	@Override
	public Env bindVariable(String name, Object value) {
		checkNotNull(name, "'name' cannot be null");
		checkNotNull(value, "'value' cannot be null");
		checkArgument(hasVariable(name), "'name' is not a variable");
		checkArgument(variablesType.get(name).isInstance(value), "'"+name+"' is associated with class '"+variablesType.get(name)+"', but 'value' is object of class '"+value.getClass()+"'");
		checkArgument(!variablesBinding.containsKey(name), "'"+name+"' is already associated with value '"+variablesBinding.get(name)+"'");
		variablesBinding.put(name, value);
		return this;
	}

	@Override
	public Set<String> getVariables() {
		return ImmutableSet.copyOf(variablesType.keySet());
	}

	@Override
	public Set<String> getFreeVariables() {
		return Sets.difference(getVariables(), getBoundFreeVariables());
	}

	@Override
	public Set<String> getBoundFreeVariables() {
		return ImmutableSet.copyOf(variablesBinding.keySet());
	}

	@Override
	public boolean isReady() {
		return getFreeVariables().size()==0;
	}

	@Override
	public void clear() {
		variablesType.clear();
		variablesBinding.clear();
	}
}
