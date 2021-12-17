package bt.cl.css;

import bt.log.Log;
import bt.utils.FileUtils;
import javafx.scene.Scene;

import java.io.File;
import java.net.MalformedURLException;

public class CssLoader
{
    private Scene scene;

    public CssLoader(Scene scene)
    {
        this.scene = scene;
    }

    public void loadCssFiles() throws MalformedURLException
    {
        Log.entry();

        File jarDirectory = FileUtils.getJarDirectory(CssLoader.class);
        File folder = new File(jarDirectory.getAbsolutePath() + "/css");

        if (!folder.exists())
        {
            folder.mkdirs();
            Log.info("Created CSS folder '" + folder.getAbsolutePath() + "'.");
        }

        for (File cssFile : FileUtils.getFiles(folder.getAbsolutePath(), "css"))
        {
            Log.info("Loading style class '" + cssFile.getAbsolutePath() + "'.");
            this.scene.getStylesheets().add(cssFile.toURI().toURL().toString());
        }

        Log.exit();
    }
}