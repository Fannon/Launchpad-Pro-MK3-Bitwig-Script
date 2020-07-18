package io.github.jengamon.novation.reactive.modes.session;

import com.bitwig.extension.controller.api.SettableIntegerValue;
import io.github.jengamon.novation.reactive.atomics.IntegerSyncWrapper;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class MixerScrollAction implements Runnable, Supplier<String> {
    private int mOffset;

    private AtomicReference<SessionPadMode> mLastRowMode;

    private int mBankSize;
    private SettableIntegerValue mBankScrollPos;
    private IntegerSyncWrapper mBankScroll;
    private IntegerSyncWrapper mBankCount;

    private int mSendBankSize;
    private SettableIntegerValue mSendScrollPos;
    private IntegerSyncWrapper mSendScroll;
    private IntegerSyncWrapper mSendCount;

    private int mSBankSize;
    private SettableIntegerValue mSBankScrollPos;
    private IntegerSyncWrapper mSBankScroll;
    private IntegerSyncWrapper mSBankCount;

    public MixerScrollAction(int offset, int bankSize, SettableIntegerValue bankScrollPos, IntegerSyncWrapper bankScroll, IntegerSyncWrapper bankCount,
                               int sendBankSize, SettableIntegerValue sendScrollPos, IntegerSyncWrapper sendScroll, IntegerSyncWrapper sendCount,
                             int sbankSize, SettableIntegerValue sbankScrollPos, IntegerSyncWrapper sbankScroll, IntegerSyncWrapper sbankCount,
                             AtomicReference<SessionPadMode> lastRowMode) {
        mOffset = offset;
        mBankSize = bankSize;
        mBankScrollPos = bankScrollPos;
        mBankScroll = bankScroll;
        mBankCount = bankCount;
        mSendBankSize = sendBankSize;
        mSendScrollPos = sendScrollPos;
        mSendScroll = sendScroll;
        mSendCount = sendCount;
        mSBankSize = sbankSize;
        mSBankScrollPos = sbankScrollPos;
        mSBankScroll = sbankScroll;
        mSBankCount = sbankCount;
        mLastRowMode = lastRowMode;
    }

    boolean isInBounds(IntegerSyncWrapper scroll, IntegerSyncWrapper count, int bankSize) {
        int target = scroll.get() + mOffset;
        return target >= 0 && target <= count.get() - bankSize;
    }

    @Override
    public void run() {
        switch(mLastRowMode.get()) {
            case VOLUME:
            case PAN:
                if(isInBounds(mBankScroll, mBankCount, mBankSize)) mBankScrollPos.inc(mOffset);
                break;
            case SENDS:
                if(isInBounds(mSendScroll, mSendCount, mSendBankSize)) mSendScrollPos.inc(mOffset);
                break;
            case STOP:
            case MUTE:
            case SOLO:
            case RECORD:
                if(isInBounds(mSBankScroll, mSBankCount, mSBankSize)) mSBankScrollPos.inc(mOffset);
                break;
            default:
                break;
        }
    }

    @Override
    public String get() {
        return "Scrolls in mixer mode by " + mOffset;
    }
}
