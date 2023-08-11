package cn.shengwenzeng.instanceinterfacesqlfx;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.FileChooser;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Interface2SQLController {
	@FXML
	public TextField ExcelPathTextField;
	@FXML
	public TextField esbTag;
	@FXML
	public TextField esbCode;
	@FXML
	public TextArea resultSQL;

	@FXML
	protected void onGenerateButtonClick() {
		String excelPathText = ExcelPathTextField.getText();
		String esbTagText = esbTag.getText();
		String esbCodeText = esbCode.getText();
		System.out.println("excelPathText = " + excelPathText);
		System.out.println("esbTagText = " + esbTagText);
		System.out.println("esbCodeText = " + esbCodeText);

		if (excelPathText == null || excelPathText.isEmpty() || esbTagText == null || esbTagText.isEmpty() || esbCodeText == null || esbCodeText.isEmpty()) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setContentText("输入信息不全！");
			alert.show();
			return;
		}


		String path = excelPathText.replaceAll("\\\\", "/");
		if (isExit(path)) {
			Sheet sheet = null;
			String consumerid = "";
			String scen = "";

			try (Workbook workbook = parseExcel(path)) {
				assert workbook != null;
				sheet = workbook.getSheet(esbCodeText);
				if (sheet.getRow(0).getCell(5) != null && "服务名称".equals(sheet.getRow(0).getCell(5).toString())) {
					consumerid = sheet.getRow(1).getCell(6).toString();
					scen = workbook.getSheet(esbCodeText).getRow(1).getCell(6).toString();
				} else if (sheet.getRow(0).getCell(6) != null && "服务名称".equals(sheet.getRow(0).getCell(6).toString())) {
					consumerid = sheet.getRow(1).getCell(6).toString();
					scen = workbook.getSheet(esbCodeText).getRow(1).getCell(7).toString();
				} else if (sheet.getRow(0).getCell(7) != null && "服务名称".equals(sheet.getRow(0).getCell(7).toString())) {
					consumerid = sheet.getRow(0).getCell(8).toString();
					scen = workbook.getSheet(esbCodeText).getRow(1).getCell(8).toString();
				} else {
					consumerid = sheet.getRow(1).getCell(6).toString();
					scen = workbook.getSheet(esbCodeText).getRow(1).getCell(7).toString();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			System.out.println("consumerid = " + consumerid);
			System.out.println("scen = " + scen);

			// 场景名称
			String SCENNAME = getTradeName(scen);
			System.out.println("场景名称:" + SCENNAME);
			// 场景号码
			scen = getConsumerId(scen);
			// 服务名称
			String MSGTYPENAME = getTradeName(consumerid);
			// 服务号码
			consumerid = getConsumerId(consumerid);

			//System.out.println("consumerid = " + consumerid);
			//System.out.println("scen = " + scen);
			//System.out.println("MSGTYPENAME = " + MSGTYPENAME);

			String MSGTYPE = "";
			String req = esbTagText + "." + consumerid + "." + scen + ".req";
			String rsp = esbTagText + "." + consumerid + "." + scen + ".rsp";
			int rowcount = sheet.getLastRowNum();
			String EN_NAME = "";
			String varchar = "";
			String CH_NAME = "";
			String notNull = "";
			String ARRAY = "";
			String COLNAME = "";
			String ESBNAME = "";
			String startEndFlag = "";
			StringBuilder totalSql = new StringBuilder();
			boolean in_out_flag = true;
			int j = 1;
			int k = 2;
			int seq = 0;
			String sql = "insert into tp_cip_interfacecolmap (MSGTYPEID,MSGTYPENAME,COLNUM,COLNAME,COLFLAG,COLDEFAULT,COLTYPE,COLLENGTH,COLDESC,COLMUST,COLMAPNAME)\n";
			totalSql.append(sql);
			totalSql.append("values('").append(req).append("','").append(SCENNAME).append("','0','/BODY','O','','Object','9999','报文体','Y','/');\n\n");

			// 从 7 开始循环，去掉顶部的前 7 行
			for (int i = 7; i <= rowcount; ++i) {
				// 如果循环到 输出 行，则插入一条 sql 语句
				if (sheet.getRow(i) != null && sheet.getRow(i).getCell(0) != null && "输出".equals(sheet.getRow(i).getCell(0).toString().trim().replace(" ", ""))) {
					in_out_flag = false;
					if (i != rowcount) {
						totalSql.append(sql);
						totalSql.append("values('").append(rsp).append("','").append(SCENNAME).append("','0','/BODY','O','','Object','9999','报文体','Y','/');\n\n");
					}
				} else if (sheet.getRow(i) != null && sheet.getRow(i).getCell(7) != null && !"".equals(sheet.getRow(i).getCell(7).toString().trim().replace(" ", ""))) {
					String COLTYPE = "";
					String COLLENGTH = "";
					String COLMUST;
					// 英文名称
					EN_NAME = sheet.getRow(i).getCell(7) != null ? sheet.getRow(i).getCell(7).toString().trim().replace(" ", "") : "";
					// 中文名称
					CH_NAME = sheet.getRow(i).getCell(8) != null ? sheet.getRow(i).getCell(8).toString().trim().replace(" ", "") : "";
					// 数据类型
					varchar = sheet.getRow(i).getCell(9) != null ? sheet.getRow(i).getCell(9).toString().trim().replace(" ", "") : "";
					// 是否必输
					notNull = sheet.getRow(i).getCell(10) != null ? sheet.getRow(i).getCell(10).toString().trim().replace(" ", "") : "";
					// 起始或结束表示
					startEndFlag = sheet.getRow(i).getCell(12) != null ? sheet.getRow(i).getCell(12).toString().trim().replace(" ", "") : "";
					// 将全角字符转为半角字符
					if (varchar.contains("（") || varchar.contains("）") || varchar.contains("，")) {
						varchar = varchar.replace("（", "(").replace("）", ")").replace("，", ",");
					}

					String[] ARRAY1;
					if (varchar.contains("String")) {
						COLTYPE = "TextString";
						// 获取类型长度，括号内的数字
						COLLENGTH = varchar.split("\\(")[1].replace(")", "");
					} else if (varchar.contains("Double")) {
						COLTYPE = "TextDouble";
						ARRAY1 = varchar.split("\\(")[1].replace(")", "").split(",");
						COLLENGTH = String.valueOf(Integer.parseInt(ARRAY1[0]) + Integer.parseInt(ARRAY1[1]));
					} else if (varchar.contains("LONG")) {
						COLTYPE = "TextLong";
						COLLENGTH = varchar.split("\\(")[1].replace(")", "");
					} else if (varchar.contains("INTEGER")) {
						COLTYPE = "TextInt";
						COLLENGTH = varchar.split("\\(")[1].replace(")", "");
					}

					if (!notNull.contains("M") && !notNull.contains("Y") && !notNull.contains("★")) {
						COLMUST = "O";
					} else {
						COLMUST = "M";
					}

					if ((EN_NAME.trim().endsWith("Arr") || EN_NAME.trim().endsWith("Array")) && ("Start".equals(startEndFlag) || "start".equals(startEndFlag))) {
						if ("".equals(ARRAY)) {
							ARRAY = "array/" + EN_NAME;
						} else if (!ARRAY.equals(EN_NAME)) {
							ARRAY = ARRAY + "/array/" + EN_NAME;
						}

						if (in_out_flag) {
							seq = j;
							MSGTYPE = req;
						} else {
							MSGTYPE = rsp;
							seq = k - j;
						}

						totalSql.append(sql);
						totalSql.append("values('").append(MSGTYPE).append("','").append(SCENNAME).append("','").append(seq).append("','/BODY/").append(ARRAY).append("','OA','','Object','9999','数组','O','/").append(ARRAY.replaceAll("array/", "")).append("');\n\n");
						if (in_out_flag) {
							++j;
						}

						++k;
					} else if ((EN_NAME.trim().endsWith("Arr") || EN_NAME.trim().endsWith("Array")) && ("End".equals(startEndFlag) || "end".equals(startEndFlag))) {
						ARRAY1 = ARRAY.split("/");
						if (ARRAY1.length == 2) {
							ARRAY = "";
						} else {
							ARRAY = ARRAY1[0] + "/" + ARRAY1[1];
						}
					} else {
						if (in_out_flag) {
							MSGTYPE = req;
							ESBNAME = ARRAY + "/" + EN_NAME;
							COLNAME = ARRAY.replaceAll("array/", "") + "/" + EN_NAME;
							seq = j;
						} else {
							seq = k - j;
							MSGTYPE = rsp;
							ESBNAME = ARRAY + "/" + EN_NAME;
							COLNAME = ARRAY.replaceAll("array/", "") + "/" + EN_NAME;
						}

						totalSql.append(sql);
						if (!"".equals(ARRAY)) {
							totalSql.append("values('").append(MSGTYPE).append("','").append(SCENNAME).append("','").append(seq).append("','/BODY/").append(ESBNAME).append("','A','','").append(COLTYPE).append("','").append(COLLENGTH).append("','").append(CH_NAME).append("','").append(COLMUST).append("','/").append(COLNAME).append("');\n\n");
						} else {
							totalSql.append("values('").append(MSGTYPE).append("','").append(SCENNAME).append("','").append(seq).append("','/BODY/").append(EN_NAME).append("','F','','").append(COLTYPE).append("','").append(COLLENGTH).append("','").append(CH_NAME).append("','").append(COLMUST).append("','/").append(EN_NAME).append("');\n\n");
						}

						if (in_out_flag) {
							++j;
						}

						++k;
					}
				}
			}
			//System.out.println(totalSql);
			resultSQL.setText(totalSql.toString());
			Clipboard clipboard = Clipboard.getSystemClipboard();
			ClipboardContent clipboardContent = new ClipboardContent();
			clipboardContent.putString(totalSql.toString());
			clipboard.setContent(clipboardContent);
		} else {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setContentText("未能找到文件，可能路径有特殊字符，请更换位置重新输入！");
			alert.show();
		}
	}

	@FXML
	protected void getExcelPath() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("选择Excel文件");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XLSX", "*.xlsx"), new FileChooser.ExtensionFilter("XLS", "*.xls"));
		File file = fileChooser.showOpenDialog(null);
		if (file != null) {
			ExcelPathTextField.setText(file.getAbsolutePath());
		}
	}

	@FXML
	protected void saveSQL() {
		String excelPathText = ExcelPathTextField.getText();
		String path = excelPathText.replaceAll("\\\\", "/");
		String totalSql = resultSQL.getText();
		String esbTagText = esbTag.getText();
		String esbCodeText = esbCode.getText();
		fileWrite(new File(path).getParent(), totalSql, esbTagText, esbCodeText);
	}


	/**
	 * 提取字符串中的数字，这里是服务名称
	 */
	public static String getConsumerId(String scenes) {
		String reg = "[^0-9]";
		Pattern p = Pattern.compile(reg);
		Matcher matcher = p.matcher(scenes);
		return matcher.replaceAll("").trim();
	}

	public static String getTradeName(String scenes) {
		String reg = "[0-9]";
		Pattern p = Pattern.compile(reg);
		Matcher matcher = p.matcher(scenes);
		return matcher.replaceAll("").trim().replace("\n", "").replace("(", "").replace(")", "").replace("（", "").replace("）", "").replace(" ", "");
	}

	public void fileWrite(String ExcelPath, String sb, String trcode, String funcode) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("保存生成的脚本");
		fileChooser.setInitialDirectory(new File(ExcelPath));
		fileChooser.setInitialFileName(trcode + "_" + funcode + ".sql");
		File file = fileChooser.showSaveDialog(null);
		if (file != null) {
			try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
				out.write(sb);
				out.flush();
			} catch (Exception ignored) {
				throw new RuntimeException(ignored);
			} finally {
				try {
					Runtime.getRuntime().exec("explorer.exe /select," + file.getPath());
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	public static boolean isExit(String file) {
		return file != null && !"".equals(file) && (new File(file)).exists();
	}

	private Workbook parseExcel(String path) {
		if (path.endsWith(".xlsx")) {
			System.out.println("读取xlsx文件");
			try {
				return new XSSFWorkbook(path);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		} else if (path.endsWith(".xls")) {
			System.out.println("读取xls文件");
			try {
				return new HSSFWorkbook(new FileInputStream(path));
			} catch (IOException e) {
				e.printStackTrace();
				return null;

			}
		} else {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setContentText("文件格式不正确！");
			alert.show();
			return null;
		}
	}

}

