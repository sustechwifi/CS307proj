package main.utils;

import main.interfaces.IDatabaseManipulation;
import main.utils.annotations.Aggregated;
import main.utils.annotations.Multiple;
import main.utils.annotations.Update;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author yjt
 * @param <T>
 */
public class DatabaseManipulationProxy<T extends IDatabaseManipulation> implements InvocationHandler {

    private final T target;

    public DatabaseManipulationProxy(T target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("Proxy handled");
        var methodProxied = target.getClass().getMethod(method.getName(),method.getParameterTypes());
        if (methodProxied.isAnnotationPresent(Update.class)){
            System.out.println("Execute update...");
//            Update update = methodProxied.getAnnotation(Update.class);
//            String sql = update.sql();
//            return SqlFactory.handleUpdate(sql,args);
        }else if(methodProxied.isAnnotationPresent(Aggregated.class)){
            System.out.println("Execute aggregated-result query...");
//            Aggregated aggregate = methodProxied.getAnnotation(Aggregated.class);
//            String sql = aggregate.sql();
//            return SqlFactory.handleQuery(sql,args);
        }else if(methodProxied.isAnnotationPresent(Multiple.class)){
            System.out.println("Execute multiple-result query...");
//            Multiple multiple = methodProxied.getAnnotation(Multiple.class);
//            String sql = multiple.sql();
//            return SqlFactory.handleQuery(sql, args);
        }
        else {
            System.out.println("No annotation found...");
            //return method.invoke(target, args);
        }
        return method.invoke(target, args);
    }

    public static <O extends IDatabaseManipulation> IDatabaseManipulation newProxy(O target){
        var fac = new DatabaseManipulationProxy<>(target);
        return (IDatabaseManipulation)Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), fac);
    }

    public IDatabaseManipulation newProxiedObj() {
        return  (IDatabaseManipulation)Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), this);
    }
}
