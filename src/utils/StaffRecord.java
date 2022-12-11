package utils;
public class StaffRecord {
    public String name;
    public String type;
    public String company;
    public String city;
    public String gender;
    public String age;
    public String phone;
    public String password;

    public StaffRecord(String record){
        String[] col = record.split(",");
        name = col[0];
        type = col[1];
        company = col[2];
        city = col[3];
        gender = col[4];
        age = col[5];
        phone = col[6];
        password = col[7];
    }
}
