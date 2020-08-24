package com.u8x.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2017/5/21.
 */
public class StringUtils {

	public static boolean isEmpty(String str) {
		return str == null || str.trim().length() == 0;
	}

	public static boolean isNotEmpty(String str) {
		return str != null && str.trim().length() > 0;
	}

	public static boolean contain(String[] array, String val){

		return Arrays.asList(array).contains(val);

	}

	public static String formatJSONStr(String jsonStr){

	    //将全角双引号转换为半角
	    return jsonStr.replaceAll("“", "\"");

    }
	
    /**
     * https://github.com/springside/springside4/blob/master/modules/utils/src/main/java/org/springside/modules/utils/StringBuilderHolder.java
     */
    private static final ThreadLocal<StringBuilderHolder> threadLocalStringBuilderHolder =
            new ThreadLocal<StringBuilderHolder>() {
                protected StringBuilderHolder initialValue() {
                    return new StringBuilderHolder(256);
                }
            };

    public static String concat(Object... strs) {
        StringBuilder sb = threadLocalStringBuilderHolder.get().resetAndGetStringBuilder();
        for (Object str : strs) {
            sb.append(str);
        }
        return sb.toString();
    }

    public static String join(String split, String... objs) {
        StringBuilder sb = threadLocalStringBuilderHolder.get().resetAndGetStringBuilder();
        int len = objs.length;
        for (int index = 0; index < len; index++) {
            if (index > 0) {
                sb.append(split);
            }
            sb.append(objs[index]);
        }
        return sb.toString();
    }

    public static String format(String template, Object... objs) {
        return String.format(template, objs);
    }

    public static StringBuilder getStringBuilder() {
        return threadLocalStringBuilderHolder.get().resetAndGetStringBuilder();
    }

    private static class StringBuilderHolder {
        private final StringBuilder sb;

        public StringBuilderHolder(int capacity) {
            sb = new StringBuilder(capacity);
        }

        public StringBuilder resetAndGetStringBuilder() {
            sb.setLength(0);
            return sb;
        }
    }

}
