package com.u8x.utils;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.thoughtworks.xstream.XStream;
import org.apache.http.NameValuePair;

import com.u8x.common.JsonHelper;
import com.u8x.common.XLogger;
import com.u8x.utils.EncryptUtils;
import com.u8x.utils.StringUtils;
import com.u8x.utils.U8XUtils;

public class U8XUtils {
	public static String genId() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	private static final XLogger logger = XLogger.getLogger(U8XUtils.class);

    private static final char[] CHARS = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    private static Random random = new Random();

    public static boolean isVilidAndroidPackageName(String name){
        if(StringUtils.isEmpty(name)) return false;

        Pattern p = Pattern.compile("^([A-Za-z]{1}[A-Za-z\\d_]*\\.)*[A-Za-z][A-Za-z\\d_]*$");
        Matcher mat = p.matcher(name);
        return mat.find();
    }

    public static boolean verifySign(String sign, String key, String... params) {
        if (sign == null || sign.length() <= 0 || key == null || params == null || params.length <= 0 || params.length % 2 != 0) {
            logger.error("Params Num Error !!!");
            return false;
        }

        Map<String, String> paramMap = new HashMap<>();
        for (int i = 0; i < params.length; ) {
            paramMap.put(params[i], params[i + 1]);
            i += 2;
        }

        String serverSign = null;
        try {
            serverSign = sign(paramMap, key);
        } catch (Throwable e) {
            logger.error("Sign Error !!!", e);
            serverSign = null;
        }

        return sign.equals(serverSign);
    }

    /**
     * 将params中的kv组合按照字母序排序, 然后附加上 key=key, 进行 md5, 最终输出大写格式
     */
    public static String sign(Map<String, String> params, String key) {
        Map<String, String> sortedParams = new TreeMap<>(params);

        StringBuilder sb = StringUtils.getStringBuilder();
        for (Map.Entry<String, String> param : sortedParams.entrySet()) {
            String value = param.getValue();
            if (value != null && value.length() > 0) {
                sb.append(param.getKey()).append("=").append(value).append("&");
            }
        }
        sb.append("key").append("=").append(key);

        String signStr = sb.toString();

        logger.debug("SignString: {}", signStr);

        String sign = EncryptUtils.md5(signStr).toUpperCase();

        return sign;
    }

    public static String sign(List<NameValuePair> params, String key) {
        Map<String, String> paramMap = new HashMap<>();
        for (NameValuePair pair : params) {
            paramMap.put(pair.getName(), pair.getValue());
        }

        return sign(paramMap, key);
    }

    /**
     * 產生隨機字符串, 小寫字母與數字的組合
     */
    public static String genRandomStr(int len) {
        StringBuilder sb = StringUtils.getStringBuilder();

        int size = CHARS.length;
        for (int i = 0; i < len; i++) {
            sb.append(CHARS[random.nextInt(size)]);
        }

        return sb.toString();
    }

    public static String randomString() {
        StringBuilder sb = StringUtils.getStringBuilder();
        for (int i = 0; i < 32; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public static String map2Xml(Map<String, String> params) {

        if (params == null || params.size() == 0) return null;

        StringBuilder sb = StringUtils.getStringBuilder();
        sb.append("<xml>");
        for (String k : params.keySet()) {
            String v = params.get(k);
            sb.append("<").append(k).append(">").append(String.format("<![CDATA[%s]]>", v)).append("</").append(k).append(">");
        }
        sb.append("</xml>");

        return sb.toString();
    }

    public static <T> T parseXml(String xml, Class<T> clazz) {
        if (xml != null && xml.length() > 0) {
            try {
                JAXBContext ctx = JAXBContext.newInstance(clazz);
                Unmarshaller unmarshaller = ctx.createUnmarshaller();
                T result = (T) unmarshaller.unmarshal(new StringReader(xml));

                return result;
            } catch (JAXBException e) {
                logger.error("Xml Parse Error: {} -- {}", clazz.getName(), xml);
                logger.error("Xml Parse Error !!!", e);
            }
        }

        return null;
    }

    public static <T> String toXml(T obj, Class<T> clazz) {
        try {
            JAXBContext ctx = JAXBContext.newInstance(clazz);
            Marshaller marshaller = ctx.createMarshaller();
            StringWriter sw = new StringWriter();
            marshaller.marshal(obj, sw);
            return sw.toString();
        } catch (JAXBException e) {
            logger.error("Xml toXml Error: {} -- {}", clazz.getName(), JsonHelper.toJson(obj));
            logger.error("Xml toXml Error !!!", e);
        }
        return null;
    }

    public static long parseStoreUnitToBytes(String storeUnit){

        if(StringUtils.isEmpty(storeUnit)) return 0L;
        if(storeUnit.contains(".")) throw new IllegalArgumentException("can not parse float store unit. ");

        long kb = 1024L;
        long mb = 1024 * kb;
        long gb = 1024 * mb;

        if(storeUnit.endsWith("GB") || storeUnit.endsWith("gb")){
            storeUnit = storeUnit.substring(0, storeUnit.length()-2);
            return Long.valueOf(storeUnit) * gb;
        }

        if(storeUnit.endsWith("G") || storeUnit.endsWith("g")){
            storeUnit = storeUnit.substring(0, storeUnit.length()-1);
            return Long.valueOf(storeUnit) * gb;
        }

        if(storeUnit.endsWith("MB") || storeUnit.endsWith("mb")){
            storeUnit = storeUnit.substring(0, storeUnit.length()-2);
            return Long.valueOf(storeUnit) * mb;
        }

        if(storeUnit.endsWith("M") || storeUnit.endsWith("m")){
            storeUnit = storeUnit.substring(0, storeUnit.length()-1);
            return Long.valueOf(storeUnit) * mb;
        }

        if(storeUnit.endsWith("KB") || storeUnit.endsWith("kb")){
            storeUnit = storeUnit.substring(0, storeUnit.length()-2);
            return Long.valueOf(storeUnit) * kb;
        }

        if(storeUnit.endsWith("K") || storeUnit.endsWith("k")){
            storeUnit = storeUnit.substring(0, storeUnit.length()-1);
            return Long.valueOf(storeUnit) * kb;
        }

        if(storeUnit.endsWith("Byte")){
            storeUnit = storeUnit.substring(0, storeUnit.length()-4);
            return Long.valueOf(storeUnit);
        }

        if(storeUnit.endsWith("B") || storeUnit.endsWith("b")){
            storeUnit = storeUnit.substring(0, storeUnit.length() - 1);
            return Long.valueOf(storeUnit);
        }

        try{

            Long t = Long.parseLong(storeUnit);
            return t;

        }catch (Exception e){
            e.printStackTrace();
            throw new IllegalArgumentException("can not parse store unit:"+storeUnit);
        }

    }

    public static Map<String, String> parseUrlStrToMap(String body) {
        Map<String, String> map = new HashMap<>();

        if (body != null && body.trim().length() > 0) {
            String[] bodyArray = body.split("&");
            if (bodyArray != null) {
                for (String str : bodyArray) {
                    String[] item = str.trim().split("=");
                    if (item != null && item.length > 0) {
                        map.put(item[0].trim(), item.length > 1 ? item[1].trim() : "");
                    }
                }
            }
        }

        return map;
    }

    public static String makeupU8XPassword(String uid, String password) {
        return EncryptUtils.md5(StringUtils.concat(uid, password)).toUpperCase();
    }



//    public static Object xml2Object(String xmlString,Class clazz) throws Exception{
//        if( clazz == null || xmlString == null || "".equals(xmlString.replace(" ", "")) ) return null;
//        Object object = clazz.newInstance();
//        XStream xStream = new XStream();
//        String className = object.getClass().getName();
//        xStream.alias(className, clazz);
//        xStream.processAnnotations(new Class[]{clazz});
//        xStream.setMode(XStream.NO_REFERENCES);
//        //转为对象
//        object = xStream.fromXML(xmlString);
//        return object;
//
//    }
//
//    public static String object2Xml(Object obj) throws Exception{
//        if(obj==null) return null;
//        XStream xStream = new XStream();
//        String xml = xStream.toXML(obj);
//        return xml;
//    }

}
