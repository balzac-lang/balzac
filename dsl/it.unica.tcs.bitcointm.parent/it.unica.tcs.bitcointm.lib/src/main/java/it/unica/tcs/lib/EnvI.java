package it.unica.tcs.lib;

import java.util.Collection;

public interface EnvI<T extends EnvI<T>> {

	/**
	 * Return true if the given {@code name} is a variable.
	 * 
	 * @param name the variable name
	 * @return true if the given {@code name} is a variable
	 */
	public boolean hasVariable(String name);
	
	/**
	 * Return true if the given {@code name} is a free-variable.
	 * If {@code name} is free, {@link #hasVariable(String)} must return true.
	 * 
	 * @param name the variable name
	 * @return true if the given {@code name} is a free-variable
	 */
	public boolean isFree(String name);

	/**
	 * Return true if the given {@code name} is a bound-variable.
	 * If {@code name} is bound, {@link #hasVariable(String)} must return true and 
	 * {@link #isFree(String)} must return false.
	 * 
	 * @param name the variable name
	 * @return true if the given {@code name} is a bound variable
	 */
	public boolean isBound(String name);
	
	/**
	 * Return the type of the variable {@code name}. 
	 * 
	 * @param name the variable name
	 * @return the type of the variable
	 */
	public Class<?> getType(String name);
	
	/**
	 * Return the value of the variable {@code name}.
	 * The variable must be bound. 
	 * 
	 * @param name the variable name
	 * @return the value associated to the variable
	 */
	public Object getValue(String name);
	
	/**
	 * Add a variable.
	 * 
	 * @param name the name of the variable
	 * @param type the expected type of the actual value for the variable
	 * @return this class
	 */
	public T addVariable(String name, Class<?> type);
	
	/**
	 * Bind a variable to a given {@code value}.
	 * 
	 * @param name the name of the variable
	 * @param value the value to bind
	 * @return this class
	 * @throws IllegalArgumentException
	 *             if the provided name is not a free variable for this
	 *             transaction, or if the provided value is an not instance of
	 *             the expected class of the free variable.
	 */
	public T bindVariable(String name, Object value);
	
	/**
	 * Return an immutable collection of the all variables (free or not).
	 * 
	 * @return the variables (free or not)
	 */
	public Collection<String> getVariables();
	
	/**
	 * Return an immutable collection of the free variables.
	 * For each name, {@link #hasVariable(String)} and {@link #isFree(String)} must return true.
	 * 
	 * @return the free variables
	 */
	public Collection<String> getFreeVariables();
	
	/**
	 * Return an immutable collection of the free variables.
	 * For each name, {@link #hasVariable(String)} and {@link #isBound(String)} must return true.
	 * 
	 * @return the bound variables
	 */
	public Collection<String> getBoundFreeVariables();
	
	/**
	 * Return true if there are not free-variables, false otherwise.
	 * 
	 * @return true if there are not free-variables, false otherwise
	 */
	public boolean isReady();
	
	/**
	 * Remove all variables and binding.
	 */
	public void clear();
}
