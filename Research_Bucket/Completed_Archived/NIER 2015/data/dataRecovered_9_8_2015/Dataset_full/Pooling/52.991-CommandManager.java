/********************************************************************************
 *
 * jMule - a Java massive parallel file sharing client
 *
 * Copyright (C) by the jMuleGroup ( see the CREDITS file )
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details ( see the LICENSE file ).
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * $Id: CommandManager.java,v 1.2 2004/01/04 20:26:01 lydna Exp $
 *
 ********************************************************************************/
package org.jmule.ui.sacli.controller;

import java.util.ArrayList;
import java.util.Iterator;

import org.jmule.resource.Messages;

/** This class manages the known commands, which can be executed on the core. */
public class CommandManager {

	private boolean usingRegExps;

	private static CommandManager singleton = new CommandManager();

	/** Searches for a given command name.
	* The command has only to start with the commandPrefix to be found.
	* The commandPrefix the first token of the line. Has to have a minimal length of currently 3 characters.
	*/
	public Command lookupCommand(String commandPrefix) throws IllegalArgumentException {
		if (commandPrefix.length() < 3)
			throw new IllegalArgumentException(
				Messages.getString(
					"Ambiguous command: \"{0}\". You must use at least enough characters to distinguish it from others.",
					commandPrefix));
		/*
		The simplest solution for the case, there are at least 2 command starting with the same user input, would be to always taking the first. *This is the way we go now*.
		The better one, but much more compilcated, would be to detect this case and to throw a ParseError( "Abigiuos Command: " + user_input )
		*/

		//Class[] knownCommands = getRegistredCommands();

		//System.out.println( "commandPrefix=" + commandPrefix );
		for (int i = 0; i < knownCommands.size(); i++) { // .length
			//System.out.println( "knownCommands[i].getName().toUpperCase()=" + knownCommands[i].getName().toUpperCase() );
			// ask not the getName() but the getCmdName
			 
			
				String cmdName = knownCommands.get(i).getClass().getName().toUpperCase();
				int idx = cmdName.lastIndexOf('.');
				if (idx > -1)
					cmdName = cmdName.substring(idx + 1, cmdName.length());
				if (cmdName.startsWith(commandPrefix.toUpperCase())) {
					return (Command)knownCommands.get(i);
				}
			}
		

		// We haven't identified the command by its name so far, maybe it is the new regExp based command ?
		// this have to be in the command parser
        /* TODO: implement the regexp command search
		if (usingRegExps) {
			Pattern s;
			for (int i = 0; i < knownCommands.size(); i++) {
				Command cmd = (Command)knownCommands.get(i).newInstance();
				if (cmd instanceof REParsableCommand) {
					s = ((REParsableCommand)cmd).getPattern();
					Matcher m = s.matcher(commandPrefix); // must be the whole line!!
					if (m.matches())
						 ((REParsableCommand)cmd).parse(m);
				}
			}
		}
        */
		throw new IllegalArgumentException(Messages.getString("Invalid command: \"{0}\".", commandPrefix));
	}

	public static CommandManager getInstance() {
		return singleton;
	}

	private CommandManager() {
		knownCommands = new ArrayList();
		knownCommands.add(new org.jmule.ui.sacli.command.HelpCLCommand());

		knownCommands.add(new org.jmule.ui.sacli.command.CloseCLCommand());
		knownCommands.add(new org.jmule.ui.sacli.command.CoreInfoCLCommand());
		////knownCommands.add(new org.jmule.ui.sacli.command.Ed2kServerCLCommand());
		// knownCommands.add(org.jmule.ui.sacli.command.DebugCLCommand.class); // move it to optional package
		// knownCommands.add(org.jmule.ui.sacli.command.ExecuteCLCommand.class);
		knownCommands.add(new org.jmule.ui.sacli.command.ConfigCLCommand());
		knownCommands.add(new org.jmule.ui.sacli.command.SearchCLCommand());
		knownCommands.add(new org.jmule.ui.sacli.command.DownloadsCLCommand());
		knownCommands.add(new org.jmule.ui.sacli.command.UploadsCLCommand());
		knownCommands.add(new org.jmule.ui.sacli.command.InstallCLCommand());
		// knownCommands.add(org.jmule.ui.sacli.command.LogCLCommand.class);
		/*,  RegisterCLCommand -> replace by installPlugin as a command set can be a plugin too */
		//knownCommands.add(org.jmule.ui.sacli.command.TeeCLCommand.class); // move it to optional package
		//knownCommands.add(org.jmule.ui.sacli.command.EchoCLCommand.class); // move it to optional package
		usingRegExps = false;
	}

	/* FIXME: use the ClassPathPackageIteratorFactory to get the  org.jmule.core.controller package and iterate over all classes
	
	Iterator packageIterator = ClassPathPackageIteratorFactory.iteratePackage( "org.jmule.core.controller" );
	
	while( packageIterator.hasNext() ) {
	 ClassPathPackage cpp = (ClassPathPackage)packageIterator.next();
	Iterator classIterator = cpp.iterateClasses();
	while( classIterator.hasNext() ) {
	  Class clazz = (Class)classIterator.next();
	  if( clazz.getName().equals( "DescribeCommand" ) ) continue; // FIXME: avoid this by renaming the DescribedCommand to something different ( without Command at the end ) or by move the really end user command to some other package like 'command'
	// Fixme: check if the clazz.getName().endsWith( "Command" );
	}
	}
	
	Do this only at the 1st time.
	*/

	/*
	  dont manage the known commands as classes but as objects.
	By doing this, we can:
	+ pool 'em : not so important, only performance relevant ( and as the cmd interpreter isn't that much stressed subsystem ... )
	+ avoid the static getHelpText and getShortDescription mess
	*/

	ArrayList knownCommands;

	public Command[] getRegistredCommands() {
		return (Command[])knownCommands.toArray(new Command[knownCommands.size()]);
	}

	public void registerCommand(Command command) {
        /* TODO: check if command implement interface REParsabeleCommand
		if (command instanceof REParsableCommand)
			usingRegExps = true;
            */
		knownCommands.add(command);
	}
    
    public void deregisterCommand(String commandName) {
        for (Iterator iter = knownCommands.iterator(); iter.hasNext();) {
            Command cmd = (Command)iter.next();
            if (cmd.getClass().getName().equals(commandName)) {
                knownCommands.remove(cmd);
                break;  
            }
        }   
    }
    
	// regcmd commandclass [jar]
	// exec cmds/debug.jmc ( regcmd org.jmule.core.controler.DebugCLCommand )
	// debug 
	// exec disableAllLogging
	// exec 
	// ohne exec, nur basic commands um core im Notfall zu bedinen
	// alles andere kann mit dem command interface plugin nachgerüstet werden.
	// : readline.jar 
	// bsh.jar oder jython.jar
	// name: bsh-cl-plugin.jar, jy-cl-plugin.jar
}
