package com.ulog.backend.contact.controller;

import com.ulog.backend.common.api.ApiResponse;
import com.ulog.backend.contact.dto.ContactRequest;
import com.ulog.backend.contact.dto.ContactResponse;
import com.ulog.backend.contact.dto.ContactUpdateRequest;
import com.ulog.backend.contact.service.ContactService;
import com.ulog.backend.security.UserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ContactResponse>> create(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody ContactRequest request) {
        ContactResponse response = contactService.create(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ContactResponse>>> list(@AuthenticationPrincipal UserPrincipal principal) {
        List<ContactResponse> response = contactService.list(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{cid}")
    public ResponseEntity<ApiResponse<ContactResponse>> get(@AuthenticationPrincipal UserPrincipal principal, @PathVariable("cid") Long contactId) {
        ContactResponse response = contactService.get(principal.getUserId(), contactId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{cid}")
    public ResponseEntity<ApiResponse<ContactResponse>> update(@AuthenticationPrincipal UserPrincipal principal, @PathVariable("cid") Long contactId, @Valid @RequestBody ContactUpdateRequest request) {
        ContactResponse response = contactService.update(principal.getUserId(), contactId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{cid}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable("cid") Long contactId) {
        contactService.delete(principal.getUserId(), contactId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
