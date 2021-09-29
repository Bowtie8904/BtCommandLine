package bt.cl.screen;

import bt.gui.fx.core.FxScreenManager;

public class ScreenManager extends FxScreenManager
{
    @Override
    protected void loadScreens()
    {
        addScreens(MainScreen.class);
    }

    @Override
    protected void startApplication()
    {
        setScreen(MainScreen.class);
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}