package bt.cl.screen;

import bt.gui.fx.core.FxScreenManager;
import bt.log.Log;

public class ScreenManager extends FxScreenManager
{
    private static String[] args;

    public static void main(String[] args)
    {
        ScreenManager.args = args;
        launch(args);
    }

    @Override
    protected void loadScreens()
    {
        Log.entry();

        addScreens(MainScreen.class);

        Log.exit();
    }

    @Override
    protected void startApplication()
    {
        Log.entry();

        setScreen(MainScreen.class);
        var screen = getScreen(MainScreen.class);
        screen.addTab(args);

        Log.exit();
    }
}