package com.ulog.backend.contact.controller;

import com.ulog.backend.common.api.ApiResponse;
import com.ulog.backend.common.api.ErrorCode;
import com.ulog.backend.common.exception.ApiException;
import com.ulog.backend.contact.dto.AibookDto;
import com.ulog.backend.contact.dto.PreviewAibookRequest;
import com.ulog.backend.contact.service.AibookService;
import com.ulog.backend.domain.contact.Contact;
import com.ulog.backend.domain.user.User;
import com.ulog.backend.repository.ContactRepository;
import com.ulog.backend.repository.UserRepository;
import com.ulog.backend.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/contacts")
public class RelationshipInsightController {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final AibookService aibookService;

    public RelationshipInsightController(ContactRepository contactRepository, 
                                      UserRepository userRepository, 
                                      AibookService aibookService) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
        this.aibookService = aibookService;
    }

    @PostMapping("/{cid}/aibook:preview")
    public ResponseEntity<ApiResponse<AibookDto>> previewAibook(
            @PathVariable("cid") Long contactId,
            @Valid @RequestBody(required = false) PreviewAibookRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        if (request == null) {
            request = new PreviewAibookRequest();
        }
        
        Long userId = principal.getUserId();
        
        // 查找联系人并验证权限
        Contact contact = contactRepository.findByIdAndOwnerAndDeletedFalse(contactId, principal.getUser())
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "contact not found"));
        
        // 获取联系人描述
        String contactDesc = (request.getContactDescriptionOverride() != null && !request.getContactDescriptionOverride().isBlank())
                ? request.getContactDescriptionOverride() 
                : contact.getDescription();
        
        // 获取用户描述
        String userDesc = (request.getUserDescriptionOverride() != null && !request.getUserDescriptionOverride().isBlank())
                ? request.getUserDescriptionOverride() 
                : getUserDescription(userId);
        
        // 生成Aibook
        AibookDto aibook = aibookService.generate(contactDesc, userDesc, request.getLanguage());
        
        return ResponseEntity.ok(ApiResponse.success(aibook));
    }
    
    private String getUserDescription(Long userId) {
        return userRepository.findActiveById(userId)
            .map(User::getDescription)
            .orElse("");
    }
}
