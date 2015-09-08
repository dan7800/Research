/*
 * Copyright (c) 2003, Vanderbilt University
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose, without fee, and without written agreement is
 * hereby granted, provided that the above copyright notice, the following
 * two paragraphs and the author appear in all copies of this software.
 * 
 * IN NO EVENT SHALL THE VANDERBILT UNIVERSITY BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE VANDERBILT
 * UNIVERSITY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE VANDERBILT UNIVERSITY SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
 * ON AN "AS IS" BASIS, AND THE VANDERBILT UNIVERSITY HAS NO OBLIGATION TO
 * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * Author: Gyorgy Balogh, Gabor Pap, Miklos Maroti
 * Date last modified: 02/09/04
 */

package net.tinyos.prowler;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

/**
 * This class is the heart of Prowler, as this is the event based scheduler, or
 * simulator.
 * 
 * @author Gyorgy Balogh, Miklos Maroti, Gabor Pap (gabor.pap@vanderbilt.edu)
 */
public class Simulator{
	
	/** 
	 * This is a static reference to a Random instance.
	 * This makes experiments repeatable, all you have to do is to set
 	 * the seed of this Random class. 
 	 */
	static public Random random = new Random();
	
	/**
	 * This defines the time resolution. Every time and time interval
	 * in the simulator is represented in this resolution. This rate
	 * corresponds to the 38.4 kbps speed, but maybe a little friendlier.
	 */
	public static final int ONE_SECOND = 40000;

	/** Holds the events */
	PriorityQueue eventQueue = new PriorityQueue();
    
	/** The time of the last event using the given resolution */            
	long lastEventTime = 0;

	/** Needed for the display, stores the maximum of both the x and y coordinates */
	private double maxCoordinate = 0;
	
	/**
	 * The nodes of the Simulator are linked together in a single linked list. 
	 * This points to the first, and then {@link Node#nextNode} to the next. 
	 */
	public Node firstNode = null;

	/** 
	 * As the simulator is event based there is a need for a queue to handle 
	 * the occurring events. This general priority queue is based on a
	 * a TreeSet.
	*/
	public class PriorityQueue{

		private TreeSet queue = new TreeSet();
	
		/** Adds an item to the queue, item must be Comparable
		 * @param item The item to be added to the queue
		*/
		public void add( Comparable item ){
			queue.add( item );
		}

		/**
		 * @return Returns the first element and removes it form the queue 
		 */
		public Object getAndRemoveFirst(){
			if( queue.size() > 0 ){
				Object first = queue.first();
				queue.remove( first );
				return first;            
			}else{
				return null;
			}
		}
    
		/**
		 * Clears the queue 
		 */
		public void clear(){
			queue.clear();
		}
    
		/**
		 * @return Returns the number of items in the queue
		*/
		public int size(){
			return queue.size();
		}
	}

	/**
	 * 
	 * @return Returns the time of the last event in 1/ONE_SECOND
	 */
	public long getSimulationTime(){
		return lastEventTime;
	}
    
	/**
	 * 
	 * @return Returns the time of the last event in milliseconds.
	 */
	public long getSimulationTimeInMillisec(){
		return (long)(1000 * lastEventTime / (double)Simulator.ONE_SECOND);
	}
    
	/**
	 * Adds an event to the event queue.
	 *  
	 * @param e the event to be added to the queue
	 */
	public void addEvent( Event e ){
		eventQueue.add( e );        
	}
    
	/**
	 * Processes and executes the next event. 
	 */    
	public void step(){
		Event event = (Event)eventQueue.getAndRemoveFirst();
		if( event != null ){
			lastEventTime = event.time;
			event.execute();
		}
	}
	    
	/**
	 * Processes and executes the next "n" event.
	 * 
	 * @param n the number of events to be processed
	 */
	public void step( int n ){
		for( int i=0; i<n; ++i )
			step();        
	}
    
	/**
	 * Runs the simulation for a given amount of time.
	 * 
	 * @param timeInSec the time in seconds until the simulation is to run
	 */
	public void run( double timeInSec ){
		long tmax = lastEventTime + (int)(Simulator.ONE_SECOND * timeInSec);                 
		while( lastEventTime < tmax )
        {
            Event event = (Event)eventQueue.getAndRemoveFirst();
            if( event != null ){
                lastEventTime = event.time;
                event.execute();
            }
            else
                break;
        }
	}
	
	/**
	 * This function runs the simulation with the display.
	 * The user of the simulator must first add all the nodes used in the 
	 * experiment!
	 */
	public void runWithDisplay(){
		Display disp = new Display( this, maxCoordinate );
		disp.show();        
		while( true ){
			step(100);
			disp.update();
		}
	}
    
	/**
	 * This function runs the simulation with the display and in real time.
	 * The user of the simulator must first add all the nodes used in the 
	 * experiment!
	 */
	public void runWithDisplayInRealTime(){      
		Thread t = new Thread(){
			public void run(){       
				Display disp = new Display( Simulator.this, maxCoordinate );
				disp.show();
                
				long initDiff = System.currentTimeMillis() - getSimulationTimeInMillisec();
				while( true ){                
					step(100);
					disp.update();
					long diff = System.currentTimeMillis() - getSimulationTimeInMillisec();
					if( diff < initDiff ){
						try{
							sleep(initDiff-diff);
						}
						catch(Exception e){
						}
					}
				}
			}            
		};
		t.start();
	}
	
	/**
	 * Called by the {@link Display} whenever it is repainted, it calls the 
	 * {@link Node#display} method on every nodes in its Node list.
	 * 
	 * @param disp the Display on which to draw Nodes
	 */
	public void display(Display disp){
		Node tempNode = firstNode;
		while (tempNode != null){
			tempNode.display(disp);
			tempNode = tempNode.nextNode;
		}
	}    

	/**
	 * Adds a single Node to the simulator.
	 * 
	 * @param app the Node to be added
	 */
	protected void addNode(Node node){
		node.nextNode = firstNode;
		firstNode = node;
		if (node.x > maxCoordinate)
			maxCoordinate = node.x;
		if (node.y > maxCoordinate)
			maxCoordinate = node.y;
	}
	
	/**
	 * Adds a List of nodes to the experiment. 
	 * WARNING: Please call {@link RadioModel#updateNeighborhoods} after you 
	 * added all nodes to the system.
	 * 
	 * @param nodes the list of nodes to be added 
	 */
	public void addNodes( List nodes ){
		Iterator nodeIterator = nodes.iterator();
		while (nodeIterator.hasNext()){
			addNode((Node)nodeIterator.next());
		}
	}

	/**
	 * Creates a node in the simulator at the given position with the given id.
	 * WARNING: Please call {@link RadioModel#updateNeighborhoods} after you 
	 * added all nodes to the system.
	 * 
	 * @param nodeClass the class of motes we want to use during the experiment
	 * @param radioModel the radioModel to use
	 * @param nodeId the id of the node
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @return returns the node created
	 * @throws Exception throws various exceptions due to the generic way of 
	 *         constructing the classes
	 */
	public Node createNode( Class nodeClass, RadioModel radioModel, int nodeId, double x, double y, double z) throws Exception{
		Constructor c = nodeClass.getConstructor( new Class[] { Simulator.class, RadioModel.class } );
		Node node = (Node)c.newInstance( new Object[] { this, radioModel } );
		node.setPosition( x, y ,z );
		node.setId( nodeId );
		addNode(node);
		return node;
	}
    
	/**
	 * Creates a given number of motes dispersed randomly over a given square area.
	 * WARNING: Please call {@link RadioModel#updateNeighborhoods} after you 
	 * added all nodes to the system.
	 * 
	 * @param nodeClass the class of motes we want to use during the experiment
	 * @param radioModel the radioModel to use
	 * @param startNodeId the first node id of the created nodes
	 * @param nodeNum the number of motes
	 * @param areaWidth the size of one side of the square shaped area
     * @param maxElevation the maximum node elevation (z coordinate)
	 * @return returns a reference to the first node in the simulator
	 * @throws Exception throws various exceptions due to the generic way of 
	 *         constructing the classes
	 */
	public Node createNodes( Class nodeClass, RadioModel radioModel, int startNodeId, int nodeNum, double areaWidth, double maxElevation ) throws Exception{        
		Constructor c = nodeClass.getConstructor( new Class[] { Simulator.class, RadioModel.class } );
		for( int i=0; i<nodeNum; ++i ){
			Node node = (Node)c.newInstance( new Object[] { this, radioModel } );
			node.setPosition( areaWidth * random.nextDouble(), areaWidth * random.nextDouble(), maxElevation * random.nextDouble() );
			node.setId( startNodeId + i );
			addNode( node );            
		}
		return firstNode;
	}
    
	/**
	 * Clears the List of nodes.
	 */
	public void clear(){
		firstNode = null;
	}

}
