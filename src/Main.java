import POJO.LogInfo;
import service.CompanyManagerService;
import service.SustcManagerService;
import utils.JdbcUtil;


public class Main {
    public static void main(String[] args) {
        JdbcUtil.getConnection();
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
