package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.common.util.PhoneFormatCheckUtils;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.user.service.UserService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Reference
	private UserService userService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbUser> findAll(){			
		return userService.findAll();
	}
	
	
	
	@RequestMapping("/findPage")
    public PageInfo<TbUser> findPage(@RequestParam(value = "pageNo", defaultValue = "1", required = true) Integer pageNo,
                                      @RequestParam(value = "pageSize", defaultValue = "10", required = true) Integer pageSize) {
        return userService.findPage(pageNo, pageSize);
    }

	/**
	 * 接收手机号  生成验证码  发送消息
	 * @param phone
	 * @return
	 */
	@RequestMapping("/sendSmsCode")
	public Result sendSmsCode(String phone){
		try {

			//判断 手机号是否是符合手机号的规则
			if (!PhoneFormatCheckUtils.isPhoneLegal(phone)) {
				return new Result(false,"手机号规则不对");
			}
			userService.createSmsCode(phone);
			return new Result(true,"请查看手机");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false,"发送验证码失败");
		}
	}
	
	/**
	 * 注册用户
	 * @param user
	 * @return
	 */
	@RequestMapping("/add/{smsCode}")
	public Result add(@PathVariable(name="smsCode") String smsCode ,@RequestBody TbUser user){
		try {

			//判断 手机号是否是符合手机号的规则
			if (!PhoneFormatCheckUtils.isPhoneLegal(user.getPhone())) {
				return new Result(false,"手机号规则不对");
			}

			//先判断验证码是否正确
			if(!userService.checkCode(smsCode,user.getPhone())){
				//说明验证没通过
				return new Result(false,"验证码验证失败");
			}
			//加密密码  存储
			String password = user.getPassword();
			//md5加密
			String encodePassword = DigestUtils.md5DigestAsHex(password.getBytes());
			user.setPassword(encodePassword);
			//创建时间
			user.setCreated(new Date());
			user.setUpdated(user.getCreated());

			userService.add(user);

			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param user
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbUser user){
		try {
			userService.update(user);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne/{id}")
	public TbUser findOne(@PathVariable(value = "id") Long id){
		return userService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(@RequestBody Long[] ids){
		try {
			userService.delete(ids);
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
	

	@RequestMapping("/search")
    public PageInfo<TbUser> findPage(@RequestParam(value = "pageNo", defaultValue = "1", required = true) Integer pageNo,
                                      @RequestParam(value = "pageSize", defaultValue = "10", required = true) Integer pageSize,
                                      @RequestBody TbUser user) {
        return userService.findPage(pageNo, pageSize, user);
    }


	/**
	 * 获取用户名的方法
	 * @return
	 */
	@RequestMapping("/getName")
	public String getName(HttpServletRequest request, HttpServletResponse response){
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		return name;
	}

	/**
	 * 描述 查询用户信息
	 * @return
	 */
	@RequestMapping("/one")
	public TbUser one(){
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		TbUser user = new TbUser();
		user.setUsername(name);
		return userService.selectOne(user);
	}

 @Reference
    private CartService cartService;

    //查出redis里的收藏列表
    @RequestMapping("/searchList")
    public List<TbItem> searchList() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        LinkedHashSet<Long> idList = cartService.getIdListFromRedis(name);
        System.out.println(cartService+"_+_+_+_+_+_+");
        List<TbItem> collectList = new ArrayList<>();
        for (Long id : idList) {
            TbItem tbItem = userService.searchTbItem(id);
            System.out.println("====================="+id+"----"+tbItem.getImage());
            collectList.add(tbItem);
        }
        System.out.println("~~~~~~~~~~~~~~~~~~~~"+collectList.size());
        return collectList;

    }
}

