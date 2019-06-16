package com.pinyougou.manager.controller;

import com.pinyougou.pojo.TbGoods;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author jiaqing.xu@hand-china.com
 * @version 1.0
 * @name
 * @description 读取并解析excel
 * @date 2017/10/19
 */
public class ImportExcel {

    //newPOIFSFileSystem(new FileInputStream("d:/test.xls"));
    private POIFSFileSystem fs;
    //excel文档对象
    private HSSFWorkbook wb;
    //excel的sheet
    private HSSFSheet sheet;
    //excel的行
    private HSSFRow row;

    /**
     * 读取Excel表格表头的内容
     * @param is
     * @return String 表头内容的数组
     */
    public String[] readExcelTitle(InputStream is) {
        try {
            //"C:\\Users\\TLJ\\Desktop\\aaa商品数据.xls"
            fs = new POIFSFileSystem(is);
            //获取excel的文档对象
            wb = new HSSFWorkbook(fs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //获取首张表对象
        sheet = wb.getSheetAt(0);
        //得到首行的row
        row = sheet.getRow(0);
        // 标题总列数
        int colNum = row.getPhysicalNumberOfCells();
        String[] title = new String[colNum];
        for (int i = 0; i < colNum; i++) {

            //row.getCell获取指定的单元格
            title[i] = getCellFormatValue(row.getCell((short) i));
        }
        return title;
    }

    /**
     * 读取Excel数据内容
     * @param is
     * @return Map 包含单元格数据内容的Map对象
     */
    public Map<Integer, String> readExcelContent(InputStream is) {
        Map<Integer, String> content = new HashMap<Integer, String>();
        String str = "";
        try {

            //"C:\\Users\\TLJ\\Desktop\\aaa商品数据.xls"
            fs = new POIFSFileSystem(is);
            //获取指定的文档对象
            wb = new HSSFWorkbook(fs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //获取工作表对象
        sheet = wb.getSheetAt(0);
        // 得到总行数
        int rowNum = sheet.getLastRowNum();
        //由于第0行和第一行已经合并了  在这里索引从2开始
        row = sheet.getRow(2);
        int colNum = row.getPhysicalNumberOfCells();
        // 正文内容应该从第二行开始,第一行为表头的标题
        for (int i = 2; i <= rowNum; i++) {
            row = sheet.getRow(i);
            int j = 0;
            while (j < colNum) {
                str += getCellFormatValue(row.getCell((short) j)).trim() + "-";
                j++;
            }
            content.put(i, str);
            str = "";
        }
        return content;
    }

    /**
     * 获取单元格数据内容为字符串类型的数据
     *
     * @param cell Excel单元格
     * @return String 单元格数据内容
     */
    private String getStringCellValue(HSSFCell cell) {
        String strCell = "";
        switch (cell.getCellType()) {
            //如果当前Cell的Type为String
            case HSSFCell.CELL_TYPE_STRING:
                strCell = cell.getStringCellValue();
                break;
                //如果当前Cell的Type为数字的
            case HSSFCell.CELL_TYPE_NUMERIC:
                strCell = String.valueOf(cell.getNumericCellValue());
                break;
                //如果当前Cell的Type为布尔
            case HSSFCell.CELL_TYPE_BOOLEAN:
                strCell = String.valueOf(cell.getBooleanCellValue());
                break;
                //如果当前Cell的Type为数空白
            case HSSFCell.CELL_TYPE_BLANK:
                strCell = "";
                break;
            default:
                strCell = "";
                break;
        }
        if (strCell.equals("") || strCell == null) {
            return "null";
        }
        if (cell == null) {
            return "";
        }
        return strCell;
    }

    /**
     * 获取单元格数据内容为日期类型的数据
     *
     * @param cell
     *            Excel单元格
     * @return String 单元格数据内容
     */
    private String getDateCellValue(HSSFCell cell) {
        String result = "";
        try {
            int cellType = cell.getCellType();
            //如果当前cell为
            if (cellType == HSSFCell.CELL_TYPE_NUMERIC) {
                Date date = cell.getDateCellValue();
                result = (date.getYear() + 1900) + "-" + (date.getMonth() + 1)
                        + "-" + date.getDate();
                //如果当前Cell的Type为String
            } else if (cellType == HSSFCell.CELL_TYPE_STRING) {
                String date = getStringCellValue(cell);
                result = date.replaceAll("[年月]", "-").replace("日", "").trim();

                // 如果当前Cell的Type为空白
            } else if (cellType == HSSFCell.CELL_TYPE_BLANK) {
                result = "";
            }
        } catch (Exception e) {
            System.out.println("日期格式不正确!");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 根据HSSFCell类型设置数据
     * @param cell
     * @return
     */
    private String getCellFormatValue(HSSFCell cell) {
        String cellvalue = "";
        if (cell != null) {
            // 判断当前Cell的Type
            switch (cell.getCellType()) {
                // 如果当前Cell的Type为NUMERIC
                case HSSFCell.CELL_TYPE_NUMERIC:
                case HSSFCell.CELL_TYPE_FORMULA: {
                    // 判断当前的cell是否为Date
                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        Date date = cell.getDateCellValue();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        cellvalue = sdf.format(date);
                    }
                    // 如果是纯数字
                    else {
                        // 取得当前Cell的数值
                        cellvalue = String.valueOf(cell.getNumericCellValue());
                    }
                    break;
                }
                // 如果当前Cell的Type为STRIN
                case HSSFCell.CELL_TYPE_STRING:
                    // 取得当前的Cell字符串
                    cellvalue = cell.getRichStringCellValue().getString();
                    break;
                // 默认的Cell值
                default:
                    cellvalue = " ";
            }
        } else {
            cellvalue = "";
        }
        return cellvalue;

    }

    public static void main(String[] args) {
        try {
            // 对读取Excel表格标题测试
            InputStream is = new FileInputStream("C:\\Users\\dell\\Desktop\\aaa商品数据.xls");
            ImportExcel excelReader = new ImportExcel();
            String[] title = excelReader.readExcelTitle(is);
            System.out.println("获得Excel表格的标题:");
            for (String s : title) {
                System.out.print(s + " ");
            }
            System.out.println();

            // 对读取Excel表格内容测试
            InputStream is2 = new FileInputStream("C:\\Users\\dell\\Desktop\\aaa商品数据.xls");
            Map<Integer, String> map = excelReader.readExcelContent(is2);
            System.out.println("获得Excel表格的内容:");
            //这里由于xls合并了单元格需要对索引特殊处理
            List<TbGoods> tbGoodsList = new ArrayList<>();
                TbGoods tbGoods = new TbGoods();
            for (int i = 2; i <= map.size(); i++) {
               // System.out.println(map.get(i));
                String[] str = map.get(i).split("-");
                if(str.length<1){
                    continue;
                }
                //List list = Collections.singletonList(str[0]);

                tbGoods.setId(Long.valueOf(str[0]));
                tbGoods.setSellerId(str[1]);
                tbGoods.setGoodsName(str[2]);
                tbGoods.setAuditStatus(str[4]);
                tbGoods.setIsMarketable(str[5]);
                tbGoods.setCaption(str[7]);
                tbGoods.setSmallPic(str[11]);
                if (str[12]=="null"){
                    str[12]="";
                }
                tbGoodsList.add(tbGoods);
            }

            System.out.println(tbGoodsList);

        } catch (FileNotFoundException e) {
            System.out.println("未找到指定路径的文件!");
            e.printStackTrace();
        }


    }

}