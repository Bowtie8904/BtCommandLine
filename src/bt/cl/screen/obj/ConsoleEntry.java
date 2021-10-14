package bt.cl.screen.obj;

import org.fxmisc.richtext.model.StyleSpans;
import org.reactfx.util.Either;

import java.util.Collection;

public record ConsoleEntry(Either<String, Hyperlink> content, StyleSpans<Collection<String>> styleSpans)
{
}