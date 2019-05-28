package com.taoding.mp.base.model;

import lombok.Data;

import java.util.List;

/**
 * 分页VO
 *
 * @param <T>
 * @author wuwentan
 */
@Data
public class PageVO<T> {

    /**
     * 当前页码
     */
    private Integer pageNo;

    /**
     * 每页显示的总条数
     */
    private Integer pageSize;

    /**
     * 总条数
     */
    private Integer totalNum;

    /**
     * 是否有下一页
     */
    private Integer isMore;

    /**
     * 总页数
     */
    private Integer totalPage;

    /**
     * 开始索引
     */
    private Integer startIndex;

    /**
     * 分页结果
     */
    private List<T> items;

    public PageVO() {
        super();
    }

    public PageVO(Integer pageNo, Integer pageSize, Integer totalNum, List<T> items) {
        super();
        this.pageNo = pageNo == null || pageNo < 1 ? 1 : pageNo;
        this.pageSize = pageSize == null ? 1 : pageSize;
        this.totalNum = totalNum;
        this.totalPage = this.pageSize == 0 ? 0 : (this.totalNum + this.pageSize - 1) / this.pageSize ;
        this.startIndex = (this.pageNo - 1) * this.pageSize;
        this.isMore = this.pageNo >= this.totalPage ? 0 : 1;
        this.items = items;
    }
}