package bt.cl;

import bt.cl.screen.ScreenManager;
import bt.console.output.styled.Style;
import bt.log.ConsoleLoggerHandler;
import bt.log.FileLoggerHandler;
import bt.log.Log;

import java.io.IOException;
import java.util.logging.Level;

public class Main
{
    public static void main(String[] args)
    {
        Style.setEnabled(true);
        Log.createDefaultLogFolder();

        try
        {
            Log.configureDefaultJDKLogger(Level.FINER, new FileLoggerHandler(FileLoggerHandler.DEFAULT_FILE_PATTERN, 1000000, 5), new ConsoleLoggerHandler());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        ScreenManager.main(args);
    }
}