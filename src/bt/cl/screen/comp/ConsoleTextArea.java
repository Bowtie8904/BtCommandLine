package bt.cl.screen.comp;

import bt.cl.screen.link.Hyperlink;
import bt.cl.screen.link.HyperlinkOps;
import bt.utils.Exceptions;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.TextExt;
import org.fxmisc.richtext.model.SegmentOps;
import org.fxmisc.richtext.model.TextOps;
import org.reactfx.util.Either;

import java.awt.*;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

public class ConsoleTextArea extends GenericStyledArea<Void, Either<String, Hyperlink>, Collection<String>>
{
    private static final TextOps<String, Collection<String>> STYLED_TEXT_OPS = SegmentOps.styledTextOps();
    private static final HyperlinkOps<Collection<String>> HYPERLINK_OPS = new HyperlinkOps<>();
    private static final TextOps<Either<String, Hyperlink>, Collection<String>> EITHER_OPS = STYLED_TEXT_OPS._or(HYPERLINK_OPS, (s1, s2) -> Optional.empty());

    {
        setUseInitialStyleForInsertion(true);
    }

    public ConsoleTextArea()
    {
        super(null,
              (t, p) -> {
              },
              Collections.<String>emptyList(),
              EITHER_OPS,
              e -> e.getSegment().unify(
                      text ->
                              createStyledTextNode(t -> {
                                  t.setText(text);
                                  t.getStyleClass().addAll(e.getStyle());
                              }),
                      hyperlink ->
                              createStyledTextNode(t -> {
                                  if (hyperlink.isReal())
                                  {
                                      t.setText(hyperlink.getDisplayedText());
                                      t.getStyleClass().addAll(e.getStyle());

                                      t.setOnMouseClicked(ae -> openLink(hyperlink.getLink()));
                                      t.setOnMouseEntered(me -> t.setCursor(Cursor.HAND));
                                      t.setOnMouseExited(me -> t.setCursor(Cursor.DEFAULT));
                                  }
                              })
              )
        );
    }

    public static void openLink(String link)
    {
        Exceptions.uncheck(() -> Desktop.getDesktop().browse(new URI(link)));
    }

    public static TextExt createStyledTextNode(Consumer<TextExt> applySegment)
    {
        TextExt t = new TextExt();
        t.setTextOrigin(VPos.TOP);
        applySegment.accept(t);
        return t;
    }
}