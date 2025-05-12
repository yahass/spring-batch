package com.example.demo.repositories;

import com.example.demo.entity.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonEntityRepository extends JpaRepository<PersonEntity, String> {

}
