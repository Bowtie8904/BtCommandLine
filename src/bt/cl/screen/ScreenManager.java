package bt.cl.screen;

import bt.gui.fx.core.FxScreenManager;

public class ScreenManager extends FxScreenManager
{
    private static String[] args;

    @Override
    protected void loadScreens()
    {
        addScreens(MainScreen.class);
    }

    @Override
    protected void startApplication()
    {
        setScreen(MainScreen.class);
        var screen = getScreen(MainScreen.class);
        screen.addTab(args);
    }

    public static void main(String[] args)
    {
        ScreenManager.args = args;
        launch(args);
    }
}