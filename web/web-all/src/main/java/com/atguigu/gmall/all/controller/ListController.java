package com.atguigu.gmall.all.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import com.atguigu.gmall.search.client.SearchFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/24 18:24
 * @Description: 列表查询
 */
@Controller
public class ListController {

    @Autowired
    SearchFeignClient searchFeignClient;

    //search list
    @RequestMapping({"search.html", "list.html"})
    public String search(Model model, SearchParam searchParam, HttpServletRequest request) {

        SearchResponseVo searchResponseVo = searchFeignClient.search(searchParam);
        model.addAttribute("goodsList", searchResponseVo.getGoodsList());
        model.addAttribute("trademarkList", searchResponseVo.getTrademarkList());//attrsList
        model.addAttribute("attrsList", searchResponseVo.getAttrsList());

        //品牌面包屑
        if (!StringUtils.isEmpty(searchParam.getTrademark())) {
            //trademarkParam
            String trademark = searchParam.getTrademark();
            String[] split = trademark.split(":");
            String trademarkParam = split[1];
            model.addAttribute("trademarkParam", trademarkParam);
            model.addAttribute("searchParam", searchParam);
        }

        //属性面包屑 propsParamList
        if (searchParam.getProps() != null && searchParam.getProps().length > 0) {
            List<SearchAttr> propsParamList = getPropsParamList(searchParam);
            model.addAttribute("propsParamList", propsParamList);
        }

        //排序 orderMap
        Map<String, String> orderMap = getOrderMap(searchParam);
        model.addAttribute("orderMap", orderMap);
        //拼接请求参数
        String urlParam = request.getRequestURI() + "?" + makeUrlParam(searchParam);
        model.addAttribute("urlParam", urlParam);

        return "list/index";
    }

    /***
     *  获取排序参数
     * @param searchParam
     * @return
     */
    private Map<String, String> getOrderMap(SearchParam searchParam) {
        String order = searchParam.getOrder();
        Map<String, String> orderMap = new HashMap<>();
        if (!StringUtils.isEmpty(order)) {
            String[] split = StringUtils.split(order, ":");
            if (split != null && split.length == 2) {
                orderMap.put("type", split[0]);
                orderMap.put("sort", split[1]);
            }
        } else {
            orderMap.put("type", "1");
            orderMap.put("sort", "asc");
        }
        return orderMap;
    }

    /**
     * 获取请求参数面包屑数据
     *
     * @param searchParam
     * @return
     */
    private List<SearchAttr> getPropsParamList(SearchParam searchParam) {
        String[] props = searchParam.getProps();
        List<SearchAttr> propsParamList = new ArrayList<>();
        for (String prop : props) {
            String[] split = prop.split(":");
            Long attrId = Long.parseLong(split[0]);
            String attrValue = split[1];
            String attrName = split[2];
            SearchAttr searchAttr = new SearchAttr();
            searchAttr.setAttrId(attrId);
            searchAttr.setAttrName(attrName);
            searchAttr.setAttrValue(attrValue);
            propsParamList.add(searchAttr);
        }
        return propsParamList;
    }

    /**
     * 拼接原有的请求参数
     *
     * @param searchParam
     * @return
     */
    private String makeUrlParam(SearchParam searchParam) {
        //获取搜索参数
        Long category3Id = searchParam.getCategory3Id();
        Long category2Id = searchParam.getCategory2Id();
        Long category1Id = searchParam.getCategory1Id();
        String keyword = searchParam.getKeyword();
        String trademark = searchParam.getTrademark();
        String[] props = searchParam.getProps();
        //拼接参数值
        StringBuilder urlParam = new StringBuilder();
        if (category3Id != null && category3Id > 0) {
            urlParam = new StringBuilder("category3Id=" + category3Id);
        }
        if (category2Id != null && category2Id > 0) {
            urlParam.append("category2Id=").append(category2Id);
        }
        if (category1Id != null && category1Id > 0) {
            urlParam.append("category1Id=").append(category1Id);
        }

        if (!StringUtils.isEmpty(keyword)) {
            urlParam.append("keyword=").append(keyword);
        }

        if (!StringUtils.isEmpty(trademark)) {
            urlParam.append("&trademark=").append(trademark);
        }

        if (null != props && props.length > 0) {
            for (String prop : props) {
                urlParam.append("&props=").append(prop);
            }
        }
        return urlParam.toString();
    }

    //首页跳转
    @RequestMapping({"/", "index","index.html"})
    public String index(Model model) {
        List<JSONObject> list = searchFeignClient.getBaseCategoryList();
        model.addAttribute("list", list);
        return "index/index";
    }
}
