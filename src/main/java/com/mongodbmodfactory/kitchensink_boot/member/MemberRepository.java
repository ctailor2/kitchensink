package com.mongodbmodfactory.kitchensink_boot.member;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

@Repository
public interface MemberRepository extends MongoRepository<Member, String>, CrudRepository<Member, String> {
    boolean existsByEmail(String email);
} 