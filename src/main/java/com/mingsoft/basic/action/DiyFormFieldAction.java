/**
The MIT License (MIT) * Copyright (c) 2015 铭飞科技

 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.mingsoft.basic.action;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.mingsoft.base.action.BaseAction;
import com.mingsoft.base.constant.e.TableEnum;
import com.mingsoft.basic.biz.IDiyFormBiz;
import com.mingsoft.basic.biz.IDiyFormFieldBiz;
import com.mingsoft.basic.constant.Const;
import com.mingsoft.basic.constant.e.DiyFormFieldEnum;
import com.mingsoft.basic.entity.DiyFormEntity;
import com.mingsoft.basic.entity.DiyFormFieldEntity;
import com.mingsoft.util.StringUtil;

/**
 * 表单字段义字段管理
 * 
 * @author 史爱华
 * @version 版本号：100-000-000<br/>
 *          创建日期：2015-1-10<br/>
 *          历史修订：<br/>
 */
@Controller("diyFormField")
@RequestMapping("/manager/diy/formField")
public class DiyFormFieldAction extends com.mingsoft.basic.action.BaseAction {

	/**
	 * 默认的字段id
	 */
	private static final String FIELD_ID = "id";

	/**
	 * 默认的字段date
	 */
	private static final String FIELD_DATE = "date";

	/**
	 * 默认的字段formId
	 */
	private static final String FIELD_FORMID = "formId";

	/**
	 * 注入自定义表单字段biz
	 */
	@Autowired
	private IDiyFormFieldBiz diyFormFieldBiz;

	/**
	 * 注入自定义表单biz
	 */
	@Autowired
	private IDiyFormBiz diyFormBiz;

	/**
	 * 查询字段的列表信息
	 * 
	 * @param diyFormId
	 *            自定义表单id
	 * @param request
	 *            请求对象
	 * @param response
	 *            响应对象
	 * @return 包含字段的map集合
	 */
	@RequestMapping("/list")
	@ResponseBody
	public Map list(int diyFormId, HttpServletRequest request, HttpServletResponse response) {
		Map map = new HashMap();
		// 查询所有的字段信息
		List<DiyFormFieldEntity> fieldList = diyFormFieldBiz.queryByDiyFormId(diyFormId);
		map.put("fieldList", fieldList);
		// 获取字段属性
		Map<Integer, String> fieldType = DiyFormFieldEnum.toMap();
		map.put("fieldType", fieldType);
		map.put("fieldNum", fieldType.size()); 
		return map;
	}

	/**
	 * 添加自定义字段
	 * 
	 * @param diyFormfield
	 *            自定义字段实体
	 * @param diyFormId
	 *            自定义表单id
	 * @param response
	 *            响应对象
	 */
	@RequestMapping("/{diyFormId}/save")
	@ResponseBody
	public void save(@ModelAttribute DiyFormFieldEntity diyFormfield, @PathVariable int diyFormId,
			HttpServletResponse response) {
		// 获取自定义表单实体
		DiyFormEntity diyForm = (DiyFormEntity) diyFormBiz.getEntity(diyFormId);
		if (diyForm == null) {
			this.outJson(response, null, false, this.getResString("err.not.exist", this.getResString("diy.form")));
			return;
		}
		// 更新前判断数据是否合法
		if (!StringUtil.checkLength(diyFormfield.getDiyFormFieldTipsName(), 1, 20)) {
			this.outJson(response, null, false,
					getResString("err.length", this.getResString("diy.form.tips.name"), "1", "20"));
			return;
		}
		if (!StringUtil.checkLength(diyFormfield.getDiyFormFieldFieldName(), 1, 20)) {
			this.outJson(response, null, false,
					getResString("err.length", this.getResString("diy.form.table.column.name"), "1", "20"));
			return;
		}

		if (diyFormFieldBiz.getByFieldName(diyFormfield.getDiyFormFieldFormId(),
				diyFormfield.getDiyFormFieldFieldName()) != null) {
			this.outJson(response, null, false,
					getResString("err.exist", this.getResString("diy.form.table.column.name")));
			return;
		}

		// 动态的修改表结构
		// 获取字段信息
		Map<String, String> fileds = new HashMap();
		// 压入字段名
		fileds.put("fieldName", diyFormfield.getDiyFormFieldFieldName());
		// 字段的数据类型
		fileds.put("fieldType", diyFormfield.getDiyFormFieldColumnType());
		fileds.put("default", diyFormfield.getDiyFormFieldDefault());
		// 在表中创建字段
		diyFormFieldBiz.alterTable(diyForm.getDiyFormTableName(), fileds, TableEnum.ALTER_ADD);
		this.diyFormFieldBiz.saveEntity(diyFormfield);
		this.outJson(response, null, true, null);
	}

	/**
	 * 获取编辑的字段实体的信息
	 * 
	 * @param diyFormFieldId
	 *            自定义字段id
	 * @param request
	 *            请求对象
	 * @return 返回自定义字段的map集合
	 */
	@RequestMapping("/{diyFormFieldId}/edit")
	@ResponseBody
	public Map edit(@PathVariable int diyFormFieldId, HttpServletRequest request) {
		Map mode = new HashMap();
		DiyFormFieldEntity diyFormfield = (DiyFormFieldEntity) diyFormFieldBiz.getEntity(diyFormFieldId);
		mode.put("diyFormfield", diyFormfield);
		return mode;
	}

	/**
	 * 更新字段信息
	 * 
	 * @param diyFormfield
	 *            要更新的字段信息的id
	 * @param response
	 *            响应对象
	 */
	@RequestMapping("/update")
	@ResponseBody
	public void update(@ModelAttribute DiyFormFieldEntity diyFormfield, HttpServletResponse response) {
		// 更新前判断数据是否合法
		if (!StringUtil.checkLength(diyFormfield.getDiyFormFieldTipsName(), 1, 20)) {
			this.outJson(response, null, false,
					getResString("err.length", this.getResString("fieldTipsName"), "1", "20"));
			return;
		}
		if (!StringUtil.checkLength(diyFormfield.getDiyFormFieldFieldName(), 1, 20)) {
			this.outJson(response, null, false,
					getResString("err.length", this.getResString("fieldFieldName"), "1", "20"));
			return;
		}

		// 获取自定义表单实体
		DiyFormEntity diyForm = (DiyFormEntity) diyFormBiz.getEntity(diyFormfield.getDiyFormFieldFormId());
		// 读取属性配置文件
		DiyFormFieldEntity oldField = (DiyFormFieldEntity) diyFormFieldBiz.getEntity(diyFormfield.getDiyFormFieldId());
		Map fields = new HashMap();
		// 更改前的字段名
		fields.put("fieldOldName", oldField.getDiyFormFieldFieldName());
		// 新字段名
		fields.put("fieldName", diyFormfield.getDiyFormFieldFieldName());
		// 字段的数据类型
		fields.put("fieldType", diyFormfield.getDiyFormFieldColumnType());
		if (diyForm == null) {
			this.outJson(response, null, false, this.getResString("err.not.exist"));
			return;
		}
		// 更新表的字段名
		diyFormFieldBiz.alterTable(diyForm.getDiyFormTableName(), fields, "modify");
		diyFormFieldBiz.updateEntity(diyFormfield);
		this.outJson(response, null, true, null);
	}

	/**
	 * 判断字段名是否存在重复
	 * 
	 * @param diyFormFieldFieldName
	 *            字段名
	 * @param request
	 *            请求对象
	 * @return true:存在重复,false:不存在重复
	 */
	@RequestMapping("/{diyFormFieldFieldName}/checkFieldNameExist")
	@ResponseBody
	public boolean checkFieldNameExist(@PathVariable String diyFormFieldFieldName, HttpServletRequest request) {
		int diyFormFieldFormId = 1;
		if (request.getParameter("diyFormFieldFormId") != null) {
			diyFormFieldFormId = Integer.parseInt(request.getParameter("diyFormFieldFormId"));
		}
		// 判断同一表单中是否存在表字段重名
		if (diyFormFieldFieldName.equalsIgnoreCase(FIELD_ID) || diyFormFieldFieldName.equalsIgnoreCase(FIELD_DATE)
				|| diyFormFieldFieldName.equalsIgnoreCase(FIELD_FORMID)
				|| diyFormFieldBiz.getByFieldName(diyFormFieldFormId, diyFormFieldFieldName) != null) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * 删除自定义字段
	 * 
	 * @param fieldId
	 *            表单ID
	 * @param request
	 *            请求对象
	 * @param response
	 *            响应对象
	 */
	@RequestMapping("/{fieldId}/delete")
	public void delete(@PathVariable int fieldId, HttpServletRequest request, HttpServletResponse response) {
		//
		DiyFormFieldEntity diyFormField = (DiyFormFieldEntity) this.diyFormFieldBiz.getEntity(fieldId);
		if (diyFormField == null) {
			this.outJson(response, null, false,
					this.getResString("err.not.exist", this.getResString("diy.form.field")));
			return;
		}
		DiyFormEntity diyForm = (DiyFormEntity) this.diyFormBiz.getEntity(diyFormField.getDiyFormFieldFormId());
		if (diyForm == null) {
			this.outJson(response, null, false, this.getResString("err.not.exist", this.getResString("diy.form")));
			return;
		}
		Map fields = new HashMap();
		// 要删除的字段名
		fields.put("fieldName", diyFormField.getDiyFormFieldFieldName());
		// 删除列
		diyFormFieldBiz.alterTable(diyForm.getDiyFormTableName(), fields, "drop");
		diyFormFieldBiz.deleteEntity(diyFormField.getDiyFormFieldId());
		this.outJson(response, null, true);
	}
}