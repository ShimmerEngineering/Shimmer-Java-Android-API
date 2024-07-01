package com.shimmerresearch.driver;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ThreadSafeByteFifoBuffer {
    private BlockingQueue<Byte> buffer;

    public ThreadSafeByteFifoBuffer(int capacity) {
        buffer = new ArrayBlockingQueue<>(capacity);
    }

    public void write(byte[] bytes) throws InterruptedException {
        for (byte b : bytes) {
            buffer.put(b);
        }
    }

    public void write(byte b) throws InterruptedException {
        buffer.put(b);
    }

    public byte[] read(int numBytes) throws InterruptedException {
        byte[] result = new byte[numBytes];
        for (int i = 0; i < numBytes; i++) {
            result[i] = buffer.take();
        }
        return result;
    }

    public byte read() throws InterruptedException {
        return buffer.take();
    }

    public int size() {
        return buffer.size();
    }
}