package com.logica.ndk.commons.utils.test;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * @author ondrusekl
 */
public class TestUtils {

  public static final void setField(Object target, String name, Object value) {
    checkNotNull(target, "Target object must not be null");
    checkNotNull(name, "Field name must not be empty");

    Field field = TestUtils.findField(target.getClass(), name);
    if (field == null) {
      throw new IllegalArgumentException("Could not find field [" + name + "] on target [" + target + "]");
    }

    TestUtils.makeAccessible(field);
    TestUtils.setField(field, target, value);
  }

  public static final Object getField(Object target, String name) {
    checkNotNull(target, "Target object must not be null");
    checkNotNull(name, "Field name must not be empty");

    Field field = TestUtils.findField(target.getClass(), name);
    if (field == null) {
      throw new IllegalArgumentException("Could not find field [" + name + "] on target [" + target + "]");
    }

    TestUtils.makeAccessible(field);
    return TestUtils.getField(field, target);
  }

  public static final <T> T invokeConstructor(Class<?> targetClass, Object... initargs) {
    Preconditions.checkNotNull(targetClass, "Target class must not be null");

    Class<?>[] parameterTypes = new Class<?>[initargs.length];
    for (int i = 0; i < initargs.length; i++) {
      parameterTypes[i] = initargs[i].getClass();
    }
    Constructor<?> constructor = TestUtils.findConstructor(targetClass, parameterTypes);
    if (constructor == null) {
      String parameterTypesString = Joiner.on(",").join(Lists.transform(Arrays.asList(parameterTypes), new Function<Class<?>, String>() {
        @Override
        public String apply(Class<?> input) {
          return input.getName();
        }
      }));
      throw new IllegalArgumentException("Could not find constructor with parameter types [" + parameterTypesString + "] on target class [" + targetClass.getName() + "]");
    }

    TestUtils.makeAccessible(constructor);
    return runConstructor(constructor, initargs);
  }

  public static final <T> T invokeMethod(Object targetObject, String methodName, Object... initargs) {
    Preconditions.checkNotNull(targetObject, "Target object must not be null");

    Class<?>[] parameterTypes = new Class<?>[initargs.length];
    for (int i = 0; i < initargs.length; i++) {
      parameterTypes[i] = initargs[i].getClass();
    }
    Method method = TestUtils.findMethod(targetObject.getClass(), methodName, parameterTypes);
    if (method == null) {
      String parameterTypesString = Joiner.on(",").join(Lists.transform(Arrays.asList(parameterTypes), new Function<Class<?>, String>() {
        @Override
        public String apply(Class<?> input) {
          return input.getName();
        }
      }));
      throw new IllegalArgumentException("Could not find method with parameter types [" + parameterTypesString + "] on target class [" + targetObject.getClass().getName() + "]");
    }

    TestUtils.makeAccessible(method);
    return runMethod(targetObject, method, initargs);
  }

  public static int generateInt(int maxNumber) {
    Random randomGenerator = new Random(new Date().getTime());
    return randomGenerator.nextInt(maxNumber);
  }

  private static void setField(Field field, Object target, Object value) {
    try {
      field.set(target, value);
    }
    catch (IllegalAccessException ex) {
      throw new IllegalStateException("Unexpected reflection exception - " + ex.getClass().getName() + ": "
          + ex.getMessage());
    }
  }

  private static Object getField(Field field, Object target) {
    try {
      return field.get(target);
    }
    catch (IllegalAccessException ex) {
      throw new IllegalStateException("Unexpected reflection exception - " + ex.getClass().getName() + ": "
          + ex.getMessage());
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> T runConstructor(Constructor<?> constructor, Object... initargs) {
    try {
      return (T) constructor.newInstance(initargs);
    }
    catch (Exception ex) {
      throw new IllegalStateException("Unexpected reflection exception - " + ex.getClass().getName() + ": "
          + ex.getMessage());
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> T runMethod(Object targetObject, Method method, Object... initargs) {
    try {
      return (T) method.invoke(targetObject, initargs);
    }
    catch (Exception ex) {
      throw new IllegalStateException("Unexpected reflection exception - " + ex.getClass().getName() + ": "
          + ex.getMessage());
    }
  }

  private static void makeAccessible(AccessibleObject object) {
    if (object instanceof Field) {
      Field field = (Field) object;
      if ((!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers()) ||
          Modifier.isFinal(field.getModifiers())) && !field.isAccessible()) {
        field.setAccessible(true);
      }
    }
    else if (object instanceof Constructor<?>) {
      Constructor<?> constructor = (Constructor<?>) object;
      if ((!Modifier.isPublic(constructor.getModifiers()) || !Modifier.isPublic(constructor.getDeclaringClass().getModifiers()) ||
          Modifier.isFinal(constructor.getModifiers())) && !constructor.isAccessible()) {
        constructor.setAccessible(true);
      }
    }
    else if (object instanceof Method) {
      Method method = (Method) object;
      if ((!Modifier.isPublic(method.getModifiers()) || !Modifier.isPublic(method.getDeclaringClass().getModifiers()) ||
          Modifier.isFinal(method.getModifiers())) && !method.isAccessible()) {
        method.setAccessible(true);
      }
    }
  }

  private static Field findField(Class<? extends Object> clazz, String name) {
    Preconditions.checkNotNull(clazz, "Class must not be null");
    Preconditions.checkNotNull(name, "Name of the field must be specified");
    Class<?> searchType = clazz;
    while (!Object.class.equals(searchType) && searchType != null) {
      Field[] fields = searchType.getDeclaredFields();
      for (Field field : fields) {
        if (name == null || name.equals(field.getName())) {
          return field;
        }
      }
      searchType = searchType.getSuperclass();
    }
    return null;
  }

  private static Constructor<?> findConstructor(Class<? extends Object> clazz, Class<?>... parameterTypes) {
    checkNotNull(clazz, "Class must not be null");
    checkNotNull(parameterTypes, "Parameter types must not be null");

    Class<?> searchType = clazz;
    while (!Object.class.equals(searchType) && searchType != null) {
      Constructor<?>[] constructors = searchType.getDeclaredConstructors();
      for (Constructor<?> constructor : constructors) {
        if (parameterTypesEquals(constructor.getParameterTypes(), parameterTypes)) {
          return constructor;
        }

      }
      searchType = searchType.getSuperclass();
    }
    return null;
  }

  private static Method findMethod(Class<? extends Object> clazz, String methodName, Class<?>... parameterTypes) {
    checkNotNull(clazz, "Class must not be null");
    checkNotNull(parameterTypes, "Parameter types must not be null");

    Class<?> searchType = clazz;
    while (!Object.class.equals(searchType) && searchType != null) {
      Method[] methods = searchType.getDeclaredMethods();
      for (Method method : methods) {
        if (method.getName().equals(methodName) && parameterTypesEquals(method.getParameterTypes(), parameterTypes)) {
          return method;
        }

      }
      searchType = searchType.getSuperclass();
    }
    return null;
  }

  private static boolean parameterTypesEquals(Class<?>[] types1, Class<?>[] types2) {
    if (types1.length != types2.length) {
      return false;
    }
    for (int i = 0; i < types1.length; i++) {
      if (!types1[i].isAssignableFrom(types2[i])) {
        return false;
      }
    }

    return true;
  }

}
