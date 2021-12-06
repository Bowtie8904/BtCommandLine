package bt.cl.screen.obj;

/**
 * @author Lukas Hartwig
 * @since 05.12.2021
 */
public class Clickable
{
    protected String displayText;
    protected String originalDisplayedText;

    public Clickable(String displayText)
    {
        this.displayText = displayText;
        this.originalDisplayedText = displayText;
    }

    public Clickable(String originalDisplayedText, String displayText)
    {
        this.displayText = displayText;
        this.originalDisplayedText = originalDisplayedText;
    }

    public String getDisplayedText()
    {
        return this.displayText;
    }

    public String getOriginalDisplayedText()
    {
        return originalDisplayedText;
    }

    public char charAt(int index)
    {
        return isEmpty() ? '\0' : this.displayText.charAt(index);
    }

    public int length()
    {
        return this.displayText.length();
    }

    public boolean isEmpty()
    {
        return length() == 0;
    }

    public boolean isReal()
    {
        return length() > 0;
    }

    public Clickable subSequence(int start, int end)
    {
        return new Clickable(this.originalDisplayedText, this.displayText.substring(start, end));
    }

    public Clickable subSequence(int start)
    {
        return new Clickable(this.originalDisplayedText, this.displayText.substring(start));
    }

    public Clickable mapDisplayedText(String text)
    {
        return new Clickable(this.originalDisplayedText, text);
    }

    public void onClick()
    {
        System.out.println("default");
    }
}
