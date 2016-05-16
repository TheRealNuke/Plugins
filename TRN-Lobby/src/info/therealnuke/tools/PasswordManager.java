/*
 *            This file is part of TRN-Lobby.
 *
 *  TRN-Lobby is free software: you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TRN-Lobby is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with  TRN-Lobby. 
 *  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package info.therealnuke.tools;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author TheRealNuke <therealnuke@gmail.com>
 */
public class PasswordManager {

    public static String getHash(String stringToHash, String digestAlgoritm)
            throws NoSuchAlgorithmException {
        MessageDigest mdEnc = MessageDigest.getInstance(digestAlgoritm);
        mdEnc.update(stringToHash.getBytes(), 0, stringToHash.length());
        return new BigInteger(1, mdEnc.digest()).toString(16);
    }

    public static String getHashedPassword(String salt, String password) {
        String hash = null;
        try {
            hash = getHash(password + salt, "MD5");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(PasswordManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return hash;
    }

}
