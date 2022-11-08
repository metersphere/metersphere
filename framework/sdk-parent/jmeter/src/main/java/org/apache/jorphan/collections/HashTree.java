/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jorphan.collections;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

/**
 * This class is used to create a tree structure of objects. Each element in the
 * tree is also a key to the next node down in the tree. It provides many ways
 * to add objects and branches, as well as many ways to retrieve.
 * <p>
 * HashTree implements the Map interface for convenience reasons. The main
 * difference between a Map and a HashTree is that the HashTree organizes the
 * data into a recursive tree structure, and provides the means to manipulate
 * that structure.
 * <p>
 * Of special interest is the {@link #traverse(HashTreeTraverser)} method, which
 * provides an expedient way to traverse any HashTree by implementing the
 * {@link HashTreeTraverser} interface in order to perform some operation on the
 * tree, or to extract information from the tree.
 *
 * @see HashTreeTraverser
 * @see SearchByClass
 */
public class HashTree implements Serializable, Map<Object, HashTree>, Cloneable {

    private static final long serialVersionUID = 240L;

    // Used for the RuntimeException to short-circuit the traversal
    private static final String FOUND = "found"; // $NON-NLS-1$

    // N.B. The keys can be either JMeterTreeNode or TestElement
    protected final Map<Object, HashTree> data;

    /**
     * Creates an empty new HashTree.
     */
    public HashTree() {
        this(null, null);
    }

    /**
     * Allow subclasses to provide their own Map.
     * @param _map {@link Map} to use
     */
    protected HashTree(Map<Object, HashTree> _map) {
        this(_map, null);
    }

    /**
     * Creates a new HashTree and adds the given object as a top-level node.
     *
     * @param key
     *            name of the new top-level node
     */
    public HashTree(Object key) {
        this(new LinkedHashMap<Object, HashTree>(), key);
    }

    /**
     * Uses the new HashTree if not null and adds the given object as a
     * top-level node if not null
     *
     * @param _map
     *            the map to be used. If <code>null</code> a new {@link LinkedHashMap}
     *            will be created
     * @param key
     *            the object to be used as the key for the root node (may be
     *            <code>null</code>, in which case no root node will be created)
     */
    private HashTree(Map<Object, HashTree> _map, Object key) {
        if(_map != null) {
            data = _map;
        } else {
            data = new LinkedHashMap<>();
        }
        if(key != null) {
            data.put(key, new HashTree());
        }
    }

    /**
     * The Map given must also be a HashTree, otherwise an
     * UnsupportedOperationException is thrown. If it is a HashTree, this is
     * like calling the add(HashTree) method.
     *
     * @see #add(HashTree)
     * @see Map#putAll(Map)
     */
    @Override
    public void putAll(Map<?, ? extends HashTree> map) {
        if (map instanceof HashTree) {
            this.add((HashTree) map);
        } else {
            throw new UnsupportedOperationException("can only putAll other HashTree objects");
        }
    }

    /**
     * Exists to satisfy the Map interface.
     *
     * @see Map#entrySet()
     */
    @Override
    public Set<Entry<Object, HashTree>> entrySet() {
        return data.entrySet();
    }

    /**
     * Implemented as required by the Map interface, but is not very useful
     * here. All 'values' in a HashTree are HashTree's themselves.
     *
     * @param value
     *            Object to be tested as a value.
     * @return True if the HashTree contains the value, false otherwise.
     * @see Map#containsValue(Object)
     */
    @Override
    public boolean containsValue(Object value) {
        return data.containsValue(value);
    }

    /**
     * This is the same as calling HashTree.add(key,value).
     *
     * @param key
     *            to use
     * @param value
     *            to store against key
     * @see Map#put(Object, Object)
     */
    @Override
    public HashTree put(Object key, HashTree value) {
        HashTree previous = data.get(key);
        add(key, value);
        return previous;
    }

    /**
     * Clears the HashTree of all contents.
     *
     * @see Map#clear()
     */
    @Override
    public void clear() {
        data.clear();
    }

    /**
     * Returns a collection of all the sub-trees of the current tree.
     *
     * @see Map#values()
     */
    @Override
    public Collection<HashTree> values() {
        return data.values();
    }

    /**
     * Adds a key as a node at the current level and then adds the given
     * HashTree to that new node.
     *
     * @param key
     *            key to create in this tree
     * @param subTree
     *            sub tree to add to the node created for the first argument.
     */
    public void add(Object key, HashTree subTree) {
        add(key).add(subTree);
    }

    /**
     * Adds all the nodes and branches of the given tree to this tree. Is like
     * merging two trees. Duplicates are ignored.
     *
     * @param newTree the tree to be added
     */
    public void add(HashTree newTree) {
        for (Object item : newTree.list()) {
            add(item).add(newTree.getTree(item));
        }
    }

    /**
     * Creates a new HashTree and adds all the objects in the given collection
     * as top-level nodes in the tree.
     *
     * @param keys
     *            a collection of objects to be added to the created HashTree.
     */
    public HashTree(Collection<?> keys) {
        data = new LinkedHashMap<>();
        for (Object o : keys) {
            data.put(o, new HashTree());
        }
    }

    /**
     * Creates a new HashTree and adds all the objects in the given array as
     * top-level nodes in the tree.
     *
     * @param keys
     *            array with names for the new top-level nodes
     */
    public HashTree(Object[] keys) {
        data = new LinkedHashMap<>();
        for (Object key : keys) {
            data.put(key, new HashTree());
        }
    }

    /**
     * If the HashTree contains the given object as a key at the top level, then
     * a true result is returned, otherwise false.
     *
     * @param o
     *            Object to be tested as a key.
     * @return True if the HashTree contains the key, false otherwise.
     * @see Map#containsKey(Object)
     */
    @Override
    public boolean containsKey(Object o) {
        return data.containsKey(o);
    }

    /**
     * If the HashTree is empty, true is returned, false otherwise.
     *
     * @return True if HashTree is empty, false otherwise.
     */
    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * Sets a key and it's value in the HashTree. It actually sets up a key, and
     * then creates a node for the key and sets the value to the new node, as a
     * key. Any previous nodes that existed under the given key are lost.
     *
     * @param key
     *            key to be set up
     * @param value
     *            value to be set up as a key in the secondary node
     */
    public void set(Object key, Object value) {
        data.put(key, createNewTree(value));
    }

    /**
     * Sets a key into the current tree and assigns it a HashTree as its
     * subtree. Any previous entries under the given key are removed.
     *
     * @param key
     *            key to be set up
     * @param t
     *            HashTree that the key maps to
     */
    public void set(Object key, HashTree t) {
        data.put(key, t);
    }

    /**
     * Sets a key and its values in the HashTree. It sets up a key in the
     * current node, and then creates a node for that key, and sets all the
     * values in the array as keys in the new node. Any keys previously held
     * under the given key are lost.
     *
     * @param key
     *            Key to be set up
     * @param values
     *            Array of objects to be added as keys in the secondary node
     */
    public void set(Object key, Object[] values) {
        data.put(key, createNewTree(Arrays.asList(values)));
    }

    /**
     * Sets a key and its values in the HashTree. It sets up a key in the
     * current node, and then creates a node for that key, and set all the
     * values in the array as keys in the new node. Any keys previously held
     * under the given key are removed.
     *
     * @param key
     *            key to be set up
     * @param values
     *            Collection of objects to be added as keys in the secondary
     *            node
     */
    public void set(Object key, Collection<?> values) {
        data.put(key, createNewTree(values));
    }

    /**
     * Sets a series of keys into the HashTree. It sets up the first object in
     * the key array as a key in the current node, recurses into the next
     * HashTree node through that key and adds the second object in the array.
     * Continues recursing in this manner until the end of the first array is
     * reached, at which point all the values of the second array are set as
     * keys to the bottom-most node. All previous keys of that bottom-most node
     * are removed.
     *
     * @param treePath
     *            array of keys to put into HashTree
     * @param values
     *            array of values to be added as keys to bottom-most node
     */
    public void set(Object[] treePath, Object[] values) {
        if (treePath != null && values != null) {
            set(Arrays.asList(treePath), Arrays.asList(values));
        }
    }

    /**
     * Sets a series of keys into the HashTree. It sets up the first object in
     * the key array as a key in the current node, recurses into the next
     * HashTree node through that key and adds the second object in the array.
     * Continues recursing in this manner until the end of the first array is
     * reached, at which point all the values of the Collection of values are
     * set as keys to the bottom-most node. Any keys previously held by the
     * bottom-most node are lost.
     *
     * @param treePath
     *            array of keys to put into HashTree
     * @param values
     *            Collection of values to be added as keys to bottom-most node
     */
    public void set(Object[] treePath, Collection<?> values) {
        if (treePath != null) {
            set(Arrays.asList(treePath), values);
        }
    }

    /**
     * Sets a series of keys into the HashTree. It sets up the first object in
     * the key list as a key in the current node, recurses into the next
     * HashTree node through that key and adds the second object in the list.
     * Continues recursing in this manner until the end of the first list is
     * reached, at which point all the values of the array of values are set as
     * keys to the bottom-most node. Any previously existing keys of that bottom
     * node are removed.
     *
     * @param treePath
     *            collection of keys to put into HashTree
     * @param values
     *            array of values to be added as keys to bottom-most node
     */
    public void set(Collection<?> treePath, Object[] values) {
        HashTree tree = addTreePath(treePath);
        tree.set(Arrays.asList(values));
    }

    /**
     * Sets the nodes of the current tree to be the objects of the given
     * collection. Any nodes previously in the tree are removed.
     *
     * @param values
     *            Collection of objects to set as nodes.
     */
    public void set(Collection<?> values) {
        clear();
        this.add(values);
    }

    /**
     * Sets a series of keys into the HashTree. It sets up the first object in
     * the key list as a key in the current node, recurses into the next
     * HashTree node through that key and adds the second object in the list.
     * Continues recursing in this manner until the end of the first list is
     * reached, at which point all the values of the Collection of values are
     * set as keys to the bottom-most node. Any previously existing keys of that
     * bottom node are lost.
     *
     * @param treePath
     *            list of keys to put into HashTree
     * @param values
     *            collection of values to be added as keys to bottom-most node
     */
    public void set(Collection<?> treePath, Collection<?> values) {
        HashTree tree = addTreePath(treePath);
        tree.set(values);
    }

    /**
     * Adds an key into the HashTree at the current level. If a HashTree exists
     * for the key already, no new tree will be added
     *
     * @param key
     *            key to be added to HashTree
     * @return newly generated tree, if no tree was found for the given key;
     *         existing key otherwise
     */
    public HashTree add(Object key) {
        if (!data.containsKey(key)) {
            HashTree newTree = createNewTree();
            data.put(key, newTree);
            return newTree;
        }
        return getTree(key);
    }

    /**
     * Adds all the given objects as nodes at the current level.
     *
     * @param keys
     *            Array of Keys to be added to HashTree.
     */
    public void add(Object[] keys) {
        for (Object key : keys) {
            add(key);
        }
    }

    /**
     * Adds a bunch of keys into the HashTree at the current level.
     *
     * @param keys
     *            Collection of Keys to be added to HashTree.
     */
    public void add(Collection<?> keys) {
        for (Object o : keys) {
            add(o);
        }
    }

    /**
     * Adds a key and it's value in the HashTree. The first argument becomes a
     * node at the current level, and the second argument becomes a node of it.
     *
     * @param key
     *            key to be added
     * @param value
     *            value to be added as a key in the secondary node
     * @return HashTree for which <code>value</code> is the key
     */
    public HashTree add(Object key, Object value) {
        return add(key).add(value);
    }

    /**
     * Adds a key and it's values in the HashTree. The first argument becomes a
     * node at the current level, and adds all the values in the array to the
     * new node.
     *
     * @param key
     *            key to be added
     * @param values
     *            array of objects to be added as keys in the secondary node
     */
    public void add(Object key, Object[] values) {
        add(key).add(values);
    }

    /**
     * Adds a key as a node at the current level and then adds all the objects
     * in the second argument as nodes of the new node.
     *
     * @param key
     *            key to be added
     * @param values
     *            Collection of objects to be added as keys in the secondary
     *            node
     */
    public void add(Object key, Collection<?> values) {
        add(key).add(values);
    }

    /**
     * Adds a series of nodes into the HashTree using the given path. The first
     * argument is an array that represents a path to a specific node in the
     * tree. If the path doesn't already exist, it is created (the objects are
     * added along the way). At the path, all the objects in the second argument
     * are added as nodes.
     *
     * @param treePath
     *            an array of objects representing a path
     * @param values
     *            array of values to be added as keys to bottom-most node
     */
    public void add(Object[] treePath, Object[] values) {
        if (treePath != null) {
            add(Arrays.asList(treePath), Arrays.asList(values));
        }
    }

    /**
     * Adds a series of nodes into the HashTree using the given path. The first
     * argument is an array that represents a path to a specific node in the
     * tree. If the path doesn't already exist, it is created (the objects are
     * added along the way). At the path, all the objects in the second argument
     * are added as nodes.
     *
     * @param treePath
     *            an array of objects representing a path
     * @param values
     *            collection of values to be added as keys to bottom-most node
     */
    public void add(Object[] treePath, Collection<?> values) {
        if (treePath != null) {
            add(Arrays.asList(treePath), values);
        }
    }

    public HashTree add(Object[] treePath, Object value) {
        return add(Arrays.asList(treePath), value);
    }

    /**
     * Adds a series of nodes into the HashTree using the given path. The first
     * argument is a List that represents a path to a specific node in the tree.
     * If the path doesn't already exist, it is created (the objects are added
     * along the way). At the path, all the objects in the second argument are
     * added as nodes.
     *
     * @param treePath
     *            a list of objects representing a path
     * @param values
     *            array of values to be added as keys to bottom-most node
     */
    public void add(Collection<?> treePath, Object[] values) {
        HashTree tree = addTreePath(treePath);
        tree.add(Arrays.asList(values));
    }

    /**
     * Adds a series of nodes into the HashTree using the given path. The first
     * argument is a List that represents a path to a specific node in the tree.
     * If the path doesn't already exist, it is created (the objects are added
     * along the way). At the path, the object in the second argument is added
     * as a node.
     *
     * @param treePath
     *            a list of objects representing a path
     * @param value
     *            Object to add as a node to bottom-most node
     * @return HashTree for which <code>value</code> is the key
     */
    public HashTree add(Collection<?> treePath, Object value) {
        HashTree tree = addTreePath(treePath);
        return tree.add(value);
    }

    /**
     * Adds a series of nodes into the HashTree using the given path. The first
     * argument is a SortedSet that represents a path to a specific node in the
     * tree. If the path doesn't already exist, it is created (the objects are
     * added along the way). At the path, all the objects in the second argument
     * are added as nodes.
     *
     * @param treePath
     *            a SortedSet of objects representing a path
     * @param values
     *            Collection of values to be added as keys to bottom-most node
     */
    public void add(Collection<?> treePath, Collection<?> values) {
        HashTree tree = addTreePath(treePath);
        tree.add(values);
    }

    protected HashTree addTreePath(Collection<?> treePath) {
        HashTree tree = this;
        for (Object temp : treePath) {
            tree = tree.add(temp);
        }
        return tree;
    }

    /**
     * Gets the HashTree mapped to the given key.
     *
     * @param key
     *            Key used to find appropriate HashTree()
     * @return the HashTree for <code>key</code>
     */
    public HashTree getTree(Object key) {
        return data.get(key);
    }

    /**
     * Returns the HashTree object associated with the given key. Same as
     * calling {@link #getTree(Object)}.
     *
     * @see Map#get(Object)
     */
    @Override
    public HashTree get(Object key) {
        return getTree(key);
    }

    /**
     * Gets the HashTree object mapped to the last key in the array by recursing
     * through the HashTree structure one key at a time.
     *
     * @param treePath
     *            array of keys.
     * @return HashTree at the end of the recursion.
     */
    public HashTree getTree(Object[] treePath) {
        if (treePath != null) {
            return getTree(Arrays.asList(treePath));
        }
        return this;
    }

    /**
     * Create a clone of this HashTree. This is not a deep clone (i.e., the
     * contents of the tree are not cloned).
     *
     */
    @Override
    public Object clone() {
        HashTree newTree = new HashTree();
        cloneTree(newTree);
        return newTree;
    }

    protected void cloneTree(HashTree newTree) {
        for (Object key : list()) {
            newTree.set(key, (HashTree) getTree(key).clone());
        }
    }

    /**
     * Creates a new tree. This method exists to allow inheriting classes to
     * generate the appropriate types of nodes. For instance, when a node is
     * added, it's value is a HashTree. Rather than directly calling the
     * HashTree() constructor, the createNewTree() method is called. Inheriting
     * classes should override these methods and create the appropriate subclass
     * of HashTree.
     *
     * @return HashTree
     */
    protected HashTree createNewTree() {
        return new HashTree();
    }

    /**
     * Creates a new tree. This method exists to allow inheriting classes to
     * generate the appropriate types of nodes. For instance, when a node is
     * added, it's value is a HashTree. Rather than directly calling the
     * HashTree() constructor, the createNewTree() method is called. Inheriting
     * classes should override these methods and create the appropriate subclass
     * of HashTree.
     *
     * @param key
     *            object to use as the key for the top level
     *
     * @return newly created {@link HashTree}
     */
    protected HashTree createNewTree(Object key) {
        return new HashTree(key);
    }

    /**
     * Creates a new tree. This method exists to allow inheriting classes to
     * generate the appropriate types of nodes. For instance, when a node is
     * added, it's value is a HashTree. Rather than directly calling the
     * HashTree() constructor, the createNewTree() method is called. Inheriting
     * classes should override these methods and create the appropriate subclass
     * of HashTree.
     *
     * @param values objects to be added to the new {@link HashTree}
     *
     * @return newly created {@link HashTree}
     */
    protected HashTree createNewTree(Collection<?> values) {
        return new HashTree(values);
    }

    /**
     * Gets the HashTree object mapped to the last key in the SortedSet by
     * recursing through the HashTree structure one key at a time.
     *
     * @param treePath
     *            Collection of keys
     * @return HashTree at the end of the recursion
     */
    public HashTree getTree(Collection<?> treePath) {
        return getTreePath(treePath);
    }

    /**
     * Gets a Collection of all keys in the current HashTree node. If the
     * HashTree represented a file system, this would be like getting a
     * collection of all the files in the current folder.
     *
     * @return Set of all keys in this HashTree
     */
    public Collection<Object> list() {
        return data.keySet();
    }

    /**
     * Gets a Set of all keys in the HashTree mapped to the given key of the
     * current HashTree object (in other words, one level down. If the HashTree
     * represented a file system, this would like getting a list of all files in
     * a sub-directory (of the current directory) specified by the key argument.
     *
     * @param key
     *            key used to find HashTree to get list of
     * @return Set of all keys in found HashTree.
     */
    public Collection<?> list(Object key) {
        HashTree temp = data.get(key);
        if (temp != null) {
            return temp.list();
        }
        return new HashSet<>();
    }

    /**
     * Removes the entire branch specified by the given key.
     *
     * @see Map#remove(Object)
     */
    @Override
    public HashTree remove(Object key) {
        return data.remove(key);
    }

    /**
     * Recurses down into the HashTree structure using each subsequent key in the
     * array of keys, and returns the Set of keys of the HashTree object at the
     * end of the recursion. If the HashTree represented a file system, this
     * would be like getting a list of all the files in a directory specified by
     * the treePath, relative from the current directory.
     *
     * @param treePath
     *            Array of keys used to recurse into HashTree structure
     * @return Set of all keys found in end HashTree
     */
    public Collection<?> list(Object[] treePath) {
        if (treePath != null) {
            return list(Arrays.asList(treePath));
        }
        return list();
    }

    /**
     * Recurses down into the HashTree structure using each subsequent key in the
     * List of keys, and returns the Set of keys of the HashTree object at the
     * end of the recursion. If the HashTree represented a file system, this
     * would be like getting a list of all the files in a directory specified by
     * the treePath, relative from the current directory.
     *
     * @param treePath
     *            List of keys used to recurse into HashTree structure
     * @return Set of all keys found in end HashTree
     */
    public Collection<?> list(Collection<?> treePath) {
        HashTree tree = getTreePath(treePath);
        if (tree != null) {
            return tree.list();
        }
        return new HashSet<>();
    }

    /**
     * Finds the given current key, and replaces it with the given new key. Any
     * tree structure found under the original key is moved to the new key.
     *
     * @param currentKey name of the key to be replaced
     * @param newKey name of the new key
     */
    public void replaceKey(Object currentKey, Object newKey) {
        HashTree tree = getTree(currentKey);
        data.remove(currentKey);
        data.put(newKey, tree);
    }

    /**
     * Gets an array of all keys in the current HashTree node. If the HashTree
     * represented a file system, this would be like getting an array of all the
     * files in the current folder.
     *
     * @return array of all keys in this HashTree.
     */
    public Object[] getArray() {
        return data.keySet().toArray();
    }

    /**
     * Gets an array of all keys in the HashTree mapped to the given key of the
     * current HashTree object (in other words, one level down). If the HashTree
     * represented a file system, this would like getting a list of all files in
     * a sub-directory (of the current directory) specified by the key argument.
     *
     * @param key
     *            key used to find HashTree to get list of
     * @return array of all keys in found HashTree
     */
    public Object[] getArray(Object key) {
        HashTree t = getTree(key);
        if (t != null) {
            return t.getArray();
        }
        return null;
    }

    /**
     * Recurses down into the HashTree structure using each subsequent key in the
     * array of keys, and returns an array of keys of the HashTree object at the
     * end of the recursion. If the HashTree represented a file system, this
     * would be like getting a list of all the files in a directory specified by
     * the treePath, relative from the current directory.
     *
     * @param treePath
     *            array of keys used to recurse into HashTree structure
     * @return array of all keys found in end HashTree
     */
    public Object[] getArray(Object[] treePath) {
        if (treePath != null) {
            return getArray(Arrays.asList(treePath));
        }
        return getArray();
    }

    /**
     * Recurses down into the HashTree structure using each subsequent key in the
     * treePath argument, and returns an array of keys of the HashTree object at
     * the end of the recursion. If the HashTree represented a file system, this
     * would be like getting a list of all the files in a directory specified by
     * the treePath, relative from the current directory.
     *
     * @param treePath
     *            list of keys used to recurse into HashTree structure
     * @return array of all keys found in end HashTree
     */
    public Object[] getArray(Collection<?> treePath) {
        HashTree tree = getTreePath(treePath);
        return (tree != null) ? tree.getArray() : null;
    }

    protected HashTree getTreePath(Collection<?> treePath) {
        HashTree tree = this;
        for (Object aTreePath : treePath) {
            tree = tree.getTree(aTreePath);
            if (tree == null) {
                return null;
            }
        }
        return tree;
    }

    /**
     * Returns a hashcode for this HashTree.
     *
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return data.hashCode() * 7;
    }

    /**
     * Compares all objects in the tree and verifies that the two trees contain
     * the same objects at the same tree levels. Returns true if they do, false
     * otherwise.
     *
     * @param o
     *            Object to be compared against
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HashTree)) {
            return false;
        }
        HashTree oo = (HashTree) o;
        if (oo.size() != this.size()) {
            return false;
        }
        return data.equals(oo.data);
    }

    /**
     * Returns a Set of all the keys in the top-level of this HashTree.
     *
     * @see Map#keySet()
     */
    @Override
    public Set<Object> keySet() {
        return data.keySet();
    }

    /**
     * Searches the HashTree structure for the given key. If it finds the key,
     * it returns the HashTree mapped to the key. If it finds nothing, it
     * returns null.
     *
     * @param key
     *            Key to search for
     * @return HashTree mapped to key, if found, otherwise <code>null</code>
     */
    public HashTree search(Object key) {
        HashTree result = getTree(key);
        if (result != null) {
            return result;
        }
        TreeSearcher searcher = new TreeSearcher(key);
        try {
            traverse(searcher);
        } catch (RuntimeException e) {
            if (!e.getMessage().equals(FOUND)){
                throw e;
            }
            // do nothing - means object is found
        }
        return searcher.getResult();
    }

    /**
     * Method readObject.
     *
     * @param ois
     *            the stream to read the objects from
     * @throws ClassNotFoundException
     *             when the class for the deserialization can not be found
     * @throws IOException
     *             when I/O error occurs
     */
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
    }

    /**
     * Returns the number of top-level entries in the HashTree.
     *
     * @see Map#size()
     */
    @Override
    public int size() {
        return data.size();
    }

    /**
     * Allows any implementation of the HashTreeTraverser interface to easily
     * traverse (depth-first) all the nodes of the HashTree. The Traverser
     * implementation will be given notification of each node visited.
     *
     * @see HashTreeTraverser
     * @param visitor
     *            the visitor that wants to traverse the tree
     */
    public void traverse(HashTreeTraverser visitor) {
        for (Object item : list()) {
            visitor.addNode(item, getTree(item));
            getTree(item).traverseInto(visitor);
        }
    }

    /**
     * The recursive method that accomplishes the tree-traversal and performs
     * the callbacks to the HashTreeTraverser.
     *
     * @param visitor
     *            the {@link HashTreeTraverser} to be notified
     */
    private void traverseInto(HashTreeTraverser visitor) {
        if (list().isEmpty()) {
            visitor.processPath();
        } else {
            for (Object item : list()) {
                final HashTree treeItem = getTree(item);
                visitor.addNode(item, treeItem);
                treeItem.traverseInto(visitor);
            }
        }
        visitor.subtractNode();
    }

    /**
     * Generate a printable representation of the tree.
     *
     * @return a representation of the tree
     */
    @Override
    public String toString() {
        ConvertToString converter = new ConvertToString();
        try {
            traverse(converter);
        } catch (Exception e) { // Just in case
            converter.reportError(e);
        }
        return converter.toString();
    }

    private static class TreeSearcher implements HashTreeTraverser {

        private final Object target;

        private HashTree result;

        public TreeSearcher(Object t) {
            target = t;
        }

        public HashTree getResult() {
            return result;
        }

        /** {@inheritDoc} */
        @Override
        public void addNode(Object node, HashTree subTree) {
            result = subTree.getTree(target);
            if (result != null) {
                // short circuit traversal when found
                throw new RuntimeException(FOUND);
            }
        }

        /** {@inheritDoc} */
        @Override
        public void processPath() {
            // Not used
        }

        /** {@inheritDoc} */
        @Override
        public void subtractNode() {
            // Not used
        }
    }

    private static class ConvertToString implements HashTreeTraverser {
        private final StringBuilder string = new StringBuilder(getClass().getName() + "{");

        private final StringBuilder spaces = new StringBuilder();

        private int depth = 0;

        @Override
        public void addNode(Object key, HashTree subTree) {
            depth++;
            string.append("\n").append(getSpaces()).append(key);
            string.append(" {");
        }

        @Override
        public void subtractNode() {
            string.append("\n" + getSpaces() + "}");
            depth--;
        }

        @Override
        public void processPath() {
            // NOOP
        }

        @Override
        public String toString() {
            string.append("\n}");
            return string.toString();
        }

        void reportError(Throwable t){
            string.append("Error: ").append(t.toString());
        }

        private String getSpaces() {
            if (spaces.length() < depth * 2) {
                while (spaces.length() < depth * 2) {
                    spaces.append("  ");
                }
            } else if (spaces.length() > depth * 2) {
                spaces.setLength(depth * 2);
            }
            return spaces.toString();
        }
    }
}
