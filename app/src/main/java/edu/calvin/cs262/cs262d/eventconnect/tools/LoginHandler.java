package edu.calvin.cs262.cs262d.eventconnect.tools;

/** interface to work with the login AsyncTask once a connection is successful
 * @author littlesnowman88
 * **/
public interface LoginHandler {
        void completeLoginTask(String result, String LoginID);
}
