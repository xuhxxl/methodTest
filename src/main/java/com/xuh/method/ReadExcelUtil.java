package com.xuh.method;

import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 读取Excel文件
 *
 * 适用于97-2003工作表格 和 2007工作表格
 *
 * Created by xuh on 2017/9/20.
 */
public class ReadExcelUtil {
    static SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd");
    static Map<String,CellStyle> styleMap = new HashMap<String,CellStyle>(); //存储单元格样式的Map

    /**
     * 读取excel文件内容
     * @param filePath
     * @throws FileNotFoundException
     * @throws FileFormatException
     */
    /**
     * 读excel
     * @param filePath excel路径
     */
    public static void readExcel(String filePath){

//        List<Object> list = new ArrayList<Object>();
        Workbook book = null;
        try {
            book = getExcelWorkbook(filePath);
            Sheet sheet = getSheetByNum(book,1);
            System.out.println("sheet名称是："+sheet.getSheetName());

            int lastRowNum = sheet.getLastRowNum();

            Row row = null;
            for(int i=0;i<=lastRowNum;i++){
                Object word = null;
                row = sheet.getRow(i);
                if(row != null){
                    word = new Object();
                    System.out.println("正在读第"+(i+1)+"行：");
                    int lastCellNum = row.getLastCellNum();
                    Cell cell = null;
                    StringBuilder sb = null;
                    for(int j=0;j<lastCellNum;j++){
                        cell = row.getCell(j);

                        if(cell != null){
                            sb = new StringBuilder("第"+(j+1)+"列的单元格内容是：");
                            String type_cn = null;
                            String type_style = cell.getCellStyle().getDataFormatString().toUpperCase();
                            String type_style_cn = getCellStyleByChinese(type_style);
                            int type = cell.getCellType();
                            String value = "";
                            switch (type) {
                                case 0:
                                    if(DateUtil.isCellDateFormatted(cell)){
                                        type_cn = "NUMBER-DATE";
                                        Date date = cell.getDateCellValue();
                                        value = sFormat.format(date);
                                    }else {
                                        type_cn = "NUMBER";
                                        double tempValue = cell.getNumericCellValue();
                                        value = String.valueOf(tempValue);

                                    }
                                    break;
                                case 1:
                                    type_cn = "STRING";
                                    value = cell.getStringCellValue();
                                    break;
                                case 2:
                                    type_cn = "FORMULA";
                                    value = cell.getCellFormula();
                                    break;
                                case 3:
                                    type_cn = "BLANK";
                                    value = cell.getStringCellValue();
                                    break;
                                case 4:
                                    type_cn = "BOOLEAN";
                                    boolean tempValue = cell.getBooleanCellValue();
                                    value = String.valueOf(tempValue);
                                    break;
                                case 5:
                                    type_cn = "ERROR";
                                    byte b = cell.getErrorCellValue();
                                    value = String.valueOf(b);
                                default:
                                    break;
                            }
                            sb.append(value + ",内容类型是："+type_cn+",单元格的格式是："+type_style_cn);
                            switch (j) {
                                case 0:
                                    System.out.println(value);
                                    break;
                                case 4:
                                    System.out.println(value);
                                    break;
                                case 5:
                                    System.out.println(value);
                                    break;
                                case 6:
                                    System.out.println(value);
                                    break;
                                default:
                                    break;
                            }
                            System.out.println(sb.toString()+"\n");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        return list;
    }

    /**
     * 根据单元格的格式 返回单元格的格式中文
     * @param type_style
     * @return
     */
    private static String getCellStyleByChinese(String type_style) {
        String cell_style_cn = "";
        if(type_style.contains("GENERAL")){
            cell_style_cn = "常规";
        }else if(type_style.equals("_ * #,##0.00_ ;_ * \\-#,##0.00_ ;_ * \"-\"??_ ;_ @_ ")){
            cell_style_cn = "会计专用";
        }else if(type_style.equals("0")){
            cell_style_cn = "整数";
        }else if(type_style.contains("YYYY/MM") || type_style.contains("YYYY\\-MM")){
            cell_style_cn = "日期";
        }else if(type_style.equals("0.00%")){
            cell_style_cn = "百分比";
        }else {
            cell_style_cn = "不符合规定格式类型:"+type_style;
//			cell_style_cn = type_style;
        }
        return cell_style_cn;
    }

    /**
     * 将传入的内容写入到excel中sheet里
     * @param list
     */
    public static boolean writeToExcel(List<Map<String,String>> list, Sheet sheet, int startRow){
        boolean result = false;
        try {
            Map<String,String> map = null;
            Row row = null;
            for(int i=0;i<list.size();i++){
                map = list.get(i);
                row = sheet.getRow(startRow-1);
                if(row == null){
                    row = sheet.createRow(startRow-1);
                }
                startRow ++;
                Cell cell = null;

                BigDecimal db = null;
                for(Map.Entry<String,String> entry : map.entrySet()){
                    String key = entry.getKey();
                    int colNum = toNum_new(key)-1;

                    String value_type = entry.getValue();
                    String value = value_type.split(",")[0];
                    String style = value_type.split(",")[1];

                    cell = row.getCell(colNum);
                    if(cell == null){
                        cell = row.createCell(colNum);
                    }
                    if(style.equals("GENERAL")){
                        cell.setCellValue(value);
                    }else{
                        if(style.equals("DOUBLE") || style.equals("INT")){
                            db = new BigDecimal(value,java.math.MathContext.UNLIMITED);
                            cell.setCellValue(db.doubleValue());
                        }else if(style.equals("PERCENT")){
                            db = new BigDecimal(value,java.math.MathContext.UNLIMITED);
                            cell.setCellValue(db.doubleValue());
                        }else if(style.equals("DATE")){
                            java.util.Date date = sFormat.parse(value);
                            cell.setCellValue(date);
                        }
                        cell.setCellStyle(styleMap.get(style));
                    }
                }
            }
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        return result;
    }
    /**
     * 获取excel的Workbook
     * @throws IOException
     */
    public static Workbook getExcelWorkbook(String filePath) throws IOException {
        Workbook book = null;
        File file  = null;
        FileInputStream fis = null;

        try {
            file = new File(filePath);
            if(!file.exists()){
                throw new RuntimeException("文件不存在");
            }else{
                fis = new FileInputStream(file);
                book = WorkbookFactory.create(fis);
                initStyleMap(book);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            if(fis != null){
                fis.close();
            }
        }
        return book;
    }

    /**
     * 根据索引 返回Sheet
     * @param number
     */
    public static Sheet getSheetByNum(Workbook book,int number){
        Sheet sheet = null;
        try {
            sheet = book.getSheetAt(number-1);
//			if(sheet == null){
//				sheet = book.createSheet("Sheet"+number);
//			}
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return sheet;
    }

    /**
     * 初始化格式Map
     */
    public static void initStyleMap(Workbook book){
        DataFormat hssfDF = book.createDataFormat();

        CellStyle doubleStyle = book.createCellStyle(); //会计专用
        doubleStyle.setDataFormat(hssfDF.getFormat("_ * #,##0.00_ ;_ * \\-#,##0.00_ ;_ * \"-\"??_ ;_ @_ ")); //poi写入后为会计专用
        styleMap.put("DOUBLE", doubleStyle);

        CellStyle intStyle = book.createCellStyle(); //会计专用
        intStyle.setDataFormat(hssfDF.getFormat("0")); //poi写入后为会计专用
        styleMap.put("INT", intStyle);

        CellStyle yyyyMMddStyle = book.createCellStyle();//日期yyyyMMdd
        yyyyMMddStyle.setDataFormat(hssfDF.getFormat("yyyy-MM-dd"));
        styleMap.put("DATE", yyyyMMddStyle);

        CellStyle percentStyle = book.createCellStyle();//百分比
        percentStyle.setDataFormat(hssfDF.getFormat("0.00%"));
        styleMap.put("PERCENT", percentStyle);
    }







    /**
     * 数字转字母
     * @return
     */
    public static String toLetterString(int number) {
        if (number < 1) {// 出错了
            return null;
        }
        if (number < 27) {
            return String.valueOf((char) ('A' + number - 1));
        }
        if (number % 26 == 0) {
            return toLetterString(number / 26 - 1) + "Z";
        }
        return toLetterString(number / 26)+ String.valueOf((char) ('A' + number % 26 - 1));
    }



    /**
     * 判断是否是数字
     */
    public static boolean isNumeric(String str){
        if(str.contains("-") || str.contains("(") || str.contains(")") ){
            str = str.replace("-","").replace("(","").replace(")","");
        }
        String value = "";
        boolean b = true;
        if(str.contains("..")){
            return false;
        }else if(str.substring(str.length() - 1).equals(".")){
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            value = str.substring(i,i+1);
            if(!value.equals(".")){
                java.text.DecimalFormat nf = new java.text.DecimalFormat("00.00");
                try {
                    nf.parse(value);
                } catch (ParseException e1) {
                    b = false;
                    return b;
                }
            }
        }
        return b;
    }


    /*
      *   将字符串转为整数
      */
    public static   int   toNum_new(String   str)
    {
        char[]   ch=str.toCharArray();
        int   ret=0;
        for(int   i=0;i <ch.length;i++)
        {
            ret*=26;
            ret+=ch2int(ch[i]);
        }
        return   ret;
    }
    /*
      *   A~Z/a~z   转为1~26
      */
    public static   int   ch2int(char   ch)
    {
        if(ch >= 'a'&&ch <='z')
            return   ch -'a'+1;
        if(ch >= 'A'&&ch <= 'Z')
            return   ch -'A'+1;
        throw   new   java.lang.IllegalArgumentException();
    }


    public static void main(String[] args) {

        String filename = "E:/001-CE-(101-200)-5746.xls";
        ReadExcelUtil.readExcel(filename);

    }
}
