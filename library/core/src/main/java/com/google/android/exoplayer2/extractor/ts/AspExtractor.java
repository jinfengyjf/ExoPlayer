package com.google.android.exoplayer2.extractor.ts;

import static com.google.android.exoplayer2.extractor.ts.TsPayloadReader.FLAG_DATA_ALIGNMENT_INDICATOR;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.extractor.Extractor;
import com.google.android.exoplayer2.extractor.ExtractorInput;
import com.google.android.exoplayer2.extractor.ExtractorOutput;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.extractor.PositionHolder;
import com.google.android.exoplayer2.extractor.SeekMap;
import com.google.android.exoplayer2.extractor.ts.TsPayloadReader.TrackIdGenerator;
import com.google.android.exoplayer2.util.ParsableByteArray;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AspExtractor implements Extractor{
    /** Factory for {@link AspExtractor} instances. */
    public static final ExtractorsFactory FACTORY = () -> new Extractor[] {new AspExtractor()};

    /**
     * The maximum number of bytes to search when sniffing, excluding ID3 information, before giving
     * up.
     */
    private static final int MAX_SNIFF_BYTES = 8 * 1024;
    private static final int MAX_SYNC_FRAME_SIZE = 2786;

    private final H264Reader reader;
    private final ParsableByteArray sampleData;

    private boolean startedPacket;

    List<Format> closedCaptionFormats = new List<Format>() {
        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean contains(@Nullable Object o) {
            return false;
        }

        @NonNull
        @Override
        public Iterator<Format> iterator() {
            return null;
        }

        @NonNull
        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @NonNull
        @Override
        public <T> T[] toArray(@NonNull T[] a) {
            return null;
        }

        @Override
        public boolean add(Format format) {
            return false;
        }

        @Override
        public boolean remove(@Nullable Object o) {
            return false;
        }

        @Override
        public boolean containsAll(@NonNull Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(@NonNull Collection<? extends Format> c) {
            return false;
        }

        @Override
        public boolean addAll(int index, @NonNull Collection<? extends Format> c) {
            return false;
        }

        @Override
        public boolean removeAll(@NonNull Collection<?> c) {
            return false;
        }

        @Override
        public boolean retainAll(@NonNull Collection<?> c) {
            return false;
        }

        @Override
        public void clear() {

        }

        @Override
        public Format get(int index) {
            return null;
        }

        @Override
        public Format set(int index, Format element) {
            return null;
        }

        @Override
        public void add(int index, Format element) {

        }

        @Override
        public Format remove(int index) {
            return null;
        }

        @Override
        public int indexOf(@Nullable Object o) {
            return 0;
        }

        @Override
        public int lastIndexOf(@Nullable Object o) {
            return 0;
        }

        @NonNull
        @Override
        public ListIterator<Format> listIterator() {
            return null;
        }

        @NonNull
        @Override
        public ListIterator<Format> listIterator(int index) {
            return null;
        }

        @NonNull
        @Override
        public List<Format> subList(int fromIndex, int toIndex) {
            return null;
        }
    };

    /** Creates a new extractor for AC-3 bitstreams. */
    public AspExtractor() {

        reader = new H264Reader(new SeiReader(closedCaptionFormats),true,true);
        sampleData = new ParsableByteArray(MAX_SYNC_FRAME_SIZE);
    }

    // Extractor implementation.

    @Override
    public boolean sniff(ExtractorInput input) throws IOException, InterruptedException {
        return true;
    }

    @Override
    public void init(ExtractorOutput output) {
        reader.createTracks(output, new TrackIdGenerator(0, 1));
        output.endTracks();
        output.seekMap(new SeekMap.Unseekable(C.TIME_UNSET));
    }

    @Override
    public void seek(long position, long timeUs) {
        startedPacket = false;
        reader.seek();
    }

    @Override
    public void release() {
        // Do nothing.
    }

    @Override
    public int read(ExtractorInput input, PositionHolder seekPosition) throws IOException,
            InterruptedException {
        int bytesRead = input.read(sampleData.data, 0, MAX_SYNC_FRAME_SIZE);
        if (bytesRead == RESULT_END_OF_INPUT) {
            return RESULT_END_OF_INPUT;
        }

        // Feed whatever data we have to the reader, regardless of whether the read finished or not.
        sampleData.setPosition(0);
        sampleData.setLimit(bytesRead);

        if (!startedPacket) {
            // Pass data to the reader as though it's contained within a single infinitely long packet.
            reader.packetStarted(/* pesTimeUs= */ 0, FLAG_DATA_ALIGNMENT_INDICATOR);
            startedPacket = true;
        }
        // TODO: Make it possible for the reader to consume the dataSource directly, so that it becomes
        // unnecessary to copy the data through packetBuffer.
        reader.consume(sampleData);
        return RESULT_CONTINUE;
    }


}
