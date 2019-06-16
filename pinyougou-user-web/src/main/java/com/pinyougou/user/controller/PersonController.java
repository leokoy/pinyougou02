package com.pinyougou.user.controller;

import com.pinyougou.user.pojo.Person;
import entity.Error;
import entity.Result;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 测试接收页面传递过来的PERSON的数据 校验数据是否符合格式的controller
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.user.controller *
 * @since 1.0
 */
@RestController
@RequestMapping("/person")
public class PersonController {

    @RequestMapping("/add")
    public Result add(@Valid @RequestBody Person person, BindingResult bindingResult){

        //判断是否有错误信息
        if(bindingResult.hasErrors()){
            Result result = new Result(false, "验证失败");
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            for (FieldError fieldError : fieldErrors) {
                System.out.println("错误的字段是："+fieldError.getField()+"错误信息："+fieldError.getDefaultMessage());
                result.getErrorsList().add(new Error(fieldError.getField(),fieldError.getDefaultMessage()));
            }

            return result;

        }else{
            System.out.println("你没错");
        }

        //处理业务
        System.out.println("保存了用户的数据");
        return new Result(true,"添加成功");
    }
}
