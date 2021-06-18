package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.TrademarkService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/13 17:51
 * @Description: 后台管理系统查询品牌信息分页
 */
@RestController
@RequestMapping("/admin/product")
@CrossOrigin
public class TrademarkApiController {

    @Autowired
    TrademarkService trademarkService;

    @PutMapping("baseTrademark/update")
    public Result update(@RequestBody BaseTrademark baseTrademark){
        trademarkService.update(baseTrademark);
        return  Result.ok();
    }

    @GetMapping("baseTrademark/get/{id}")
    public Result get(@PathVariable("id") Long id){
        BaseTrademark baseTrademark = trademarkService.get(id);
        return  Result.ok(baseTrademark);
    }

    @DeleteMapping("/baseTrademark/remove/{id}")
    public Result remove(@PathVariable("id") Long id){
        trademarkService.remove(id);
        return  Result.ok();
    }

    @PostMapping("baseTrademark/save")
    public Result save(@RequestBody BaseTrademark baseTrademark){
        trademarkService.save(baseTrademark);
        return Result.ok();
    }


    //baseTrademark/getTrademarkList
    @GetMapping("baseTrademark/getTrademarkList")
    public Result getTrademarkList(){

        List<BaseTrademark> baseTrademarks  = trademarkService.getTrademarkList();

        return Result.ok(baseTrademarks);
    }


    @GetMapping("baseTrademark/{page}/{limit}")
    public Result baseTrademarkPage(@PathVariable("page")Long page,@PathVariable("limit")Long limit){

        Page<BaseTrademark> trademarkPage = trademarkService.baseTrademarkPage(page,limit);

        return Result.ok(trademarkPage);
    }
}
