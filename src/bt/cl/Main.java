package bt.cl;

import bt.cl.screen.ScreenManager;
import bt.console.output.styled.Style;

public class Main
{
    public static void main(String[] args)
    {
        Style.setEnabled(true);
        ScreenManager.main(args);
    }
}