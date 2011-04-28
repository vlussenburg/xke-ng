
package com.xebia.xcoss.storage.common;

import java.io.Serializable;

public interface GenericDao<T, K extends Serializable> {

    public void insert(T entity);

    public void update(T entity);

    public void delete(T entity);

    public T findById(K id);
}
