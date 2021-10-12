package bt.cl.screen;

import bt.cl.css.CssClasses;
import bt.cl.css.CssLoader;
import bt.gui.fx.core.FxScreen;
import bt.gui.fx.core.annot.FxmlElement;
import bt.gui.fx.core.annot.css.FxStyleClass;
import bt.gui.fx.core.annot.handl.FxHandler;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnAction;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnMouseEntered;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnMouseExited;
import bt.gui.fx.core.exc.FxException;
import bt.gui.fx.util.ButtonHandling;
import bt.utils.Null;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

@FxStyleClass(CssClasses.class)
public class MainScreen extends FxScreen
{
    @FxmlElement
    private TabPane tabPane;

    @FxmlElement
    @FxHandler(type = FxOnAction.class, method = "addTab", withParameters = false)
    @FxHandler(type = FxOnMouseEntered.class, methodClass = ButtonHandling.class, method = "onMouseEnter", withParameters = false, passField = true)
    @FxHandler(type = FxOnMouseExited.class, methodClass = ButtonHandling.class, method = "onMouseExit", withParameters = false, passField = true)
    private Button addButton;

    @FxmlElement
    @FxHandler(type = FxOnAction.class, method = "scrollDown", withParameters = false)
    @FxHandler(type = FxOnMouseEntered.class, methodClass = ButtonHandling.class, method = "onMouseEnter", withParameters = false, passField = true)
    @FxHandler(type = FxOnMouseExited.class, methodClass = ButtonHandling.class, method = "onMouseExit", withParameters = false, passField = true)
    private Button scrollDownButton;

    private ConsoleTabScreen activeTab;

    public void addTab()
    {
        addTab(new String[0]);
    }

    public void addTab(String[] args)
    {
        Tab tab = new Tab();
        this.tabPane.getTabs().add(tab);

        ConsoleTabScreen screen = constructScreenInstance(ConsoleTabScreen.class);
        screen.setScreenManager(this.screenManager);
        screen.setTab(tab);
        tab.setOnSelectionChanged(e ->
                                  {
                                      if (tab.isSelected())
                                      {
                                          setActiveTab(screen);
                                          screen.onSelect();
                                      }
                                  });

        tab.setOnClosed(e -> {
            screen.kill();
            setActiveTab(null);
        });

        Parent root = screen.load();
        screen.setStage(this.stage);
        screen.prepareStage(this.stage);
        screen.setScene(this.scene);
        screen.prepareScene(this.scene);
        screen.setMainScreen(this);

        tab.setContent(root);
        tab.setText("New tab");

        this.tabPane.getSelectionModel().select(tab);

        if (args.length > 0)
        {
            screen.setProcess(args);
        }
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

    @Override
    protected void loadCssClasses()
    {
        super.loadCssClasses();

        try
        {
            new CssLoader(scene).loadCssFiles();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
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

    public void setActiveTab(ConsoleTabScreen activeTab)
    {
        this.activeTab = activeTab;
    }

    public void scrollDown()
    {
        Null.checkRun(this.activeTab, () -> this.activeTab.scrollToEnd());
    }
}