package bt.cl.screen;

import bt.cl.css.CssClasses;
import bt.cl.screen.comp.ConsoleTextArea;
import bt.console.output.styled.StyledTextNode;
import bt.console.output.styled.StyledTextParser;
import bt.gui.fx.core.FxScreen;
import bt.gui.fx.core.annot.FxmlElement;
import bt.gui.fx.core.annot.handl.FxHandler;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnKeyReleased;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnScroll;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnScrollFinished;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnScrollStarted;
import bt.gui.fx.core.annot.setup.FxSetup;
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
import org.fxmisc.richtext.Caret;

import java.util.ArrayList;
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

    public void setTab(Tab tab)
    {
        this.tab = tab;
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
        this.textArea.setShowCaret(Caret.CaretVisibility.ON);
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

    public void afterSetup()
    {
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

    public void appendText(String text, List<String> styles)
    {
        this.textArea.append(text, styles);

        if (this.autoScroll)
        {
            this.textArea.scrollYBy(Double.MAX_VALUE);
        }
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

    @Override
    public void kill()
    {

    }
}