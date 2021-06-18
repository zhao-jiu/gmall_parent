package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.SpuService;
import com.atguigu.gmall.product.utils.FileUploadUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/14 11:30
 * @Description:
 */
@RestController
@RequestMapping("/admin/product")
@CrossOrigin
public class SpuApiController {


    @Autowired
    SpuService spuService;


    @GetMapping("/spuImageList/{spuId}")
    public Result spuImageList(@PathVariable("spuId") Long spuId){
        List<SpuImage> spuImages = spuService.spuImageList(spuId);
        return Result.ok(spuImages);
    }

    @GetMapping("/spuSaleAttrList/{spuId}")
    public Result spuSaleAttrList(@PathVariable("spuId") Long spuId){
        List<SpuSaleAttr> spuSaleAttrs = spuService.spuSaleAttrList(spuId);
        return Result.ok(spuSaleAttrs);
    }

    @PostMapping("/fileUpload")
    public Result fileUpload(@RequestParam("file") MultipartFile multipartFile){
        String imageUrl = FileUploadUtil.imageUpload(multipartFile);
        return Result.ok(imageUrl);
    }


    //saveSpuInfo
    @PostMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){
        spuService.saveSpuInfo(spuInfo);
        return Result.ok();
    }


    @GetMapping("baseSaleAttrList")
    public Result getBaseSaleAttrList(){
        List<BaseSaleAttr> baseSaleAttrs = spuService.getBaseSaleAttrList();
        return Result.ok(baseSaleAttrs);
    }

    @GetMapping("/{page}/{limit}")
    public Result SpuInfoPage(Long category3Id,
                              @PathVariable("page") Long page,
                              @PathVariable("limit") Long limit) {

       IPage<SpuInfo> spuInfoPage = spuService.SpuInfoPage(page,limit,category3Id);
        return Result.ok(spuInfoPage);
    }

}
