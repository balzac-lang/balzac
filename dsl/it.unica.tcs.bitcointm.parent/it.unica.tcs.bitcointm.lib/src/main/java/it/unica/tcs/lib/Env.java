package it.unica.tcs.lib;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

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
		checkArgument(hasVariable(name), "'"+name+"' is not a variable");
		return !isBound(name);
	}

	@Override
	public boolean isBound(String name) {
		checkNotNull(name, "'name' cannot be null");
		checkArgument(hasVariable(name), "'"+name+"' is not a variable");
		return variablesBinding.containsKey(name);
	}

	@Override
	public Class<?> getType(String name) {
		checkNotNull(name, "'name' cannot be null");
		checkArgument(hasVariable(name), "'"+name+"' is not a variable");
		return variablesType.get(name);
	}
	
	@Override
	public Object getValue(String name) {
		checkNotNull(name, "'name' cannot be null");
		checkArgument(hasVariable(name), "'"+name+"' is not a variable");
		checkArgument(isBound(name), "'"+name+"' is not bounded");
		return variablesBinding.get(name);
	}
	
	@Override
	public Object getValueOrDefault(String name, Object defaultValue) {
		checkNotNull(name, "'name' cannot be null");
		checkNotNull(defaultValue, "'defaultValue' cannot be null");
		checkArgument(hasVariable(name), "'"+name+"' is not a variable");
		return isBound(name)? getValue(name): defaultValue;
	}
	
	@Override
	public <T> T getValue(String name, Class<T> type) {
		checkNotNull(name, "'name' cannot be null");
		checkNotNull(type, "'type' cannot be null");
		Class<?> expectedType = getType(name);
		checkState(expectedType.equals(type));
		return type.cast(variablesBinding.get(name));
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
	public Env removeVariable(String name) {
		checkNotNull(name, "'name' cannot be null");
		checkArgument(hasVariable(name), "'"+name+"' is not a variable");
		variablesType.remove(name);
		variablesBinding.remove(name);
		return this;
	}

	@Override
	public Env bindVariable(String name, Object value) {
		checkNotNull(name, "'name' cannot be null");
		checkNotNull(value, "'value' cannot be null");
		checkArgument(hasVariable(name), "'"+name+"' is not a variable");
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
		return Sets.difference(getVariables(), getBoundVariables());
	}

	@Override
	public Set<String> getBoundVariables() {
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
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Set<String> variables = getVariables();
		if (variables.isEmpty())
			return "No variables";
		
		int maxNameLength = variables.stream().mapToInt(String::length).reduce(0, Integer::max);
		int maxTypeLength = variables.stream().map(s -> variablesType.get(s).getSimpleName().length()).reduce(0, Integer::max);
		int maxValuesLength = variables.stream().map(s -> variablesBinding.getOrDefault(s,"").toString().length()).reduce(0, Integer::max);
		
		sb.append(StringUtils.rightPad(" Name", maxNameLength)).append("    ");
		sb.append(StringUtils.rightPad("Type", maxTypeLength)).append("    ");
		sb.append(StringUtils.rightPad("Binding", maxValuesLength)).append("\n");
		
		String line = StringUtils.repeat('-', sb.length())+"\n";
		sb.insert(0, line);
		sb.append(line);
		
		for (String name : variables) {
			String name2 = StringUtils.rightPad(name, maxNameLength);
			String type = StringUtils.rightPad(variablesType.get(name).getSimpleName(), maxTypeLength);
			String value = variablesBinding.getOrDefault(name, "").toString();
			sb.append(" ").append(name2).append("    ").append(type).append(value.isEmpty()? "":"    "+value).append("\n");
		}
		sb.append(line);
		return sb.toString();
	}
}
