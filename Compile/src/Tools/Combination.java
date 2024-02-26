package Tools;

import java.util.Objects;

public class Combination<K,V> {
    private final K key;
    private V value;

    public Combination(K key, V value) {
        this.key = key;
        this.value = value;
    }
    public K getKey() {
        return key;
    }
    public V getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return key.hashCode() * 17 + (value == null ? 0 : value.hashCode());
    }

    @Override
    public String toString() {
        return key + "->" + value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Combination) {
            Combination<K, V> combination = (Combination<K, V>)obj;
            return Objects.equals(key, combination.key) && Objects.equals(value, combination.value);
        }
        return false;
    }


}
