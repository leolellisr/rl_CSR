package org.deeplearning4j.rl4j.observation.transform.operation.historymerge;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.deeplearning4j.rl4j.observation.transform.operation.HistoryMergeTransform;
import org.nd4j.common.base.Preconditions;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * CircularFifoStore is used with the {@link HistoryMergeTransform HistoryMergeTransform}. This store is a first-in first-out queue
 * with a fixed size that replaces its oldest element if full.
 *
 * @author leolellisr
 */
public class CircularFifoStore implements HistoryMergeElementStore {

    private final CircularFifoQueue<INDArray> queue;

    public CircularFifoStore(int size) {
        Preconditions.checkArgument(size > 0, "The size must be at least 1, got %s", size);
        queue = new CircularFifoQueue<>(size);
    }

    /**
     * Add an element to the store, if this addition would make the store to overflow, the new element replaces the oldest.
     * @param elem
     */
    @Override
    public void add(INDArray elem) {
        queue.add(elem);
    }

    /**
     * @return The content of the store, returned in order from oldest to newest.
     */
    @Override
    public INDArray[] get() {
        int size = queue.size();
        INDArray[] array = new INDArray[size];
        for (int i = 0; i < size; ++i) {
            array[i] = queue.get(i).castTo(Nd4j.dataType());
        }
        return array;
    }

    /**
     * The CircularFifoStore needs to be completely filled before being ready.
     * @return false when the number of elements in the store is less than the store capacity (default is 4)
     */
    @Override
    public boolean isReady() {
        return queue.isAtFullCapacity();
    }

    /**
     * Clears the store.
     */
    @Override
    public void reset() {
        queue.clear();
    }
}
