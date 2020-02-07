package tera.aiwolf.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class HashCounter<K> {

    public ListMap<Integer, K> getCounts() {
        sort(false);
        ListMap<Integer, K> map = new ListMap<>();
        countMap.forEach((k, c) -> map.add(c.count, k));
        return map;
    }

    public int totalCount() {
        int count = 0;
        for (Count c : countMap.values()) {
            count += c.count;
        }
        return count;
    }

    public void sort(boolean accend) {
        ArrayList<Count> counts = new ArrayList<Count>(countMap.values());
        if (accend) {
            Collections.sort(counts);
        } else {
            Collections.sort(counts, Collections.reverseOrder());
        }
        countMap.clear();
        for (Count c : counts) {
            countMap.put(c.key, c);
        }
    }

    public Set<K> getMatchedKeySet(Double min, Double max) {
        double minimum = min == null ? Double.MIN_VALUE : min;
        double maximum = max == null ? Double.MAX_VALUE : max;
        HashSet<K> set = new HashSet<K>();
        for (Count c : countMap.values()) {
            if (c.count > minimum && c.count < maximum) {
                set.add(c.key);
            }
        }
        return set;
    }

    public Set<K> getMatchedKeySet(double equalValue) {
        HashSet<K> set = new HashSet<K>();
        for (Count c : countMap.values()) {
            if (c.count == equalValue) {
                set.add(c.key);
            }
        }
        return set;
    }

    public Set<K> getKeySet() {
        return countMap.keySet();
    }

    public List<K> getKeyList() {
        return new ArrayList<K>(getKeySet());
    }

    public class Count implements Comparable<Count> {

        private K key;
        private int count = 0;

        public Count(K key) {
            this.key = key;
        }

        public int getCount() {
            return count;
        }

        public K getKey() {
            return key;
        }

        @Override
        public String toString() {
            return Integer.toString(count);
        }

        @Override
        public int compareTo(Count o) {
            return Integer.compare(count, o.count);
        }
    }

    public K getKeyAt(int rank) {
        int i = 0;
        for (Count c : countMap.values()) {
            if (++i == rank) return c.key;
        }
        return null;
    }
    private LinkedHashMap<K, Count> countMap = new LinkedHashMap<K, Count>();

    public void countPlus(K key) {
        countValue(key, 1);
    }

    public void countMinus(K key) {
        countValue(key, -1);
    }

    public void removeCount(K key) {
        countMap.remove(key);
    }

    public void countValue(K key, int count) {
        if (key == null) {
            return;
        }
        Count c = countMap.get(key);
        if (c == null) {
            c = new Count(key);
            countMap.put(key, c);
        }
        c.count += count;
    }

    public int getCount(K key) {
        if (key == null) {
            return 0;
        }
        Count c = countMap.get(key);
        if (c == null) {
            return 0;
        } else {
            return c.count;
        }
    }

    public void multiplyAllCount(int multiplier) {
        for (Count c : countMap.values()) {
            c.count = c.count * multiplier;
        }
    }

    public void addAllCount(int modifier) {
        for (Count c : countMap.values()) {
            c.count = c.count + modifier;
        }
    }

    @Override
    public String toString() {
        return countMap.toString();
    }
}
