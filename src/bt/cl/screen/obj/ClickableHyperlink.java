package bt.cl.screen.obj;

import bt.log.Log;
import bt.utils.Exceptions;

import java.awt.*;
import java.net.URI;

public class ClickableHyperlink extends Clickable
{
    private final String link;

    public ClickableHyperlink(String originalDisplayedText, String displayedText, String link)
    {
        super(displayedText);
        this.link = link;
    }

    public String getLink()
    {
        return link;
    }

    @Override
    public Clickable subSequence(int start, int end)
    {
        return new ClickableHyperlink(this.originalDisplayedText, this.displayText.substring(start, end), this.link);
    }

    @Override
    public Clickable subSequence(int start)
    {
        return new ClickableHyperlink(this.originalDisplayedText, this.displayText.substring(start), this.link);
    }

    @Override
    public Clickable mapDisplayedText(String text)
    {
        return new ClickableHyperlink(this.originalDisplayedText, text, this.link);
    }

    @Override
    public void onClick()
    {
        Log.entry();

        Exceptions.uncheck(() -> Desktop.getDesktop().browse(new URI(getLink())));

        Log.exit();
    }
}