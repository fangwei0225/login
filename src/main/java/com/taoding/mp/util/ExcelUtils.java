package com.taoding.mp.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 封装对excel的操作，包括本地读写excel和流中输出excel。
 * 支持2007版本及以下，支持2010版本。使用对应不同版本的方法操作
 * 依赖于poi-3.9.jar,poi-ooxml-3.9.jar,poi-ooxml-schemas-3.9.jar,dom4j-1.6.1.jar
 *
 * @author wuwentan
 */
public class ExcelUtils {
    /**
     * 读取2010Excel
     *
     * @return
     * @throws IOException
     */
    public static List<List<Object>> read_2010(InputStream inputStream, int sheetsNum) throws IOException {
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(inputStream);
        List<List<Object>> data = new ArrayList<List<Object>>();
        List rows = null;
        // Read the Sheet
        XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(sheetsNum);
        // Read the Row
        for (int rowNum = xssfSheet.getFirstRowNum(); rowNum <= xssfSheet.getLastRowNum(); rowNum++) {
            XSSFRow xssfRow = xssfSheet.getRow(rowNum);
            if (xssfRow != null) {
                rows = new ArrayList();
                for (int celNum = 0; celNum < xssfSheet.getRow(xssfSheet.getFirstRowNum()).getPhysicalNumberOfCells(); celNum++) {
                    rows.add(getValue(xssfRow.getCell(celNum)));
                }
                data.add(rows);
            }

        }
        return data;
    }

    /**
     * 读取2003-2007版本的xml
     *
     * @param inputStream
     * @param sheetsNum
     * @return
     * @throws IOException
     */
    public static List<List<Object>> read_2007(InputStream inputStream, int sheetsNum) throws IOException {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook(inputStream);
        List<List<Object>> data = new ArrayList<List<Object>>();
        List rows = null;
        // Read the Sheet
        HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(sheetsNum);
        // Read the Row
        for (int rowNum = hssfSheet.getFirstRowNum(); rowNum <= hssfSheet.getLastRowNum(); rowNum++) {
            HSSFRow hssfRow = hssfSheet.getRow(rowNum);
            if (hssfRow != null) {
                rows = new ArrayList();
                for (int celNum = 0; celNum < hssfSheet.getRow(hssfSheet.getFirstRowNum()).getPhysicalNumberOfCells(); celNum++) {
                    rows.add(getValue(hssfRow.getCell(celNum)));
                }
                data.add(rows);
            }
        }
        return data;
    }

    public static void showExcel(String filePath) throws Exception {
        HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(new File(filePath)));
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {//获取每个Sheet表
            HSSFSheet sheet = workbook.getSheetAt(i);
            sheet.getSheetName();

        }
    }

    /**
     * 获取XSSFCell中的值
     *
     * @param xssfRow
     * @return
     */
    private static String getValue(XSSFCell xssfRow) {
        if (xssfRow == null) {
            return "";
        } else {
            xssfRow.setCellType(CellType.STRING);//设置单元格内容格式为字符串，如需特殊处理数字格式请去掉此行
        }
        if (xssfRow.getCellType() == CellType.BOOLEAN) {
            return String.valueOf(xssfRow.getBooleanCellValue());
        } else if (xssfRow.getCellType() == CellType.NUMERIC) {
            double doubleValue = xssfRow.getNumericCellValue();
            long longVal = Math.round(xssfRow.getNumericCellValue());
            if (HSSFDateUtil.isCellDateFormatted(xssfRow)) {
                return CommonUtils.DateToString(xssfRow.getDateCellValue(), "yyyy年MM月dd日 HH时mm分");
            }
            if (Double.parseDouble(longVal + ".0") == doubleValue) {
                return String.valueOf(longVal);
            }
            return String.valueOf(doubleValue);
        } else {
            try {
                return String.valueOf(xssfRow.getStringCellValue());
            } catch (IllegalStateException e) {
                return String.valueOf(xssfRow.getNumericCellValue());
            }
        }
    }

    /**
     * 获取HSSFCell中的值（重载方法）
     *
     * @param hssfCell
     * @return
     */
    @SuppressWarnings("static-access")
    private static String getValue(HSSFCell hssfCell) {
        if (hssfCell == null) {
            return "";
        }
        if (hssfCell.getCellType() == CellType.BOOLEAN) {
            return String.valueOf(hssfCell.getBooleanCellValue());
        } else if (hssfCell.getCellType() == CellType.NUMERIC) {
            double doubleValue = hssfCell.getNumericCellValue();
            long longVal = Math.round(hssfCell.getNumericCellValue());
            if (HSSFDateUtil.isCellDateFormatted(hssfCell)) {
                return CommonUtils.DateToString(hssfCell.getDateCellValue(), "yyyy年MM月dd日 HH时mm分");
            }
            if (Double.parseDouble(longVal + ".0") == doubleValue) {
                return String.valueOf(longVal);
            }
            return String.valueOf(doubleValue);
        } else {
            return String.valueOf(hssfCell.getStringCellValue());
        }
    }

    /**
     * 读取某个工作簿上的某个单元格的值。
     *
     * @param sheetOrder 工作簿序号,从0开始。
     * @param colum      列数 从1开始
     * @param row        行数 从1开始
     * @return 单元格的值。
     * @throws Exception 加载excel异常。
     */
    public String read(FileInputStream fis, int sheetOrder, int colum, int row) throws Exception {
        Workbook workbook = WorkbookFactory.create(fis);
        if (fis != null) {
            fis.close();
        }
        Sheet sheet = workbook.getSheetAt(sheetOrder);
        Row rows = sheet.getRow(row - 1);
        Cell cell = rows.getCell(colum - 1);
        String content = cell.getStringCellValue();
        return content;
    }


    /**
     * 得到一个工作区最后一条记录的序号，相当于这个工作簿共多少行数据。
     *
     * @param fis        excel文件流
     * @param sheetOrder 工作区序号
     * @return int 序号。
     */
    public int getSheetLastRowNum(FileInputStream fis, int sheetOrder) throws IOException,
            InvalidFormatException {
        Workbook workbook = WorkbookFactory.create(fis);
        if (fis != null) {
            fis.close();
        }
        Sheet sheet = workbook.getSheetAt(sheetOrder);
        return sheet.getLastRowNum();
    }

    /**
     * 创建
     *
     * @param fileType 输出文件类型：xls、xlsx
     * @throws IOException
     * @Title: writeExcel
     */
    public static void writeExcel(List<List<Object>> dataList, OutputStream outputStream, String fileType, String sheetsName) throws IOException {
        //创建工作文档对象
        Workbook wb = null;
        if (fileType.equals("xls")) {
            wb = new HSSFWorkbook();
        } else if (fileType.equals("xlsx")) {
            wb = new XSSFWorkbook();
        } else {
            System.out.println("您的文档格式不正确！");
        }
        //创建sheet对象
        Sheet sheet1 = wb.createSheet(sheetsName);
        //循环写入行数据
        int row = 0, col = 0;
        for (List<Object> rowMeta : dataList) {
            col = 0;
            Row row1 = sheet1.createRow(row);
            for (Object cell : rowMeta) {
                Cell cell1 = row1.createCell(col);
                cell1.setCellValue(cell.toString());
                col++;
            }
            row++;
        }
        //写入数据
        wb.write(outputStream);
        //关闭文件流
        outputStream.close();
    }

    /**
     * 创建(重载) 本方法除列宽外只对指定的列号行号生效
     *
     * @param fileType 输出文件类型：xls、xlsx
     * @throws IOException
     * @Title: writeExcel
     */
    public static void writeExcel(List<List<Object>> dataList, OutputStream outputStream, String fileType, String sheetsName, Map<String, Object> params) throws IOException {
        short startRow = 0, endRow = 0, startCol = 0, endCol = 0; //合并单元格所需要的  第一个为开始的行号 第二个为结束的行号 第三个为开始的列号 第四个为结束的列号
        String rowNumber = params.get("rowNumber") != null ? params.get("rowNumber").toString() : ""; //行号
        String colNumber = params.get("colNumber") != null ? params.get("colNumber").toString() : ""; //列号
        String height = params.get("height") != null ? params.get("height").toString() : "";//行高 单位为px 前提要行号不为null的时候生效
        String defaultRow = params.get("defaultRow") != null ? params.get("defaultRow").toString() : "";//默认行高
        String defaultCol = params.get("defaultCol") != null ? params.get("defaultCol").toString() : "";//默认列宽
        Map<String, Integer> colWidth = params.get("colWidth") != null ? (Map<String, Integer>) params.get("colWidth") : null;//key是列号value是需要的宽度 单位为px
        String fontColor = params.get("fontColor") != null ? params.get("fontColor").toString() : "";  //字体颜色 值参考HSSFColor对象中的颜色值 前提要行号和列号不为空的情况下生效

        //创建工作文档对象
        Workbook wb = null;
        if (fileType.equals("xls")) {
            wb = new HSSFWorkbook();
        } else if (fileType.equals("xlsx")) {
            wb = new XSSFWorkbook();
        } else {
            System.out.println("您的文档格式不正确！");
        }


        //创建sheet对象
        Sheet sheet1 = wb.createSheet(sheetsName);

        try {
            startRow = params.get("startRow") == null ? 0 : Short.parseShort(params.get("startRow").toString());
            endRow = params.get("endRow") == null ? 0 : Short.parseShort(params.get("endRow").toString());
            startCol = params.get("startCol") == null ? 0 : Short.parseShort(params.get("startCol").toString());
            endCol = params.get("endCol") == null ? 0 : Short.parseShort(params.get("endCol").toString());
            //设置列宽
            if (colWidth != null) {
                for (String keyCol : colWidth.keySet()) {
                    sheet1.setColumnWidth(new Integer(keyCol), colWidth.get(keyCol));
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        //合并单元格
        if (startRow != 0 || endRow != 0 || startCol != 0 || endCol != 0) {
            CellRangeAddress region1 = new CellRangeAddress(startCol, endCol, startRow, endRow);
            sheet1.addMergedRegion(region1);
        }

        //创建工作文档样式
        CellStyle style = wb.createCellStyle();
        ;
        style.setWrapText(true);
        if (StringUtils.isNotBlank(fontColor)) {
            Font font = wb.createFont();
            font.setColor(Short.parseShort(fontColor));
            style.setFont(font);
        }

        //默认宽高
        if (StringUtils.isNotBlank(defaultRow)) {
            sheet1.setDefaultRowHeight((short) (Short.parseShort(defaultRow) * 20));
        }
        if (StringUtils.isNotBlank(defaultCol)) {
            sheet1.setDefaultColumnWidth((short) (Short.parseShort(defaultCol) * 20));
        }

        //循环写入行数据
        int row = 0, col = 0;
        for (List<Object> rowMeta : dataList) {
            col = 0;
            Row row1 = sheet1.createRow(row);
            for (Object cell : rowMeta) {
                Cell cell1 = row1.createCell(col);
                cell1.setCellValue(cell.toString());
                if (StringUtils.isNotBlank(rowNumber) && StringUtils.isNotBlank(colNumber) && row == new Integer(rowNumber) && col == new Integer(colNumber) && style != null) {
                    cell1.setCellStyle(style);
                }
                if (StringUtils.isNotBlank(rowNumber) && StringUtils.isNotBlank(height) && row == new Integer(rowNumber)) {
                    row1.setHeight((short) (Short.parseShort(height) * 20));
                }
                col++;
            }
            row++;
        }

        //写入数据
        wb.write(outputStream);
        //关闭文件流
        outputStream.close();
    }
}