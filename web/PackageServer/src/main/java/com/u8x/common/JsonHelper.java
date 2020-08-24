package com.u8x.common;

import com.google.gson.Gson;

/**
 * @author guofeng.qin
 */
public class JsonHelper {
    private static final XLogger logger = XLogger.getLogger(JsonHelper.class);

    private static final Gson GSON = new Gson();
    // new GsonBuilder().registerTypeAdapterFactory(new GsonSupport.NullArrayToEmptyAdapterFactory<Object[]>())
    //         .registerTypeAdapter(Double.class, (JsonSerializer<Double>) (src, typeOfSrc, context) -> {
    //             if (src == src.longValue())
    //                 return new JsonPrimitive(src.longValue());
    //             return new JsonPrimitive(src);
    //         }).create();

    public static String toJson(Object obj) {
        try {
            return GSON.toJson(obj);
        } catch (Throwable e) {
            throw e;
        }
    }

    public static <T> T fromJson(byte[] json, Class<T> clazz) {
        String str = new String(json);
        return fromJson(str, clazz);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json != null && json.length() > 0) {
            try {
                return GSON.fromJson(json, clazz);
            } catch (Throwable e) {
                logger.error("Json deserialize Error: {}", json);
            }
        }

        return null;
    }

    // public static <T> T fromJson(String json, Type type) {
    //     return GSON.fromJson(json, type);
    // }
}
