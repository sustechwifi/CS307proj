import POJO.LogInfo;
import service.CompanyManagerService;
import service.SustcManagerService;
import utils.JdbcUtil;
import utils.annotations.SqlSupport;


@SqlSupport(
        DRIVER = "org.postgresql.Driver",
        USERNAME = "postgres",
        PASSWORD = "20030118",
        URL = "jdbc:postgresql://127.0.0.1:5432/sustc2?useUnicode=true&characterEncoding=utf8&rewriteBatchedStatement=true"
)
public class Main {
    public static void main(String[] args) {
        JdbcUtil.getConnection(Main.class);
        CompanyManagerService company  = new CompanyManagerService();
        SustcManagerService sustcManagerService = new SustcManagerService();
        System.out.println(company.getImportTaxRate(
                new LogInfo("xxx",LogInfo.StaffType.CompanyManager,"xxx")
                ,"wuhan","cherry"));
        System.out.println(sustcManagerService.getCompanyCount(
                new LogInfo("xxx",LogInfo.StaffType.SustcManager,"xxx")
        ));

    }
}
