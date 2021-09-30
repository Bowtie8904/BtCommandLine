package bt.cl.screen;

import bt.cl.css.CssClasses;
import bt.cl.process.AttachedProcess;
import bt.cl.screen.comp.ConsoleTextArea;
import bt.console.input.ArgumentParser;
import bt.console.output.styled.Style;
import bt.console.output.styled.StyledTextNode;
import bt.console.output.styled.StyledTextParser;
import bt.gui.fx.core.FxScreen;
import bt.gui.fx.core.annot.FxmlElement;
import bt.gui.fx.core.annot.handl.FxHandler;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnKeyReleased;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnScroll;
import bt.gui.fx.core.annot.setup.FxSetup;
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
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualFlow;
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
    private ScrollPane scrollPane;

    @FxmlElement
    @FxSetup(css = CssClasses.INPUT_TEXT_FIELD)
    @FxHandler(type = FxOnKeyReleased.class, method = "onTextEnter")
    private TextField inputTextField;

    @FxSetup(css = CssClasses.TEXT_AREA)
    @FxHandler(type = FxOnScroll.class, method = "onScroll")
    private ConsoleTextArea textArea;

    private VirtualizedScrollPane virtualScrollPane;

    private int historySize = 50;
    private List<String> history;
    private int historyIndex = -1;

    private boolean autoScroll = true;
    private double lastScrollPosition;

    private Tab tab;
    private MainScreen mainScreen;
    private StyledTextParser parser;
    private AttachedProcess process;
    private int maxLines = 500;

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
        this.textArea = new ConsoleTextArea();
        this.textArea.setEditable(false);
        this.textArea.setPadding(new Insets(0, 0, 0, 5));
        this.virtualScrollPane =  new VirtualizedScrollPane(this.textArea);
        this.virtualScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.virtualScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.scrollPane.setContent(this.virtualScrollPane);
        this.textArea.requestFocus();
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
        if (this.history.size() >= this.historySize - 1 && !this.history.contains(text))
        {
            this.history.remove(this.history.size() - 1);
        }

        this.history.remove(text);
        this.history.add(0, text);
    }

    public void setHistoryEntryAsactive()
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
            var node = new StyledTextParser().parseNode(this.inputTextField.getText() + "\n");
            apply(node);

            if (this.process != null)
            {
                try
                {
                    this.process.write(this.inputTextField.getText() + "\n");
                }
                catch (IOException ex)
                {
                    printException(ex);
                }
            }
            else
            {
                setProcess(ArgumentParser.parseArguments(this.inputTextField.getText()));
            }

            addHistory(this.inputTextField.getText());
            this.inputTextField.setText("");
            this.historyIndex = -1;
        }
        else if (e.getCode() == KeyCode.UP)
        {
            this.historyIndex++;
            setHistoryEntryAsactive();
        }
        else if (e.getCode() == KeyCode.DOWN)
        {
            this.historyIndex--;
            setHistoryEntryAsactive();
        }
    }

    public void apply(StyledTextNode node)
    {
        List<String> allStyles = node.getStyles();

        if (allStyles.isEmpty())
        {
            allStyles.add(CssClasses.DEFAULT_TEXT);
        }

        appendText(node.getText(), allStyles);

        for (var child : node.getChildren())
        {
            apply(child);
        }
    }

    public void appendText(String text, String... styles)
    {
        appendText(text, List.of(styles));
    }

    public synchronized void appendText(String text, List<String> styles)
    {
        Platform.runLater(() -> {
            this.textArea.append(text, styles);

            while (this.textArea.getParagraphs().size() > this.maxLines)
            {
                this.textArea.replaceText(0, 0, 1, 0, "");
            }

            if (this.autoScroll)
            {
                this.textArea.scrollYBy(Double.MAX_VALUE);
            }
        });
    }

    public void parseAndAppendText(String text)
    {
        apply(this.parser.parseNode(text));
    }

    public void onScroll(ScrollEvent e)
    {
        if (!(e.getTarget() instanceof VirtualFlow))
        {
            this.autoScroll = !e.isShiftDown() && e.getDeltaY() < 0 && this.lastScrollPosition == this.textArea.getEstimatedScrollY();
            this.lastScrollPosition = this.textArea.getEstimatedScrollY();
        }
    }

    public void scrollToEnd()
    {
        this.textArea.scrollYBy(Double.MAX_VALUE);
        this.autoScroll = true;
    }

    public void onSelect()
    {
        this.inputTextField.requestFocus();
    }

    public void setProcess(String... args)
    {
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
                                                     Style.apply(Arrays.toString(this.process.getArgs()), "magenta")+ "\n"));

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
                }
            }, this.process.getExecutable());
        }
        catch (Exception e)
        {
            parseAndAppendText(Style.apply(e.getMessage(), "red", "bold"));
        }
    }

    protected String styleExitStatus(int status)
    {
        String[] styles = null;

        if (status < 0)
        {
            styles = new String[]{"red", "bold"};
        }
        else if (status >= 0)
        {
            styles = new String[]{"green", "bold"};
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
        if (!InstanceKiller.isActive())
        {
            InstanceKiller.unregister(this);
        }

        Null.checkKill(this.process);
    }
}