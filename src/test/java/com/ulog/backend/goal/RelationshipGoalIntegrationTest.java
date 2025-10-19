package com.ulog.backend.goal;

import com.ulog.backend.AbstractIntegrationTest;
import com.ulog.backend.domain.contact.Contact;
import com.ulog.backend.domain.goal.RelationshipGoal;
import com.ulog.backend.domain.goal.enums.GoalStatus;
import com.ulog.backend.domain.user.User;
import com.ulog.backend.goal.dto.CreateGoalRequest;
import com.ulog.backend.goal.dto.GoalDetailResponse;
import com.ulog.backend.goal.dto.UpdateGoalRequest;
import com.ulog.backend.goal.service.RelationshipGoalService;
import com.ulog.backend.repository.ContactRepository;
import com.ulog.backend.repository.RelationshipGoalRepository;
import com.ulog.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class RelationshipGoalIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RelationshipGoalService goalService;

    @Autowired
    private RelationshipGoalRepository goalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Contact testContact;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = new User();
        testUser.setPhone("+8613800138000");
        testUser.setPasswordHash(passwordEncoder.encode("password"));
        testUser.setName("Test User");
        testUser = userRepository.save(testUser);

        // 创建测试联系人
        testContact = new Contact(testUser, "Test Contact", "A test contact");
        testContact = contactRepository.save(testContact);
    }

    @Test
    void testCreateGoal_Success() {
        // Given
        CreateGoalRequest request = new CreateGoalRequest();
        request.setContactId(testContact.getId());
        request.setGoalDescription("希望能够和这个联系人建立更深厚的友谊");

        // When
        GoalDetailResponse response = goalService.createGoal(testUser.getId(), request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getGoalId());
        assertEquals(testContact.getId(), response.getContactId());
        assertEquals(testContact.getName(), response.getContactName());
        assertEquals("希望能够和这个联系人建立更深厚的友谊", response.getGoalDescription());
        assertEquals(GoalStatus.ACTIVE, response.getStatus());
        
        // 验证数据库中是否存在
        RelationshipGoal savedGoal = goalRepository.findById(response.getGoalId()).orElse(null);
        assertNotNull(savedGoal);
        assertEquals(testUser.getId(), savedGoal.getUser().getId());
        assertEquals(testContact.getId(), savedGoal.getContact().getId());
    }

    @Test
    void testUpdateGoal_Success() {
        // Given - 创建一个目标
        CreateGoalRequest createRequest = new CreateGoalRequest();
        createRequest.setContactId(testContact.getId());
        createRequest.setGoalDescription("初始目标描述");
        GoalDetailResponse created = goalService.createGoal(testUser.getId(), createRequest);

        // When - 更新目标
        UpdateGoalRequest updateRequest = new UpdateGoalRequest();
        updateRequest.setGoalDescription("更新后的目标描述");
        updateRequest.setStatus(GoalStatus.COMPLETED);

        GoalDetailResponse updated = goalService.updateGoal(testUser.getId(), created.getGoalId(), updateRequest);

        // Then
        assertEquals("更新后的目标描述", updated.getGoalDescription());
        assertEquals(GoalStatus.COMPLETED, updated.getStatus());
    }

    @Test
    void testDeleteGoal_Success() {
        // Given
        CreateGoalRequest createRequest = new CreateGoalRequest();
        createRequest.setContactId(testContact.getId());
        createRequest.setGoalDescription("要被删除的目标");
        GoalDetailResponse created = goalService.createGoal(testUser.getId(), createRequest);

        // When
        goalService.deleteGoal(testUser.getId(), created.getGoalId());

        // Then
        RelationshipGoal deletedGoal = goalRepository.findById(created.getGoalId()).orElse(null);
        assertNotNull(deletedGoal);
        assertTrue(deletedGoal.isDeleted());
    }

    @Test
    void testListGoals_Success() {
        // Given - 创建多个目标
        CreateGoalRequest request1 = new CreateGoalRequest();
        request1.setContactId(testContact.getId());
        request1.setGoalDescription("目标1");
        goalService.createGoal(testUser.getId(), request1);

        CreateGoalRequest request2 = new CreateGoalRequest();
        request2.setContactId(testContact.getId());
        request2.setGoalDescription("目标2");
        goalService.createGoal(testUser.getId(), request2);

        // When
        var goals = goalService.listGoals(testUser.getId(), null);

        // Then
        assertTrue(goals.size() >= 2);
    }

    @Test
    void testListGoalsByContact_Success() {
        // Given
        CreateGoalRequest request = new CreateGoalRequest();
        request.setContactId(testContact.getId());
        request.setGoalDescription("按联系人筛选的目标");
        goalService.createGoal(testUser.getId(), request);

        // When
        var goals = goalService.listGoals(testUser.getId(), testContact.getId());

        // Then
        assertTrue(goals.size() >= 1);
        assertTrue(goals.stream().allMatch(g -> g.getContactId().equals(testContact.getId())));
    }
}

