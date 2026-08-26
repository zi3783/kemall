package com.kemall.account.domain.result;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kemall.account.domain.vo.WalletLogVO;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Data
public class PageResult<T> {
    private Long total;
    private Long pageNum;
    private Long pageSize;
    private Long pages;
    private List<T> records;

    public PageResult() {
        this.total = 0L;
        this.pageNum = 0L;
        this.pageSize = 0L;
        this.pages = 0L;
        this.records = new ArrayList<>();
    }

    public static <VO,PO> PageResult<VO> convert(Page<PO> page, Supplier<VO> supplier) {
        if(page == null) {
            throw new IllegalArgumentException("page cannot be null");
        }
        if(page.getTotal() == 0){
            return new PageResult<>();
        }
        List<VO> vos = page.getRecords().stream().map(po -> {
            VO vo = supplier.get();
            BeanUtils.copyProperties(po, vo);
            return vo;
        }).toList();
        PageResult<VO> pageResult = new PageResult<>();
        pageResult.setPageNum(page.getCurrent());
        pageResult.setPageSize(page.getSize());
        pageResult.setPages(page.getPages());
        pageResult.setRecords(vos);
        pageResult.setTotal(page.getTotal());
        return pageResult;
    }
}
