package com.u8x.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间工具类
 * 
 */
public final class DateUtil {

	public static final String F_FULL = "yyyy-MM-dd HH:mm:ss.SSS";

	public static final String F_yyyyMMddHHmmss = "yyyy-MM-dd HH:mm:ss";

	public static final String F_yyyyMMdd = "yyyy-MM-dd";

	public static final String F_HHmmss = "HH:mm:ss";

	public static final String F_yyyyMM = "yyyyMM";
	
	public static final String F_yyyyMMdd_new = "yyyy/MM/dd";
	

	/** 时间戳 - 毫秒 */
	public static long getTime() {
		return System.currentTimeMillis();
	}

	/** Unix 时间戳 - 秒 */
	public static int getTimeSec() {
		return (int) (System.currentTimeMillis() / 1000);
	}

	/** 将字符串形式转为日期 */
	public static Date format(String timeUTF) {
		if (null == timeUTF || "".equals(timeUTF.trim())) {
			return new Date();
		}
		StringBuilder builder = new StringBuilder();
		timeUTF = timeUTF.replaceAll("/|,|\\.|_", "-").trim();

		String time = null;
		int index1 = timeUTF.indexOf('-');
		if(index1 < 0) index1 = timeUTF.indexOf(' ');
		int index2 = timeUTF.indexOf(':');
		if (index1 < 0 || (index1 > index2 && index2 > 0)) {
			builder.append(getDate());
			time = timeUTF;
		} else {
			String[] dateTmps = new String[] { "00", "00", "00" };
			String dateUTF = null;
			if (timeUTF.indexOf(" ") != -1) {
				dateUTF = timeUTF.substring(0, timeUTF.indexOf(" "));
				time = timeUTF.substring(timeUTF.indexOf(" ") + 1);
			} else {
				dateUTF = timeUTF;
				time = "";
			}
			String[] dates = dateUTF.split("-");
			String date = null;
			for (int i = dates.length - 1, j = 2; i >= 0; i--, j--) {
				date = dates[i].trim();
				dateTmps[j] = date.length() > 1 ? date : 0 + date;
			}
			if (dates.length < 3) {
				Calendar calender = Calendar.getInstance();
				if (dates.length < 2) {
					dateTmps[1] = String.valueOf(calender.get(Calendar.MONTH) + 1);
				}
				dateTmps[0] = String.valueOf(calender.get(Calendar.YEAR));
			}
			builder.append(dateTmps[0]).append('-').append(dateTmps[1]).append('-').append(dateTmps[2]);
		}

		String[] timeTmps = new String[] { "00", "00", "00", "000" };
		String[] ms = time.split("-");
		if (ms.length > 1) {
			String millS = ms[1].trim();
			if (millS.length() > 3) {
				millS = millS.substring(0, 3);
			} else {
				for (; millS.length() < 3;) {
					millS = 0 + millS;
				}
			}
			timeTmps[3] = millS;
		}
		String[] times = ms[0].split(":");
		String tmp = null;
		for (int i = times.length - 1, j = 2; i >= 0; i--, j--) {
			tmp = times[i].trim();
			timeTmps[j] = tmp.length() > 1 ? tmp : 0 + tmp;
		}

		builder.append(' ').append(timeTmps[0]).append(':').append(timeTmps[1]).append(':').append(timeTmps[2])
				.append('.').append(timeTmps[3]);

		return format(builder.toString(), F_FULL);
	}

	/** 自定义格式格式化当前文本形式为时间（内置有格式） */
	public static Date format(String timeUTF, String style) {
		Date date = null;
		try {
			SimpleDateFormat format = new SimpleDateFormat(style);
			Date xdate = format.parse(timeUTF);
			date = new Date(xdate.getTime());
		} catch (ParseException pe) {
			pe.printStackTrace();
		}
		return date;
	}
	
	public static String formatUTF(String timeUTF) {
		return format(format(timeUTF), F_FULL);
	}

	/** yyyy-MM-dd HH:mm:ss格式格式化当前时间为文本形式（内置有格式） */
	public static String format(Date date) {
		return format(date, F_yyyyMMddHHmmss);
	}

	/** 自定义格式格式化当前时间为文本形式（内置有格式） */
	public static String format(Date date, String style) {
		return new SimpleDateFormat(style).format(date);
	}

	/** 当前日期的格式化文本 */
	public static String getDate() {
		return getTime(F_yyyyMMdd);
	}

	/** 当前时间的yyyy-MM-dd HH:mm:ss格式化文本 */
	public static String getDateTime() {
		return getTime(F_yyyyMMddHHmmss);
	}

	/** 当前时间的格式化文本 */
	public static String getTime(String style) {
		return format(new Date(), style);
	}

	/** 得到几天前的时间 */
	public static Calendar getDateBefore(Date date, int day) {
		Calendar now = Calendar.getInstance();
		now.setTime(date);
		now.add(Calendar.DATE, -day);
		return now;
	}

	/** 得到几天后的时间 */
	public static Calendar getDateAfter(Date date, int day) {
		Calendar now = Calendar.getInstance();
		now.setTime(date);
		now.add(Calendar.DATE, day);
		return now;
	}

	/** 得到几天前的时间 */
	public static Date getDateBeforeDay(int day) {
		return new Date(System.currentTimeMillis() - day * 24 * 60 * 60 * 1000);
	}

	/** 得到几小时前的时间 */
	public static Date getDateBeforeHour(int hour) {
		return new Date(System.currentTimeMillis() - hour * 60 * 60 * 1000);
	}

	/** 得到几天前的时间的0点 */
	public static Calendar getDateBeforeIn0(int day) {
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(System.currentTimeMillis() - day * 24 * 60 * 60 * 1000);
		now.set(Calendar.HOUR_OF_DAY, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);
		return now;
	}

	/** 得到几天后的时间的0点 */
	public static Calendar getDateAfterIn0(int day) {
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(System.currentTimeMillis() + day * 24 * 60 * 60 * 1000);
		now.set(Calendar.HOUR_OF_DAY, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);
		return now;
	}

	/** 得到几天后的时间 */
	public static Date getDateAfter(int day) {
		return new Date(System.currentTimeMillis() + day * 24 * 60 * 60 * 1000);
	}

	/** 得到几小时后的时间 */
	public static Date getAfterHour(int hour) {
		return new Date(System.currentTimeMillis() + hour * 60 * 60 * 1000);
	}

	/** 得到下个整点的时间 */
	public static Calendar getNextHour() {
		Calendar now = Calendar.getInstance();
		now.add(Calendar.HOUR_OF_DAY, 1);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);
		return now;
	}

	/** 获取延迟时间 */
	public static long getDelay(String time) {
		if (time == null)
			return -1;
		long runTime = format(time).getTime();
		return runTime - System.currentTimeMillis();
	}

	/** 获取下个整点的时间戳 */
	public static long getNextHourMillisecond() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.add(Calendar.HOUR_OF_DAY, 1);
		return c.getTimeInMillis();
	}

	/** 获取下个0点的时间戳 */
	public static long getNextDayMillisecond() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.add(Calendar.DAY_OF_MONTH, 1);
		return c.getTimeInMillis();
	}

	/** 获取一天的开始时间 */
	public static Date getFirstTimeOfDay(Date time) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(time);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/** 获取一天的结束时间 */
	public static Date getLastTimeOfDay(Date time) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(time);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar.getTime();
	}

	/** 获取指定日期所在月份的第一天 */
	public static Date getFirstDayOfMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/** 是否是同一天 */
	public static boolean isSameDay(Date day1, Date day2) {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(day1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(day2);
		return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
				&& cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
	}

	/**
	 * 是否当天
	 *
	 * @param time
	 * @return
	 */
	public static boolean isToday(Date time) {
		return isSameDay(Calendar.getInstance().getTime(), time);
	}

	/** 昨天的这个时候 */
	public static Date getYesterday() {
		return getDateBefore(new Date(), 1).getTime();
	}

	/** 明天的这个时候 */
	public static Date getTomorrow() {
		return getNextDay(new Date());
	}

	/** 指定日期的第二天 */
	public static Date getNextDay(Date date) {
		return getDateAfter(date, 1).getTime();
	}

	public static Date getZeroDate() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, 0);
		c.set(Calendar.MONTH, 0);
		c.set(Calendar.DATE, 0);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	
	/**
	 * @return 返回当天的零点时间
	 */
	public static Date getZeroToday() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}
	
	/**
	 * @return 返回明天的零点时间
	 */
	public static Date getZeroTomorrow() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DATE, c.get(Calendar.DATE) + 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		c.add(Calendar.SECOND, 10);//  0点执行延迟10秒
		return c.getTime();
	}
	
	/**
	 * 判断一个字符串日期是否过期
	 * @param dateTime
	 * @return  0：时间未到  1：正在进行中 2：已结束 
	 */
	public static int isOutDate(String dateTime) {
		long now = new Date().getTime();
		long time = format(dateTime).getTime();
		if (now == time) {
			return 1;
		} else if (now > time) {
			return 2;
		}

		return 0;
	}
	
	/**
	 * 判断事件在起止日期内 例如:2017-04-15 00:00:00~2017-05-15 00:00:00
	 * @return 0：时间未到  1：正在进行中 2：已结束
	 */
	public static int isOutDate(String startStr, String endStr) {
		long start = format(startStr).getTime();
		long end = format(endStr).getTime();

		return isOutDate(start, end);
	}
	
	/**
	 * 判断事件在起止日期内 例如:2017-04-15 00:00:00~2017-05-15 00:00:00
	 * @return 0：时间未到  1：正在进行中 2：已结束
	 */
	public static int isOutDate(long start, long end) {
		long now = System.currentTimeMillis();
		if (now > start && now < end) {
			return 1;
		} else if (now > end) {
			return 2;
		}
		return 0;
	}

	/**
	 * 判断具体时间是否过期
	 * @param time
	 * @param startStr
	 * @param endStr
	 * @return 0：时间未到  1：正在进行中 2：已结束
	 */
	public static int isOutDate(String time, String startStr, String endStr) {
		long now = format(time).getTime();
		long start = format(startStr).getTime();
		long end = format(endStr).getTime();
		if (now > start && now < end) {
			return 1;
		} else if (now > end) {
			return 2;
		}
		return 0;
	}

	/**
	 * 判断是否在一个时间段内 例如:8:00~10:00
	 * @return 0：时间未到 1：正在进行中 2：已结束
	 */
	public static int isInTime(String startHour, String endHour) {
		if (startHour.indexOf(":") == startHour.lastIndexOf(":")) {
			startHour += ":00";
		}
		if (endHour.indexOf(":") == endHour.lastIndexOf(":")) {
			endHour += ":00";
		}
		return isOutDate(startHour, endHour);
	}

	public static void main(String[] args) {
		System.out.println(formatUTF("31 "));
		System.out.println(formatUTF("1:31"));
		System.out.println(formatUTF("12:31.31"));
		System.out.println(formatUTF("2010.8.30 "));
		System.out.println(formatUTF("2010.08.3 11"));
		System.out.println(formatUTF("13 13:00:00"));
		System.out.println(formatUTF("11"));
		System.out.println(formatUTF("11:11"));
		System.out.println(formatUTF("11:11:11"));
		System.out.println(formatUTF("11:11:11.111"));
		System.out.println(isInTime("8:00", "12:00"));
	}

}