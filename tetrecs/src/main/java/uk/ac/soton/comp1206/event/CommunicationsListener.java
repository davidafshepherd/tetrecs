package uk.ac.soton.comp1206.event;

/**
 * The Communications Listener is used for listening to messages received by the communicator.
 */
public interface CommunicationsListener {

    /**
     * Handles an incoming message received by the Communicator
     * @param communication the message that was received
     */
    void receiveCommunication(String communication);
}
