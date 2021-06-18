package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;
import com.atguigu.gmall.product.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.product.mapper.BaseAttrValueMapper;
import com.atguigu.gmall.product.service.AttrInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/13 18:11
 * @Description:
 */
@Service
public class AttrInfoServiceImpl implements AttrInfoService {

    @Autowired
    BaseAttrInfoMapper AttrInfoMapper;

    @Autowired
    BaseAttrValueMapper attrValueMapper;


    /**
     * 添加或修改平台属性
     *
     * @param baseAttrInfo
     */
    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        Long attrId = baseAttrInfo.getId();

        if (attrId == null) {
            //添加
            AttrInfoMapper.insert(baseAttrInfo);
            attrId = baseAttrInfo.getId();
        } else {
            //修改
            AttrInfoMapper.updateById(baseAttrInfo);
            //修改删除平台属性
            QueryWrapper<BaseAttrValue> valueQueryWrapper = new QueryWrapper<>();
            valueQueryWrapper.eq("attr_id", attrId);
            attrValueMapper.delete(valueQueryWrapper);
        }

        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        for (BaseAttrValue baseAttrValue : attrValueList) {
            baseAttrValue.setAttrId(attrId);
            attrValueMapper.insert(baseAttrValue);
        }
    }


    @Override
    public List<BaseAttrInfo> attrInfoList(Long category1Id, Long category2Id, Long category3Id) {

        QueryWrapper<BaseAttrInfo> queryWrapperAttrInfo = new QueryWrapper<>();
        queryWrapperAttrInfo.eq("category_id", category3Id);
        queryWrapperAttrInfo.eq("category_level", 3);

        List<BaseAttrInfo> baseAttrInfos = AttrInfoMapper.selectList(queryWrapperAttrInfo);

        for (BaseAttrInfo baseAttrInfo : baseAttrInfos) {
            //平台属性id
            Long attrId = baseAttrInfo.getId();

            QueryWrapper<BaseAttrValue> valueQueryWrapper = new QueryWrapper<>();
            valueQueryWrapper.eq("attr_id", attrId);
            List<BaseAttrValue> baseAttrValues = attrValueMapper.selectList(valueQueryWrapper);
            baseAttrInfo.setAttrValueList(baseAttrValues);
        }
        return baseAttrInfos;
    }

    //根据平台属性ID获取平台属性
    @Override
    public List<BaseAttrValue> getAttrValueList(Long attrId) {
        QueryWrapper<BaseAttrValue> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("attr_id", attrId);
        return attrValueMapper.selectList(queryWrapper);
    }
}
