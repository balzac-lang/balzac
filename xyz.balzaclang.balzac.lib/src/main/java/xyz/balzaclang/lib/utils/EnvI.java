/*
 * Copyright 2019 Nicola Atzei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.balzaclang.lib.utils;

import java.io.Serializable;
import java.util.Collection;

public interface EnvI<T,ENV extends EnvI<T,ENV>> extends Serializable {

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
    public Class<? extends T> getType(String name);

    /**
     * Return the value of the variable {@code name}.
     * The variable must be bound, otherwise an exception is thrown.
     *
     * @param name the variable name
     * @return the value associated to the variable
     */
    public T getValue(String name);

    /**
     * Return the value of the variable {@code name}, casted to the given class.
     * @param <A> a type that extends T
     * @param name the variable name
     * @param clazz the expected class of the object
     * @return the value associated to the variable
     */
    public <A extends T> A getValue(String name, Class<A> clazz);

    /**
     * Return the value of the variable {@code name}.
     * The variable must be bound, otherwise the default value is returned.
     *
     * @param name the variable name
     * @param defaultValue a default value if the variable is unbound
     * @return the value associated to the variable
     * @throws IllegalArgumentException if name is not a variable
     */
    public T getValueOrDefault(String name, T defaultValue);

    /**
     * Add a variable.
     *
     * @param name the name of the variable
     * @param type the expected type of the actual value for the variable
     * @return this class
     */
    public ENV addVariable(String name, Class<? extends T> type);

    /**
     * Remove a variable.
     *
     * @param name the name of the variable
     * @return this class
     */
    public ENV removeVariable(String name);


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
    public ENV bindVariable(String name, T value);

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
    public Collection<String> getBoundVariables();

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
