package com.smartdocs.aikb.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;
import java.util.function.Function;

/**
 * 通用分页响应结构，对应前端 {@code PageResult<T>}。
 */
@Data
public class PageVO<T> {

    private List<T> records;
    private Long total;
    private Long page;
    private Long size;

    public static <T> PageVO<T> of(IPage<T> page) {
        PageVO<T> vo = new PageVO<>();
        vo.records = page.getRecords();
        vo.total = page.getTotal();
        vo.page = page.getCurrent();
        vo.size = page.getSize();
        return vo;
    }

    public static <S, T> PageVO<T> map(IPage<S> page, Function<S, T> mapper) {
        PageVO<T> vo = new PageVO<>();
        vo.records = page.getRecords().stream().map(mapper).toList();
        vo.total = page.getTotal();
        vo.page = page.getCurrent();
        vo.size = page.getSize();
        return vo;
    }
}
