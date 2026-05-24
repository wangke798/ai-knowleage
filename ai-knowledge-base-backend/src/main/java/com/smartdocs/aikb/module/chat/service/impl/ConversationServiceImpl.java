package com.smartdocs.aikb.module.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartdocs.aikb.common.exception.BusinessException;
import com.smartdocs.aikb.common.result.ResultCode;
import com.smartdocs.aikb.module.chat.entity.ChatConversation;
import com.smartdocs.aikb.module.chat.entity.ChatMessage;
import com.smartdocs.aikb.module.chat.mapper.ChatConversationMapper;
import com.smartdocs.aikb.module.chat.mapper.ChatMessageMapper;
import com.smartdocs.aikb.module.chat.service.ConversationService;
import com.smartdocs.aikb.module.kb.entity.KbDocument;
import com.smartdocs.aikb.module.kb.entity.KbDocumentChunk;
import com.smartdocs.aikb.module.kb.entity.KbMember;
import com.smartdocs.aikb.module.kb.entity.KnowledgeBase;
import com.smartdocs.aikb.module.kb.mapper.KbDocumentChunkMapper;
import com.smartdocs.aikb.module.kb.mapper.KbDocumentMapper;
import com.smartdocs.aikb.module.kb.mapper.KbMemberMapper;
import com.smartdocs.aikb.module.kb.mapper.KnowledgeBaseMapper;
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
    private final KbMemberMapper kbMemberMapper;
    private final KbDocumentMapper documentMapper;
    private final KbDocumentChunkMapper chunkMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<Map<String, Object>> listMine(Long userId, String keyword, Boolean favoriteOnly) {
        LambdaQueryWrapper<ChatConversation> wrapper = new LambdaQueryWrapper<ChatConversation>()
                .eq(ChatConversation::getUserId, userId)
                .orderByDesc(ChatConversation::getUpdateTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(ChatConversation::getTitle, keyword);
        }
        if (Boolean.TRUE.equals(favoriteOnly)) {
            wrapper.eq(ChatConversation::getIsFavorite, 1);
        }
        List<ChatConversation> rows = conversationMapper.selectList(wrapper);
        if (rows.isEmpty()) return List.of();
        Map<Long, String> kbNames = loadKbNames(rows.stream().map(ChatConversation::getKbId).collect(Collectors.toSet()));
        return rows.stream().map(c -> toMap(c, kbNames.get(c.getKbId()))).toList();
    }

    @Override
    @Transactional
    public Map<String, Object> create(Long userId, Map<String, Object> params) {
        Long kbId = toLong(params.get("kbId"));
        String title = (String) params.get("title");
        if (kbId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "kbId \u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (resolveKbRole(userId, kbId) == null) {
            throw new BusinessException(ResultCode.KB_NO_PERMISSION);
        }
        ChatConversation c = new ChatConversation();
        c.setUserId(userId);
        c.setKbId(kbId);
        c.setTitle(StringUtils.hasText(title) ? title : "\u65b0\u4f1a\u8bdd");
        conversationMapper.insert(c);
        return toMap(c, kbName(kbId));
    }

    @Override
    @Transactional
    public Map<String, Object> rename(Long userId, Long conversationId, String title) {
        ChatConversation c = requireOwn(userId, conversationId);
        c.setTitle(title);
        conversationMapper.updateById(c);
        return toMap(c, kbName(c.getKbId()));
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
    public Map<String, Object> detail(Long userId, Long conversationId) {
        ChatConversation c = requireOwn(userId, conversationId);
        return toMap(c, kbName(c.getKbId()));
    }

    @Override
    public List<Map<String, Object>> listMessages(Long userId, Long conversationId) {
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
        Map<Long, Map<String, Object>> citationMap = loadCitations(chunkIds);
        return rows.stream().map(m -> {
            Map<String, Object> msg = new LinkedHashMap<>();
            msg.put("id", m.getId());
            msg.put("conversationId", m.getConversationId());
            msg.put("role", m.getRole());
            msg.put("content", m.getContent());
            msg.put("tokenCount", m.getTokenCount());
            msg.put("createTime", m.getCreateTime());
            List<Long> ids = idsPerMsg.getOrDefault(m.getId(), List.of());
            if (!ids.isEmpty()) {
                msg.put("citations", ids.stream().map(citationMap::get).filter(Objects::nonNull).toList());
            }
            return msg;
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

    @Override
    @Transactional
    public int toggleFavorite(Long userId, Long conversationId) {
        ChatConversation c = requireOwn(userId, conversationId);
        int newVal = (c.getIsFavorite() != null && c.getIsFavorite() == 1) ? 0 : 1;
        ChatConversation upd = new ChatConversation();
        upd.setId(c.getId());
        upd.setIsFavorite(newVal);
        conversationMapper.updateById(upd);
        return newVal;
    }

    @Override
    public String exportConversation(Long userId, Long conversationId, String format) {
        List<Map<String, Object>> msgs = listMessages(userId, conversationId);
        ChatConversation conv = requireOwn(userId, conversationId);
        String fmt = (format == null) ? "markdown" : format.toLowerCase();
        return switch (fmt) {
            case "json" -> {
                try { yield objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(msgs); }
                catch (Exception e) { yield "[]"; }
            }
            case "text" -> buildTextExport(conv, msgs);
            default     -> buildMarkdownExport(conv, msgs);
        };
    }

    private String buildMarkdownExport(ChatConversation conv, List<Map<String, Object>> msgs) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(conv.getTitle()).append("\n\n");
        sb.append("> 导出时间：").append(java.time.LocalDateTime.now()).append("\n\n---\n\n");
        for (Map<String, Object> m : msgs) {
            String role = (String) m.get("role");
            sb.append("**").append("USER".equals(role) ? "🧑 用户" : "🤖 AI").append("**\n\n");
            sb.append(m.get("content")).append("\n\n---\n\n");
        }
        return sb.toString();
    }

    private String buildTextExport(ChatConversation conv, List<Map<String, Object>> msgs) {
        StringBuilder sb = new StringBuilder();
        sb.append("== ").append(conv.getTitle()).append(" ==\n\n");
        for (Map<String, Object> m : msgs) {
            String role = "USER".equals(m.get("role")) ? "用户" : "AI";
            sb.append("[").append(role).append("]\n");
            sb.append(m.get("content")).append("\n\n");
        }
        return sb.toString();
    }

    // ─── helpers ───

    private Map<String, Object> toMap(ChatConversation c, String kbName) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("kbId", c.getKbId());
        m.put("kbName", kbName);
        m.put("title", c.getTitle());
        m.put("isFavorite", c.getIsFavorite() != null && c.getIsFavorite() == 1);
        m.put("createTime", c.getCreateTime());
        m.put("updateTime", c.getUpdateTime());
        return m;
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

    private Map<Long, Map<String, Object>> loadCitations(Set<Long> chunkIds) {
        if (chunkIds.isEmpty()) return Map.of();
        List<KbDocumentChunk> chunks = chunkMapper.selectBatchIds(chunkIds);
        if (chunks.isEmpty()) return Map.of();
        Set<Long> docIds = chunks.stream().map(KbDocumentChunk::getDocId).collect(Collectors.toSet());
        Map<Long, String> docNames = documentMapper.selectBatchIds(docIds).stream()
                .collect(Collectors.toMap(KbDocument::getId, KbDocument::getName));
        Map<Long, Map<String, Object>> out = new HashMap<>();
        for (KbDocumentChunk c : chunks) {
            Map<String, Object> cit = new LinkedHashMap<>();
            cit.put("chunkId", c.getId());
            cit.put("docId", c.getDocId());
            cit.put("docName", docNames.get(c.getDocId()));
            cit.put("seq", c.getSeq());
            cit.put("snippet", truncate(c.getContent(), 200));
            out.put(c.getId(), cit);
        }
        return out;
    }

    private static Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return null; }
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
    /** 简单权限检查：是 KB 所有者或成员则返回角色，否则返回 null。 */
    private String resolveKbRole(Long userId, Long kbId) {
        KnowledgeBase kb = knowledgeBaseMapper.selectById(kbId);
        if (kb == null) return null;
        if (kb.getOwnerId().equals(userId)) return "OWNER";
        KbMember member = kbMemberMapper.selectOne(
                new LambdaQueryWrapper<KbMember>()
                        .eq(KbMember::getKbId, kbId)
                        .eq(KbMember::getUserId, userId));
        return member == null ? null : member.getRole();
    }}
