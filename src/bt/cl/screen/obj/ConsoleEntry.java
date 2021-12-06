package bt.cl.screen.obj;

import org.fxmisc.richtext.model.StyleSpans;
import org.reactfx.util.Either;

import java.util.Collection;

public class ConsoleEntry
{
    private Collection<String> styles;
    private Either<String, Clickable> content;
    private StyleSpans<Collection<String>> styleSpans;

    public ConsoleEntry(Either<String, Clickable> content, StyleSpans<Collection<String>> styleSpans)
    {
        this.content = content;
        this.styleSpans = styleSpans;
    }

    public ConsoleEntry(Either<String, Clickable> content, Collection<String> styles)
    {
        this.content = content;
        this.styles = styles;
    }

    public Collection<String> getStyles()
    {
        return styles;
    }

    public void setStyles(Collection<String> styles)
    {
        this.styles = styles;
    }

    public Either<String, Clickable> getContent()
    {
        return content;
    }

    public void setContent(Either<String, Clickable> content)
    {
        this.content = content;
    }

    public StyleSpans<Collection<String>> getStyleSpans()
    {
        return styleSpans;
    }

    public void setStyleSpans(StyleSpans<Collection<String>> styleSpans)
    {
        this.styleSpans = styleSpans;
    }
}