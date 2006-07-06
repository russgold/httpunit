package com.meterware.httpunit.dom;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.FunctionObject;
import org.w3c.dom.Node;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;

/**
 * Utilities to support scripting.
 */
class ScriptingSupport {

    /** A non-null method value to be used to indicate that we have already looked up and failed to find one. **/
    private static final Method NO_SUCH_PROPERTY = ScriptingSupport.class.getDeclaredMethods()[0];
    
    private static final Object[] NO_ARGS = new Object[0];

    /** map of classes to maps of string to function objects. **/
    private static Hashtable _classFunctionMaps = new Hashtable();

    /** map of classes to maps of string to getter methods. **/
    private static Hashtable _classGetterMaps = new Hashtable();

    /** map of classes to maps of string to setter methods. **/
    private static Hashtable _classSetterMaps = new Hashtable();


    static Object getNamedProperty( Object element, String javaPropertyName, Scriptable scriptable ) {
        Method getter = getPropertyGetter( element.getClass(), javaPropertyName );
        if (getter == NO_SUCH_PROPERTY) {
            Object function = getFunctionObject( element.getClass(), javaPropertyName, scriptable );
            return function == null ? Scriptable.NOT_FOUND : function;
        }
        try {
            return getter.invoke( element, NO_ARGS );
        } catch (IllegalAccessException e) {
            return Scriptable.NOT_FOUND;
        } catch (InvocationTargetException e) {
            return Scriptable.NOT_FOUND;
        }
    }

    private static FunctionObject getFunctionObject( Class aClass, String methodName, Scriptable scriptable ) {
        Hashtable functionMap = (Hashtable) _classFunctionMaps.get( aClass );
        if (functionMap == null) {
            _classFunctionMaps.put( aClass, functionMap = new Hashtable() );
        }

        Object result = functionMap.get( methodName );
        if (result == NO_SUCH_PROPERTY) return null;
        if (result != null) return (FunctionObject) result;

        Method[] methods = aClass.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equalsIgnoreCase( methodName )) {
                FunctionObject function = new FunctionObject( methodName, method, scriptable );
                functionMap.put( methodName, function );
                return function;
            }
        }
        functionMap.put( methodName, NO_SUCH_PROPERTY );
        return null;
    }


    private static Method getPropertyGetter( Class aClass, String propertyName ) {
        Hashtable methodMap = (Hashtable) _classGetterMaps.get( aClass );
        if (methodMap == null) {
            _classGetterMaps.put( aClass, methodMap = new Hashtable() );
        }

        Method result = (Method) methodMap.get( propertyName );
        if (result != null) return result;

        Method[] methods = aClass.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getParameterTypes().length > 0) continue;
            if (method.getName().equalsIgnoreCase( "is" + propertyName ) || method.getName().equalsIgnoreCase( "get" + propertyName )) {
                methodMap.put( propertyName, method );
                return method;
            }
        }
        methodMap.put( propertyName, NO_SUCH_PROPERTY );
        return NO_SUCH_PROPERTY;
    }


    static void setNamedProperty( NodeImpl element, String javaPropertyName, Object value ) {
        Method setter = getPropertySetter( element.getClass(), javaPropertyName, value );
        if (setter == NO_SUCH_PROPERTY) return;

        try {
            setter.invoke( element, new Object[] { value } );
        } catch (IllegalAccessException e) { /* do nothing */
        } catch (InvocationTargetException e) { /* do nothing */
        }
    }


    static Method getPropertySetter( Class aClass, String propertyName, Object value ) {
        Hashtable methodMap = (Hashtable) _classSetterMaps.get( aClass );
        if (methodMap == null) {
            _classSetterMaps.put( aClass, methodMap = new Hashtable() );
        }

        Method result = (Method) methodMap.get( propertyName );
        if (result != null) return result;

        String setterName = "set" + Character.toUpperCase( propertyName.charAt( 0 ) ) + propertyName.substring(1);
        Method[] methods = aClass.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equalsIgnoreCase( setterName ) &&
                    method.getParameterTypes().length == 1 &&
                    isConvertableTo( value.getClass(), method.getParameterTypes()[0] )) {
                methodMap.put( propertyName, method );
                return method;
            }
        }
        methodMap.put( propertyName, NO_SUCH_PROPERTY );
        return NO_SUCH_PROPERTY;
    }


    static Object evaluateExpression( Node node, String expression ) throws JavaScriptException {
        try {
            Context context = Context.enter();
            context.initStandardObjects( null );
            return Context.getCurrentContext().evaluateString( (Scriptable) node, expression, "httpunit", 0, null );
        } finally {
            Context.exit();
        }
    }

    public static boolean isConvertableTo( Class valueType, Class parameterType ) {
        if (valueType.equals( parameterType )) return true;
        if (parameterType.equals( String.class )) return true;
        if (valueType.equals( String.class ) && isNumericParameter( parameterType )) return true;
        if (Number.class.isAssignableFrom( valueType ) && isNumericParameter( parameterType )) return true;
        if (valueType.equals( String.class ) && parameterType.equals( Boolean.class )) return true;
        return false;
    }

    private static boolean isNumericParameter( Class parameterType ) {
        if (parameterType.isPrimitive() && !(parameterType.equals( boolean.class ))) return true;
        return Number.class.isAssignableFrom( parameterType );
    }
}
