package com.feiyi.common.domain;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

/**
 * 分页结果封装类
 *
 * @author system
 */
@Data
public class PageResultDTO<T> {

    /**
     * 当前页码
     */
    private Long pageNum;

    /**
     * 每页大小
     */
    private Long pageSize;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 数据列表
     */
    private List<T> list;

    public PageResultDTO() {
    }

    public PageResultDTO(Page<T> page) {
        this.pageNum = page.getCurrent();
        this.pageSize = page.getSize();
        this.total = page.getTotal();
        this.pages = page.getPages();
        this.list = page.getRecords();
    }

    /**
     * 根据Page对象创建分页结果
     */
    public static <T> PageResultDTO<T> build(Page<T> page) {
        return new PageResultDTO<>(page);
    }

    /**
     * 手动构建分页结果
     */
    public static <T> PageResultDTO<T> build(Long pageNum, Long pageSize, Long total, List<T> list) {
        PageResultDTO<T> result = new PageResultDTO<>();
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setTotal(total);
        result.setPages((total + pageSize - 1) / pageSize);
        result.setList(list);
        return result;
    }
}
