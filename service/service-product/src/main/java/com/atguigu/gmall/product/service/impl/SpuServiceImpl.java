package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.cache.GmallCache;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.SpuService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/14 11:35
 * @Description:
 */
@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    SpuInfoMapper spuInfoMapper;
    @Autowired
    SpuImageMapper spuImageMapper;
    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    SpuPosterMapper spuPosterMapper;
    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;

    @Override
    public IPage<SpuInfo> SpuInfoPage(Long page, Long limit, Long category3Id) {
        IPage<SpuInfo> iPage = new Page<>();
        iPage.setCurrent(page);
        iPage.setSize(limit);
        QueryWrapper<SpuInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category3_id", category3Id);
        IPage<SpuInfo> infoIPage = spuInfoMapper.selectPage(iPage, queryWrapper);

        return infoIPage;
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectList(null);
    }

    //添加spu信息
    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        //添加spu信息
        spuInfoMapper.insert(spuInfo);
        //获取主键
        Long spuId = spuInfo.getId();
        //添加图片信息
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();

        if (spuImageList != null && spuImageList.size() > 0) {
            for (SpuImage spuImage : spuImageList) {
                spuImage.setSpuId(spuId);
                spuImageMapper.insert(spuImage);
            }
        }

        //获取销售属性集合
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (spuSaleAttrList != null && spuSaleAttrList.size() > 0) {
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                Long baseSaleAttrId = spuSaleAttr.getBaseSaleAttrId();
                //添加销售属性
                spuSaleAttr.setSpuId(spuId);
                spuSaleAttrMapper.insert(spuSaleAttr);
                //添加销售属性值
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                    spuSaleAttrValue.setSpuId(spuId);
                    spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
//                    spuSaleAttrValue.setBaseSaleAttrId(baseSaleAttrId);
                    spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                }
            }
        }

    }

    //获取图片
    @Override
    public List<SpuImage> spuImageList(Long spuId) {
        QueryWrapper<SpuImage> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("spu_id",spuId);

        List<SpuImage> spuImages = spuImageMapper.selectList(queryWrapper);

        return spuImages;
    }

    //获取销售属性信息
    @Override
    public List<SpuSaleAttr> spuSaleAttrList(Long spuId) {
        QueryWrapper<SpuSaleAttr> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spu_id",spuId);

        List<SpuSaleAttr> spuSaleAttrs = spuSaleAttrMapper.selectList(queryWrapper);

        for (SpuSaleAttr spuSaleAttr : spuSaleAttrs) {
            QueryWrapper<SpuSaleAttrValue> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("spu_id",spuId);
            queryWrapper1.eq("base_sale_attr_id",spuSaleAttr.getBaseSaleAttrId());
            List<SpuSaleAttrValue> spuSaleAttrValues = spuSaleAttrValueMapper.selectList(queryWrapper1);
            spuSaleAttr.setSpuSaleAttrValueList(spuSaleAttrValues);
        }

        return spuSaleAttrs;
    }

    @GmallCache
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListBySpuId(Long spuId,Long skuId) {

        List<SpuSaleAttr> spuSaleAttrs = spuSaleAttrMapper.getSpuSaleAttrListBySpuId(spuId,skuId);

        return spuSaleAttrs;
    }
}
