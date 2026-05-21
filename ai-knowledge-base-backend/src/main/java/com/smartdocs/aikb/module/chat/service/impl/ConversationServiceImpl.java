package com.smartdocs.aikb.module.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartdocs.aikb.common.exception.BusinessException;
import com.smartdocs.aikb.common.result.ResultCode;
import com.smartdocs.aikb.module.chat.dto.ChatMessageVO;
import com.smartdocs.aikb.module.chat.dto.CitationVO;
import com.smartdocs.aikb.module.chat.dto.ConversationCreateRequest;
import com.smartdocs.aikb.module.chat.dto.ConversationVO;
import com.smartdocs.aikb.module.chat.entity.ChatConversation;
import com.smartdocs.aikb.module.chat.entity.ChatMessage;
import com.smartdocs.aikb.module.chat.mapper.ChatConversationMapper;
import com.smartdocs.aikb.module.chat.mapper.ChatMessageMapper;
import com.smartdocs.aikb.module.chat.service.ConversationService;
import com.smartdocs.aikb.module.kb.entity.KbDocument;
import com.smartdocs.aikb.module.kb.entity.KbDocumentChunk;
import com.smartdocs.aikb.module.kb.entity.KnowledgeBase;
import com.smartdocs.aikb.module.kb.mapper.KbDocumentChunkMapper;
import com.smartdocs.aikb.module.kb.mapper.KbDocumentMapper;
import com.smartdocs.aikb.module.kb.mapper.KnowledgeBaseMapper;
import com.smartdocs.aikb.module.kb.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ChatConversationMapper conversationMapper;
    private final ChatMessageMapper messageMapper;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final KnowledgeBaseService knowledgeBaseService;
    private final KbDocumentMapper documentMapper;
    private final KbDocumentChunkMapper chunkMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<ConversationVO> listMine(Long userId) {
        List<ChatConversation> rows = conversationMapper.selectList(
                new LambdaQueryWrapper<ChatConversation>()
                        .eq(ChatConversation::getUserId, userId)
                        .orderByDesc(ChatConversation::getUpdateTime));
        if (rows.isEmpty()) return List.of();
        Map<Long, String> kbNames = loadKbNames(rows.stream().map(ChatConversation::getKbId).collect(Collectors.toSet()));
        return rows.stream().map(c -> toVO(c, kbNames.get(c.getKbId()))).toList();
    }

    @Override
    @Transactional
    public ConversationVO create(Long userId, ConversationCreateRequest request) {
        if (knowledgeBaseService.resolveRole(userId, request.getKbId()) == null) {
            throw new BusinessException(ResultCode.KB_NO_PERMISSION);
        }
        ChatConversation c = new ChatConversation();
        c.setUserId(userId);
        c.setKbId(request.getKbId());
        c.setTitle(StringUtils.hasText(request.getTitle()) ? request.getTitle() : "新会话");
        conversationMapper.insert(c);
        return toVO(c, kbName(request.getKbId()));
    }

    @Override
    @Transactional
    public ConversationVO rename(Long userId, Long conversationId, String title) {
        ChatConversation c = requireOwn(userId, conversationId);
        c.setTitle(title);
        conversationMapper.updateById(c);
        return toVO(c, kbName(c.getKbId()));
    }

    @Override
    @Transactional
    public void delete(Long userId, Long conversationId) {
        ChatConversation c = requireOwn(userId, conversationId);
        conversationMapper.deleteById(c.getId());
        // 物理删除消息（会话已是逻辑删，消息无需保留）
        messageMapper.delete(new LambdaQueryWrapper<ChatMessage>().eq(ChatMessage::getConversationId, c.getId()));
    }

    @Override
    public ConversationVO detail(Long userId, Long conversationId) {
        ChatConversation c = requireOwn(userId, conversationId);
        return toVO(c, kbName(c.getKbId()));
    }

    @Override
    public List<ChatMessageVO> listMessages(Long userId, Long conversationId) {
        requireOwn(userId, conversationId);
        List<ChatMessage> rows = messageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getConversationId, conversationId)
                        .orderByAsc(ChatMessage::getId));
        if (rows.isEmpty()) return List.of();
        // 一次性把所有引用 chunkIds 收齐再批量查
        Set<Long> chunkIds = new HashSet<>();
        Map<Long, List<Long>> idsPerMsg = new HashMap<>();
        for (ChatMessage m : rows) {
            List<Long> ids = parseChunkIds(m.getCitationChunkIds());
            idsPerMsg.put(m.getId(), ids);
            chunkIds.addAll(ids);
        }
        Map<Long, CitationVO> citationMap = loadCitations(chunkIds);
        return rows.stream().map(m -> {
            ChatMessageVO vo = new ChatMessageVO();
            vo.setId(m.getId());
            vo.setConversationId(m.getConversationId());
            vo.setRole(m.getRole());
            vo.setContent(m.getContent());
            vo.setTokenCount(m.getTokenCount());
            vo.setCreateTime(m.getCreateTime());
            List<Long> ids = idsPerMsg.getOrDefault(m.getId(), List.of());
            if (!ids.isEmpty()) {
                vo.setCitations(ids.stream().map(citationMap::get).filter(Objects::nonNull).toList());
            }
            return vo;
        }).toList();
    }

    @Override
    public ChatConversation requireOwn(Long userId, Long conversationId) {
        ChatConversation c = conversationMapper.selectById(conversationId);
        if (c == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会话不存在");
        }
        if (!Objects.equals(c.getUserId(), userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问该会话");
        }
        return c;
    }

    // ─── helpers ───

    private ConversationVO toVO(ChatConversation c, String kbName) {
        return new ConversationVO(c.getId(), c.getKbId(), kbName, c.getTitle(), c.getCreateTime(), c.getUpdateTime());
    }

    private String kbName(Long kbId) {
        KnowledgeBase kb = knowledgeBaseMapper.selectById(kbId);
        return kb == null ? null : kb.getName();
    }

    private Map<Long, String> loadKbNames(Collection<Long> kbIds) {
        if (kbIds.isEmpty()) return Map.of();
        List<KnowledgeBase> kbs = knowledgeBaseMapper.selectBatchIds(kbIds);
        return kbs.stream().collect(Collectors.toMap(KnowledgeBase::getId, KnowledgeBase::getName));
    }

    private List<Long> parseChunkIds(String json) {
        if (!StringUtils.hasText(json)) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            log.warn("[Chat] parse citation_chunk_ids failed: {}", json, e);
            return List.of();
        }
    }

    private Map<Long, CitationVO> loadCitations(Set<Long> chunkIds) {
        if (chunkIds.isEmpty()) return Map.of();
        List<KbDocumentChunk> chunks = chunkMapper.selectBatchIds(chunkIds);
        if (chunks.isEmpty()) return Map.of();
        Set<Long> docIds = chunks.stream().map(KbDocumentChunk::getDocId).collect(Collectors.toSet());
        Map<Long, String> docNames = documentMapper.selectBatchIds(docIds).stream()
                .collect(Collectors.toMap(KbDocument::getId, KbDocument::getName));
        Map<Long, CitationVO> out = new HashMap<>();
        for (KbDocumentChunk c : chunks) {
            CitationVO v = new CitationVO();
            v.setChunkId(c.getId());
            v.setDocId(c.getDocId());
            v.setDocName(docNames.get(c.getDocId()));
            v.setSeq(c.getSeq());
            v.setSnippet(truncate(c.getContent(), 200));
            out.put(c.getId(), v);
        }
        return out;
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
