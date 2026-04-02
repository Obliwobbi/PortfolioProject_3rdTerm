package app.interfaces;

import java.util.Set;

public interface IDAO<T> {
    T create(T entity);
    Set<T> getAll();
    T getById(Long id);
    T update(T entity);
    Long delete(T entity);
}
