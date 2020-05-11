package com.song;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @描述
 * @创建人 liangsong
 * @创建时间 2018/ 07/2018/7/7/007 18:31
 */
public class StringUtils {
    private static Pattern pattern = Pattern.compile("\\$(\\d+)");
    private static Pattern number = Pattern.compile("\\d+");
    private static Pattern unicode = Pattern.compile("\"((?:\\\\\\d+)+)\"");
    //    private static Pattern worldPattern = Pattern.compile("[a-zA-Z0-9]+");
    private static Pattern worldPattern = Pattern.compile("[A-Z]{1,1}[a-z]{0,100}");
    private static Pattern UPPERCASE = Pattern.compile("[A-Z]+");
    private static final Logger logger = LoggerFactory.getLogger(StringUtils.class);

    public static String replace(String content, String... args) {

        Matcher matcher = pattern.matcher(content);
        StringBuffer stringBuffer = new StringBuffer();
        while (matcher.find()) {
            int index = Integer.parseInt(matcher.group(1));
            matcher.appendReplacement(stringBuffer, args[index]);
        }
        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }

    public static String replaceKey(String content, String key, String value) {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            Pattern p = Pattern.compile("\\$" + key);
            Matcher m = p.matcher(content);
            boolean isFind = false;
            while (m.find()) {
                isFind = true;
                m.appendReplacement(stringBuffer, value);
            }
            logger.error("replaceKey isFind:{}", isFind);

            m.appendTail(stringBuffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer.toString();
    }

    /**
     * 字符串转驼峰
     * @param s
     * @param separator 字符串分割符
     * @return
     */
    public static String toHump(String s, String separator) {
        String[] nameArr = s.split(separator);
        StringBuffer stringBuffer = new StringBuffer();
        int nameArrLen = nameArr.length;
        for (int i = 0; i < nameArrLen; i++) {
            if (nameArr[i].length() > 0) {
                if (stringBuffer.length() == 0) {
                    stringBuffer.append(Character.toLowerCase(nameArr[i].charAt(0)));
                } else {
                    stringBuffer.append(Character.toUpperCase(nameArr[i].charAt(0)));
                }
                stringBuffer.append(nameArr[i].substring(1));
            }
        }
        return stringBuffer.toString();
    }

    public static String toUpCase(String s) {
        return toUpCase(s, "_");
    }

    public static String toUpCase(String s, String separator) {
        Matcher mather = UPPERCASE.matcher(s);
        StringBuffer stringBuffer = new StringBuffer();
        int startIndex = 0;
        while (mather.find()) {
            String string = s.substring(startIndex, mather.start());
            appendUpper(stringBuffer, string, separator);
            stringBuffer.append(separator);
            startIndex = mather.start();
        }
        if (startIndex < s.length() - 1) {
            String string = s.substring(startIndex, s.length());
            appendUpper(stringBuffer, string, separator);
        }
        return stringBuffer.toString();
    }

    private static void appendUpper(StringBuffer stringBuffer, String string, String separator) {
        while (string.endsWith(separator)) {
            string = string.substring(0, string.length() - separator.length());
        }
        stringBuffer.append(string.toUpperCase());
    }

    public static boolean isEmpty(String string) {
        return string == null || string.equals("");
    }

    public static boolean isNotEmpty(String string) {
        return !isEmpty(string);
    }

    public static String toUpLowerString(String newValue) {
        return toUpLowerString(newValue, false);
    }

    public static String toUpLowerString(String newValue, boolean firstUpper) {
        StringBuilder ret = new StringBuilder();
        boolean hasBig = false;
        for (char c : newValue.toCharArray()) {
            if (isSmall(c)) {

                if (ret.length() == 0) {
                    hasBig = true;
                    if (firstUpper) {
                        ret.append(Character.toUpperCase(c));
                        continue;
                    }
                } else if (!hasBig) {
                    ret.append(Character.toUpperCase(c));
                    hasBig = true;
                    continue;
                }
                ret.append(c);

            } else if (isBig(c)) {
                ret.append(c);
                hasBig = true;
            } else {
                hasBig = false;
                if (isNumber(c)) {
                    ret.append(c);
                }
            }
        }
        return ret.toString();
    }

    private static boolean isNumber(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isSmall(char c) {
        return c >= 'a' && c <= 'z';
    }

    private static boolean isBig(char c) {
        return c >= 'A' && c <= 'Z';
    }

    private static byte[] getBytes(String s1) {

        String[] split = s1.split("\\\\");
        byte[] bytes2 = new byte[split.length - 1];
        int count = 0;
        for (String s : split) {
            if (s.equals("")) {
                continue;
            }
            int i = Integer.parseInt(s, 8);
            bytes2[count++] = (byte) i;
        }
        return bytes2;
    }

    public static String convertToString(String src) {
        StringBuffer stringBuffer = new StringBuffer();
        Matcher matcher = unicode.matcher(src);


        while (matcher.find()) {
            String group = matcher.group(1);
            byte[] bytes = getBytes(group);
            String string = null;
            try {
                string = new String(bytes, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            matcher.appendReplacement(stringBuffer, "\"" + string + "\"");
        }
        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }

    public static long safeParseLong(String str, long defaultValue) {
        try {
            return Long.parseLong(str);
        } catch (Exception e) {
        }

        return defaultValue;
    }

    public static int safeParseInt(String str, int defaultValue) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
        }

        return defaultValue;
    }

    public static String rightFill(String s, int i, String oneChar) {
        while (s.length() < i) {
            s = s + oneChar;
        }
        return s;
    }
}
