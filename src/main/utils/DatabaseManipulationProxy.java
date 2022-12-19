package main.utils;

import main.utils.annotations.Aggregated;
import main.utils.annotations.Multiple;
import main.utils.annotations.Update;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author yjt 动态代理
 */
public class DatabaseManipulationProxy implements InvocationHandler {

    private Object target;

    public DatabaseManipulationProxy() {
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("Proxy handled");
        var returnType = method.getReturnType();
        if (method.isAnnotationPresent(Update.class)){
            System.out.println("Execute update...");
            Update update = method.getAnnotation(Update.class);
            String sql = update.sql();
            return SqlFactory.handleUpdate(sql,args);
        }else if(method.isAnnotationPresent(Aggregated.class)){
            System.out.println("Execute aggregated-result query...");
            Aggregated aggregate = method.getAnnotation(Aggregated.class);
            String sql = aggregate.sql();
            return SqlFactory.singleSelect(SqlFactory.handleQuery(sql,args),returnType);
        }else if(method.isAnnotationPresent(Multiple.class)){
            System.out.println("Execute multiple-result query...");
//            Multiple multiple = methodProxied.getAnnotation(Multiple.class);
//            String sql = multiple.sql();
//            return SqlFactory.handleQuery(sql, args);
        }
        else {
            System.out.println("No annotation found...");
        }
        try {
            return method.invoke(target, args);
        } catch (Exception e) {
            System.out.println("Exception found!");
            System.out.println(e.getMessage());
            //do something ...
            return null;
        }
    }


    public Object newProxiedObj() {
        return  Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), this);
    }
}
