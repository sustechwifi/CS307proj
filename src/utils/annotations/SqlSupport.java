package utils.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @author yjt
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SqlSupport {
    String DRIVER () default  "org.postgresql.Driver";
    String USERNAME () default "postgres";
    String PASSWORD () default "123456";
    String URL () default "jdbc:postgresql://127.0.0.1:5432/postgres";
}
