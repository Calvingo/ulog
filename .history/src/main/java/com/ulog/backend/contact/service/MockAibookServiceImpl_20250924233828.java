package com.ulog.backend.contact.service;

import com.ulog.backend.contact.dto.AibookDto;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("test")
public class MockAibookServiceImpl implements AibookService {
    
    @Override
    public AibookDto generate(String contactDesc, String userDesc, String language) {
        AibookDto dto = new AibookDto();
        
        // 创建模拟数据
        AibookDto.Item item1 = new AibookDto.Item("测试行为倾向", "基于描述推断", "contact");
        AibookDto.Item item2 = new AibookDto.Item("测试价值观", "基于描述推断", "user");
        
        dto.setBehaviorTendencies(List.of(item1));
        dto.setValuesAndEmotions(List.of(item2));
        dto.setLatentNeeds(List.of());
        dto.setTaboos(List.of());
        dto.setRelationshipOpportunities(List.of());
        
        return dto;
    }
}
