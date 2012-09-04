import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

/** @author Krustnic */
public class DesktopMain {
    public static void main(String[] args) {
        //new LwjglApplication(new Game(), "LibGDX horizonal sliding menu", 1280, 800, false);
        //new LwjglApplication(new Game(), "LibGDX horizonal sliding menu", 800, 480, false);
        new LwjglApplication(new Game(), "LibGDX: HorizonalSlidingPane", 1024, 600, false);
    }
}
