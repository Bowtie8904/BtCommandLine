package bt.cl.screen.comp;

import bt.cl.css.CssClasses;
import bt.cl.screen.obj.*;
import bt.console.output.styled.Style;
import bt.console.output.styled.StyledTextNode;
import bt.scheduler.Threads;
import bt.types.Killable;
import bt.utils.Exceptions;
import javafx.application.Platform;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.TextExt;
import org.fxmisc.richtext.model.SegmentOps;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.richtext.model.TextOps;
import org.fxmisc.undo.UndoManager;
import org.reactfx.util.Either;

import java.awt.*;
import java.net.URI;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsoleTextArea extends GenericStyledArea<Void, Either<String, Hyperlink>, Collection<String>> implements Killable
{
    private static final TextOps<String, Collection<String>> STYLED_TEXT_OPS = SegmentOps.styledTextOps();
    private static final HyperlinkOps<Collection<String>> HYPERLINK_OPS = new HyperlinkOps<>();
    private static final TextOps<Either<String, Hyperlink>, Collection<String>> EITHER_OPS = STYLED_TEXT_OPS._or(HYPERLINK_OPS, (s1, s2) -> Optional.empty());

    private Queue<ConsoleEntry> consoleQueue;
    private int maxParagraphs = 2000;
    private boolean autoScroll = true;
    private Pattern hyperlinkTextpattern = Pattern.compile(Style.HYPERLINK_STYLE + "\\((.*?)\\)");
    private ScheduledFuture queueFuture;

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

        setUndoManager(getNoOpUndoManager());
        setUseInitialStyleForInsertion(false);
        setAutoScrollOnDragDesired(false);
        this.consoleQueue = new ConcurrentLinkedQueue<>();
        this.queueFuture = Threads.get().scheduleAtFixedRateDaemon(this::appendFromQueue, 100, 100, TimeUnit.MILLISECONDS);
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

    protected UndoManager getNoOpUndoManager()
    {
        return new NoOpUndoManager();
    }

    public void append(StyledTextNode node)
    {
        ConsoleNode entry = new ConsoleNode(new StringBuilder(), new StyleSpansBuilder<>());
        appendNode(node, entry);

        if (!entry.getStringBuilder().isEmpty())
        {
            this.consoleQueue.add(new ConsoleEntry(Either.left(entry.getStringBuilder().toString()), entry.getStyleSpans().create()));
        }
    }

    protected java.util.List<String> getNodeStyles(StyledTextNode node)
    {
        java.util.List<String> allStyles = node.getStyles();

        if (allStyles.isEmpty())
        {
            allStyles.add(CssClasses.DEFAULT_TEXT);
        }

        return allStyles;
    }

    protected void appendNode(StyledTextNode node, ConsoleNode entry)
    {
        Hyperlink hyperlink = null;
        java.util.List<String> styles = getNodeStyles(node);

        String hyperlinkStyle = styles.stream()
                                      .filter(sty -> sty.startsWith(Style.HYPERLINK_STYLE))
                                      .findAny().orElse(null);

        if (hyperlinkStyle != null)
        {
            Matcher matcher = this.hyperlinkTextpattern.matcher(hyperlinkStyle);
            String link = "";

            if (matcher.find())
            {
                link = matcher.group(1);
            }

            // append existing text and recreate buffers
            if (!entry.getStringBuilder().isEmpty())
            {
                this.consoleQueue.add(new ConsoleEntry(Either.left(entry.getStringBuilder().toString()), entry.getStyleSpans().create()));
            }

            entry.setStringBuilder(new StringBuilder());
            entry.setStyleSpans(new StyleSpansBuilder<>());

            hyperlink = new Hyperlink(node.getText(), node.getText(), link.isBlank() ? node.getText() : link);
            this.consoleQueue.add(new ConsoleEntry(Either.right(hyperlink), null));
        }
        else
        {
            entry.getStringBuilder().append(node.getText());
            entry.getStyleSpans().add(styles, node.getText().length());
        }

        for (var child : node.getChildren())
        {
            appendNode(child, entry);
        }
    }

    protected void appendFromQueue()
    {
        ConsoleEntry entry = null;
        Hyperlink hyperlink = null;
        StringBuilder stringBuilder = new StringBuilder();
        StyleSpansBuilder<Collection<String>> styleSpans = new StyleSpansBuilder<>();

        int count = 0;

        while (count < 100 && (entry = this.consoleQueue.poll()) != null)
        {
            count++;

            if (entry.content().isRight())
            {
                if (!stringBuilder.isEmpty())
                {
                    appendText(stringBuilder.toString(), styleSpans.create());
                    stringBuilder = new StringBuilder();
                    styleSpans = new StyleSpansBuilder<>();
                }

                hyperlink = entry.content().getRight();
                appendHyperlink(hyperlink, List.of(Style.HYPERLINK_STYLE));
            }
            else
            {
                stringBuilder.append(entry.content().getLeft());

                for (var span : entry.styleSpans())
                {
                    styleSpans.add(span);
                }
            }
        }

        if (!stringBuilder.isEmpty())
        {
            appendText(stringBuilder.toString(), styleSpans.create());
        }
    }

    protected void appendHyperlink(Hyperlink link, Collection<String> styles)
    {
        Platform.runLater(() -> {
            try
            {
                int oldLength = getLength();
                append(Either.right(link), styles);

                while (getParagraphs().size() > this.maxParagraphs)
                {
                    replaceText(0, 0, 1, 0, "");
                }

                if (this.autoScroll)
                {
                    scrollToEnd();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    protected void appendText(String text, StyleSpans<Collection<String>> styleSpans)
    {
        if (!text.isEmpty())
        {
            Platform.runLater(() -> {
                try
                {
                    int oldLength = getLength();
                    appendText(text);
                    setStyleSpans(oldLength, styleSpans);

                    while (getParagraphs().size() > this.maxParagraphs)
                    {
                        replaceText(0, 0, 1, 0, "");
                    }

                    if (this.autoScroll)
                    {
                        scrollToEnd();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            });
        }
    }

    public void scrollToEnd()
    {
        scrollYBy(Double.MAX_VALUE);
    }

    public void setAutoScroll(boolean autoScroll)
    {
        this.autoScroll = autoScroll;

        if (this.autoScroll)
        {
            scrollToEnd();
        }
    }

    @Override
    public void kill()
    {
        if (this.queueFuture != null)
        {
            this.queueFuture.cancel(true);
        }
    }
}