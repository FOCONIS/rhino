/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.mozilla.javascript;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class NativeJavaMap extends NativeJavaObject {
    
    private static final long serialVersionUID = 46513864372878618L;
    
    private Map<Object, Object> map;
    private Class<?> keyType;
    private Class<?> valueType;
    private transient Map<String, Object> keyTranslationMap;

    @SuppressWarnings("unchecked")
    public NativeJavaMap(Scriptable scope, Object map, Type staticType) {
        super(scope, map, staticType);
        assert map instanceof Map;
        this.map = (Map<Object, Object>) map;
        if (staticType == null) {
            staticType = map.getClass().getGenericSuperclass();
        }
        if (staticType instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) staticType).getActualTypeArguments();
            this.keyType = ScriptRuntime.getRawType(types[0]);
            this.valueType = ScriptRuntime.getRawType(types[1]);
        } else {
            this.keyType = Object.class;
            this.valueType = Object.class;
        }
    }

    @Override
    public String getClassName() {
        return "JavaMap";
    }


    @Override
    public boolean has(String name, Scriptable start) {
        if (map.containsKey(toKey(name, false))) {
            return true;
        }
        return super.has(name, start);
    }

    @Override
    public boolean has(int index, Scriptable start) {
        if (map.containsKey(toKey(index, false))) {
            return true;
        }
        return super.has(index, start);
    }

    @Override
    public Object get(String name, Scriptable start) {
        Object key = toKey(name, false);
        if (map.containsKey(key)) {
            Context cx = Context.getContext();
            Object obj = map.get(key);
            if (obj == null) {
                return null;
            }
            return cx.getWrapFactory().wrap(cx, this, obj, obj.getClass());
        }
        return super.get(name, start);
    }

    @Override
    public Object get(int index, Scriptable start) {
        if (map.containsKey(toKey(index, false))) {
            Context cx = Context.getContext();
            Object obj = map.get(Integer.valueOf(index));
            if (obj == null) {
                return null;
            }
            return cx.getWrapFactory().wrap(cx, this, obj, obj.getClass());
        }
        return super.get(index, start);
    }
    
    private Object toKey(String key, boolean translateNew) {
        if (keyType == String.class) {
            return key;
        }
        if (keyTranslationMap == null) {
            keyTranslationMap = new HashMap<>();
            map.keySet().forEach(k -> keyTranslationMap.put(ScriptRuntime.toString(k), k));
        }
        
        Object ret = keyTranslationMap.get(key);
        if (ret == null) {
            if (translateNew) {
                ret = Context.jsToJava(key, keyType);
                keyTranslationMap.put(key, ret);
            } else {
                ret = key;
            }
        }
        return ret;
    }
    
    private Object toKey(int key, boolean translateNew) {
        return toKey(ScriptRuntime.toString(key), translateNew);
    }
    
    private Object toValue(Object value) {
        return Context.jsToJava(value, valueType);
    }

    @Override
    public void put(String name, Scriptable start, Object value) {
        map.put(toKey(name, true), toValue(value));
    }

    @Override
    public void put(int index, Scriptable start, Object value) {
        map.put(toKey(index, true), toValue(value));
    }

    @Override
    public Object[] getIds() {
        Object[] ids = new Object[map.size()];
        int i = 0;
        for (Object key : map.keySet()) {
            if (key instanceof Number) {
                ids[i++] = (Number)key;
            } else {
                ids[i++] = ScriptRuntime.toString(key);
            }
        }
        return ids;
    }

}
