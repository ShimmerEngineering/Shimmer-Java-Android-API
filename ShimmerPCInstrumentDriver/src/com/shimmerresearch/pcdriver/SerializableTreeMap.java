package com.shimmerresearch.pcdriver;
//
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.io.Serializable;
//import java.util.Map;
//import java.util.TreeMap;
//
//public class SerializableTreeMap<K, V> extends TreeMap<K, V> {
//	private static final long serialVersionUID = -1525292268623732635L;
//	
//	private void writeObject(ObjectOutputStream stream) throws IOException {
//		for (Map.Entry<K, V> e : this.entrySet()) {
//			stream.writeObject(e.getKey());
//			stream.writeObject(e.getValue());
//		}
//		// We do it like this because items can be added and removed 
//		// during serialization and an "end of object" indicator is more 
//		// robust given this
//		stream.writeObject(new EndObject());
//		stream.writeObject(new EndObject());
//	}
//	
//	@SuppressWarnings("unchecked")
//	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
//		while (true) {
//			Object k = stream.readObject();
//			Object v = stream.readObject();
//			if ((k instanceof EndObject) && (v instanceof EndObject)) {
//				break;
//			}
//			this.put((K) k, (V) v);
//		}
//	}
//	
//	private static class EndObject implements Serializable {
//	private static final long serialVersionUID = -5347717251973679220L;
//	}
//}


/**
 * 
 */
//package at.fhv.smartdevices.commons;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

//import org.simpleframework.xml.*;

/**
 * @author kepe
 * 
 */
//@Root
public class SerializableTreeMap<K, V> extends TreeMap<K, V> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

//	@ElementMap(entry = "value", key = "key")
	private TreeMap<K, V> _map;

	public SerializableTreeMap(SerializableTreeMap<K, V> map) {
		_map = new TreeMap<K, V>(map);
	}

	public SerializableTreeMap() {
		_map = new TreeMap<K, V>();
	}

	@Override
	public void clear() {

		_map.clear();
	}

	@Override
	public Object clone() {

		return _map.clone();
	}

	@Override
	public boolean containsKey(Object key) {

		return _map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {

		return _map.containsValue(value);
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {

		return _map.entrySet();
	}

	@Override
	public V get(Object key) {

		return _map.get(key);
	}

	@Override
	public boolean isEmpty() {

		return _map.isEmpty();
	}

	@Override
	public Set<K> keySet() {

		return _map.keySet();
	}

	@Override
	public V put(K key, V value) {

		return _map.put(key, value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {

		_map.putAll(m);
	}

	@Override
	public V remove(Object key) {

		return _map.remove(key);
	}

	@Override
	public int size() {

		return _map.size();
	}

	@Override
	public Collection<V> values() {

		return _map.values();
	}

	@Override
	public java.util.Map.Entry<K, V> ceilingEntry(K key) {

		return _map.ceilingEntry(key);
	}

	@Override
	public K ceilingKey(K key) {

		return _map.ceilingKey(key);
	}

	@Override
	public Comparator<? super K> comparator() {

		return _map.comparator();
	}

	@Override
	public NavigableSet<K> descendingKeySet() {

		return _map.descendingKeySet();
	}

	@Override
	public NavigableMap<K, V> descendingMap() {

		return _map.descendingMap();
	}

	@Override
	public java.util.Map.Entry<K, V> firstEntry() {

		return _map.firstEntry();
	}

	@Override
	public K firstKey() {

		return _map.firstKey();
	}

	@Override
	public java.util.Map.Entry<K, V> floorEntry(K key) {

		return _map.floorEntry(key);
	}

	@Override
	public K floorKey(K key) {

		return _map.floorKey(key);
	}

	@Override
	public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {

		return _map.headMap(toKey, inclusive);
	}

	@Override
	public SortedMap<K, V> headMap(K toKey) {

		return _map.headMap(toKey);
	}

	@Override
	public java.util.Map.Entry<K, V> higherEntry(K key) {

		return _map.higherEntry(key);
	}

	@Override
	public K higherKey(K key) {

		return _map.higherKey(key);
	}

	@Override
	public java.util.Map.Entry<K, V> lastEntry() {

		return _map.lastEntry();
	}

	@Override
	public K lastKey() {

		return _map.lastKey();
	}

	@Override
	public java.util.Map.Entry<K, V> lowerEntry(K key) {

		return _map.lowerEntry(key);
	}

	@Override
	public K lowerKey(K key) {

		return _map.lowerKey(key);
	}

	@Override
	public NavigableSet<K> navigableKeySet() {

		return _map.navigableKeySet();
	}

	@Override
	public java.util.Map.Entry<K, V> pollFirstEntry() {

		return _map.pollFirstEntry();
	}

	@Override
	public java.util.Map.Entry<K, V> pollLastEntry() {

		return _map.pollLastEntry();
	}

	@Override
	public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {

		return _map.subMap(fromKey, fromInclusive, toKey, toInclusive);
	}

	@Override
	public SortedMap<K, V> subMap(K fromKey, K toKey) {

		return _map.subMap(fromKey, toKey);
	}

	@Override
	public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {

		return _map.tailMap(fromKey, inclusive);
	}

	@Override
	public SortedMap<K, V> tailMap(K fromKey) {

		return _map.tailMap(fromKey);
	}

}

