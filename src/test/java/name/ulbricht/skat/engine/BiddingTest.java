package name.ulbricht.skat.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import name.ulbricht.skat.model.Seat;

class BiddingTest {

    @Test
    void middlehandSaysToForehandFirst() {
        Bidding bidding = new Bidding();
        assertEquals(Seat.MIDDLEHAND, bidding.toAct());
        assertTrue(bidding.isAskingToSay());
        assertEquals(18, bidding.pendingValue());
    }

    @Test
    void forehandHoldsThenMiddlehandPassesThenRearhandOutbids() {
        Bidding bidding = new Bidding();
        bidding.accept();
        assertEquals(Seat.FOREHAND, bidding.toAct());
        assertFalse(bidding.isAskingToSay());
        assertEquals(18, bidding.pendingValue());
        bidding.accept();
        assertEquals(Seat.MIDDLEHAND, bidding.toAct());
        assertEquals(20, bidding.pendingValue());
        bidding.pass();
        assertEquals(Seat.REARHAND, bidding.toAct());
        assertEquals(20, bidding.pendingValue());
        bidding.accept();
        assertEquals(Seat.FOREHAND, bidding.toAct());
        bidding.pass();
        assertTrue(bidding.isDone());
        assertEquals(Seat.REARHAND, bidding.winner());
        assertEquals(20, bidding.currentBid());
    }

    @Test
    void survivorWinsWhenRearhandPassesAtOnce() {
        Bidding bidding = new Bidding();
        bidding.accept();
        bidding.pass();
        bidding.pass();
        assertEquals(Seat.MIDDLEHAND, bidding.winner());
        assertEquals(18, bidding.currentBid());
    }

    @Test
    void forehandMayPlayAloneWhenNobodyBid() {
        Bidding bidding = new Bidding();
        bidding.pass();
        bidding.pass();
        assertEquals(Seat.FOREHAND, bidding.toAct());
        assertEquals(18, bidding.pendingValue());
        bidding.accept();
        assertEquals(Seat.FOREHAND, bidding.winner());
        assertEquals(18, bidding.currentBid());
    }

    @Test
    void allPassMeansPassedIn() {
        Bidding bidding = new Bidding();
        bidding.pass();
        bidding.pass();
        bidding.pass();
        assertTrue(bidding.isPassedIn());
        assertThrows(IllegalStateException.class, bidding::toAct);
    }
}
