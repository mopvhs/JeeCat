package com.jeesite.common.utils;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间工具类
 *
 */
public class DateTimeUtils {
	private static final Logger logger = LoggerFactory.getLogger(DateTimeUtils.class);

	public static String yyyyMMdd = "yyyy-MM-dd";

	public static String yyyyMMddhhmmss = "yyyy-MM-dd HH:mm:ss";


	/**
	 * 生日计算年龄
	 *
	 * @param birthday
	 * @return
	 */
	public static int getAgeByBirth(long birthday) {
		return getAgeByBirth(new Date(birthday));
	}

	/**
	 * 生日计算年龄
	 *
	 * @param birthday
	 * @return
	 */
	public static int getAgeByBirth(Date birthday) {
		int age = 0;
		try {
			Calendar now = Calendar.getInstance();
			now.setTime(new Date());// 当前时间

			Calendar birth = Calendar.getInstance();
			birth.setTime(birthday);

			if (birth.after(now)) {//如果传入的时间，在当前时间的后面，返回0岁
				age = 0;
			} else {
				age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
				if (now.get(Calendar.DAY_OF_YEAR) > birth.get(Calendar.DAY_OF_YEAR)) {
					age += 1;
				}
			}
			return age;
		} catch (Exception e) {//兼容性更强,异常后返回数据
			logger.error("getAgeByBirth error, birthday:{}", birthday, e);
			return 0;
		}
	}

	/**
	 * 下一天
	 *
	 * @return
	 */
	public static Date nextDate() {
		return nextDate(new Date());
	}


	/**
	 * 下一天
	 *
	 * @param date
	 * @return
	 */
	public static Date nextDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);

		calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 1);
		return calendar.getTime();
	}

	/**
	 * 获取n天的时间， n可以是正数后者负数
	 * 默认当前时间的前后 +—n天时间
	 *
	 * @param n
	 * @return
	 */
	public static Date nDays(int n) {
		return nDays(new Date(), n);
	}

	/**
	 * 获取n天的时间， n可以是正数后者负数
	 * date 用户指定
	 *
	 * @param date
	 * @param n
	 * @return
	 */
	public static Date nDays(Date date, int n) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.setTimeInMillis(calendar.getTimeInMillis() + n * DateUtils.MILLIS_PER_DAY);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		return calendar.getTime();
	}

	public static Date nDays(long time, int n) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time + n * DateUtils.MILLIS_PER_DAY);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		return calendar.getTime();
	}

	/**
	 * 获取n天的当前时间，n可以是正数或负数
	 *
	 * @param time
	 * @param n
	 * @return
	 */
	public static Date nDaysFromNow(long time, int n) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time + n * DateUtils.MILLIS_PER_DAY);
		return calendar.getTime();
	}

	/**
	 * 下一天 凌晨
	 *
	 * @param date
	 * @return
	 */
	public static Date nextDateStart(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(nextDate(date));

		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		return calendar.getTime();
	}

	/**
	 * 获取当天00:00:00.000时间
	 *
	 * @param time
	 * @return
	 */
	public static long earliestTimeToday(long time) {
		Calendar now = Calendar.getInstance();

		now.setTimeInMillis(time);

		now.set(Calendar.HOUR_OF_DAY, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);

		return now.getTimeInMillis();
	}

	/**
	 * 获取某天00:00:00时间
	 *
	 * @param date
	 * @return
	 */
	public static long earliestByDay(Date date) {
		Calendar now = Calendar.getInstance();
		now.setTime(date);
		now.set(Calendar.HOUR_OF_DAY, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);

		return now.getTimeInMillis();
	}

	/**
	 * 获取某天23:59:59时间
	 *
	 * @param date
	 * @return
	 */
	public static long lastByDay(Date date) {
		Calendar now = Calendar.getInstance();
		now.setTime(date);
		now.set(Calendar.HOUR_OF_DAY, 23);
		now.set(Calendar.MINUTE, 59);
		now.set(Calendar.SECOND, 59);
		now.set(Calendar.MILLISECOND, 0);

		return now.getTimeInMillis();
	}

	/**
	 * 获取当前时间整点
	 *
	 * @param time
	 * @return
	 */
	public static long earliestByHour(long time) {
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(time);

		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);

		return now.getTimeInMillis();
	}

	/**
	 * 获取n天前的00:00:00.000时间
	 *
	 * @param n
	 * @return
	 */
	public static long nDaysAgo(int n) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(c.getTimeInMillis() - n * DateUtils.MILLIS_PER_DAY);

		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		return c.getTimeInMillis();
	}

	/**
	 * 获取今天短时间字符串格式yyyy-MM-dd
	 *
	 * @return
	 */
	public static String getStringDateShort(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.format(date);
	}

	/**
	 * 获取时间字符串格式 yyyy-MM-dd HH:mm:ss
	 *
	 * @param date
	 * @return
	 */
	public static String getStringDate(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat(yyyyMMddhhmmss);
		return formatter.format(date);
	}

	/**
	 * 获取时间字符串格式 yyyy-MM-dd HH:mm:ss
	 *
	 * @param date
	 * @return
	 */
	public static Date getDate(String date) {
		try {
			if (NumberUtils.isDigits(date)) {
				return new Date(Long.valueOf(date));
			}


			SimpleDateFormat formatter = new SimpleDateFormat(yyyyMMddhhmmss);
			return formatter.parse(date);
		} catch (Exception e) {
			logger.error("getDate 解析日期失败 date {}", date, e);
		}
		return null;
	}

	public static void main(String[] args) {
		int currentHour = LocalTime.now().getHour();
		System.out.println(currentHour);
	}


}
