package bt.cl.screen.obj;

import bt.cl.process.AttachedProcess;

import java.io.IOException;

/**
 * @author Lukas Hartwig
 * @since 05.12.2021
 */
public class ClickableCommand extends Clickable
{
    private AttachedProcess process;
    private String command;

    public ClickableCommand(AttachedProcess process, String originalDisplayedText, String displayText, String command)
    {
        super(originalDisplayedText, displayText);
        this.process = process;
        this.command = command;
    }

    public ClickableCommand(AttachedProcess process, String displayText, String command)
    {
        super(displayText);
        this.process = process;
        this.command = command;
    }

    @Override
    public Clickable subSequence(int start, int end)
    {
        return new ClickableCommand(this.process, this.originalDisplayedText, this.displayText.substring(start, end));
    }

    @Override
    public Clickable subSequence(int start)
    {
        return new ClickableCommand(this.process, this.originalDisplayedText, this.displayText.substring(start));
    }

    @Override
    public Clickable mapDisplayedText(String text)
    {
        return new ClickableCommand(this.process, this.originalDisplayedText, text);
    }

    @Override
    public void onClick()
    {
        if (this.process != null && this.process.isAlive())
        {
            try
            {
                this.process.write(this.command + "\n");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}