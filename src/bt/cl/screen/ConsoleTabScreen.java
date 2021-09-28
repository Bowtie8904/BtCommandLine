package bt.cl.screen;

import bt.cl.css.CssClasses;
import bt.cl.css.CssLoader;
import bt.cl.screen.comp.ConsoleTextArea;
import bt.console.output.styled.StyledTextNode;
import bt.console.output.styled.StyledTextParser;
import bt.gui.fx.core.FxScreen;
import bt.gui.fx.core.annot.FxmlElement;
import bt.gui.fx.core.annot.css.FxStyleClass;
import bt.gui.fx.core.annot.handl.FxHandler;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnKeyReleased;
import bt.gui.fx.core.annot.setup.FxSetup;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

@FxStyleClass(CssClasses.class)
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

    private List<String> history;
    private int historyIndex = -1;

    private Tab tab;

    public void setTab(Tab tab)
    {
        this.tab = tab;
    }

    @Override
    protected void prepareScreen()
    {
        this.history = new ArrayList<>(50);
        this.textArea = new ConsoleTextArea();
        this.textArea.setEditable(false);
        this.basePane.setCenter(new StackPane(new VirtualizedScrollPane<>(this.textArea, ScrollPane.ScrollBarPolicy.NEVER, ScrollPane.ScrollBarPolicy.NEVER)));
        this.textArea.requestFocus();
    }

    @Override
    protected void prepareStage(Stage stage)
    {

    }

    @Override
    protected void prepareScene(Scene scene)
    {
        try
        {
            new CssLoader(scene).loadCssFiles();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        loadCssClasses();
        setupFields();
        populateFxHandlers();
    }

    private void addHistory(String text)
    {
        if (this.history.size() >= 49 && !this.history.contains(text))
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

    protected void apply(StyledTextNode node)
    {
        List<String> allStyles = node.getStyles();

        if (allStyles.isEmpty())
        {
            allStyles.add(CssClasses.DEFAULT_TEXT);
        }

        this.textArea.append(node.getText(), allStyles);

        for (var child : node.getChildren())
        {
            apply(child);
        }
    }

    @Override
    public void kill()
    {

    }
}