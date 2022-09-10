package com.sonnh.coreuibe.repositories;

import com.sonnh.coreuibe.models.TypeCodeModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPriceFx extends CrudRepository<TypeCodeModel, Integer> {
}
