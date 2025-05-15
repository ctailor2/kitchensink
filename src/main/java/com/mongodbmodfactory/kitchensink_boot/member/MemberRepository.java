package com.mongodbmodfactory.kitchensink_boot.member;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "members", path = "members")
public interface MemberRepository extends PagingAndSortingRepository<Member, Long>, CrudRepository<Member, Long> {
}
