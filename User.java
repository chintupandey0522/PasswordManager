public class User {
    private String name;
    private String pan;
    private String dob; // format: DD/MM/YYYY

    public User(String name, String pan, String dob) {
        this.name = name;
        this.pan = pan.toUpperCase();
        this.dob = dob;
    }

    public String getName() { return name; }
    public String getPan()  { return pan; }
    public String getDob()  { return dob; }
}
