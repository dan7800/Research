/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IC,--- COPYRIGHT (c) --  Open ebXML - 2001 ---

     The contents of this file are subject to the Open ebXML Public License
     Version 1.0 (the "License"); you may not use this file except in
     compliance with the License. You may obtain a copy of the License at
     'http://www.openebxml.org/LICENSE/OpenebXML-LICENSE-1.0.txt'

     Software distributed under the License is distributed on an "AS IS"
     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
     License for the specific language governing rights and limitations
     under the License.
     The Initial Developer of the Original Code is Anders W. Tell.
     Portions created by Financial Toolsmiths AB are Copyright (C)
     Financial Toolsmiths AB 1993-2001. All Rights Reserved.

     Contributor(s): see author tag.

---------------------------------------------------------------------*/
package org.openebxml.comp.language.behavioral.state_machines.engine.test;

/************************************************
 *
 * Includes
 *
 * \************************************************/
import java.io.*;
import java.util.*;
import java.net.*;

import java.lang.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.AssertionFailedError;

import org.openebxml.comp.util.*;
import org.openebxml.comp.language.execution.*;

import org.openebxml.comp.language.expression.*;
import org.openebxml.comp.language.expression.logical.*;

import org.openebxml.comp.language.foundation.core.*;

import org.openebxml.comp.language.behavioral.common_behavior.*;

import org.openebxml.comp.language.behavioral.state_machines.*;
import org.openebxml.comp.language.behavioral.state_machines.impl.*;
import org.openebxml.comp.language.behavioral.state_machines.engine.*;

import org.openebxml.comp.language.expression.time.*;

import org.openebxml.comp.language.behavioral.common_behavior.jdk.*;
import org.openebxml.comp.language.DefaultLanguageModelElement;
//Variable
import org.openebxml.comp.language.behavioral.actions.composite_actions.*;



//import org.openebxml.comp.language.LanguageModelElement;
//import org.openebxml.comp.language.DefaultLanguageModelElement;
/**
 *
 *  TestCase for StateMachine
 *
 *
 *
 * @author Anders W. Tell   Financial Toolsmiths AB
 *
 * @version $Id: TestBallStateConfiguration.java,v 1.5 2002/11/16 21:50:06 awtopensource Exp $
 *
 */

public class TestBallStateConfiguration  extends TestCase {


    /****/
    String[]        fTrace      = new String[200];
    int             fTrace_No;

    /****/
    public TestBallStateConfiguration()
    {
        super("Test StateConfiguration");
        fTrace_No   =0;
    }

    public TestBallStateConfiguration(String name)
    {
        super(name);
    }

    /****/
    protected void setUp()
    throws Exception
    {
    }

    /****/
    protected void tearDown()
    throws Exception
    {
    }

    /****/
    protected void Reset() {
        fTrace_No = 0;
    }


    /*---------------------------------------------*/
    /****/
    String Trace(String name, Object target, Object[] parameters) {
        String lsS = ""+name+"[";
        if( parameters != null )
            for(int i = 0; i < parameters.length; i++)
                lsS += ""+parameters[i];
        lsS += "]";
        fTrace[fTrace_No] = lsS;
        fTrace_No++;
        System.err.println("TRACE:"+lsS+"'");
        return lsS;
    }

    /*---------------------------------------------*/
    public class Enter extends Procedure {
        public Enter(String name) {super("enter-"+name);}
        public Object[]  Call(Invocation inv, Object target, Object[] parameters) { return new Object[]{Trace(getName(),target,parameters)};};
    }
    public class Enter_A extends Enter{public Enter_A() {super("A");} }
    public class Enter_B extends Enter{public Enter_B() {super("B");} }
    public class Enter_C extends Enter{public Enter_C() {super("C");} }
    public class Enter_D extends Enter{public Enter_D() {super("D");} }
    public class Enter_E extends Enter{public Enter_E() {super("E");} }
    public class Enter_F extends Enter{public Enter_F() {super("F");} }
    public class Enter_G extends Enter{public Enter_G() {super("G");} }
    public class Enter_H extends Enter{public Enter_H() {super("H");} }
    public class Enter_I extends Enter{public Enter_I() {super("I");} }
    public class Enter_J extends Enter{public Enter_J() {super("J");} }
    public class Enter_K extends Enter{public Enter_K() {super("K");} }
    public class Enter_L extends Enter{public Enter_L() {super("L");} }
    public class Enter_M extends Enter{public Enter_M() {super("M");} }
    public class Enter_N extends Enter{public Enter_N() {super("N");} }
    public class Enter_O extends Enter{public Enter_O() {super("O");} }
    public class Enter_P extends Enter{public Enter_P() {super("P");} }
    public class Enter_Q extends Enter{public Enter_Q() {super("Q");} }
    public class Enter_R extends Enter{public Enter_R() {super("R");} }
    public class Enter_S extends Enter{public Enter_S() {super("S");} }
    public class Enter_T extends Enter{public Enter_T() {super("T");} }
    public class Enter_U extends Enter{public Enter_U() {super("U");} }
    public class Enter_V extends Enter{public Enter_V() {super("V");} }
    public class Enter_W extends Enter{public Enter_W() {super("W");} }
    public class Enter_X extends Enter{public Enter_X() {super("X");} }
    public class Enter_Y extends Enter{public Enter_Y() {super("Y");} }
    public class Enter_Z extends Enter{public Enter_Z() {super("Z");} }

    /*---------------------------------------------*/
    public class Exit extends Procedure {
        public Exit(String name) {super("exit-"+name);}
        public Object[]  Call(Invocation inv, Object target, Object[] parameters) {
            return new Object[]{Trace(getName(),target,parameters)};};
    }
    public class Exit_A extends Exit{public Exit_A() {
        super("A");} }
    public class Exit_B extends Exit{public Exit_B() {super("B");} }
    public class Exit_C extends Exit{public Exit_C() {super("C");} }
    public class Exit_D extends Exit{public Exit_D() {super("D");} }
    public class Exit_E extends Exit{public Exit_E() {super("E");} }
    public class Exit_F extends Exit{public Exit_F() {super("F");} }
    public class Exit_G extends Exit{public Exit_G() {super("G");} }
    public class Exit_H extends Exit{public Exit_H() {super("H");} }
    public class Exit_I extends Exit{public Exit_I() {super("I");} }
    public class Exit_J extends Exit{public Exit_J() {super("J");} }
    public class Exit_K extends Exit{public Exit_K() {super("K");} }
    public class Exit_L extends Exit{public Exit_L() {super("L");} }
    public class Exit_M extends Exit{public Exit_M() {super("M");} }
    public class Exit_N extends Exit{public Exit_N() {super("N");} }
    public class Exit_O extends Exit{public Exit_O() {super("O");} }
    public class Exit_P extends Exit{public Exit_P() {super("P");} }
    public class Exit_Q extends Exit{public Exit_Q() {super("Q");} }
    public class Exit_R extends Exit{public Exit_R() {super("R");} }
    public class Exit_S extends Exit{public Exit_S() {super("S");} }
    public class Exit_T extends Exit{public Exit_T() {super("T");} }
    public class Exit_U extends Exit{public Exit_U() {super("U");} }
    public class Exit_V extends Exit{public Exit_V() {super("V");} }
    public class Exit_W extends Exit{public Exit_W() {super("W");} }
    public class Exit_X extends Exit{public Exit_X() {super("X");} }
    public class Exit_Y extends Exit{public Exit_Y() {super("Y");} }
    public class Exit_Z extends Exit{public Exit_Z() {super("Z");} }

    /*---------------------------------------------*/
    public class Effect extends Procedure {
        public Effect(String name) {super("effect-"+name);}
        public Object[]  Call(Invocation inv, Object target, Object[] parameters) { return new Object[]{Trace(getName(),target,parameters)};};
    }
    public class Effect_A extends Effect{public Effect_A() {super("A");} }

    /**eddie changed :
     *this effect B is overrides the Call method to be able to return the time
     *of a bouncing
     **/
    public class Effect_B extends Effect{
        public Effect_B() {super("B");}
        public Object[]  Call(Invocation inv, Object target, Object[] parameters) {
            Ball b = (Ball) (inv.getTopExecutionFrame().getTarget());
            //return time as second result
            //this is one way of passing the return values.  But the result should be related to the
            //event mechanism, the return value should be the ACT used for Event handling. the return values from
            //calling a method in an active object should instead be contained within a separate object,
            //a future
            return new Object[]{Trace(getName(),target,parameters), (Object) (new Double(b.getTime())) };
        }
    }
    public class Timed_Effect_B extends Effect{
        public Timed_Effect_B() {super("Timed_B");}
        public Object[]  Call(Invocation inv, Object target, Object[] parameters) {
          //it might also be of interest here to be able to
          //pass some kind of resultvalue..
            System.err.println("[TIMED_B]"+target);
            Timed_ball b;// = (Timed_ball) (inv.getTopExecutionFrame().getTarget());
          b = (Timed_ball) target;
          b.restart();
          return new Object[]{Trace(getName(),target,parameters)};
        }
    }
    public class Effect_C extends Effect{public Effect_C() {super("C");} }
    public class Effect_D extends Effect{public Effect_D() {super("D");} }
    public class Effect_E extends Effect{public Effect_E() {super("E");} }
    public class Effect_F extends Effect{public Effect_F() {super("F");} }
    public class Effect_G extends Effect{public Effect_G() {super("G");} }
    public class Effect_H extends Effect{public Effect_H() {super("H");} }
    public class Effect_I extends Effect{public Effect_I() {super("I");} }
    public class Effect_J extends Effect{public Effect_J() {super("J");} }
    public class Effect_K extends Effect{public Effect_K() {super("K");} }
    public class Effect_L extends Effect{public Effect_L() {super("L");} }
    public class Effect_M extends Effect{public Effect_M() {super("M");} }
    public class Effect_N extends Effect{public Effect_N() {super("N");} }
    public class Effect_O extends Effect{public Effect_O() {super("O");} }
    public class Effect_P extends Effect{public Effect_P() {super("P");} }
    public class Effect_Q extends Effect{public Effect_Q() {super("Q");} }
    public class Effect_R extends Effect{public Effect_R() {super("R");} }
    public class Effect_S extends Effect{public Effect_S() {super("S");} }
    public class Effect_T extends Effect{public Effect_T() {super("T");} }
    public class Effect_U extends Effect{public Effect_U() {super("U");} }
    public class Effect_V extends Effect{public Effect_V() {super("V");} }
    public class Effect_W extends Effect{public Effect_W() {super("W");} }
    public class Effect_X extends Effect{public Effect_X() {super("X");} }
    public class Effect_Y extends Effect{public Effect_Y() {super("Y");} }
    public class Effect_Z extends Effect{public Effect_Z() {super("Z");} }




    /*---------------------------------------------*/
    TimeEvent lTimeEvent =  new DefaultTimeEvent("bounceTimeEvent", new TimeExpression("TimeToBounce"));
    CallEvent   lCallEvent_Release = new DefaultCallEvent("release", new Operation("operRelease"));
    CallEvent   lCallEvent_Bounce = new DefaultCallEvent("bounce", new Operation("operBounce"));
    CallEvent   lCallEvent_Turn = new DefaultCallEvent("turn", new Operation("operTurn"));
    CallEvent   lCallEvent_A = new DefaultCallEvent("callA", new Operation("operA"));
    CallEvent   lCallEvent_B = new DefaultCallEvent("callB", new Operation("operB"));
    CallEvent   lCallEvent_C = new DefaultCallEvent("callC", new Operation("operC"));
    CallEvent   lCallEvent_D = new DefaultCallEvent("callD", new Operation("operD"));
    CallEvent   lCallEvent_E = new DefaultCallEvent("callE", new Operation("operE"));
    CallEvent   lCallEvent_F = new DefaultCallEvent("callF", new Operation("operF"));

    CallEvent   lCallEvent_G = new DefaultCallEvent("callG", new Operation("operG"));
    CallEvent   lCallEvent_H = new DefaultCallEvent("callH", new Operation("operH"));
    CallEvent   lCallEvent_I = new DefaultCallEvent("callI", new Operation("operI"));
    CallEvent   lCallEvent_J = new DefaultCallEvent("callJ", new Operation("operJ"));
    CallEvent   lCallEvent_K = new DefaultCallEvent("callK", new Operation("operK"));
    CallEvent   lCallEvent_L = new DefaultCallEvent("callL", new Operation("operL"));
    CallEvent   lCallEvent_M = new DefaultCallEvent("callM", new Operation("operM"));
    CallEvent   lCallEvent_N = new DefaultCallEvent("callN", new Operation("operN"));
    CallEvent   lCallEvent_O = new DefaultCallEvent("callO", new Operation("operO"));
    CallEvent   lCallEvent_P = new DefaultCallEvent("callP", new Operation("operP"));
    CallEvent   lCallEvent_Q = new DefaultCallEvent("callQ", new Operation("operQ"));
    CallEvent   lCallEvent_R = new DefaultCallEvent("callR", new Operation("operR"));
    CallEvent   lCallEvent_S = new DefaultCallEvent("callS", new Operation("operS"));
    CallEvent   lCallEvent_T = new DefaultCallEvent("callT", new Operation("operT"));
    CallEvent   lCallEvent_U = new DefaultCallEvent("callU", new Operation("operU"));
    CallEvent   lCallEvent_V = new DefaultCallEvent("callV", new Operation("operV"));
    CallEvent   lCallEvent_W = new DefaultCallEvent("callW", new Operation("operW"));
    CallEvent   lCallEvent_X = new DefaultCallEvent("callX", new Operation("operX"));
    CallEvent   lCallEvent_Y = new DefaultCallEvent("callY", new Operation("operY"));
    CallEvent   lCallEvent_Z = new DefaultCallEvent("callZ", new Operation("operZ"));

    CallEvent   lCallEvent_A2 = new DefaultCallEvent("callA", new Operation("operA"));
    CallEvent   lCallEvent_B2 = new DefaultCallEvent("callB", new Operation("operB"));
    CallEvent   lCallEvent_C2 = new DefaultCallEvent("callC", new Operation("operC"));
    CallEvent   lCallEvent_D2 = new DefaultCallEvent("callD", new Operation("operD"));
    CallEvent   lCallEvent_E2 = new DefaultCallEvent("callE", new Operation("operE"));
    CallEvent   lCallEvent_F2 = new DefaultCallEvent("callF", new Operation("operF"));
    CallEvent   lCallEvent_G2 = new DefaultCallEvent("callG", new Operation("operG"));
    CallEvent   lCallEvent_H2 = new DefaultCallEvent("callH", new Operation("operH"));
    CallEvent   lCallEvent_I2 = new DefaultCallEvent("callI", new Operation("operI"));
    CallEvent   lCallEvent_J2 = new DefaultCallEvent("callJ", new Operation("operJ"));
    CallEvent   lCallEvent_K2 = new DefaultCallEvent("callK", new Operation("operK"));
    CallEvent   lCallEvent_L2 = new DefaultCallEvent("callL", new Operation("operL"));
    CallEvent   lCallEvent_M2 = new DefaultCallEvent("callM", new Operation("operM"));
    CallEvent   lCallEvent_N2 = new DefaultCallEvent("callN", new Operation("operN"));
    CallEvent   lCallEvent_O2 = new DefaultCallEvent("callO", new Operation("operO"));
    CallEvent   lCallEvent_P2 = new DefaultCallEvent("callP", new Operation("operP"));
    CallEvent   lCallEvent_Q2 = new DefaultCallEvent("callQ", new Operation("operQ"));
    CallEvent   lCallEvent_R2 = new DefaultCallEvent("callR", new Operation("operR"));
    CallEvent   lCallEvent_S2 = new DefaultCallEvent("callS", new Operation("operS"));
    CallEvent   lCallEvent_T2 = new DefaultCallEvent("callT", new Operation("operT"));
    CallEvent   lCallEvent_U2 = new DefaultCallEvent("callU", new Operation("operU"));
    CallEvent   lCallEvent_V2 = new DefaultCallEvent("callV", new Operation("operV"));
    CallEvent   lCallEvent_W2 = new DefaultCallEvent("callW", new Operation("operW"));
    CallEvent   lCallEvent_X2 = new DefaultCallEvent("callX", new Operation("operX"));
    CallEvent   lCallEvent_Y2 = new DefaultCallEvent("callY", new Operation("operY"));
    CallEvent   lCallEvent_Z2 = new DefaultCallEvent("callZ", new Operation("operZ"));

    /*---------------------------------------------*/
    public class GuardBase extends DefaultGuard {
        public GuardBase(String name,BooleanExpression e) {super("guard-"+name, e);}
        public Object[]  Call(Invocation inv, Object target, Object[] parameters) { return new Object[]{Trace(getName(),target,parameters)};};
    }
    public class Guard_TRUE extends GuardBase{public Guard_TRUE() {super("TRUE", new BooleanExpression("true",new TRUE()));} }
    public class Guard_FALSE extends GuardBase{public Guard_FALSE() {super("FALSE", new BooleanExpression("false",new FALSE()));} }

    /*---------------------------------------------*/

    /****/
    protected StateMachine  setupBounce() throws OEXException {
        DefaultStateMachine lSM = new DefaultStateMachine("simple");
        CompositeState      lCompositeState = lSM.makeCompositeState("top", null,false,
        new Enter_X(),null, new Exit_X()) ;
        InitialState  lInitial = lSM.makeInitialState("initial-Comp", lCompositeState) ;
        FinalState  lFinalState = lSM.makeFinalState("final",lCompositeState);
        SimpleState  lSimpleState1 = lSM.makeSimpleState("down",lCompositeState,new Enter_A(),
        new Calculate("yplusone"), new Exit_A());
        SimpleState  lSimpleState2 = lSM.makeSimpleState("up",lCompositeState,new Enter_B(),
        null, new Exit_A());
        Transition lT1 = lSM.makeTransition("release",lInitial, lSimpleState1,
        lCallEvent_Release,new Guard_TRUE(), new Effect_R());
        Transition lT2 = lSM.makeTransition("bounce",lSimpleState1, lSimpleState2,
        lCallEvent_Bounce,new Guard_TRUE(), new Effect_B());
        Transition lT3 = lSM.makeTransition("bounce",lSimpleState2, lSimpleState1,
        lCallEvent_Bounce,new Guard_TRUE(), new Effect_B());
        //Transition lT3 = lSM.makeTransition("turn",lSimpleState2, lSimpleState1,
        //                                    lCallEvent_Turn,new Guard_TRUE(), new Effect_B());


        lSM.setTop(lCompositeState);
        lSM.Prepare();

        return lSM;
    }

    protected StateMachine  setupTimedBounce() throws OEXException {
        DefaultStateMachine lSM = new DefaultStateMachine("simple");
        CompositeState      lCompositeState = lSM.makeCompositeState("top", null,false,
        new Enter_X(),null, new Exit_X()) ;
        InitialState  lInitial = lSM.makeInitialState("initial-Comp", lCompositeState) ;
        FinalState  lFinalState = lSM.makeFinalState("final",lCompositeState);
        SimpleState  lSimpleState1 = lSM.makeSimpleState("down",lCompositeState,new Enter_A(),
        new Timed_Calculate("yplusone"), new Exit_A());
        SimpleState  lSimpleState2 = lSM.makeSimpleState("up",lCompositeState,new Enter_B(),
        /*new Timed_Calculate("yplusone")*/null, new Exit_A());
        Transition lT1 = lSM.makeTransition("release",lInitial, lSimpleState1,
        lCallEvent_Release,new Guard_TRUE(), new Effect_R());
        Transition lT2 = lSM.makeTransition("bounce",lSimpleState1, lSimpleState2,
        lTimeEvent,
        new Guard_TRUE(), new Timed_Effect_B());
        Transition lT3 = lSM.makeTransition("bounce",lSimpleState2, lSimpleState1,
        lTimeEvent,
        new Guard_TRUE(), new Timed_Effect_B());
        //Transition lT3 = lSM.makeTransition("turn",lSimpleState2, lSimpleState1,
        //                                    lCallEvent_Turn,new Guard_TRUE(), new Effect_B());
        lSM.setTop(lCompositeState);
        lSM.Prepare();
        return lSM;
    }
    /****/
    /****/
    protected StateMachine  setupSimple() throws OEXException {
        DefaultStateMachine lSM = new DefaultStateMachine("simple");
        CompositeState      lCompositeState = lSM.makeCompositeState("top", null,false,new Enter_X(),null, new Exit_X()) ;

        InitialState  lInitial = lSM.makeInitialState("initial-Comp", lCompositeState) ;
        //lCompositeState.setSubVertex(0, lInitial);

        FinalState  lFinalState = lSM.makeFinalState("final",lCompositeState);
        //lCompositeState.setSubVertex(1,lFinalState);


        SimpleState  lSimpleState1 = lSM.makeSimpleState("on",lCompositeState,new Enter_A(),null, new Exit_A());
        //lCompositeState.setSubVertex(2,lSimpleState1);

        /* Testing pseudostates */

        //Pseudostate lPseudoState = lSM.makePseudostate("Pseudo",lCompositeState, new PseudostateKind("Initial"));
        //lCompositeState.setSubVertex(2,lPseudoState);


        SimpleState  lSimpleState2 =lSM.makeSimpleState("off",lCompositeState, new Enter_B(),null, new Exit_B());
        //lCompositeState.setSubVertex(3,lSimpleState2);


        Transition lT1 = lSM.makeTransition("on",lInitial, lSimpleState1,
        null,new Guard_TRUE(), new Effect_A());

        Transition lT2 = lSM.makeTransition("switchOff",
        lSimpleState1, lSimpleState2,
        lCallEvent_A /*new DefaultTimeEvent("A",new TimeExpression("testing"))*/,null, new Effect_B());
        Transition lT3 = lSM.makeTransition("switchOn",
        lSimpleState2, lSimpleState1,
        lCallEvent_B,null,new Effect_C());
        Transition lT4 = lSM.makeTransition("break",
        lSimpleState2,lFinalState,
        lCallEvent_C,null,null);


        lSM.setTop(lCompositeState);
        lSM.Prepare();

        return lSM;
    }

    /****/

    /****/
    //declaring the stubstate to be used as the entry point for
    //the submachine
    private StubState lStub;

    protected StateMachine  setupSubmachine() throws OEXException {
        DefaultStateMachine lSM = new DefaultStateMachine("simple");
        CompositeState      lCompositeState = lSM.makeCompositeState("top", null,false,new Enter_X(),null, new Exit_X()) ;

        InitialState  lInitial = lSM.makeInitialState("initial-Comp", lCompositeState) ;
        lCompositeState.setSubVertex(0, lInitial);

        lStub = lSM.makeStubState("stub",lCompositeState,"substate");
        lCompositeState.setSubVertex(1, lStub);

        FinalState  lFinalState = lSM.makeFinalState("final",lCompositeState);
        lCompositeState.setSubVertex(2,lFinalState);


        SimpleState  lSimpleState1 = lSM.makeSimpleState("on",lCompositeState,new Enter_A(),null, new Exit_A());
        lCompositeState.setSubVertex(3,lSimpleState1);

        /* Testing pseudostates */

        //Pseudostate lPseudoState = lSM.makePseudostate("Pseudo",lCompositeState, new PseudostateKind("Initial"));
        //lCompositeState.setSubVertex(2,lPseudoState);


        SimpleState  lSimpleState2 =lSM.makeSimpleState("off",lCompositeState, new Enter_B(),null, new Exit_B());
        //lCompositeState.setSubVertex(3,lSimpleState2);


        Transition lT1 = lSM.makeTransition("on",lInitial, lStub,
        null,new Guard_TRUE(), new Effect_A());

        Transition lT2 = lSM.makeTransition("switchOff",
        lStub, lSimpleState2,
        lCallEvent_D /*new DefaultTimeEvent("A",new TimeExpression("testing"))*/,null, new Effect_B());
        Transition lT3 = lSM.makeTransition("switchOn",
        lSimpleState2, lSimpleState1,
        lCallEvent_F,null,new Effect_C());
        Transition lT4 = lSM.makeTransition("break",
        lSimpleState2,lFinalState,
        lCallEvent_C,null,null);


        lSM.setTop(lCompositeState);
        lSM.Prepare();

        return lSM;
    }
    public void testConstructor()throws OEXException
    {
        StateConfiguration lSM;

        lSM = new StateConfiguration(null);

    }

    /****/
    public void testPreparation()throws OEXException
    {
        StateMachine lSM;

        lSM = setupSimple();

        StateConfiguration lSC = new StateConfiguration(lSM);
    }

    /****/

    public void runEventSequence(StateMachine sm,
    Event[] sequence,
    Object[] parameters,
    String[] expected)
    throws TimeoutException,OEXException, IllegalArgumentException
    {
        fTrace_No   = 0;

        StateConfiguration lSC           = new StateConfiguration(sm);
        StateMachineProcessor lProcessor = new StateMachineProcessor(lSC);
        Invocation lINV = new DefaultInvocation();

        CreateEvent	lCreateEvent = new DefaultCreateEvent();
        lProcessor.Start(lSC,lINV,lCreateEvent);
        // stoppa in i constructorn til Ball.

        for( int i = 0; i < sequence.length; i++)
        {
            Event lEvent  = sequence[i];
            /*.TODO push parameters on stack */

            Object[] lResult = lProcessor.CallEvent(lINV,(CallEvent)lEvent,null, null);
        }/*for*/

        DestroyEvent	lDestroyEvent = new DefaultDestroyEvent();
        lProcessor.End(lSC,lINV,lDestroyEvent);


        if( fTrace_No != expected.length )
        {
            /*.TODO throw exception*/
        }
        for( int i = 0; i < expected.length; i++)
        {
            String lNeed = expected[i];

            if( ! lNeed.equals(fTrace[i]))
            {
                /*.TODO throw exception*/
            }
        }/*for*/
    }

    /****/

    /**want to create Procedure which calls sety in Ball as a doActivity
     *
     * startdoactivity in staemachineprocessor has been implemented
     *
     * need to implement abortdoactivity as well?
     *
     * the time for a bounce is returned
     *
     * **/

    public class Calculate extends Procedure implements Runnable{
        Thread t;
        Ball b;
        double y;
        public Calculate(String name) {
            super("Calculate-"+name);
            t = new Thread(this);
        }

        public Object[]  Call(Invocation inv, Object target, Object[] parameters) {


            /**problem here:  ftarget in Executionframe was never set
             *
             * changes made: added pushframe in starty and added invocation and
             *
             * parameters as argument to doActivity. Changes made to SCState,
             *
             * SCStateVertex, StateMachineProcessor. All Changes marked with "eddie changed".
             *
             * see comments made by anders where these arguments are added.
             *
             *
             *
             **/

            b = (Ball) inv.getTopExecutionFrame().getTarget();
            //maybe sety should take Object[] as argument instead?
            y=((Double) parameters[0]).doubleValue();
            //y=1;
            //double y;

            //should the iterating be done here or in sety?
            for(int i=0;i<500;i++){

                y = b.sety(y);

            }

            return new Object[]{Trace(getName(),target,parameters)};
        }
        public ACToken  CallAsync(Invocation inv, Object target, Object[] parameters, TimeValue tv) {

            /**problem here:  ftarget in Executionframe was never set
             *
             * changes made: added pushframe in starty and added invocation and
             *
             * parameters as argument to doActivity. Changes made to SCState,
             *
             * SCStateVertex, StateMachineProcessor. All Changes marked with "eddie changed".
             *
             * see comments made by anders where these arguments are added.
             *
             *
             *
             **/

            b = (Ball) inv.getTopExecutionFrame().getTarget();
            //maybe sety should take Object[] as argument instead?
            //y=((Double) parameters[0]).doubleValue();
            y=500;


            //should this procedure BE a seperate thread or should it HAVE a
            //a seperate thread?
            /*
            t.setPriority (Thread.MIN_PRIORITY);
            t.start();
            try {
                //to get the printout right
                Thread.currentThread().sleep(2000);
            }
            catch (InterruptedException e) { }
             */

            Calculate_Thread CT = new Calculate_Thread(b,y);
            CT.setPriority(Thread.MIN_PRIORITY);
            CT.start();
            try {
                //to get the printout right
                Thread.currentThread().sleep(2000);
            }
            catch (InterruptedException e) { }

            //is this the ACT?
            return new DefaultACToken(new Object[]{Trace(getName(),target,parameters)});
        }

        public void run() {
        }

    }
    public class Timed_Calculate extends Procedure implements Runnable{
        Thread t;
        Timed_ball b;
        double y;
        public Timed_Calculate(String name) {
            super("Calculate-"+name);
            t = new Thread(this);
        }

        public Object[]  Call(Invocation inv, Object target, Object[] parameters) {


            /**problem here:  ftarget in Executionframe was never set
             *
             * changes made: added pushframe in starty and added invocation and
             *
             * parameters as argument to doActivity. Changes made to SCState,
             *
             * SCStateVertex, StateMachineProcessor. All Changes marked with "eddie changed".
             *
             * see comments made by anders where these arguments are added.
             *
             *
             *
             **/

            b = (Timed_ball) inv.getTopExecutionFrame().getTarget();
            //maybe sety should take Object[] as argument instead?
            y=((Double) parameters[0]).doubleValue();
            //y=1;
            //double y;

            //should the iterating be done here or in sety?
            for(int i=0;i<500;i++){

                y = b.sety(y);

            }

            return new Object[]{Trace(getName(),target,parameters)};
        }
        public ACToken  CallAsync(Invocation inv, Object target, Object[] parameters, TimeValue tv) {

            /**problem here:  ftarget in Executionframe was never set
             *
             * changes made: added pushframe in starty and added invocation and
             *
             * parameters as argument to doActivity. Changes made to SCState,
             *
             * SCStateVertex, StateMachineProcessor. All Changes marked with "eddie changed".
             *
             * see comments made by anders where these arguments are added.
             *
             *
             *
             **/

            b = (Timed_ball) inv.getTopExecutionFrame().getTarget();
            //maybe sety should take Object[] as argument instead?
            //y=((Double) parameters[0]).doubleValue();
            y=50;


            //should this procedure BE a seperate thread or should it HAVE a
            //a seperate thread?
            /*
            t.setPriority (Thread.MIN_PRIORITY);
            t.start();
            try {
                //to get the printout right
                Thread.currentThread().sleep(2000);
            }
            catch (InterruptedException e) { }
             */

            Timed_Calculate_Thread CT = new Timed_Calculate_Thread(b,y);
            CT.setPriority(Thread.MIN_PRIORITY);
            CT.start();
            try {

                Thread.currentThread().sleep(2000);
            }
            catch (InterruptedException e) { }

            //is this the ACT?
            return new DefaultACToken(new Object[]{Trace(getName(),target,parameters)});
        }

        public void run() {

        }

    }

    public class Calculate_Thread extends Thread{

        private double y;
        private Ball b;

        public Calculate_Thread(Ball b_in, double y_in){
            b = b_in;
            y = y_in;
        }

        public void run() {
            //should the iterating be done here or in sety?
            for(int i=0;i<300;i++){
                y = b.sety(y);
            }
        }

    }

    public class Timed_Calculate_Thread extends Thread{

        private double y;
        private Timed_ball b;

        public Timed_Calculate_Thread(Timed_ball b_in, double y_in){
            b = b_in;
            y = y_in;
        }

        public void run() {
            //should the iterating be done here or in sety?
            for(int i=0;i<100;i++){
                y = b.sety(y);
                try{
                  Thread.sleep(100);
                }
                catch(InterruptedException e){}
            }
        }

    }

    static Operation[]  lBallOpers = new Operation[0];

    /** This class simulates a bouncing ball which generates a callevent when
     * the y-coordinate changes sign. This change is checked manually, since changeevents
     * is not implemented.
     * **/
    public class Ball extends DefaultStateMachineObject{

        StateMachine lSM;
//        StateConfiguration fSC;
        Invocation lINV;
        private double g = 9.82;
        private double dt = 0.005;
        private double t = 0;
        private double h_bounce=0;
        private double start_y=5;
        private double v0=10;
        private double v=0;
        public Ball(StateMachine sm) throws OEXException{
            super("Ball", lBallOpers, sm );
       /*eddie comment: have to cast fProcessor from abstract ActiveObjectProcessor to
        *StateMachineProcessor which is an DefaultActiveObjectProcessor that contains
        *the CreateEvent method. this method also starts the processor
        *This causes problem with the locking in DefaultActiveObjectProcessor however...
        */
            lINV        = new DefaultInvocation();
            ((StateMachineProcessor)fProcessor).CreateEvent(lINV);

            try{
                System.err.println("Nu körs bounce i Ballstateconfiguration ---------------------------------------------------------------------------");
                System.err.println(" ------------------------------------------------------------------------------------------------------------------");

                this.startup();
            }
            catch (OEXException e){
                System.err.print("Caught OEXException "+e.getMessage());
            }
            catch (TimeoutException e){
                System.err.print("Caught TimeoutException "+e.getMessage());
            }

        }
        public double sety(double y){

            //save values so that changes can be detected
            t+=dt;
            v=(g*t+v0);
            y=y-(g*t+v0)*dt;
            /** Visualization **/
            String yprint= " ";
            double d=0;
            while(d < y){
                yprint+=" ";
                d+=0.3;
            }

            y = checkChangeEvent(y);
            System.err.println(yprint+y);
            return y;
        }

        public double getTime(){return t;}

        //THis method needs to be updated.
        private double checkChangeEvent(double y_in) {
            if(y_in < h_bounce){
                //to use ChangeEvent a list must be added to statemachineprocessor (and stateconfiguration)
                //check with anders first
                //ChangeEvent e = new DefaultChangeEvent("bounce", new BooleanExpression("true",new TRUE()));
                CallEvent e = new DefaultCallEvent("bounce", new Operation("operBounce"));
                Object[] parameters = new Object[]{ (Object) (new Double(y_in))};
                lINV.PushFrame(this,e,parameters);
                Variable[] lVAR = new Variable[]{new Variable()};
                TimeValue lTV = new TimeValue();
                //public DefaultCallEventProcedure(String name, CallEvent evt,Variable[] locals, TimeValue	timeout)
                DefaultCallEventProcedure lDCEP = new DefaultCallEventProcedure("bounce", e, lVAR, lTV);
                try{ lDCEP.Call(new DefaultInvocation(), this, parameters); }
                catch(TimeoutException t){ }
                catch(OEXException t){ }
                //test this result in some way. for example it would be interesting
                //to know when the bounce occured
                //the result here is the ACT returned
                //from the corresponding procedure's call method.
                //Object[] lResult = fProcessor.CallEvent(lINV,e);
                //System.err.println("Time when bouncing occured: "+ lResult[1].toString());
                //reverse the velocity, restart the time and set the bouncing height
                v0=-v;
                t=0;
                return h_bounce;
            }
            return y_in;
        }

        private void startup() throws OEXException, TimeoutException{

            CallEvent e = new DefaultCallEvent("release", new Operation("operRelease"));
            //note that this start_y is printed as Effect-R[start_y]...
//            Object[] parameters = new Object[]{ (Object) (new Double(start_y))};
//            lINV.PushFrame(this,e,parameters);
//            CreateEvent lEvent = new DefaultCreateEvent();
            //eddie: must get StateConfiguration from the new StateMachineProcessor
            //this starting procedure should normally be handled in the constructor together
            //with the createevent call
//            fSC = ((StateMachineProcessor)fProcessor).getStateConfiguration();
//            ((StateMachineProcessor) fProcessor).Start(fSC,lINV,lEvent);
            //test this result in some way...
            //Object[] lResult = ((StateMachineProcessor) fProcessor).CallEvent(e,lINV);



        };

        /*
         * Called at the end */
        public void end() throws OEXException, TimeoutException{
//            DestroyEvent	lEvent = new DefaultDestroyEvent();
            //fProcessor.End(fSC,lINV,lEvent);
            lINV        = new DefaultInvocation();
            ((StateMachineProcessor)fProcessor).DestroyEvent(lINV);
        }

        /***/
        public Object[] Call(Invocation inv, Operation operation, Object[] parameters) throws TimeoutException, OEXException, IllegalArgumentException {
            //what is supposed to be returned???
            return new Object[]{inv,operation,parameters};
        }

        /***/
        public ACToken CallAsync(Invocation inv, Operation operation, Object[] parameters, TimeValue timeout) throws TimeoutException, OEXException, IllegalArgumentException {
            return new DefaultACToken(Call(inv,operation,parameters));
        }

        /***/
        public Object[] Convert(Invocation inv, Operation operation, Object[] parameters) throws OEXException, IllegalArgumentException {
            return new Object[]{inv,operation,parameters};
        }
    }

    /*----------------------------------------------------*/
    public class Timed_ball extends DefaultStateMachineObject{
        Invocation lINV;
        private double g = 9.82;
        private double dt = 0.005;
        private double t = 0;
        private double h_bounce=0;
        private double start_y=500;
        private double v0=10;
        private double v=0;
        private double t0=System.currentTimeMillis();

        public String toString(){
            return "[Timed_ball: "
            +" t:'"+t+"'"
            +" t0:'"+t0+"'"
            +"]";
            }

        public Timed_ball(StateMachine sm) throws OEXException{
            super("Ball", lBallOpers, sm );

            ((StateMachineProcessor)fProcessor).CreateEvent();

            try{
                lINV        = new DefaultInvocation();
                System.err.println("Nu körs bounce i Ballstateconfiguration ---------------------------------------------------------------------------");
                System.err.println(" ------------------------------------------------------------------------------------------------------------------");
                //t_start = System.currentTimeMillis();
                this.setup();

                /* these Objects are always active */
                Thread t = new Thread(fProcessor);
                t.start();

            }
            catch (OEXException e){
                System.err.print("Caught OEXException "+e.getMessage());
            }
            catch (TimeoutException e){
                System.err.print("Caught TimeoutException "+e.getMessage());
            }

        }
        public void restart(){
          t=0;
          v0=-v;
          System.err.println("Time when restart occured "+ (System.currentTimeMillis()-t0));
        }
        public double sety(double y){
          //save values so that changes can be detected
          //Real time
          t= System.currentTimeMillis()-t0;
          System.err.println("Time in sety: "+ t);
          //this "explicit calculation" of y does not work yet
          y=y-(g*t*t/1000+v0)*dt;
          //Simulated time - not related to the timeEvent
          //t+=dt;
          //v=(g*t+v0);
          //y=-g*t*t-v0*t+start_y;
          /** Visualization **/
          String yprint= " ";
          double d=0;
          while(d < y){
            yprint+=" ";
            d+=0.3;
          } //
          //System.err.println(yprint+y);
          //y = checkChangeEvent(y);
          //check for timeEvents
          //this does not work since PrecessEvents has  has protected
          //access in DefaultActiveObjectProcessor
          //(TimerQueue) lTimerQueue = lProcessor.fTimerQueue;
          /*try{
            //obs! Eddie changed visibility for method ProcessEvents
            //how can i do this with run instead?
            lProcessor.ProcessEvents();
            //((DefaultActiveObjectProcessor) lProcessor).run();
          }
          catch(TimeoutException e){e.printStackTrace();}
           catch(OEXException e){e.printStackTrace();}
          */
            /*
            try {
            //obs! this sleep might act on the wrong thread
            Thread.currentThread().sleep(2000);
            }
            catch (InterruptedException e) { }
            */

            return y;
        }
        public double getTimeToBounce(){return v0/g;}
        private double checkChangeEvent(double y_in) {
            if(y_in < h_bounce){
                //to use ChangeEvent a list must be added to statemachineprocessor (and stateconfiguration)
                //check with anders first
                //ChangeEvent e = new DefaultChangeEvent("bounce", new BooleanExpression("true",new TRUE()));
                CallEvent e = new DefaultCallEvent("bounce", new Operation("operBounce"));
                Object[] parameters = new Object[]{ (Object) (new Double(y_in))};
                lINV.PushFrame(this,e,parameters);
                Variable[] lVAR = new Variable[]{new Variable()};
                TimeValue lTV = new TimeValue();
                //public DefaultCallEventProcedure(String name, CallEvent evt,Variable[] locals, TimeValue	timeout)
                DefaultCallEventProcedure lDCEP = new DefaultCallEventProcedure("bounce", e, lVAR, lTV);
                try{ lDCEP.Call(new DefaultInvocation(), this, parameters); }
                catch(TimeoutException t){ }
                catch(OEXException t){ }
                v0=-v;
                t=0;
                return h_bounce;
            }
            return y_in;
        }

        private void setup() throws OEXException, TimeoutException{
            CallEvent e = new DefaultCallEvent("release", new Operation("operRelease"));
            //note that this start_y is printed as Effect-R[start_y]...

            Object[] parameters = new Object[]{ (Object) (new Double(start_y))};
            lINV.PushFrame(this,e,parameters);
            //CreateEvent	lEvent = new DefaultCreateEvent();
            //eddie: must get StateConfiguration from the new StateMachineProcessor
            //this starting procedure should normally be handled in the constructor together
            //with the createevent call

            // Start by setting the time for when the bouncing is supposed to occur
            int timeToBounceMs = (int) (getTimeToBounce()*1000);
            System.err.println("timeToBounce (ms) =  "+timeToBounceMs);
            TimeValue lTimeout = new TimeValue(3,0000000*timeToBounceMs);
            TimeValue lInterval = new TimeValue(3,0);
            int res = ((StateMachineProcessor)fProcessor).scheduleTimeEvent (lTimeEvent,lTimeout,lInterval);

            //pushframe
            lINV = new DefaultInvocation();
            //parameters = new Object[]{ (Object) (new Double(0))};
            //lINV.PushFrame(this,lTimeEvent,parameters);
            //then start the processor
        }

        public void end() throws OEXException, TimeoutException{
            lINV        = new DefaultInvocation();
            ((StateMachineProcessor)fProcessor).DestroyEvent(lINV);
//            DestroyEvent	lEvent = new DefaultDestroyEvent();
//            //fProcessor.End(fSC,lINV,lEvent);
        }
    }
    /**This method simulates a bouncing ball.
     * thesetup is done iin setupBounce, the doActivityProcedure is Calulate,
     * which maintains its own thread.
     * **/
    public void BounceCallevent()throws TimeoutException,OEXException, IllegalArgumentException
    {
        StateMachine sm;
            try {
                sm = setupBounce();
                Ball b1 = new Ball(sm);
                Ball b2 = new Ball(sm);
            }
            catch(OEXException e){
                //e.PrintStackTrace();
            }

    }
    public void testTimedBounceCallevent()throws TimeoutException,OEXException, IllegalArgumentException
    {
        StateMachine sm;
            try {
                sm = setupTimedBounce();
                Timed_ball tb1 = new Timed_ball(sm);
                Thread.sleep(10000);
                //while(true) Thread.currentThread().wait();
            }
            catch(OEXException e){
                //e.PrintStackTrace();
            }
            catch(InterruptedException e){
                //e.PrintStackTrace();
            }


        //Ball b2 = new Ball();
    }

    /****/



    /****/

    /*----------------*/
    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestBallStateConfiguration.class);
        return suite;
    }

    /****/
    /****/

    public void Submachine()throws TimeoutException,OEXException, IllegalArgumentException
    {
        StateMachine lSM = setupSubmachine();
        /* Define sequence of event */
        Event[] lSequence = new Event[]{
            lCallEvent_D,
            lCallEvent_F
        };
        Object[][] lParameters = new Object[][]{
            new Object[]{"1", "1.1"},
            new Object[]{"2", "2.1"}
        };

        /*.TODO evaluate result */
        String[]  lExpected = new String[] {
            ""
        };

        runEventSequence(lSM,lSequence,lParameters,lExpected);

    }


    /****/
    /****/

    /****/
    public static void main(String args[])
    {
        String[] testCaseName = {TestBallStateConfiguration.class.getName()};

        if(args != null && args.length >= 1)
        {
            if( "-text".equals(args[0]))
                junit.textui.TestRunner.main(testCaseName);
            else if( "-swing".equals(args[0]))
                junit.swingui.TestRunner.main(testCaseName);
            else
                junit.textui.TestRunner.main(testCaseName);
        }
        else
            junit.textui.TestRunner.main(testCaseName);
    }
};
/*.IEnd,TestBallStateConfiguration,====================================*/
