public class ConfigUtils {
    public String getBool(String k, String defVal) {
        return "";
    }

    public String getString(String k, String defVal) {
        return "";
    }

    public String getInt(String k, String defVal) {
        return "";
    }

    public static final String k = "fafs";

    public static void main(String[] args) {
        ConfigUtils configUtils = new ConfigUtils();
        ConfigUtils configHolder = new ConfigUtils();
        ConfigUtils configManager = new ConfigUtils();

        configUtils.getBool(k, "test1111111111111111111111");
        configHolder.getString(k, "test1111111111111111111111");
        configManager.getInt(k, "test1111111111111111111111");

    }
}
