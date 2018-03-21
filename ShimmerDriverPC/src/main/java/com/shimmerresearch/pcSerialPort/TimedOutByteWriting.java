package com.shimmerresearch.pcSerialPort;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/** TimedOutByteWriting : the class to perform the writing timeout.
 * 
 * @author Bastien Aracil
 * 
 * https://stackoverflow.com/questions/20034470/jssc-serial-connection-set-write-timeout/37076508#37076508
 *
 */
class TimedOutByteWriting {

    private final ByteWriter byteWriter;

    private final boolean onlyOneByte;

    private final byte oneByte;

    private final byte[] bytes;

    private final long timeout;

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "TimedOutByteWriting Thread");
        t.setDaemon(true);
        return t;
    });

    TimedOutByteWriting(ByteWriter byteWriter, byte oneByte, long timeout) {
        if (timeout <= 0) {
            throw new IllegalArgumentException("Invalid time out value : "+timeout+". Must be greater than 0");
        }
        this.byteWriter = Objects.requireNonNull(byteWriter, "byteWriter");
        this.bytes = null;
        this.oneByte = oneByte;
        this.timeout = timeout;
        this.onlyOneByte = true;
    }

    TimedOutByteWriting(ByteWriter byteWriter, byte[] bytes, long timeout) {
        if (timeout <= 0) {
            throw new IllegalArgumentException("Invalid time out value : "+timeout+". Must be greater than 0");
        }
        this.byteWriter = Objects.requireNonNull(byteWriter, "byteWriter");
        this.bytes = Objects.requireNonNull(bytes, "bytes");
        this.timeout = timeout;
        this.oneByte = 0;
        this.onlyOneByte = false;
    }

    void write() throws IOException, InterruptedException, TimeoutException {
        final Lock lock = new ReentrantLock();
        final Condition condition = lock.newCondition();
        final Result result = new Result();

        final Future<?> writeThread = EXECUTOR_SERVICE.submit(new WriteRunnable(result, lock, condition));
        final Future<?> timeoutThread = EXECUTOR_SERVICE.submit(new TimeoutRunnable(result, lock, condition));

        lock.lock();
        try {
            if (!result.timedout && !result.writeDone) {
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    writeThread.cancel(true);
                    timeoutThread.cancel(true);
                    throw e;
                }
            }
            if (!result.writeDone) {
                byteWriter.cancelWrite();
            }
            else {
                timeoutThread.cancel(true);
            }
        }
        finally {
            lock.unlock();
        }

        result.handleResult();
    }

    private abstract class TimedOutByteWritingRunnable implements Runnable {

        protected final Result result;

        final Lock lock;

        final Condition condition;

        TimedOutByteWritingRunnable(Result result, Lock lock, Condition condition) {
            this.result = result;
            this.lock = lock;
            this.condition = condition;
        }
    }

    private class WriteRunnable extends TimedOutByteWritingRunnable {

        private WriteRunnable(Result result, Lock lock, Condition condition) {
            super(result, lock, condition);
        }

        @Override
        public void run() {
            IOException exception;
            try {
                if (onlyOneByte) {
                    byteWriter.write(oneByte);
                } else {
                    byteWriter.write(bytes);
                }
                exception = null;
            } catch (IOException e) {
                exception = e;
            }
            lock.lock();
            try {
                result.writeException = exception;
                result.writeDone = exception == null;
                condition.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }

    private class TimeoutRunnable extends TimedOutByteWritingRunnable {

        private TimeoutRunnable(Result result, Lock lock, Condition condition) {
            super(result, lock, condition);
        }

        @Override
        public void run() {
            boolean interrupted;
            try {
                TimeUnit.MILLISECONDS.sleep(timeout);
                interrupted = false;
            } catch (InterruptedException e) {
                interrupted = true;
            }

            lock.lock();
            try {
                result.timedout = !interrupted;
                condition.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }


    private static class Result {

        IOException writeException;

        boolean writeDone = false;

        boolean timedout = false;

        void handleResult() throws IOException, TimeoutException {
            if (writeDone) {
                return;
            }
            if (timedout) {
                throw new TimeoutException("Write timed out");
            }
            else if (writeException != null) {
                throw writeException;
            }
        }
    }

}