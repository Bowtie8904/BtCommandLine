package bt.cl.screen;

import bt.cl.css.CssClasses;
import bt.cl.css.CssLoader;
import bt.cl.text.TextParser;
import bt.gui.fx.core.FxScreen;
import bt.gui.fx.core.annot.FxmlElement;
import bt.gui.fx.core.annot.css.FxStyleClass;
import bt.gui.fx.core.annot.handl.FxHandler;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnKeyReleased;
import bt.gui.fx.core.annot.setup.FxSetup;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import java.net.MalformedURLException;
import java.util.List;

@FxStyleClass(CssClasses.class)
public class MainScreen extends FxScreen
{
    @FxmlElement
    private BorderPane basePane;

    @FxmlElement
    @FxSetup(css = CssClasses.INPUT_TEXT_FIELD)
    @FxHandler(type = FxOnKeyReleased.class, method = "onTextEnter")
    private TextField inputTextField;

    @FxSetup(css = CssClasses.TEXT_AREA)
    private CodeArea textArea;

    @Override
    protected void prepareScreen()
    {
        this.textArea = new CodeArea();
        this.textArea.setEditable(false);
        this.basePane.setCenter(new StackPane(new VirtualizedScrollPane<>(this.textArea, ScrollPane.ScrollBarPolicy.NEVER, ScrollPane.ScrollBarPolicy.NEVER)));
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
    }

    private void onTextEnter(KeyEvent e)
    {
        if (e.getCode() == KeyCode.ENTER)
        {
            //this.textArea.append(this.inputTextField.getText() + "\n", List.of("green"));

            var node = new TextParser().parse(this.inputTextField.getText());

            node.apply(this.textArea);

            this.inputTextField.setText("");
        }
    }
}