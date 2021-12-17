package bt.cl.screen.comp;

import bt.cl.css.CssClasses;
import bt.cl.screen.ConsoleTabScreen;
import bt.cl.screen.obj.*;
import bt.console.output.styled.Style;
import bt.console.output.styled.StyledTextNode;
import bt.log.Log;
import bt.scheduler.Threads;
import bt.types.Killable;
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

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsoleTextArea extends GenericStyledArea<Void, Either<String, Clickable>, Collection<String>> implements Killable
{
    private static final TextOps<String, Collection<String>> STYLED_TEXT_OPS = SegmentOps.styledTextOps();
    private static final ClickableOps<Collection<String>> CLICKABLE_OPS = new ClickableOps<>();
    private static final TextOps<Either<String, Clickable>, Collection<String>> EITHER_OPS = STYLED_TEXT_OPS._or(CLICKABLE_OPS, (s1, s2) -> Optional.empty());
    private final String[] nonDefaultNodes = { Style.HYPERLINK_STYLE, Style.CLICKABLE_COMMAND_STYLE };
    private Queue<ConsoleEntry> consoleQueue;
    private int maxParagraphs = 2000;
    private boolean autoScroll = true;
    private Pattern hyperlinkTextpattern = Pattern.compile(Style.HYPERLINK_STYLE + "\\((.*?)\\)");
    private Pattern clickableCommandTextpattern = Pattern.compile(Style.CLICKABLE_COMMAND_STYLE + "\\((.*?)\\)");
    private ScheduledFuture queueFuture;

    private ConsoleTabScreen screen;

    public ConsoleTextArea(ConsoleTabScreen screen)
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
                      clickable ->
                              createStyledTextNode(t -> {
                                  if (clickable.isReal())
                                  {
                                      t.setText(clickable.getDisplayedText());
                                      t.getStyleClass().addAll(e.getStyle());

                                      t.setOnMouseClicked(ae -> clickable.onClick());
                                      t.setOnMouseEntered(me -> t.setCursor(Cursor.HAND));
                                      t.setOnMouseExited(me -> t.setCursor(Cursor.DEFAULT));
                                  }
                              })
              )
        );

        this.screen = screen;
        setUndoManager(getNoOpUndoManager());
        setUseInitialStyleForInsertion(false);
        setAutoScrollOnDragDesired(false);
        this.consoleQueue = new ConcurrentLinkedQueue<>();
        this.queueFuture = Threads.get().scheduleAtFixedRateDaemon(this::appendFromQueue, 100, 100, TimeUnit.MILLISECONDS);
    }

    public static TextExt createStyledTextNode(Consumer<TextExt> applySegment)
    {
        Log.entry(applySegment);
        TextExt t = new TextExt();
        t.setTextOrigin(VPos.TOP);
        applySegment.accept(t);

        Log.exit(t);

        return t;
    }

    protected UndoManager getNoOpUndoManager()
    {
        return new NoOpUndoManager();
    }

    public void append(StyledTextNode node)
    {
        Log.entry(node);

        ConsoleNode entry = new ConsoleNode(new StringBuilder(), new StyleSpansBuilder<>());
        appendNode(node, entry);

        if (!entry.getStringBuilder().isEmpty())
        {
            this.consoleQueue.add(new ConsoleEntry(Either.left(entry.getStringBuilder().toString()), entry.getStyleSpans().create()));
        }

        Log.exit();
    }

    protected List<String> getNodeStyles(StyledTextNode node)
    {
        Log.entry(node);

        List<String> allStyles = node.getStyles();

        if (allStyles.isEmpty())
        {
            allStyles.add(CssClasses.DEFAULT_TEXT);
        }

        Log.exit(allStyles);

        return allStyles;
    }

    protected void createHyperLink(StyledTextNode node, ConsoleNode entry, List<String> styles, String hyperlinkStyle)
    {
        Log.entry(node, entry, styles, hyperlinkStyle);

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

        ClickableHyperlink hyperlink = new ClickableHyperlink(node.getText(), node.getText(), link.isBlank() ? node.getText() : link);
        this.consoleQueue.add(new ConsoleEntry(Either.right(hyperlink), styles));

        Log.exit(hyperlink);
    }

    protected void createClickableCommand(StyledTextNode node, ConsoleNode entry, List<String> styles, String commandStyle)
    {
        Log.entry(node, entry, styles, commandStyle);

        Matcher matcher = this.clickableCommandTextpattern.matcher(commandStyle);
        String command = "";

        if (matcher.find())
        {
            command = matcher.group(1);
        }

        // append existing text and recreate buffers
        if (!entry.getStringBuilder().isEmpty())
        {
            this.consoleQueue.add(new ConsoleEntry(Either.left(entry.getStringBuilder().toString()), entry.getStyleSpans().create()));
        }

        entry.setStringBuilder(new StringBuilder());
        entry.setStyleSpans(new StyleSpansBuilder<>());

        ClickableCommand clickableCommand = new ClickableCommand(this.screen.getProcess(), node.getText(), command.isBlank() ? node.getText() : command);
        this.consoleQueue.add(new ConsoleEntry(Either.right(clickableCommand), styles));

        Log.exit(clickableCommand);
    }

    protected void appendNode(StyledTextNode node, ConsoleNode entry)
    {
        Log.entry(node, entry);

        boolean nonDefaultNode = false;
        List<String> styles = getNodeStyles(node);

        for (String nodeName : this.nonDefaultNodes)
        {
            String style = styles.stream()
                                 .filter(sty -> sty.startsWith(nodeName))
                                 .findAny().orElse(null);

            if (style != null)
            {
                styles.removeIf(sty -> sty.startsWith(nodeName));
                styles.add(0, nodeName);

                if (nodeName.equals(Style.HYPERLINK_STYLE))
                {
                    createHyperLink(node, entry, styles, style);
                }
                else if (nodeName.equals(Style.CLICKABLE_COMMAND_STYLE))
                {
                    createClickableCommand(node, entry, styles, style);
                }

                nonDefaultNode = true;
                break;
            }
        }

        if (!nonDefaultNode)
        {
            entry.getStringBuilder().append(node.getText());
            entry.getStyleSpans().add(styles, node.getText().length());
        }

        for (var child : node.getChildren())
        {
            appendNode(child, entry);
        }

        Log.exit();
    }

    protected void appendFromQueue()
    {
        ConsoleEntry entry = null;
        StringBuilder stringBuilder = new StringBuilder();
        StyleSpansBuilder<Collection<String>> styleSpans = new StyleSpansBuilder<>();

        int count = 0;

        while (count < 100 && (entry = this.consoleQueue.poll()) != null)
        {
            count++;

            if (entry.getContent().isRight())
            {
                if (!stringBuilder.isEmpty())
                {
                    appendText(stringBuilder.toString(), styleSpans.create());
                    stringBuilder = new StringBuilder();
                    styleSpans = new StyleSpansBuilder<>();
                }

                Clickable clickable = entry.getContent().getRight();
                appendClickable(entry.getContent().getRight(), entry.getStyles());
            }
            else
            {
                stringBuilder.append(entry.getContent().getLeft());

                for (var span : entry.getStyleSpans())
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

    protected void appendClickable(Clickable clickable, Collection<String> styles)
    {
        Log.entry(clickable, styles);

        Platform.runLater(() -> {
            try
            {
                int oldLength = getLength();
                append(Either.right(clickable), styles);

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
                Log.error("Failed to append text", e);
            }
        });

        Log.exit();
    }

    protected void appendText(String text, StyleSpans<Collection<String>> styleSpans)
    {
        Log.entry(text, styleSpans);

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
                    Log.error("Failed to append text", e);
                }
            });
        }

        Log.exit();
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
        Log.entry();

        if (this.queueFuture != null)
        {
            this.queueFuture.cancel(true);
        }

        Log.exit();
    }
}