package org.openejb.server.security;

import java.io.*;
import java.util.*;

/**
 * Reads usernames, password, and groups from a text file with contents like this:
 * <pre>
 * username:password:group1,group2,group3,...
 * </pre>
 * Usernames, passwords, and group names should not contain colons or commas.
 *
 * @version $Revision$
 */
public class FileAuthenticator implements Authenticator {
    private Map users;
    private File dataFile;

    public FileAuthenticator(File dataFile) {
        this.dataFile = dataFile;
        System.err.println("Using auth file "+dataFile.getAbsolutePath());
    }

    private void initialize() throws AuthenticationException {
        if(users != null) {
            return;
        }
        try {
            BufferedReader in = new BufferedReader(new FileReader(dataFile));
            String line;
            Map temp = new HashMap();
            while((line = in.readLine()) != null) {
                StringTokenizer tok = new StringTokenizer(line, ":", false);
                User user = new User();
                user.username = tok.nextToken();
                if(!tok.hasMoreTokens()) {
                    throw new AuthenticationException("Bad syntax in auth file.  Expecting user:password:group,group,group got '"+line+"'");
                }
                user.password = tok.nextToken();
                if(tok.hasMoreTokens()) {
                    String groups = tok.nextToken();
                    tok = new StringTokenizer(groups, ",", false);
                    List gl = new ArrayList();
                    while(tok.hasMoreTokens()) {
                        gl.add(tok.nextToken());
                    }
                    user.groups = (String[])gl.toArray(new String[gl.size()]);
                }
                temp.put(user.username, user);
            }
            in.close();
            users = temp;
        } catch(FileNotFoundException e) {
            throw new AuthenticationException("Unable to read file authenticator data file");
        } catch(IOException e) {
            throw new AuthenticationException("Unable to read file authenticator data file");
        }
    }

    public boolean isValid(String username, String password) throws AuthenticationException {
        if(username == null || password == null) {
            return false;
        }
        initialize();
        User user = (User)users.get(username);
        if(user == null) {
            return false;
        }
        return user.password.equals(password);
    }

    public String[] getGroups(String username) throws AuthenticationException {
        if(username == null) {
            return new String[0];
        }
        initialize();
        User user = (User) users.get(username);
        if(user == null) {
            return new String[0];
        }
        return user.groups;
    }

    private static class User {
        String username;
        String password;
        String[] groups;
    }
}
