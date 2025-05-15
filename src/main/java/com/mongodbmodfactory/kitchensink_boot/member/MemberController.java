package com.mongodbmodfactory.kitchensink_boot.member;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MemberController {
    private final MemberRepository memberRepository;

    public MemberController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @GetMapping("/")
    public String getMembers(Model model, Member member) {
        model.addAttribute("members", memberRepository.findAll());
        return "members-page";
    }

    @PostMapping("/createMember")
    public String createMember(@Valid Member member, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("members", memberRepository.findAll());
            return "members-page";
        }

        memberRepository.save(member);
        return "redirect:/";
    }
}
