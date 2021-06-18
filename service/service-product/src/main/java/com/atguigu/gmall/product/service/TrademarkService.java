package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/13 17:54
 * @Description:
 */
public interface TrademarkService {
    Page<BaseTrademark> baseTrademarkPage(Long page,Long limit);

    List<BaseTrademark> getTrademarkList();

    void save(BaseTrademark baseTrademark);

    void remove(Long id);

    BaseTrademark get(Long id);

    void update(BaseTrademark baseTrademark);

    BaseTrademark getTrademarkById(Long tmId);
}
