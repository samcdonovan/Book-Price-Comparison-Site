package eu.samcdonovan.application;

/**
 * Debug interface for switching debugging messages on or off for different
 * parts of the program
 */
public interface Debug {

    static final boolean DEBUG_WATERSTONES = true;
    static final boolean DEBUG_BLACKWELLS = false;
    static final boolean DEBUG_DAUNTBOOKS = false;
    static final boolean DEBUG_FOYLES = false;
    static final boolean DEBUG_AMAZON = false;
    static final boolean DEBUG_WHSMITH = false;
    static final boolean DEBUG_BOOKDAO = false;
    static final boolean DEBUG_SELENIUM = false;
}
