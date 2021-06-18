package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;

import java.util.List;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/13 18:11
 * @Description:
 */
public interface AttrInfoService {
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    List<BaseAttrInfo> attrInfoList(Long category1Id, Long category2Id, Long category3Id);

    List<BaseAttrValue> getAttrValueList(Long attrId);
}
