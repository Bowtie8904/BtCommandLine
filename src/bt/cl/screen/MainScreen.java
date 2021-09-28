package bt.cl.screen;

import bt.gui.fx.core.FxScreen;
import bt.gui.fx.core.annot.FxmlElement;
import bt.gui.fx.core.annot.handl.FxHandler;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnAction;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnMouseEntered;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnMouseExited;
import bt.gui.fx.core.exc.FxException;
import bt.gui.fx.util.ButtonHandling;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class MainScreen extends FxScreen
{
    @FxmlElement
    private BorderPane basePane;

    @FxmlElement
    private TabPane tabPane;

    @FxmlElement
    @FxHandler(type = FxOnAction.class, method = "addTab", withParameters = false)
    @FxHandler(type = FxOnMouseEntered.class, methodClass = ButtonHandling.class, method = "onMouseEnter", withParameters = false, passField = true)
    @FxHandler(type = FxOnMouseExited.class, methodClass = ButtonHandling.class, method = "onMouseExit", withParameters = false, passField = true)
    private Button addButton;

    public void addTab()
    {
        ConsoleTabScreen screen = constructScreenInstance(ConsoleTabScreen.class);
        screen.setScreenManager(this.screenManager);
        Parent root = screen.load();
        screen.setStage(this.stage);
        screen.prepareStage(this.stage);
        //Scene scene = new Scene(root, screen.getWidth(), screen.getHeight());
        screen.setScene(scene);
        screen.prepareScene(scene);

        Tab tab = new Tab();
        this.tabPane.getTabs().add(tab);
        tab.setContent(root);
        screen.setTab(tab);
    }

    @Override
    protected void prepareScreen()
    {

    }

    @Override
    protected void prepareStage(Stage stage)
    {
        stage.setTitle("BtCommandLine");
        setIcons("/icon.png");
    }

    @Override
    protected void prepareScene(Scene scene)
    {

    }

    protected <T extends FxScreen> T constructScreenInstance(Class<T> screenType)
    {
        T screen = null;

        try
        {
            Constructor<T> construct = screenType.getConstructor();
            construct.setAccessible(true);
            screen = construct.newInstance();
        }
        catch (InstantiationException | IllegalAccessException
                | InvocationTargetException | SecurityException e1)
        {
            e1.printStackTrace();
        }
        catch (NoSuchMethodException noEx)
        {
            throw new FxException("Screen class must implement a constructor without arguments.");
        }

        return screen;
    }
}