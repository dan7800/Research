package org.eclipse.jdt.internal.compiler.classfmt;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;
public class ClassFormatException extends Exception {
       private int errorCode;
       private int bufferPosition;
       public static final int ErrBadMagic = 1;
       public static final int ErrBadMinorVersion = 2;
       public static final int ErrBadMajorVersion = 3;
       public static final int ErrBadConstantClass= 4;
       public static final int ErrBadConstantString= 5;
       public static final int ErrBadConstantNameAndType = 6;
       public static final int ErrBadConstantFieldRef= 7;
       public static final int ErrBadConstantMethodRef = 8;
       public static final int ErrBadConstantInterfaceMethodRef = 9;
       public static final int ErrBadConstantPoolIndex = 10;
       public static final int ErrBadSuperclassName = 11;
       public static final int ErrInterfaceCannotBeFinal = 12;
       public static final int ErrInterfaceMustBeAbstract = 13;
       public static final int ErrBadModifiers = 14;
       public static final int ErrClassCannotBeAbstractFinal = 15;
       public static final int ErrBadClassname = 16;
       public static final int ErrBadFieldInfo = 17;
       public static final int ErrBadMethodInfo = 17; 
       public static final int ErrEmptyConstantPool =18;
       public static final int ErrMalformedUtf8 = 19;
       public static final int ErrUnknownConstantTag = 20;
       public static final int ErrTruncatedInput = 21;
       public static final int ErrMethodMustBeAbstract = 22;
       public static final int ErrMalformedAttribute = 23;
       public static final int ErrBadInterface = 24;
       public static final int ErrInterfaceMustSubclassObject = 25;
       public static final int ErrIncorrectInterfaceMethods = 26;
       public static final int ErrInvalidMethodName = 27;
       public static final int ErrInvalidMethodSignature = 28;
/**
 * (c)1998 Object Technology International.
 * (c)1998 International Business Machines Corporation.
 * 
 * 
 */
public ClassFormatException(int code) {
       errorCode = code; }
/**
 * (c)1998 Object Technology International.
 * (c)1998 International Business Machines Corporation.
 * 
 * 
 */
public ClassFormatException(int code, int bufPos) {
       errorCode = code;
       bufferPosition = bufPos; }
/**
 * @return int
 */
public int getErrorCode() {
       return errorCode; } }
