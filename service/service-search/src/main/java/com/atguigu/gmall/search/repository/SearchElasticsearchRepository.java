package com.atguigu.gmall.search.repository;

import com.atguigu.gmall.model.list.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/24 14:42
 * @Description:
 */
@Component
public interface SearchElasticsearchRepository extends ElasticsearchRepository<Goods,Long> {
}
