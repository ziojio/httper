package httper.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeUtil {

    public static Type getSuperclassTypeParameter(Object object) {
        if (object == null) {
            return null;
        }
        Type superClass = object.getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType) {
            return ((ParameterizedType) superClass).getActualTypeArguments()[0];
        }
        return null;
    }

    public static Type getGenericInterfaceTypeParameter(Object object) {
        if (object == null) {
            return null;
        }
        Type[] genericInterfaces = object.getClass().getGenericInterfaces();
        if (genericInterfaces.length > 0) {
            Type genericInterface = genericInterfaces[0];
            if (genericInterface instanceof ParameterizedType) {
                return ((ParameterizedType) genericInterface).getActualTypeArguments()[0];
            }
        }
        return null;
    }

}
