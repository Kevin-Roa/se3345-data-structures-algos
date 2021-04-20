/** Starter code for P3
 *  @author Kevin Roa
 */

package kar180005;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

public class MDS {
    public class Product {
        Integer price;
        List<Integer> desc;

        public Product( int price, java.util.List<Integer> desc) {
            this.price = price;
            this.desc = new LinkedList<>(desc);;
        }

        // Used for debugging
        public String toString() {
            return "$" + price + ", " + desc.toString();
        }
    }

    // Add fields of MDS here

    // id -> {price, desc}
    TreeMap<Integer, Product> tree;
    // desc -> [ids]
    Map<Integer, TreeSet<Integer>> table;

    // Constructors
    public MDS() {
        tree = new TreeMap<>();
        table = new HashMap<>();
    }

    // Public methods of MDS. Do not change their signatures.

    /*
     * a. Insert(id,price,list): insert a new item whose description is given in the
     * list. If an entry with the same id already exists, then its description and
     * price are replaced by the new values, unless list is null or empty, in which
     * case, just the price is updated. Returns 1 if the item is new, and 0
     * otherwise.
     */
    public int insert(int id, int price, java.util.List<Integer> list) {
        // If new product
        if(!tree.containsKey(id)) {
            // Add product to tree
            tree.put(id, new Product(price, list));

            for(Integer i : list) {
                // Add product to table
                TreeSet<Integer> set = table.get(i);
                if (set == null) {
                    TreeSet<Integer> value = new TreeSet<>();
                    value.add(id);
                    table.put(i, value);
                } else {
                    set.add(id);
                    table.put(i, set);
                }
    
            }
            return 1;
        }

        // Get current product desc
        List<Integer> desc = tree.get(id).desc;

        delete(id);

        // Insert product with new data
        if (list == null || list.size() == 0)
            insert(id, price, desc);
        else
            insert(id, price, list);
        
        return 0;

    }

    // b. Find(id): return price of item with given id (or 0, if not found).
    public int find(int id) {
        Product prod = tree.get(id);
        return prod != null ? prod.price : 0;
    }

    /*
     * c. Delete(id): delete item from storage. Returns the sum of the ints that are
     * in the description of the item deleted, or 0, if such an id did not exist.
     */
    public int delete(int id) {
        // Remove product from tree, get data
        Product key = tree.remove(id);

        int sum = 0;
        if (key != null) {
            // Loop over all values in product desc
            for (Integer i : key.desc) {
                // Remove reference to product from key
                TreeSet<Integer> ids = table.get(i);
                if(ids != null) {
                    if (ids.size() > 1) {
                        ids.remove(id);
                        table.put(i, ids);
                    }
                    else 
                        table.remove(i);
                }
                sum += i;
            }
        }

        return sum;
    }

    /*
     * d. FindMinPrice(n): given an integer, find items whose description contains
     * that number (exact match with one of the ints in the item's description), and
     * return lowest price of those items. Return 0 if there is no such item.
     */
    public int findMinPrice(int n) {
        Integer min = null;
        for(Integer i : table.get(n)) {
            Product prod = tree.get(i);
            if (prod != null) {
                int price = prod.price;
                if(min == null || price < min)
                    min = price;
            }
        }

        return min == null ? 0 : min;
    }

    /*
     * e. FindMaxPrice(n): given an integer, find items whose description contains
     * that number, and return highest price of those items. Return 0 if there is no
     * such item.
     */
    public int findMaxPrice(int n) {
        Integer max = null;
        for (Integer i : table.get(n)) {
            Product prod = tree.get(i);
            if (prod != null) {
                int price = prod.price;
                if (max == null || price > max)
                    max = price;
            }
        }

        return max == null ? 0 : max;
    }

    /*
     * f. FindPriceRange(n,low,high): given int n, find the number of items whose
     * description contains n, and in addition, their prices fall within the given
     * range, [low, high].
     */
    public int findPriceRange(int n, int low, int high) {
        int count = 0;
        for (Integer i : table.get(n)) {
            Product prod = tree.get(i);
            if (prod != null) {
                int price = prod.price;
                if (price >= low && price <= high)
                    count ++;
            }
        }

        return count;
    }

    /*
     * g. RemoveNames(id, list): Remove elements of list from the description of id.
     * It is possible that some of the items in the list are not in the id's
     * description. Return the sum of the numbers that are actually deleted from the
     * description of id. Return 0 if there is no such id.
     */
    public int removeNames(int id, java.util.List<Integer> list) {
        Product prod = tree.get(id);
        int sum = 0;

        if (prod != null) {
            // List of values the product will have afterward
            List<Integer> keep = new LinkedList<>();
            for(Integer i : list) {
                // If a value from list is also in the product desc
                TreeSet<Integer> ids = table.get(i);
                if (ids != null && ids.contains(id))
                    sum += i;
                else
                    // If value not in product desc
                    keep.add(i);
            }
            
            insert(id, prod.price, keep);
        }

        return sum;
    }
}
