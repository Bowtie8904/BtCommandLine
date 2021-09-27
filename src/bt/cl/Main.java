package bt.cl;

import bt.cl.screen.ScreenManager;
import bt.log.Logger;

public class Main
{
    public static void main(String[] args)
    {
        Logger.global().setLogToFile(false);
        Logger.global().hookSystemErr();
        Logger.global().hookSystemOut();

        // hello<+bt blue>bla <+bt yellow>blau<-bt>end<-bt>hi
        ScreenManager.main(args);
    }
}