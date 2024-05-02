package examples;

public class NativeLibraryLoader {
    public static void loadLibrary() {
        String projectRoot = System.getProperty("user.dir");
        String dllPath = projectRoot + "/libs/liblsl64.dll";
        //String dllPath = projectRoot + "/libs/lsl.dll";
        System.load(dllPath);
    }
}
