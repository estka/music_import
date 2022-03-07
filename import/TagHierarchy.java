/**
 * provides uniform interface for all hierarchies
 */
public interface TagHierarchy<T> {
    
    void addToTagSet(T tag);

    void statistics();

    @Override
    String toString();
}
