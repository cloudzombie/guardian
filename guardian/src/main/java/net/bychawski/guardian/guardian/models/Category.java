package net.bychawski.guardian.guardian.models;

import net.bychawski.guardian.guardian.GuardianApp;
        import net.bychawski.guardian.guardian.R;


/**
 * Categories of items
 */
public enum Category {
    Book      (R.string.category_book,      R.drawable.ic_book),
    Movie     (R.string.category_movie,     R.drawable.ic_movie),
    Boardgame (R.string.category_boardgame, R.drawable.ic_chess),
    Other     (R.string.category_other,     R.drawable.ic_question_mark)
    ;

    /**
     * Id of string resource
     */
    private final int _stringId;
    /**
     * Id of icon resource
     */
    private final int _iconId;

    /**
     * Private constructor
     * @param stringId id of string resource
     * @param iconId id of icon resource
     */
    private Category(int stringId, int iconId) {
        this._stringId = stringId;
        this._iconId = iconId;
    }

    @Override
    public String toString() {
        return GuardianApp.getInstance().getString(this._stringId);
    }

    /**
     *
     * @return icon resource id
     */
    public int getIconId() {
        return _iconId;
    }
}
