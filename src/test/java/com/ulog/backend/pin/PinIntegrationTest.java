package com.ulog.backend.pin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ulog.backend.AbstractIntegrationTest;
import com.ulog.backend.conversation.dto.QaHistoryEntry;
import com.ulog.backend.domain.contact.Contact;
import com.ulog.backend.domain.conversation.ConversationSession;
import com.ulog.backend.domain.pin.Pin;
import com.ulog.backend.domain.pin.PinSourceType;
import com.ulog.backend.domain.user.User;
import com.ulog.backend.pin.dto.CreatePinRequest;
import com.ulog.backend.pin.dto.PinResponse;
import com.ulog.backend.pin.dto.PinSummaryResponse;
import com.ulog.backend.pin.dto.UpdatePinRequest;
import com.ulog.backend.pin.service.PinService;
import com.ulog.backend.repository.ContactRepository;
import com.ulog.backend.repository.ConversationSessionRepository;
import com.ulog.backend.repository.PinRepository;
import com.ulog.backend.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class PinIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PinService pinService;

    @Autowired
    private PinRepository pinRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private ConversationSessionRepository conversationSessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Contact testContact;
    private ConversationSession testSession;

    @BeforeEach
    void setUp() throws Exception {
        // 创建测试用户
        testUser = new User();
        testUser.setPhone("+8613900139000");
        testUser.setPasswordHash(passwordEncoder.encode("password"));
        testUser.setName("Test User");
        testUser = userRepository.save(testUser);

        // 创建测试联系人
        testContact = new Contact(testUser, "Test Contact", "A test contact");
        testContact = contactRepository.save(testContact);

        // 创建测试会话并添加QA历史
        testSession = new ConversationSession("test-session-001", testUser.getId(), "Test Contact");
        testSession.setContactId(testContact.getId());
        testSession.setStatus("QA_ACTIVE");

        // 创建QA历史
        List<QaHistoryEntry> qaHistory = new ArrayList<>();
        qaHistory.add(new QaHistoryEntry("这是第一个问题", "这是第一个答案", false));
        qaHistory.add(new QaHistoryEntry("这是第二个问题", "这是第二个答案", 
                                        "需要补充什么", "补充后的答案", true));

        String qaHistoryJson = objectMapper.writeValueAsString(qaHistory);
        testSession.setQaHistory(qaHistoryJson);
        testSession = conversationSessionRepository.save(testSession);
    }

    @Test
    void testCreatePin_Success() {
        // Given
        CreatePinRequest request = new CreatePinRequest();
        request.setSessionId(testSession.getSessionId());
        request.setQaIndex(0);
        request.setNote("这是一个有用的回答");
        request.setTags("测试,重要");

        // When
        PinResponse response = pinService.createPin(testUser.getId(), request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getPinId());
        assertEquals(PinSourceType.CONTACT_QA, response.getSourceType());
        assertEquals("这是第一个问题", response.getQuestion());
        assertEquals("这是第一个答案", response.getAnswer());
        assertEquals("这是一个有用的回答", response.getNote());
        assertEquals(2, response.getTags().size());
        assertTrue(response.getTags().contains("测试"));
        assertTrue(response.getTags().contains("重要"));
    }

    @Test
    void testCreatePin_WithSupplementInfo() {
        // Given
        CreatePinRequest request = new CreatePinRequest();
        request.setSessionId(testSession.getSessionId());
        request.setQaIndex(1);

        // When
        PinResponse response = pinService.createPin(testUser.getId(), request);

        // Then
        assertNotNull(response);
        assertEquals("这是第二个问题", response.getQuestion());
        assertEquals("这是第二个答案", response.getAnswer());
        assertEquals("需要补充什么", response.getSupplementQuestion());
        assertEquals("补充后的答案", response.getSupplementAnswer());
        assertTrue(response.getHasSupplementInfo());
    }

    @Test
    void testCreatePin_Duplicate_ShouldFail() {
        // Given - 先创建一个Pin
        CreatePinRequest request1 = new CreatePinRequest();
        request1.setSessionId(testSession.getSessionId());
        request1.setQaIndex(0);
        pinService.createPin(testUser.getId(), request1);

        // When & Then - 尝试创建重复的Pin应该失败
        CreatePinRequest request2 = new CreatePinRequest();
        request2.setSessionId(testSession.getSessionId());
        request2.setQaIndex(0);

        assertThrows(Exception.class, () -> {
            pinService.createPin(testUser.getId(), request2);
        });
    }

    @Test
    void testListPins_Success() {
        // Given - 创建多个Pin
        CreatePinRequest request1 = new CreatePinRequest();
        request1.setSessionId(testSession.getSessionId());
        request1.setQaIndex(0);
        pinService.createPin(testUser.getId(), request1);

        CreatePinRequest request2 = new CreatePinRequest();
        request2.setSessionId(testSession.getSessionId());
        request2.setQaIndex(1);
        pinService.createPin(testUser.getId(), request2);

        // When
        List<PinSummaryResponse> pins = pinService.listPins(testUser.getId(), null, null);

        // Then
        assertEquals(2, pins.size());
    }

    @Test
    void testListPinsByContact_Success() {
        // Given
        CreatePinRequest request = new CreatePinRequest();
        request.setSessionId(testSession.getSessionId());
        request.setQaIndex(0);
        pinService.createPin(testUser.getId(), request);

        // When
        List<PinSummaryResponse> pins = pinService.listPins(testUser.getId(), testContact.getId(), null);

        // Then
        assertEquals(1, pins.size());
        assertEquals(testContact.getId(), pins.get(0).getContactId());
    }

    @Test
    void testUpdatePin_Success() {
        // Given - 创建一个Pin
        CreatePinRequest createRequest = new CreatePinRequest();
        createRequest.setSessionId(testSession.getSessionId());
        createRequest.setQaIndex(0);
        PinResponse created = pinService.createPin(testUser.getId(), createRequest);

        // When - 更新Pin
        UpdatePinRequest updateRequest = new UpdatePinRequest();
        updateRequest.setNote("更新后的备注");
        updateRequest.setTags("新标签1,新标签2,新标签3");

        PinResponse updated = pinService.updatePin(testUser.getId(), created.getPinId(), updateRequest);

        // Then
        assertEquals("更新后的备注", updated.getNote());
        assertEquals(3, updated.getTags().size());
    }

    @Test
    void testDeletePin_Success() {
        // Given
        CreatePinRequest createRequest = new CreatePinRequest();
        createRequest.setSessionId(testSession.getSessionId());
        createRequest.setQaIndex(0);
        PinResponse created = pinService.createPin(testUser.getId(), createRequest);

        // When
        pinService.deletePin(testUser.getId(), created.getPinId());

        // Then
        Pin deletedPin = pinRepository.findById(created.getPinId()).orElse(null);
        assertNull(deletedPin);
    }

    @Test
    void testIsPinned_Success() {
        // Given
        CreatePinRequest request = new CreatePinRequest();
        request.setSessionId(testSession.getSessionId());
        request.setQaIndex(0);
        pinService.createPin(testUser.getId(), request);

        // When
        boolean isPinned = pinService.isPinned(testUser.getId(), testSession.getSessionId(), 0);
        boolean isNotPinned = pinService.isPinned(testUser.getId(), testSession.getSessionId(), 1);

        // Then
        assertTrue(isPinned);
        assertFalse(isNotPinned);
    }
}

