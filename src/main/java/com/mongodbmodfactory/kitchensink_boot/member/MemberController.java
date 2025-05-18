package com.mongodbmodfactory.kitchensink_boot.member;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/kitchensink")
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute("member")) {
            model.addAttribute("member", new Member());
        }
        model.addAttribute("members", memberService.getAllMembers());
        return "index";
    }

    @PostMapping("/kitchensink/register")
    public String register(@Valid @ModelAttribute("member") Member member,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.member", result);
            redirectAttributes.addFlashAttribute("member", member);
            return "redirect:/kitchensink";
        }

        try {
            memberService.createMember(member);
            redirectAttributes.addFlashAttribute("message", "Registered!");
        } catch (DuplicateEmailException e) {
            result.rejectValue("email", "error.member", "Unique index or primary key violation");
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.member", result);
            redirectAttributes.addFlashAttribute("member", member);
        }

        return "redirect:/kitchensink";
    }
} 