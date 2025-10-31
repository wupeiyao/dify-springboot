package com.agent.repository;


import com.agent.entity.ConversationPayloadEntity;
import com.agent.mapper.ConversationPayloadMapper;
import com.agent.model.ConversationPayloadRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.xiaowu.wpywebframework.common.model.Pageable;
import org.xiaowu.wpywebframework.core.generic.JmapRepository;

import java.util.List;

/**
 * @description:
 * @author: xiaowu
 * @time: 2025/8/5 23:59
 */
@Repository
public class ConversationPayloadRepository extends JmapRepository<ConversationPayloadMapper, ConversationPayloadEntity> {

    @Autowired
    public ConversationPayloadRepository(ConversationPayloadMapper mapper) {
        super.setMapper(mapper);
    }

    public void deleteLatestPayload(String conversationId, ConversationPayloadRole role) {
        LambdaQueryWrapper<ConversationPayloadEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConversationPayloadEntity::getConversationId, conversationId);
        wrapper.eq(ConversationPayloadEntity::getRole, role.name());
        wrapper.orderByDesc(ConversationPayloadEntity::getId);
        wrapper.last(" limit 1");
        this.delete(wrapper);
    }

    /**
     * 根据对话Id进行删除
     *
     * @param conversationId 对话Id
     */
    public void deleteByConversationId(String conversationId) {
        LambdaQueryWrapper<ConversationPayloadEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConversationPayloadEntity::getConversationId, conversationId);
        this.delete(wrapper);
    }

    /**
     * 根据对话Id进行查询最新的对话条数
     *
     * @param conversationId  对话Id
     * @param oldestPayloadId 上一条对话内容Id
     */
    public IPage<ConversationPayloadEntity> getConversationPayloadList(String conversationId, String oldestPayloadId, Pageable pageable) {
        IPage<ConversationPayloadEntity> iPage = new Page<>(pageable.getPageIndex(), pageable.getPageSize());
        LambdaQueryWrapper<ConversationPayloadEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConversationPayloadEntity::getConversationId, conversationId);
        if (oldestPayloadId != null) {
            wrapper.lt(ConversationPayloadEntity::getId, oldestPayloadId);
        }
        wrapper.orderByDesc(ConversationPayloadEntity::getId);

        return this.selectPage(iPage, wrapper);

    }

    /**
     * 根据对话Id进行查询最新的对话条数
     *
     * @param conversationId 对话Id
     * @param size           对话条数
     */
    public List<ConversationPayloadEntity> getLatestConversationPayloadList(String conversationId, int size) {
        LambdaQueryWrapper<ConversationPayloadEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConversationPayloadEntity::getConversationId, conversationId);
        wrapper.orderByDesc(ConversationPayloadEntity::getCreateTime);
        wrapper.last(" limit " + size);
        return this.selectList(wrapper);
    }

    /**
     * 根据请求Id进行查询问题内容
     *
     * @param requestId 请求ID
     */
    public ConversationPayloadEntity getConversationQuestionPayload(String requestId) {
        LambdaQueryWrapper<ConversationPayloadEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConversationPayloadEntity::getRequestId, requestId);
        wrapper.eq(ConversationPayloadEntity::getRole, ConversationPayloadRole.Q.name());
        wrapper.last(" limit 1");
        return this.selectOne(wrapper);
    }
}
