package io.metersphere.system.utils;

import lombok.experimental.UtilityClass;

import java.text.DecimalFormat;

/**
 * 计划模块百分比计算工具类
 */
@UtilityClass
public class RateCalculateUtils {

	public static final int MAX_BOUNDARY = 100;
	public static final int MIN_BOUNDARY = 0;

	/**
	 * 计算并保留精度
	 * @param molecular 分子
	 * @param denominator 分母
	 * @param precision 精度
	 * @return rate
	 */
	public static Double divWithPrecision(Long molecular, Long denominator, Integer precision) {
		DecimalFormat rateFormat = new DecimalFormat("#.##");
		rateFormat.setMinimumFractionDigits(precision);
		rateFormat.setMaximumFractionDigits(precision);
		double rate = (molecular == 0 || denominator == 0) ? 0 :
				Double.parseDouble(rateFormat.format((double) molecular * 100 / (double) denominator));
		/*
		 * V2旧逻辑(边界数据展示问题):
		 * 如果算出的结果由于精度问题四舍五入为100%, 且计算数量小于总数, 实际值设为99.99%
		 * 如果算出的结果由于精度问题四舍五入为0%, 实际计算数量 > 0, 实际值设为0.01%
		 */
		if (rate == MAX_BOUNDARY && molecular < denominator) {
			return 99.99;
		} else if (rate == MIN_BOUNDARY && molecular > 0) {
			return 0.01;
		}
		return rate;
	}

	public static Double divWithPrecision(Integer molecular, Integer denominator, Integer precision) {
		return divWithPrecision((long) molecular, (long) denominator, precision);
	}
}
