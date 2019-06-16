package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.user.service.UserService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.manager.controller *
 * @since 1.0
 */
@RestController
@RequestMapping("/user")
public class LoginUserInfoController {

    @Reference
    private UserService userService;

    @RequestMapping("/info")
    public String getUserInfo(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @RequestMapping("/findAll")
    public Long findAll(){
        TbUser tbUser = new TbUser();
        List<TbUser> tbUserList = userService.select(tbUser);
        Long size = Long.valueOf(tbUserList.size());
        return size;
    }

    @RequestMapping("/search")
    public PageInfo<TbUser> findPage(@RequestParam(value = "pageNo", defaultValue = "1", required = true) Integer pageNo,
                                      @RequestParam(value = "pageSize", defaultValue = "100", required = true) Integer pageSize,
                                      @RequestBody TbUser user) {
        return userService.findPage(pageNo, pageSize, user);
    }

    /**
     * 描述：批量修改账户状态
     *
     * @param ids    用户ID
     * @param status 状态
     * @return 修改结果
     */
    @RequestMapping("/updateStatus")
    public Result updateStatus(@RequestBody Long[] ids, String status) {
        try {
            //修改审核状态
            userService.updateStatus(ids,status);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }

    }

}
