package utils;

/**
 * @author ashan on 2020-05-08
 */
public enum ClusterStatus {
    WAIT_FOR_LEADER,
    PENDING_ELECTION,
    LEADER_ELECTED;
}
