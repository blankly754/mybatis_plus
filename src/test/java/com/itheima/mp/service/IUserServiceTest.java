package com.itheima.mp.service;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mp.domain.po.User;
import com.itheima.mp.domain.po.UserInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IUserServiceTest {

    @Autowired
    private IUserService userService;

    @Test
    void testInsert() {
        User user = new User();
        //user.setId(5L);
        user.setUsername("LiLei");
        user.setPassword("123");
        user.setPhone("18688990013");
        user.setBalance(200);
        //user.setInfo("{\"age\": 24, \"intro\": \"英文老师\", \"gender\": \"female\"}");
        user.setInfo(UserInfo.of(24, "英文老师", "female"));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userService.save(user);
    }

    @Test
    void testQuery() {
        List<User> list = userService.listByIds(List.of(1L, 2L, 3L));
        list.forEach(System.out::println);
    }

    @Test
    void testPageQuery() {
        int pageNo = 1, pageSize = 2;
        Page<User> page = Page.of(pageNo, pageSize);
        // 按照余额升序排序
        page.addOrder(new OrderItem("balance", true));
        // 按照id降序排序
        page.addOrder(new OrderItem("id", false));

        Page<User> p = userService.page(page);

        long total = p.getTotal();
        System.out.println("总记录数：" + total);

        long pages = p.getPages();
        System.out.println("总页数：" + pages);

        List<User> records = p.getRecords();
        records.forEach(System.out::println);

    }
}