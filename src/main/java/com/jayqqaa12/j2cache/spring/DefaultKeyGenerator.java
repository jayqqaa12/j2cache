package com.jayqqaa12.j2cache.spring;

import com.jayqqaa12.j2cache.util.ReflectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DefaultKeyGenerator {

    public static final String LINK = "_";

    public static Pattern pattern = Pattern.compile("\\{\\d+\\.?[\\w]*\\}");

    public String buildKey(String key, Class<?>[] parameterTypes, Object[] arguments) {
        boolean isFirst = true;
        if (key.indexOf("{") > 0) {
            key = key.replace("{", ":{");

            Matcher matcher = pattern.matcher(key);
            while (matcher.find()) {
                String tmp = matcher.group();

                String  k= matcher.group().replace("{","").replace("}","");

                String express[] = k.split("\\.");
                String i = express[0];
                int index = Integer.parseInt(i) - 1;
                Object value = arguments[index];

                if (parameterTypes[index].isAssignableFrom(List.class)) {
                    List result = (List) arguments[index];
                    value = result.get(0);
                }
                if (value == null || value.equals("null"))
                    value = "";
                if (express.length > 1) {
                    String field = express[1];
                    value = ReflectionUtils.getFieldValue(value, field);
                }
                if (isFirst) {
                    key = key.replace(tmp, value.toString());
                } else {
                    key = key.replace(tmp, LINK + value.toString());
                }
            }
        }
        return key;
    }


}
