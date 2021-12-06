package bt.cl.screen.obj;

import org.fxmisc.richtext.model.SegmentOpsBase;

import java.util.Optional;

public class ClickableOps<S> extends SegmentOpsBase<Clickable, S>
{
    public ClickableOps()
    {
        super(new Clickable("", ""));
    }

    @Override
    public int length(Clickable clickable)
    {
        return clickable.length();
    }

    @Override
    public char realCharAt(Clickable clickable, int index)
    {
        return clickable.charAt(index);
    }

    @Override
    public String realGetText(Clickable clickable)
    {
        return clickable.getDisplayedText();
    }

    @Override
    public Clickable realSubSequence(Clickable clickable, int start, int end)
    {
        return clickable.subSequence(start, end);
    }

    @Override
    public Clickable realSubSequence(Clickable clickable, int start)
    {
        return clickable.subSequence(start);
    }

    @Override
    public Optional<Clickable> joinSeg(Clickable currentSeg, Clickable nextSeg)
    {
        if (currentSeg.isEmpty())
        {
            if (nextSeg.isEmpty())
            {
                return Optional.empty();
            }
            else
            {
                return Optional.of(nextSeg);
            }
        }
        else
        {
            if (nextSeg.isEmpty())
            {
                return Optional.of(currentSeg);
            }
            else
            {
                return concatClickables(currentSeg, nextSeg);
            }
        }
    }

    private Optional<Clickable> concatClickables(Clickable leftSeg, Clickable rightSeg)
    {
        System.out.println(leftSeg + "   " + rightSeg);
        String leftText = leftSeg.getDisplayedText();
        String rightText = rightSeg.getDisplayedText();

        return Optional.of(leftSeg.mapDisplayedText(leftText + rightText));
    }

}