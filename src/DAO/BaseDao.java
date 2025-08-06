package DAO;

import java.util.List;

public interface BaseDao <E>{
    int insert(E e);
    int update(E e);
    int delete(int id);
    List<E> findAll();
}
