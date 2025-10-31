package com.agent.repository;

import com.agent.entity.ConversationEntity;
import com.agent.mapper.ConversationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import org.xiaowu.wpywebframework.core.generic.JmapRepository;

import java.util.List;

/**
 * @description:
 * @author: xiaowu
 * @time: 2025/8/5 23:58
 */
@Repository
public class ConversationRepository extends JmapRepository<ConversationMapper, ConversationEntity> {

    @Autowired
    public ConversationRepository(ConversationMapper mapper) {
        super.setMapper(mapper);
    }

    /**
     * 根据对话人和主题进行查询
     *
     * @param creatorId 对话人
     * @param topic     主题
     * @param size      条数
     */
    public List<ConversationEntity> getConversationList(String creatorId, String topic, int size) {
        LambdaQueryWrapper<ConversationEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(String.valueOf(creatorId))) {
            wrapper.eq(ConversationEntity::getCreatorId, creatorId);
        }
        wrapper.eq(ConversationEntity::getTopic, topic);
        wrapper.orderByDesc(ConversationEntity::getCreateTime);
        wrapper.last(" limit " + size);
        return this.selectList(wrapper);

    }

    /**
     * 根据对话人和主题进行查询
     *
     * @param creatorId 对话人
     * @param topic     主题
     * @param size      条数
     */
    public List<ConversationEntity> getLatestConversationList(String creatorId, String topic, int size) {
        LambdaQueryWrapper<ConversationEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(String.valueOf(creatorId))) {
            wrapper.eq(ConversationEntity::getCreatorId, creatorId);
        }
        wrapper.eq(ConversationEntity::getTopic, topic);
        wrapper.orderByDesc(ConversationEntity::getCreateTime);
        wrapper.last(" limit " + size);
        return this.selectList(wrapper);
    }
}
