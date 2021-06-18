package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.service.TrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/13 17:55
 * @Description:
 */
@Service
public class TrademarkServiceImpl implements TrademarkService {

    @Autowired
    BaseTrademarkMapper baseTrademarkMapper;

    @Override
    public Page<BaseTrademark> baseTrademarkPage(Long page, Long limit) {
        Page<BaseTrademark> trademarkPage = new Page<>(page,limit);
        baseTrademarkMapper.selectPage(trademarkPage, null);
        return trademarkPage;
    }

    @Override
    public List<BaseTrademark> getTrademarkList() {
        return baseTrademarkMapper.selectList(null);
    }

    @Override
    public void save(BaseTrademark baseTrademark) {
        Long id = baseTrademark.getId();
        baseTrademarkMapper.insert(baseTrademark);
    }

    @Override
    public void remove(Long id) {
        baseTrademarkMapper.deleteById(id);
    }

    @Override
    public BaseTrademark get(Long id) {
        return baseTrademarkMapper.selectById(id);
    }

    @Override
    public void update(BaseTrademark baseTrademark) {
        baseTrademarkMapper.updateById(baseTrademark);
    }

    @Override
    public BaseTrademark getTrademarkById(Long tmId) {
        return baseTrademarkMapper.selectById(tmId);
    }
}
