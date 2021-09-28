package bt.cl.screen.comp;

import org.fxmisc.richtext.StyleClassedTextArea;

public class ConsoleTextArea extends StyleClassedTextArea
{
    {
        setUseInitialStyleForInsertion(true);
    }

    public ConsoleTextArea()
    {
        super(false);
    }
}