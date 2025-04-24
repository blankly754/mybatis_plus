package com.itheima.mp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.itheima.mp.domain.dto.PageDTO;
import com.itheima.mp.domain.po.Address;
import com.itheima.mp.domain.po.User;
import com.itheima.mp.domain.query.UserQuery;
import com.itheima.mp.domain.vo.AddressVO;
import com.itheima.mp.domain.vo.UserVO;
import com.itheima.mp.enums.UserStatus;
import com.itheima.mp.mapper.UserMapper;
import com.itheima.mp.service.IUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Provider;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional   // 事务注解
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public void deductBalance(Long id, Integer money) {
        //查询用户
        User user = getById(id);
        //校验状态
        if (user == null || user.getStatus() == UserStatus.FROZEN) {
            throw new RuntimeException("用户状态异常！");
        }
        //检验余额
        if (user.getBalance() < money) {
            throw new RuntimeException("余额不足！");
        }
        //扣减余额
        //baseMapper.deductBalance(id, money);
        int remainBalance = user.getBalance() - money;
        lambdaUpdate()
                .set(User::getBalance, remainBalance)
                .set(remainBalance == 0, User::getStatus, 2)
                .eq(User::getId, id)
                .eq(User::getBalance, user.getBalance())   //乐观锁
                .update();

    }

    @Override
    public List<User> queryUsers(String name, Integer status, Integer minBalance, Integer maxBalance) {
        return lambdaQuery()
                .like(name != null, User::getUsername, name)
                .eq(status != null, User::getStatus, status)
                .ge(minBalance != null, User::getBalance, minBalance)
                .le(maxBalance != null, User::getBalance, maxBalance)
                .list();
    }

    @Override
    public UserVO queryUserAddressById(Long id) {
        //查询用户
        User user = getById(id);
        if (user == null || user.getStatus() == UserStatus.FROZEN) {
            throw new RuntimeException("用户状态异常！");
        }
        //查询地址
        List<Address> address = Db.lambdaQuery(Address.class)
                .eq(Address::getUserId, id)
                .list();
        //封装Vo
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        if (CollUtil.isNotEmpty(address)) {
            userVO.setAddresses(BeanUtil.copyToList(address, AddressVO.class));
        }
        return userVO;
    }

    @Override
    public List<UserVO> queryUserAddressByIds(List<Long> ids) {
        List<User> users = listByIds(ids);
        if (CollUtil.isEmpty(users)) {
            return Collections.emptyList();
        }

        //获取用户id集合
        List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
        //根据用户id查询地址
        List<Address> addresses = Db.lambdaQuery(Address.class).in(Address::getUserId, userIds).list();
        List<AddressVO> addressVOList = BeanUtil.copyToList(addresses, AddressVO.class);
        //对用户地址集合分组
        Map<Long, List<AddressVO>> addressMap = new HashMap<>();
        if (CollUtil.isNotEmpty(addressVOList)) {
            addressMap = addressVOList.stream().collect(Collectors.groupingBy(AddressVO::getUserId));
        }

        //转换VO返回
        List<UserVO> list = new ArrayList<>(users.size());
        for (User user : users) {
            UserVO vo = BeanUtil.copyProperties(user, UserVO.class);
            list.add(vo);
            vo.setAddresses(addressMap.get(user.getId()));
        }
        return List.of();
    }

    @Override
    public PageDTO<UserVO> queryUserPage(UserQuery query) {
        String name = query.getName();
        Integer status = query.getStatus();
        //分页条件
        Page<User> page = query.toMpPageDefaultSortByUpdateTime();
        //分页查询
        Page<User> p = lambdaQuery()
                .like(name != null, User::getUsername, name)
                .eq(status != null, User::getStatus, status)
                .page(page);
        //封装VO
        return PageDTO.of(p,UserVO.class);


    }
}
