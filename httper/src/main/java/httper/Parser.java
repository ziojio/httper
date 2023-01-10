package httper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;


public class Parser {

    private static Factory mParserFactory;

    public static Factory getParserFactory() {
        if (mParserFactory == null) {
            mParserFactory = new GsonParserFactory();
        }
        return mParserFactory;
    }

    public static void setParserFactory(Factory factory) {
        mParserFactory = factory;
    }

    public static void release() {
        mParserFactory = null;
    }


    public interface Factory {
        <T> T fromJson(String str, Type type);

        String toJson(Object object);

        Map<String, String> fromObject(Object object);
    }

    public static class GsonParserFactory implements Factory {
        private final Gson gson;

        public GsonParserFactory() {
            this.gson = new Gson();
        }

        public GsonParserFactory(Gson gson) {
            this.gson = gson;
        }

        @Override
        public <T> T fromJson(String str, Type type) {
            try {
                return gson.fromJson(str, type);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public String toJson(Object obj) {
            try {
                return gson.toJson(obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Map<String, String> fromObject(Object object) {
            try {
                return (Map<String, String>)
                        gson.fromJson(gson.toJson(object), TypeToken.getParameterized(Map.class, String.class, String.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
