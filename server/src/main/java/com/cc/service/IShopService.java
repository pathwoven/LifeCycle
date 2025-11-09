package com.cc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cc.dto.Result;
import com.cc.dto.ShopNearDTO;
import com.cc.entity.Shop;

public interface IShopService extends IService<Shop> {
    Shop queryShopByIdWithMutex(Long id);
    Shop queryShopByIdWithLogicExpiration(Long id);

}
