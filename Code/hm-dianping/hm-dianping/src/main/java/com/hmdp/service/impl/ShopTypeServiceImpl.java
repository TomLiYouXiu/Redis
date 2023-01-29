package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    //Redis缓存
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryTypeList() {
        //1.从Redis中查询缓存
        String shopTypeJson = stringRedisTemplate.opsForValue().get(RedisConstants.CACHE_SHOP_TYPE_KEY);
        //2.判断是否存在
        if(StrUtil.isNotBlank(shopTypeJson)){
            //3.存在直接返回转型为java实体 json---->list
            List<ShopType> shopTypeList=JSONUtil.toList(shopTypeJson,ShopType.class);
            return Result.ok(shopTypeList);
        }
        //4.不存在，查询数据库
        List<ShopType> shopTypeList = query().orderByAsc("sort").list();
        //5.不存在返回错误
        if (shopTypeList == null) {
            return Result.fail("没有找到店铺");
        }
        //6.存在返回实体，并存入Redis List----->Json
        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_TYPE_KEY,JSONUtil.toJsonStr(shopTypeList));

        //7.返回
        return Result.ok(shopTypeList);
    }
}
