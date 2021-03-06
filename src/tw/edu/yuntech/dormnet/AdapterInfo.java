package tw.edu.yuntech.dormnet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Created by Clode on 2017/3/29.
 */
public class AdapterInfo {


    public static ArrayList<String> getDisplayName() {
        return displayName;
    }

    public static String[] getDisplayNameArray() {
        return displayName.toArray(new String[displayName.size()]);
    }

    public static String getAdapterName(String disName) {
        for (int i = 0; i < name.size(); ++i) {
            if (disName.contains(displayName.get(i))) {
                return adapterName.get(i);
            }
        }
        return "null";
    }

    private static ArrayList<String> name = new ArrayList<>();
    private static ArrayList<String> displayName = new ArrayList<>();
    private static ArrayList<String> GUID = new ArrayList<>();
    private static ArrayList<String> adapterName = new ArrayList<>();

    static void captureAdapters(SystemInfo systemInfo) {
        name.clear();
        displayName.clear();
        GUID.clear();
        adapterName.clear();
        try {
            // Capture All Active Interface.
            Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface face = (NetworkInterface) interfaces.nextElement();
                // Exclude down card, loopback, can't multi-cast, and VM interface.
                if (face.isUp() && !face.isLoopback() && face.supportsMulticast() && !face.getDisplayName().contains("VM")) {
                    name.add(face.getName());
                    displayName.add(face.getDisplayName());
                    GUID.add("null");
                    adapterName.add("null");
                }
            }

            if (systemInfo.isWindows())
                windowsCapture();
            else if (systemInfo.isMac())
                macCapture();
            else if (systemInfo.isLinux())
                linuxCapture();

            for (int i = 0; i < name.size(); ++i) {
                System.out.println(name.get(i) + " + " + displayName.get(i) + " + " + GUID.get(i) + " + " + adapterName.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void windowsCapture() throws Exception {
        // Execute cmd only for windows user to capture interface's GUID
        Process p = Runtime.getRuntime().exec("cmd.exe /C reg query \"HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\NetworkCards\"");
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        ArrayList<String> tmp = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            if (line.equals("")) continue;
            tmp.add(line);
        }
        p.getInputStream().close();
        // Get every adapter name also through cmd.
        for (int i = 0; i < tmp.size(); ++i) {
            String reg = "HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control\\Network\\{4D36E972-E325-11CE-BFC1-08002BE10318}\\";
            String description = getRegValue(tmp.get(i), "Description");
            for (int j = 0; j < name.size(); ++j) {
                if (displayName.get(j).contains(description)) {
                    GUID.set(j, getRegValue(tmp.get(i), "ServiceName"));
                    adapterName.set(j, getRegValue(reg + GUID.get(j) + "\\Connection", "Name"));
                }
            }
        }

    }

    private static void macCapture() throws Exception {

    }

    private static void linuxCapture() throws Exception {

    }

    // Windows Only.
    static String getRegValue(String path, String key) throws Exception {
        Process p = Runtime.getRuntime().exec("cmd.exe /C reg query \"" + path + "\" /v \"" + key + "\"");
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), "MS950"));
        String line, tmp = "";
        while ((line = reader.readLine()) != null) {
            if (!line.contains(key)) continue;
            tmp = line;
        }
        p.getInputStream().close();
        tmp = tmp.split("REG_SZ")[1];
        while (tmp.startsWith(" "))
            tmp = tmp.replaceFirst(" ", "");
        return tmp;
    }

}