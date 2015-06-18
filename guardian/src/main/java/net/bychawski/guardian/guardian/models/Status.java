package net.bychawski.guardian.guardian.models;

import net.bychawski.guardian.guardian.GuardianApp;
import net.bychawski.guardian.guardian.R;

/**
 * Item status
 */
public enum Status {
    Free(R.string.status_free, R.drawable.ic_free),
    InUse(R.string.status_in_use, R.drawable.ic_in_use),
    ;

    private final int _textId;
    private final int _iconId;

    private Status(int textId, int iconId) {
        this._textId = textId;
        this._iconId = iconId;
    }

    @Override
    public String toString() {
        return GuardianApp.getInstance().getString(_textId);
    }

    public int getIconId() {
        return _iconId;
    }
}
