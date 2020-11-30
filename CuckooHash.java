import java.lang.Object;
import java.util.Objects;

/**
 * See README for sources referenced
 *
 * CuckooHash implementation
 *
 * @param <Key> Objects to use as keys for this hashmap. ID.
 * @param <Value> An object associated with a key.
 *
 * @student Ana-Lea N; CSCI 361; For extra credit; 11/20/2020
 */
public class CuckooHash<Key, Value> {
    private int m;                                          // capacity
    private int n;                                          // elements in the Key Value arrays.

    private Key[] keyOne;
    private Key[] keyTwo;

    private Value[] valueOne;
    private Value[] valueTwo;

    static final int[] PRIMES = {7, 11, 19, 41, 79, 163, 317, 641,
            1279, 2557, 5119, 10243, 20479,
            40961, 81919, 163841, 327673};

    private int indexPrime;                                 // index position for PRIMES

    private final int MAX_ITERATIONS = PRIMES[0];           // chains should be short in separate chainging
                                                            // likewise we should limit the amount we bounce back and forth between arrays. Keep that short to keep it constant.

    /**
     * Creates an empty hashTable
     * with a size of 0 and a minimum capacity
     */
    @SuppressWarnings("unchecked")
    public CuckooHash() {
        n = 0;

        indexPrime = 0;
        m = PRIMES[indexPrime];

        valueOne = (Value[]) new Object[m];
        valueTwo = (Value[]) new Object[m];
        keyOne = (Key[]) new Object[m];
        keyTwo = (Key[]) new Object[m];
    }

    /**
     * gets the hash of this key using it's default function hash().
     *
     * @param key the key with a value
     * @return the index in which this key will be placed in. Depends on it's hashing algorithm
     * and the m / capacity of this instances array.
     */
    public int getHash(Key key) {
        return (key.hashCode() & 0x7fffffff) % m;
    }

    /**
     * See README0 for the sources used to make this hash
     *
     * this is used for the second hash function needed for this datastructure.
     *
     * @param key the object to get hash from.
     * @return the key's second has function.
     */
    public int getSecondHash(Key key) {
        if (key == null) {
            return 0;
        }
        return ((Objects.hash(key, m) * 486187739) & 0x7fffffff) % m; // Works okay for current test domain

        //Use this bad hash if you want cycles in your rehash. It should be able to run fine but there will be more rehashing done.
//        return (key.hashCode() & 0x7fffffff) % m != ((Objects.hash(key, m)) & 0x7fffffff) % m ?
//                ((Objects.hash(key, m)) & 0x7fffffff) % m :
//                (key.hashCode() & 0x7fffffff) % m;
    }

    /**
     * Put this Key Value pair into their respective spots.
     *
     * @param key The object inserted in this hashmap.
     * @param value the object associated with the given key.
     */
    public void put(Key key, Value value) {
        if (!checkLoadFactor()) {
            return;
        }

        int hashOne = getHash(key);
        int hashTwo = getSecondHash(key);

        // Does the key already exist?
        if (keyOne[hashOne] != null && keyOne[hashOne].equals(key)) {
            valueOne[hashOne] = value;
            return;
        } else if (keyTwo[hashTwo] != null && keyTwo[hashTwo].equals(key)) {
            valueTwo[hashTwo] = value;
            return;
        }

        //Key does not exist in table, try to insert
        int i = 0;
        Key currKey = key;
        Value currVal = value;

        Key insertKey;
        Value insertVal;

        while (i <= MAX_ITERATIONS) {
            hashOne = getHash(currKey);

            insertKey = currKey;
            insertVal = currVal;

            currKey = keyOne[hashOne];
            currVal = valueOne[hashOne];

            keyOne[hashOne] = insertKey;
            valueOne[hashOne] = insertVal;

            if (currKey == null) {
                n++;
                return;
            }


            hashTwo = getSecondHash(currKey);

            insertKey = currKey;
            insertVal = currVal;

            currKey = keyTwo[hashTwo];
            currVal = valueTwo[hashTwo];

            keyTwo[hashTwo] = insertKey;
            valueTwo[hashTwo] = insertVal;

            if (currKey == null) {
                n++;
                return;
            }

            i++;
        }
        rehash();
        put(currKey, currVal);
    }

    /**
     * Used when needing to re-adding elements into the hashMap
     * ensures that we are not rehashing during the rehash if an cycle exist.
     *
     * @param key Object to insert, using it as a an id.
     * @param value Object that is associated with key.
     * @return  true    if successfully inserted entry with out any cycles detected.
     *          false   if a cycle is found during the put process.
     */
    private boolean findSpotToPut(Key key, Value value) {
        int hashOne;
        int hashTwo;

        int i = 0;
        Key currKey = key;
        Value currVal = value;

        Key insertKey;
        Value insertVal;

        while (i <= MAX_ITERATIONS) {
            hashOne = getHash(currKey);

            insertKey = currKey;
            insertVal = currVal;

            currKey = keyOne[hashOne];
            currVal = valueOne[hashOne];

            keyOne[hashOne] = insertKey;
            valueOne[hashOne] = insertVal;

            if (currKey == null) {
                n++;
                return false;
            }


            hashTwo = getSecondHash(currKey);

            insertKey = currKey;
            insertVal = currVal;

            currKey = keyTwo[hashTwo];
            currVal = valueTwo[hashTwo];

            keyTwo[hashTwo] = insertKey;
            valueTwo[hashTwo] = insertVal;

            if (currKey == null) {
                n++;
                return false;
            }

            i++;
        }

        return true;
    }

    /**
     * resizes all key value arrays, and tries to reinsert all old values into the
     * newly made larger array.
     *
     * If there is a cycle detected/ or if bouncing back and forth between arrays are too long,
     * then this function will rehash with a larger array, and being it's process again.
     */
    @SuppressWarnings("unchecked")
    private void rehash() {

        // save old arrays
        Key[] tempKeyOne = this.keyOne;
        Key[] tempKeyTwo = this.keyTwo;
        Value[] tempValOne = this.valueOne;
        Value[] tempValTwo = this.valueTwo;

        //boolean used to check if cycle was found in rehash
        boolean clear = true;

        while (clear) {
            clear = false;

            if (indexPrime >= PRIMES.length - 1) {       // cannot grow m anymore
                return;
            }

            // init the new hashTable
            indexPrime++;
            m = PRIMES[indexPrime];

            keyOne = (Key[]) new Object[m];
            keyTwo = (Key[]) new Object[m];
            valueOne = (Value[]) new Object[m];
            valueTwo = (Value[]) new Object[m];

            n = 0;

            //try to add all old values into the new table.
            for (int i = 0; i < tempKeyOne.length; i++) {
                if ((tempKeyOne[i] != null && this.findSpotToPut(tempKeyOne[i], tempValOne[i]))
                    || (tempKeyTwo[i] != null && this.findSpotToPut(tempKeyTwo[i], tempValTwo[i]))) {
                    clear = true;
                    break;
                }
            }
        }
    }


    /**
     * checks the set loadFactor. resize if its over .49.
     */
    private boolean checkLoadFactor() {
        if ((double) n / (m + m) > .49) {
            rehash();
            return true;
        } else if ((double) n / (m + m) == 1) { // Cases where no more rehashing can be done
            System.out.println("Cannot add anymore items and maximum size has been reached.");
            return false;
        }
        return true;
    }


    /**
     * @param key the object you want to get the associated value of.
     * @return the value associate with key
     */
    public Value get(Key key) {
        int hash = getHash(key);
        if (keyOne[hash] != null && keyOne[hash].equals(key)) {
            return valueOne[hash];
        }

        // Check in second table, if it doesn't exist in table 1
        hash = getSecondHash(key);
        if (keyTwo[hash] != null && keyTwo[hash].equals(key)) {
            return valueTwo[hash];
        }

        return null;
    }

    /**
     * deletes the key value pair from this table.
     *
     * @param key the key to delete.
     */
    public void delete(Key key) {
        int hash = getHash(key);
        if (keyOne[hash] != null && keyOne[hash].equals(key)) {
            keyOne[hash] = null;
            valueOne[hash] = null;
            return;
        }

        // Check in second table, if it doesn't exist in table 1
        hash = getSecondHash(key);
        if (keyTwo[hash] != null && keyTwo[hash].equals(key)) {
            keyTwo[hash] = null;
            valueTwo[hash] = null;
        }
    }

    /**
     * @return the elements within this hashTable
     */
    public int size() {
        return n;
    }

    /**
     * Does this hashTable contain the given key?
     *
     * @param key the key to find entry with
     * @return true is this hashTable contains the given key
     */
    public boolean contains(Key key) {
        return (keyOne[getHash(key)] != null && keyOne[getHash(key)].equals(key)) ||
                (keyTwo[getSecondHash(key)] != null && keyTwo[getSecondHash(key)].equals(key));
    }

    /**
     * is this hashTable empty?
     *
     * @return true if there are no elements in this hashTable.
     */
    public boolean isEmpty() {
        return n == 0;
    }

    /**
     * Prints every key value pair found in table.
     */
    public void printTable() {
        System.out.println("First table");
        for (int i = 0; i < m; i++) {
            if (keyOne[i] != null) {
                System.out.printf("%5s, %5s", keyOne[i].toString(), valueOne[i].toString());
            }
        }
        System.out.println();
        System.out.println("Second table");
        for (int i = 0; i < m; i++) {
            if (keyTwo[i] != null) {
                System.out.printf("%5s, %5s", keyTwo[i].toString(), valueTwo[i].toString());
            }
        }
        System.out.println();
    }
}
