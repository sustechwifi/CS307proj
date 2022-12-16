package utils.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;


/**
 * @author yjt
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SqlSupport {
    String DRIVER () default  "org.postgresql.Driver";
    String USERNAME () default "postgres";
    String PASSWORD () default "20030118";
    String URL () default "jdbc:postgresql://127.0.0.1:5432/sustc2";
    String [] otherConfigs() default {
        "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO sustcmanager;",
        "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO seaportofficer;",
        "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO courier;",
        "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO companymanager;",
            "grant select on sustc2.public.undertake, sustc2.public.city, sustc2.public.company,sustc2.public.container,sustc2.public.staff,sustc2.public.ship,sustc2.public.record to sustcmanager,courier,seaportofficer,companymanager;",
            "grant insert on sustc2.public.undertake, sustc2.public.record to courier;",
            "grant update on sustc2.public.record , sustc2.public.undertake to seaportofficer, courier;",
            "grant update on sustc2.public.record,sustc2.public.container,sustc2.public.ship to companymanager;"
    };
}
