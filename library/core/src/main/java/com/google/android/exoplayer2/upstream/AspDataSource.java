package com.google.android.exoplayer2.upstream;

import android.content.Context;
import android.net.Uri;

import java.io.IOException;

import androidx.annotation.Nullable;

public final class AspDataSource extends BaseDataSource{
    public static class AspSourceException extends IOException {
        public AspSourceException(IOException cause) {
            super(cause);
        }

        public AspSourceException(String message, IOException cause) {
            super(message, cause);
        }
    }
    public static final class Factory implements DataSource.Factory {
        @Nullable private TransferListener listener;

        public AspDataSource.Factory setListener(@Nullable TransferListener listener) {
            this.listener = listener;
            return this;
        }
        @Override
        public DataSource createDataSource() {
            AspDataSource dataSource = new AspDataSource();
            if (listener != null) {
                dataSource.addTransferListener(listener);
            }
            return dataSource;
        }
    }


    /**
     * Creates base data source.
     *
     */
    public AspDataSource() {
        super(false);
    }

    @Override
    public long open(DataSpec dataSpec) throws IOException {
        return 0;
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws IOException {
        return 0;
    }

    @Nullable
    @Override
    public Uri getUri() {
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
