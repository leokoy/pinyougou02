package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.sellergoods.service.BrandService;
import entity.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * controller
 *
 * @author Administrator
 */
@RestController
@RequestMapping("/brand")
public class BrandController {

    @Reference
    private BrandService brandService;

    /**
     * 苏红霖 2019.6.13
     * 描述：返回全部已审核的品牌列表
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbBrand> findAll() {
        TbBrand tbBrand = new TbBrand();
        tbBrand.setStatus("1");
        return brandService.select(tbBrand);
    }

    /**
     * 增加
     *
     * @param brand
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody TbBrand brand) {
        try {
            brand.setStatus("0");
            brandService.add(brand);
            return new Result(true, "增加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "增加失败");
        }
    }

    /**
     * 修改
     *
     * @param brand
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody TbBrand brand) {
        try {
            brandService.update(brand);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    /**
     * 获取实体
     *
     * @param id
     * @return
     */
    @RequestMapping("/findOne/{id}")
    public TbBrand findOne(@PathVariable(value = "id") Long id) {
        return brandService.findOne(id);
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(@RequestBody Long[] ids) {
        try {
            brandService.delete(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }


    @RequestMapping("/search")
    public PageInfo<TbBrand> findPage(@RequestParam(value = "pageNo", defaultValue = "1", required = true) Integer pageNo,
                                      @RequestParam(value = "pageSize", defaultValue = "10", required = true) Integer pageSize,
                                      @RequestBody TbBrand brand) {
        return brandService.findPage(pageNo, pageSize, brand);
    }

    /**
     * 描述：批量修改品牌审核状态[苏红霖]2019.6.13
     *
     * @param ids    品牌id
     * @param status 审核状态
     * @return 修改结果
     */
    @RequestMapping("/updateStatus")
    public Result updateStatus(@RequestBody Long[] ids, String status) {
        try {
            //修改审核状态
            brandService.updateStatus(ids, status);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }

    }

    /**
     * 描述：表格导入
     *
     * @param request   请求
     * @param brandFile 文件
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/importBrand", method = RequestMethod.POST)
    public ModelAndView BrandExcel(HttpServletRequest request, MultipartFile brandFile)  {
        try {
            InputStream inputStream = brandFile.getInputStream();
            // 对读取Excel表格标题测试
            ImportExcel excelReader = new ImportExcel();
            String[] title = excelReader.readExcelTitle(inputStream);
            System.out.println("获得Excel表格的标题:");
            for (String s : title) {
                System.out.print(s + " ");
            }
            System.out.println();

            // 对读取Excel表格内容测试
            InputStream inputStream1 = brandFile.getInputStream();
            Map<Integer, String> map = excelReader.readExcelContent(inputStream1);
            System.out.println("获得Excel表格的内容:");
            //这里由于xls合并了单元格需要对索引特殊处理
            List<TbBrand> tbGoodsList = new ArrayList<>();
            TbBrand tbBrand = new TbBrand();
            tbBrand.setStatus("1");
            for (int i = 2; i <= map.size(); i++) {
                String[] str = map.get(i).split("-");
                if (str.length < 1) {
                    continue;
                }
                tbBrand.setName(str[0]);
                tbBrand.setFirstChar(str[1]);
            }

            System.out.println(tbGoodsList);
            brandService.add(tbBrand);

            ModelAndView mv = new ModelAndView();
            mv.setViewName("cj");

            Result result = new Result(true, "成功");
            return mv;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("未找到指定路径的文件!");
            ModelAndView mv = new ModelAndView();
            mv.setViewName("cw");

            Result result = new Result(true, "错误");
            return mv;
        }


    }


}
