package com.pinyougou.user.service.impl;

import java.text.SimpleDateFormat;
import java.util.*;

import com.pinyougou.pojo.TbBrand;
import com.pinyougou.user.service.UserService;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import com.pinyougou.core.service.CoreServiceImpl;

import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import com.pinyougou.mapper.TbUserMapper;
import com.pinyougou.pojo.TbUser;


/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class UserServiceImpl extends CoreServiceImpl<TbUser> implements UserService {


    private TbUserMapper userMapper;

    @Autowired
    public UserServiceImpl(TbUserMapper userMapper) {
        super(userMapper, TbUser.class);
        this.userMapper = userMapper;
    }


    @Override
    public PageInfo<TbUser> findPage(Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<TbUser> all = userMapper.selectAll();
        PageInfo<TbUser> info = new PageInfo<TbUser>(all);

        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbUser> pageInfo = JSON.parseObject(s, PageInfo.class);
        return pageInfo;
    }


    @Override
    public PageInfo<TbUser> findPage(Integer pageNo, Integer pageSize, TbUser user) {
        PageHelper.startPage(pageNo, pageSize);

        Example example = new Example(TbUser.class);
        Example.Criteria criteria = example.createCriteria();

        if (user != null) {
            if (StringUtils.isNotBlank(user.getUsername())) {
                criteria.andLike("username", "%" + user.getUsername() + "%");
                //criteria.andUsernameLike("%"+user.getUsername()+"%");
            }
            if (StringUtils.isNotBlank(user.getPassword())) {
                criteria.andLike("password", "%" + user.getPassword() + "%");
                //criteria.andPasswordLike("%"+user.getPassword()+"%");
            }
            if (StringUtils.isNotBlank(user.getPhone())) {
                criteria.andLike("phone", "%" + user.getPhone() + "%");
                //criteria.andPhoneLike("%"+user.getPhone()+"%");
            }
            if (StringUtils.isNotBlank(user.getEmail())) {
                criteria.andLike("email", "%" + user.getEmail() + "%");
                //criteria.andEmailLike("%"+user.getEmail()+"%");
            }
            if (StringUtils.isNotBlank(user.getSourceType())) {
                criteria.andLike("sourceType", "%" + user.getSourceType() + "%");
                //criteria.andSourceTypeLike("%"+user.getSourceType()+"%");
            }
            if (StringUtils.isNotBlank(user.getNickName())) {
                criteria.andLike("nickName", "%" + user.getNickName() + "%");
                //criteria.andNickNameLike("%"+user.getNickName()+"%");
            }
            if (StringUtils.isNotBlank(user.getName())) {
                criteria.andLike("name", "%" + user.getName() + "%");
                //criteria.andNameLike("%"+user.getName()+"%");
            }
            if (StringUtils.isNotBlank(user.getStatus())) {
                criteria.andLike("status", "%" + user.getStatus() + "%");
                //criteria.andStatusLike("%"+user.getStatus()+"%");
            }
            if (StringUtils.isNotBlank(user.getHeadPic())) {
                criteria.andLike("headPic", "%" + user.getHeadPic() + "%");
                //criteria.andHeadPicLike("%"+user.getHeadPic()+"%");
            }
            if (StringUtils.isNotBlank(user.getQq())) {
                criteria.andLike("qq", "%" + user.getQq() + "%");
                //criteria.andQqLike("%"+user.getQq()+"%");
            }
            if (StringUtils.isNotBlank(user.getIsMobileCheck())) {
                criteria.andLike("isMobileCheck", "%" + user.getIsMobileCheck() + "%");
                //criteria.andIsMobileCheckLike("%"+user.getIsMobileCheck()+"%");
            }
            if (StringUtils.isNotBlank(user.getIsEmailCheck())) {
                criteria.andLike("isEmailCheck", "%" + user.getIsEmailCheck() + "%");
                //criteria.andIsEmailCheckLike("%"+user.getIsEmailCheck()+"%");
            }
            if (StringUtils.isNotBlank(user.getSex())) {
                criteria.andLike("sex", "%" + user.getSex() + "%");
                //criteria.andSexLike("%"+user.getSex()+"%");
            }

        }
        List<TbUser> all = userMapper.selectByExample(example);

        PageInfo<TbUser> info = new PageInfo<TbUser>(all);
        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbUser> pageInfo = JSON.parseObject(s, PageInfo.class);

        return pageInfo;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DefaultMQProducer producer;

    /**
     * 1.根据手机号  生成验证码
     * <p>
     * 2.将验证码 存储到 redis中（key-value）  (1.加入依赖2.配置redis.xml,3注入redisTemplate)
     * 3.将验证码 和手机号 签名 模板的内容作为消息体 发送消息到mq中  3.1 加入依赖 3.2 配置spring的配置文件 （配置生产者对象）
     *
     * @param phone
     */
    @Override
    public void createSmsCode(String phone) {
        //1.生成验证码    生成随机的6位数字
        String code = (long) ((Math.random() * 9 + 1) * 100000) + "";
        //2.将验证码 存储到 redis中
        redisTemplate.boundValueOps("Register_" + phone).set(code);//简单的字符串数据类型

        System.out.println("验证码生成之后的值为：" + redisTemplate.boundValueOps("Register_" + phone).get());
        //3.
        Map<String, String> messageInfo = new HashMap<>();
        messageInfo.put("mobile", phone);
        messageInfo.put("sign_name", "黑马三国的包子");
        messageInfo.put("template_code", "SMS_126865257");
        //设置JSON字符串的参数
        messageInfo.put("param", "{\"code\":\"" + code + "\"}");

        String s = JSON.toJSONString(messageInfo);//消息的内容的字符串
        Message msage = new Message("SMS_TOPIC", "SEND_MESSAGE_TAG", "createSmsCode", s.getBytes());
        try {
            SendResult send = producer.send(msage);
            System.out.println(send);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean checkCode(String smsCode, String phone) {
        if (StringUtils.isEmpty(smsCode) || StringUtils.isEmpty(phone)) {
            return false;
        }

        //根据手机号获取reids中的验证码
        String codefromRedis = (String) redisTemplate.boundValueOps("Register_" + phone).get();

        if (smsCode.equals(codefromRedis)) {
            return true;
        }

        return false;
    }

    /**
     * 描述：查询用户状态
     *
     * @param username
     * @return
     */
    @Override
    public String findUserStatus(String username) {
        TbUser tbUser = new TbUser();
        tbUser.setUsername(username);
        TbUser user = userMapper.selectOne(tbUser);
        return user.getStatus();
    }

    /**
     * 描述：更新用户最后登陆时间
     *
     * @param username
     * @param date
     */
    @Override
    public void updateUserLastLoginTime(String username, Date date) {
        Example example = new Example(TbUser.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username", username);

        TbUser tbUser = new TbUser();
        tbUser.setLastLoginTime(date);
        /*SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = simpleDateFormat.format(date);
*/
        userMapper.updateByExampleSelective(tbUser, example);
    }

    @Override
    public void updateStatus(Long[] ids, String status) {
        //update tb_brand set status = ? where id in(?,?,?)

        //设置条件
        Example example = new Example(TbUser.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", Arrays.asList(ids));

        //更新状态
        TbUser tbUser = new TbUser();
        tbUser.setStatus(status);
        userMapper.updateByExampleSelective(tbUser, example);

    }


}
