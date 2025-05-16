package com.mongodbmodfactory.kitchensink_boot.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Member> getMemberById(String id) {
        return memberRepository.findById(id);
    }

    @Transactional
    public Member createMember(Member member) {
        if (memberRepository.existsByEmail(member.getEmail())) {
            throw new DuplicateEmailException("Email taken");
        }
        return memberRepository.save(member);
    }
} 