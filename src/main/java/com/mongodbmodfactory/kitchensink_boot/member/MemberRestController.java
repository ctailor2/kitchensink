package com.mongodbmodfactory.kitchensink_boot.member;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/kitchensink/rest/members")
public class MemberRestController {

    private final MemberService memberService;

    public MemberRestController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<Member> listAllMembers() {
        return memberService.getAllMembers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Member> getMemberById(@PathVariable String id) {
        return memberService.getMemberById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Member> createMember(@Valid @RequestBody Member member) {
        return ResponseEntity.ok(memberService.createMember(member));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    @ExceptionHandler(DuplicateEmailException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleDuplicateEmailException(DuplicateEmailException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("email", ex.getMessage());
        return errors;
    }
}