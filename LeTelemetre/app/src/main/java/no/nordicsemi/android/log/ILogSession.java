package no.nordicsemi.android.log;

import android.content.Context;
import android.net.Uri;

public interface ILogSession {
    Context getContext();

    Uri getSessionContentUri();

    Uri getSessionEntriesUri();

    Uri getSessionUri();
}
