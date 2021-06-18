package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;
import com.atguigu.gmall.product.service.AttrInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/13 18:08
 * @Description: 后台系统平台分类信息管理
 */
@RestController
@RequestMapping("/admin/product")
@CrossOrigin
public class AttrApiController {


    @Autowired
    AttrInfoService attrInfoService;

    //根据平台属性ID获取平台属性
    @GetMapping("getAttrValueList/{attrId}")
    public Result getAttrValueList(@PathVariable("attrId") Long attrId) {

        List<BaseAttrValue> baseAttrValues = attrInfoService.getAttrValueList(attrId);

        return Result.ok(baseAttrValues);
    }


    //添加平台属性
    @PostMapping("saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo) {
        attrInfoService.saveAttrInfo(baseAttrInfo);
        return Result.ok();
    }

    //获取分类id获取平台属性
    @GetMapping("attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result attrInfoList(@PathVariable("category1Id") Long category1Id,
                               @PathVariable("category2Id") Long category2Id,
                               @PathVariable("category3Id") Long category3Id) {

        List<BaseAttrInfo> baseAttrInfos = attrInfoService.attrInfoList(category1Id, category2Id, category3Id);
        return Result.ok(baseAttrInfos);
    }


}
