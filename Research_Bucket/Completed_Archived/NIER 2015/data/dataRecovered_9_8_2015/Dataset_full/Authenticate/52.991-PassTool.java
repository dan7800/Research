/*
 * Copyright(c) 2002 Center for E-Commerce Infrastructure Development, The
 * University of Hong Kong (HKU). All Rights Reserved.
 *
 * This software is licensed under the Academic Free License Version 1.0
 *
 * Academic Free License
 * Version 1.0
 *
 * This Academic Free License applies to any software and associated 
 * documentation (the "Software") whose owner (the "Licensor") has placed the 
 * statement "Licensed under the Academic Free License Version 1.0" immediately 
 * after the copyright notice that applies to the Software. 
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of the Software (1) to use, copy, modify, merge, publish, perform, 
 * distribute, sublicense, and/or sell copies of the Software, and to permit 
 * persons to whom the Software is furnished to do so, and (2) under patent 
 * claims owned or controlled by the Licensor that are embodied in the Software 
 * as furnished by the Licensor, to make, use, sell and offer for sale the 
 * Software and derivative works thereof, subject to the following conditions: 
 *
 * - Redistributions of the Software in source code form must retain all 
 *   copyright notices in the Software as furnished by the Licensor, this list 
 *   of conditions, and the following disclaimers. 
 * - Redistributions of the Software in executable form must reproduce all 
 *   copyright notices in the Software as furnished by the Licensor, this list 
 *   of conditions, and the following disclaimers in the documentation and/or 
 *   other materials provided with the distribution. 
 * - Neither the names of Licensor, nor the names of any contributors to the 
 *   Software, nor any of their trademarks or service marks, may be used to 
 *   endorse or promote products derived from this Software without express 
 *   prior written permission of the Licensor. 
 *
 * DISCLAIMERS: LICENSOR WARRANTS THAT THE COPYRIGHT IN AND TO THE SOFTWARE IS 
 * OWNED BY THE LICENSOR OR THAT THE SOFTWARE IS DISTRIBUTED BY LICENSOR UNDER 
 * A VALID CURRENT LICENSE. EXCEPT AS EXPRESSLY STATED IN THE IMMEDIATELY 
 * PRECEDING SENTENCE, THE SOFTWARE IS PROVIDED BY THE LICENSOR, CONTRIBUTORS 
 * AND COPYRIGHT OWNERS "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE 
 * LICENSOR, CONTRIBUTORS OR COPYRIGHT OWNERS BE LIABLE FOR ANY CLAIM, DAMAGES 
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE. 
 *
 * This license is Copyright (C) 2002 Lawrence E. Rosen. All rights reserved. 
 * Permission is hereby granted to copy and distribute this license without 
 * modification. This license may not be modified without the express written 
 * permission of its copyright owner. 
 */

/* ===== 
 *
 * $Header: /cvsroot/sino/ebxmlms/src/hk/hku/cecid/phoenix/common/util/PassTool.java,v 1.3 2003/12/11 06:41:28 bobpykoon Exp $
 *
 * Code authored by:
 *
 * kcyee [2002-08-28]
 *
 * Code reviewed by:
 *
 * username [YYYY-MM-DD]
 *
 * Remarks:
 *
 * =====
 */

package hk.hku.cecid.phoenix.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
/**
 * A command line utility to manipulate the password file used by
 * AuthenticationManager.
 *
 * @author kcyee
 * @version $Revision: 1.3 $
 */
public class PassTool {
    /** Main program */
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            printUsage();
        }
        String fileName = null;
        if (args.length >= 3 && args[1].equalsIgnoreCase("-f")) {
            fileName = args[2];
        }
        BufferedReader in = 
            new BufferedReader(new InputStreamReader(System.in));
        if (fileName == null) {
            System.out.print("Password file: ");
            fileName = in.readLine();
        }

        AuthenticationManager auth =
            new AuthenticationManager(new File(fileName));

        if (args[0].equalsIgnoreCase("add")) {
            System.out.print("User: ");
            String user = in.readLine();
            System.out.print("Password: ");
            String password = in.readLine();
            System.out.println(auth.addUser(user, password) ? 
                "Succeeded" : "Failed");
        }
        else if (args[0].equalsIgnoreCase("change")) {
            System.out.print("User: ");
            String user = in.readLine();
            System.out.print("Password: ");
            String password = in.readLine();
            System.out.print("New password: ");
            String newPass = in.readLine();
            System.out.println(auth.editUser(user, password, newPass) ? 
                "Succeeded" : "Failed");
        }
        else if (args[0].equalsIgnoreCase("remove")) {
            System.out.print("User: ");
            String user = in.readLine();
            System.out.print("Password: ");
            String password = in.readLine();
            System.out.println(auth.removeUser(user, password) ? 
                "Succeeded" : "Failed");
        }
        else if (args[0].equalsIgnoreCase("verify")) {
            System.out.print("User: ");
            String user = in.readLine();
            System.out.print("Password: ");
            String password = in.readLine();
            System.out.println(auth.authenticate(user, password) ? 
                "Succeeded" : "Failed");
        }
        else {
            printUsage();
        }
    }

    /** Prints out the usage screen of the program */
    protected static void printUsage() {
        System.out.println("Usage: java " + PassTool.class.getName() + "\n"
            + "            add|change|remove|verify [-f filename]\n");
        System.exit(0);
    }
}

