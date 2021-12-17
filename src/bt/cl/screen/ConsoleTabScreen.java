package bt.cl.screen;

import bt.cl.css.CssClasses;
import bt.cl.process.AttachedProcess;
import bt.cl.screen.comp.ConsoleTextArea;
import bt.console.input.ArgumentParser;
import bt.console.output.styled.Style;
import bt.console.output.styled.StyledTextParser;
import bt.gui.fx.core.FxScreen;
import bt.gui.fx.core.annot.FxmlElement;
import bt.gui.fx.core.annot.handl.FxHandler;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnKeyReleased;
import bt.gui.fx.core.annot.setup.FxSetup;
import bt.log.Log;
import bt.runtime.InstanceKiller;
import bt.scheduler.Threads;
import bt.utils.Null;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConsoleTabScreen extends FxScreen
{
    @FxmlElement
    private BorderPane basePane;

    @FxmlElement
    @FxSetup(css = CssClasses.INPUT_TEXT_FIELD)
    @FxHandler(type = FxOnKeyReleased.class, method = "onTextEnter")
    private TextField inputTextField;

    @FxSetup(css = CssClasses.TEXT_AREA)
    private ConsoleTextArea textArea;

    private VirtualizedScrollPane virtualScrollPane;

    private int historySize = 50;
    private List<String> history;
    private int historyIndex = -1;

    private Tab tab;
    private MainScreen mainScreen;
    private StyledTextParser parser;
    private AttachedProcess process;

    public ConsoleTabScreen()
    {
        this.parser = new StyledTextParser();
        InstanceKiller.killOnShutdown(this);
    }

    public void setTab(Tab tab)
    {
        this.tab = tab;
    }

    public void setMainScreen(MainScreen mainScreen)
    {
        this.mainScreen = mainScreen;
    }

    @Override
    protected void prepareScreen()
    {
        this.history = new ArrayList<>(this.historySize);
        this.textArea = new ConsoleTextArea(this);
        this.textArea.setEditable(false);
        this.textArea.setPadding(new Insets(0, 10, 10, 5));
        this.virtualScrollPane = new VirtualizedScrollPane(this.textArea);
        this.virtualScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.basePane.setCenter(new StackPane(this.virtualScrollPane));
        this.inputTextField.requestFocus();
    }

    @Override
    protected void prepareStage(Stage stage)
    {

    }

    @Override
    protected void prepareScene(Scene scene)
    {
        loadCssClasses();
        setupFields();
        populateFxHandlers();
    }

    private void addHistory(String text)
    {
        Log.entry(text);

        if (this.history.size() >= this.historySize - 1 && !this.history.contains(text))
        {
            this.history.remove(this.history.size() - 1);
        }

        this.history.remove(text);
        this.history.add(0, text);

        Log.exit();
    }

    public void setHistoryEntryAsActive()
    {
        if (!this.history.isEmpty() && this.historyIndex >= 0 && this.historyIndex < this.history.size())
        {
            this.inputTextField.setText(this.history.get(this.historyIndex));
            this.inputTextField.end();
        }
        else
        {
            this.historyIndex = -1;
        }
    }

    private void onTextEnter(KeyEvent e)
    {
        if (e.getCode() == KeyCode.ENTER)
        {
            Log.debug("User entered text into textfield '{}'", this.inputTextField.getText());

            var node = this.parser.parseNode(this.inputTextField.getText() + "\n");
            this.textArea.append(node);

            if (this.process != null)
            {
                try
                {
                    Log.debug("Sending user input to process '{}'", this.process.getExecutable());
                    this.process.write(this.inputTextField.getText() + "\n");
                }
                catch (IOException ex)
                {
                    printException(ex);
                    Log.error("Failed to write line to process", ex);
                }
            }
            else
            {
                Log.debug("No active process. Attempting to start new process");
                setProcess(ArgumentParser.parseArguments(this.inputTextField.getText()));
            }

            addHistory(this.inputTextField.getText());
            this.inputTextField.setText("");
            this.historyIndex = -1;
        }
        else if (e.getCode() == KeyCode.UP)
        {
            Log.debug("User pressed up");
            this.historyIndex++;
            setHistoryEntryAsActive();
        }
        else if (e.getCode() == KeyCode.DOWN)
        {
            Log.debug("User pressed down");
            this.historyIndex--;
            setHistoryEntryAsActive();
        }
    }

    public void parseAndAppendText(String text)
    {
        Log.entry(text);

        if (text.startsWith("<clear>"))
        {
            Log.debug("Received clear command. Clearing text area");
            this.textArea.clear();
            return;
        }

        var node = this.parser.parseNode(text, true);
        this.textArea.append(node);

        Log.exit();
    }

    public void setAutoScroll(boolean autoScroll)
    {
        this.textArea.setAutoScroll(autoScroll);
    }

    public void onSelect()
    {
        this.inputTextField.requestFocus();
    }

    public AttachedProcess getProcess()
    {
        return this.process;
    }

    public void setProcess(String... args)
    {
        Log.entry(args);

        try
        {
            this.process = new AttachedProcess(args);
            this.tab.setText(this.process.getExecutable());

            Threads.get().executeCachedDaemon(() -> {
                try
                {
                    String executableName = this.process.getExecutable();

                    parseAndAppendText(String.format(Style.apply("Starting process %s (%s) with arguments %s", "default_text"),
                                                     Style.apply(executableName, "yellow", "bold"),
                                                     Style.apply(this.process.getExecutablePath(), "green"),
                                                     Style.apply(Arrays.toString(this.process.getArgs()), "magenta") + "\n"));

                    this.process.setIncominTextConsumer(this::parseAndAppendText);
                    int exitStatus = this.process.start();

                    parseAndAppendText(String.format(Style.apply("Process finished with status %s", "default_text"),
                                                     styleExitStatus(exitStatus) + "\n"));

                    Platform.runLater(() -> this.tab.setText(executableName + " (killed)"));
                    this.process = null;
                }
                catch (Exception e)
                {
                    printException(e);
                    Log.error("Error during process", e);
                }
            }, this.process.getExecutable());
        }
        catch (IllegalArgumentException e)
        {
            parseAndAppendText(Style.apply(e.getMessage(), "red", "bold"));
        }
        catch (Exception e1)
        {
            Log.error("Failed to set process", e1);
        }

        Log.exit();
    }

    protected String styleExitStatus(int status)
    {
        String[] styles = null;

        if (status < 0)
        {
            styles = new String[] { "red", "bold" };
        }
        else if (status >= 0)
        {
            styles = new String[] { "green", "bold" };
        }

        return Style.apply(status + "", styles);
    }

    public void printException(Exception e)
    {
        parseAndAppendText(Style.apply(e));
    }

    @Override
    public void kill()
    {
        Log.entry();

        if (!InstanceKiller.isActive())
        {
            InstanceKiller.unregister(this);
        }

        Null.checkKill(this.process);
        Null.checkKill(this.textArea);

        Log.exit();
    }
}