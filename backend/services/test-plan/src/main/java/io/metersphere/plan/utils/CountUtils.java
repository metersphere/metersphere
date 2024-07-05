package io.metersphere.plan.utils;

import io.metersphere.plan.dto.CaseCount;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.LogUtils;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 汇总工具类
 */
@UtilityClass
public class CountUtils {

	/**
	 * 集合属性汇总
	 * @param list 集合
	 * @return 汇总实体
	 */
	public static CaseCount summarizeProperties(List<CaseCount> list)  {
		if (list.isEmpty()) {
			return new CaseCount();
		}

		Class<CaseCount> clazz = (Class<CaseCount>) list.getFirst().getClass();
		CaseCount summary;
		try {
			summary = clazz.newInstance();
			Field[] fields = clazz.getDeclaredFields();
			for (CaseCount caseCount : list) {
				for (Field field : fields) {
					field.setAccessible(true);
					Integer value = (Integer) field.get(summary) + (Integer) field.get(caseCount);
					field.set(summary, value);
				}
			}
		} catch (Exception e) {
			LogUtils.error("汇总数据异常: " + e.getMessage());
			throw new MSException(e);
		}

		return summary;
	}
}
