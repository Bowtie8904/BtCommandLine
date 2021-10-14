package bt.cl.screen.obj;

import javafx.beans.value.ObservableBooleanValue;
import org.fxmisc.undo.UndoManager;
import org.reactfx.value.Val;

public class NoOpUndoManager implements UndoManager
{
    private final Val<Boolean> alwaysFalse = Val.constant(false);

    @Override
    public boolean undo()
    {
        return false;
    }

    @Override
    public boolean redo()
    {
        return false;
    }

    @Override
    public Val<Boolean> undoAvailableProperty()
    {
        return alwaysFalse;
    }

    @Override
    public boolean isUndoAvailable()
    {
        return false;
    }

    @Override
    public Val<Boolean> redoAvailableProperty()
    {
        return alwaysFalse;
    }

    @Override
    public boolean isRedoAvailable()
    {
        return false;
    }

    @Override
    public boolean isPerformingAction()
    {
        return false;
    }

    @Override
    public boolean isAtMarkedPosition()
    {
        return false;
    }

    // not sure whether these may throw NPEs at some point
    @Override
    public Val nextUndoProperty()
    {
        return null;
    }

    @Override
    public Val nextRedoProperty()
    {
        return null;
    }

    @Override
    public ObservableBooleanValue performingActionProperty()
    {
        return null;
    }

    @Override
    public UndoPosition getCurrentPosition()
    {
        return null;
    }

    @Override
    public ObservableBooleanValue atMarkedPositionProperty()
    {
        return null;
    }

    // ignore these
    @Override
    public void preventMerge()
    {
    }

    @Override
    public void forgetHistory()
    {
    }

    @Override
    public void close()
    {
    }
}