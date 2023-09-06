package com.example.myapplication.Utils;

import java.util.HashMap;
import java.util.Map;
public class ObservableMap<K, V> extends HashMap<K, V> {
    private Map<MapChangeListener<K, V>, Object> listeners;

    public ObservableMap() {
        super();
        listeners = new HashMap<>();
    }

    public void addListener(MapChangeListener<K, V> listener) {
        listeners.put(listener, null);
    }

    public void removeListener(MapChangeListener<K, V> listener) {
        listeners.remove(listener);
    }

    @Override
    public V put(K key, V value) {
        V previousValue = super.put(key, value);
        notifyListeners(new MapChangeEvent<>(MapChangeType.PUT, key, previousValue, value));
        return previousValue;
    }

    @Override
    public V remove(Object key) {
        V removedValue = super.remove(key);
        notifyListeners(new MapChangeEvent<>(MapChangeType.REMOVE, (K) key, removedValue, null));
        return removedValue;
    }

    private void notifyListeners(MapChangeEvent<K, V> event) {
        for (MapChangeListener<K, V> listener : listeners.keySet()) {
            listener.onMapChange(event);
        }
    }

    public interface MapChangeListener<K, V> {
        void onMapChange(MapChangeEvent<K, V> event);
    }

    public static class MapChangeEvent<K, V> {
        private final MapChangeType changeType;
        private final K key;
        private final V oldValue;
        private final V newValue;

        public MapChangeEvent(MapChangeType changeType, K key, V oldValue, V newValue) {
            this.changeType = changeType;
            this.key = key;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public MapChangeType getChangeType() {
            return changeType;
        }

        public K getKey() {
            return key;
        }

        public V getOldValue() {
            return oldValue;
        }

        public V getNewValue() {
            return newValue;
        }
    }

    public enum MapChangeType {
        PUT,
        REMOVE
    }
}