import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.FileNotFoundException;

public class BatteryTrayPercent {

    private static TrayIcon trayIcon;
    private static Font font;
    public static void main(String[] args) throws Exception {

        SystemTray tray = SystemTray.getSystemTray();
        String path = "roboto.ttf";
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, new File(path)).deriveFont(20f);
        }
        catch (FileNotFoundException e) {
            System.out.println("Font file not found: " + path);
        }
        

        trayIcon = new TrayIcon(makeTextIcon(""), "Battery");
        trayIcon.setImageAutoSize(true);

        PopupMenu menu = new PopupMenu();
        MenuItem exit = new MenuItem("Exit");
        exit.addActionListener(e -> {
            tray.remove(trayIcon);
            System.exit(0);
        });
        menu.add(exit);
        trayIcon.setPopupMenu(menu);

        tray.add(trayIcon);

        // update every 15 seconds
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

        exec.scheduleAtFixedRate(() -> {
            int percent = getBatteryPercent();
            trayIcon.setToolTip("Battery: " + percent);
            trayIcon.setImage(makeTextIcon(String.valueOf(percent)));
        }, 0, 15, TimeUnit.SECONDS);
    }

    // Calls Windows API
    private static int getBatteryPercent() {
        Kernel32.SYSTEM_POWER_STATUS status = new Kernel32.SYSTEM_POWER_STATUS();
        Kernel32.INSTANCE.GetSystemPowerStatus(status);

        int val = Byte.toUnsignedInt(status.BatteryLifePercent); // use unsigned value
        if (val < 0 || val > 100) val = 0;
        if (val == 100) val = 99; // to fit in icon
        return val;
    }

    // draw to tray icon
    private static Image makeTextIcon(String text) {
        int size = 24;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        g.setFont(font);

        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();

        // center text
        int x = (size - textWidth) / 2 - 1;
        int y = (size + textHeight) / 2 - 2;

        // print text
        g.setColor(Color.WHITE);
        g.drawString(text, x, y);

        g.dispose();
        return img;
    }

}

interface Kernel32 extends Library {
    Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);

    class SYSTEM_POWER_STATUS extends Structure {
        public byte ACLineStatus;
        public byte BatteryFlag;
        public byte BatteryLifePercent;
        public byte Reserved1;
        public int BatteryLifeTime;
        public int BatteryFullLifeTime;

        @Override
        protected java.util.List<String> getFieldOrder() {
            return java.util.Arrays.asList(
                "ACLineStatus",
                "BatteryFlag",
                "BatteryLifePercent",
                "Reserved1",
                "BatteryLifeTime",
                "BatteryFullLifeTime"
            );
        }
    }

    boolean GetSystemPowerStatus(SYSTEM_POWER_STATUS result);
}
