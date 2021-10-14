package bt.cl.screen.obj;

import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;

public class ConsoleNode
{
    private StringBuilder stringBuilder;
    private StyleSpansBuilder<Collection<String>> styleSpans;

    public ConsoleNode(StringBuilder stringBuilder, StyleSpansBuilder<Collection<String>> styleSpans)
    {
        this.stringBuilder = stringBuilder;
        this.styleSpans = styleSpans;
    }

    public StringBuilder getStringBuilder()
    {
        return stringBuilder;
    }

    public void setStringBuilder(StringBuilder stringBuilder)
    {
        this.stringBuilder = stringBuilder;
    }

    public StyleSpansBuilder<Collection<String>> getStyleSpans()
    {
        return styleSpans;
    }

    public void setStyleSpans(StyleSpansBuilder<Collection<String>> styleSpans)
    {
        this.styleSpans = styleSpans;
    }
}