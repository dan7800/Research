/*-------------------------------------------------------------------------
 *
 * rewriteDefine.h
 *
 *
 *
 * Portions Copyright (c) 1996-2001, PostgreSQL Global Development Group
 * Portions Copyright (c) 1994, Regents of the University of California
 *
 * $Id: rewriteDefine.h,v 1.13 2001/11/05 17:46:35 momjian Exp $
 *
 *-------------------------------------------------------------------------
 */
#ifndef REWRITEDEFINE_H
#define REWRITEDEFINE_H
#include "nodes/parsenodes.h"
extern void DefineQueryRewrite(RuleStmt *args);
extern void RenameRewriteRule(char *oldname, char *newname);
#endif   /* REWRITEDEFINE_H */
