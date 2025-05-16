package com.mongodbmodfactory.kitchensink_boot.member;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "members", path = "members")
public interface MemberRepository extends MongoRepository<Member, String> {
}
