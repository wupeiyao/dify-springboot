package com.agent.conversation;


import com.agent.entity.ConversationEntity;
import com.agent.entity.ConversationPayloadEntity;
import com.agent.model.Conversation;
import com.agent.model.ConversationGroup;
import com.agent.model.ConversationPayload;
import com.agent.repository.ConversationPayloadRepository;
import com.agent.repository.ConversationRepository;
import com.baomidou.mybatisplus.core.metadata.IPage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.xiaowu.wpywebframework.authorization.context.UserContext;
import org.xiaowu.wpywebframework.common.model.Pageable;
import org.xiaowu.wpywebframework.common.model.PagerResult;
import org.xiaowu.wpywebframework.core.genid.IDGenerator;
import org.xiaowu.wpywebframework.core.utils.Y;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: xiaowu
 * @time: 2025/8/5 23:51
 */

@Component
@RequiredArgsConstructor
public class DefaultConversationManager implements ConversationManager{

    private final ConversationRepository conversationRepository;

    private final ConversationPayloadRepository conversationPayloadRepository;

    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 日志过期时间，单位天数
     */
    @Value("${spring.ai.agent.conversation.days:30}")
    private int days;


    @Override
    public Conversation insert(UserContext authorization, Conversation conversation) {

        ConversationEntity entity = conversationRepository.selectById(conversation.getId());
        Conversation conversationVo = new Conversation();
        if (entity != null) {
            BeanUtils.copyProperties(entity, conversationVo);
            return conversationVo;
        }
        entity = new ConversationEntity();
        entity.setId(IDGenerator.nextIdStr());
        entity.setConversationId(conversation.getConversationId());
        entity.setTopic(conversation.getTopic());
        String name = conversation.getName();
        if (name != null && name.length() > 20) {
            // 限制最多20个字符
            name = name.substring(0, 20);
        }
        entity.setName(name);
        if (authorization != null) {
            entity.setCreatorId(String.valueOf(authorization.getUserId()));
            entity.setCreatorName(authorization.getUsername());
        }
        conversationRepository.insert(entity);
        BeanUtils.copyProperties(entity, conversationVo);
        return conversationVo;
    }

    @Override
    public Conversation rename(UserContext userContext, Conversation conversation) {
        ConversationEntity entity = conversationRepository.selectById(conversation.getId());
        String name = conversation.getName();
        if (name != null && name.length() > 20) {
            name = name.substring(0, 20);
        }
        entity.setName(name);
        entity.setConversationId(conversation.getConversationId());
        conversationRepository.updateById(entity);
        return Y.bean.copy(entity,Conversation.class);
    }

    @Override
    public void addPayLoad(UserContext userContext, ConversationPayload payload, boolean refresh) {
        ConversationPayloadEntity entity = new ConversationPayloadEntity();
        entity.setId(payload.getId());
        entity.setRequestId(payload.getRequestId());
        entity.setMessageId(payload.getMessageId());
        entity.setConversationId(payload.getConversationId());
        entity.setContent(payload.getContent());
        entity.setTopic(payload.getTopic());
        entity.setOk(payload.getOk());
        if (payload.getId() == null && userContext != null) {
            entity.setCreatorId(String.valueOf(userContext.getUserId()));
            entity.setCreatorName(userContext.getUsername());
        }
        entity.setCreateTime(new Date());
        conversationPayloadRepository.insert(entity);
    }

    @Override
    public Conversation getConversation(String conversationId) {
        ConversationEntity entity = conversationRepository.selectById(conversationId);
        if (entity != null) {
            return  Y.bean.copy(entity,Conversation.class);
        }
        throw new RuntimeException("没有相关会话");

    }

    @Override
    public List<Conversation> getConversationList(UserContext context, String topic) {
        return conversationRepository.getConversationList(context.getUserId(), topic, days)
                .stream()
                .map(e -> {
                    Conversation conversation = Y.bean.copy(e, Conversation.class);
                    conversation.setGroup(getDateGroup(conversation));
                    return conversation;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Conversation delete(UserContext context, Conversation payload) {
        ConversationEntity entity = conversationRepository.selectById(payload.getId());
        Conversation conversation = Y.bean.copy(entity, Conversation.class);
        conversationRepository.deleteById(entity.getId());
        //同步删除对话内容
        conversationPayloadRepository.deleteByConversationId(entity.getConversationId());
        return conversation;
    }

    @Override
    public List<Conversation> getLatestConversationList(UserContext context, String topic, int size) {
        return conversationRepository.getLatestConversationList(context.getUserId(), topic, size)
                .stream()
                .map(e -> {
                    Conversation conversation = Y.bean.copy(e, Conversation.class);
                    conversation.setGroup(getDateGroup(conversation));
                    return conversation;
                })
                .collect(Collectors.toList());
    }

    @Override
    public PagerResult<ConversationPayload> getConversationPayloadList(UserContext context, String conversationId, String oldestPayloadId) {
        Pageable pageable = new Pageable(1, 20);
        IPage<ConversationPayloadEntity> iPage = conversationPayloadRepository.getConversationPayloadList(conversationId, oldestPayloadId, pageable);
        List<ConversationPayload> list = iPage.getRecords().stream()
                //聊天记录是按下往上排序
                .sorted(Comparator.comparing(ConversationPayloadEntity::getId))
                .map(e -> Y.bean.copy(e, ConversationPayload.class))
                .toList();

        return PagerResult.of(pageable, list, iPage.getTotal());
    }

    @Override
    public List<ConversationPayload> getLatestConversationPayloadList(UserContext context, String conversationId, int size) {
        return conversationPayloadRepository.getLatestConversationPayloadList(conversationId, size)
                .stream()
                //聊天记录是按下往上排序
                .sorted(Comparator.comparing(ConversationPayloadEntity::getId))
                .map(e -> Y.bean.copy(e, ConversationPayload.class))
                .toList();
    }


    /**
     *
     * @param conversation 会话信息
     * @return ConversationGroup 会话分组
     */
    private ConversationGroup getDateGroup(Conversation conversation) {
        Date date = conversation.getCreateTime();
        // 获取当前日期
        LocalDate today = LocalDate.now();
        // 将输入日期转换为LocalDate
        LocalDate inputLocalDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (inputLocalDate.isEqual(today)) {
            return ConversationGroup.of("D0", "今天");
        }
        // 计算日期差
        long days = java.time.temporal.ChronoUnit.DAYS.between(inputLocalDate, today);

        if (days > 0 && days <= 7) {
            return ConversationGroup.of("D7", "7天内");
        } else if (days > 7 && days <= 30) {
            return ConversationGroup.of("D30", "30天内");
        } else {
            return ConversationGroup.of(format.format(date), format.format(date));
        }
    }

}
